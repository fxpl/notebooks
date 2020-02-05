package notebooks;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Analyzer for Jupyter notebooks.
 */
public class NotebookAnalyzer extends Analyzer {
	private List<Notebook> notebooks;
	private static LangSpec[] langSpecFields = {LangSpec.METADATA_LANGUAGE , LangSpec.METADATA_LANGUAGEINFO_NAME, 
			LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_NAME,
			LangSpec.CODE_CELLS};
	private final int CONNECTION_NOTEBOOKS = 50000;
	
	/**
	 * Note that when you are done with this Analyzer, you must call the method
	 * shutDown!
	 */
	public NotebookAnalyzer() {
		super();
		this.notebooks = new ArrayList<Notebook>();
	}
	
	/**
	 * Find each notebook file (i.e. file whose name ends with ".ipynb") in the
	 * file whose name is given as an argument to the method, and its sub
	 * directories (recursively). Create a Notebook from each file and store
	 * it. (The argument may be the path to a Jupyter notebook file. In that
	 * case, a Notebook is created from it.)
	 * @param fileName Name of directory or file to look in for notebooks
	 */
	public void initializeNotebooksFrom(String fileName) {
		File file = new File(fileName);
		if (!file.isDirectory()) {
			// Regular file
			if (fileName.endsWith(".ipynb")) {
				this.notebooks.add(new Notebook(fileName));
			}
		} else {
			System.out.println("Traversing " + file.getPath());
			// The file is a directory. Traverse it.
			String[] subFiles = file.list();
			for (String subFileName: subFiles) {
				initializeNotebooksFrom(file.getPath() + "/" + subFileName);
			}
		}
	}
	
	/**
	 * Initialize the map from notebook name to repro, and add information about repro to each notebook.
	 * @param fileName Name of file with mapping from notebook number to repro
	 */
	void initializeReproInfo(String fileName) throws FileNotFoundException {
		Map<String, String> reproMap = createReproMap(fileName);
		for (Notebook nb: notebooks) {
			nb.setRepro(reproMap.get(nb.getName()));
		}
	}
	
	/**
	 * Create CSV files with information about LOC, languages (actual one and
	 * all defined ones respectively) and clones.
	 * @throws IOException On problems handling the output file
	 */
	public void allAnalyzes() throws IOException {
		final int TASKS_PER_NOTEBOOK = 7;
		List<Callable<Object>> tasks = new ArrayList<>(notebooks.size()); 
		for (Notebook notebook: this.notebooks) {
			tasks.add(new CodeCellCounterWrapper(notebook));
			tasks.add(new TotalLOCCounterWrapper(notebook));
			tasks.add(new NonBlankLOCCounterWrapper(notebook));
			tasks.add(new BlankLOCCounterWrapper(notebook));
			tasks.add(new LanguageExtractorWrapper(notebook));
			tasks.add(new AllLanguagesExtractorWrapper(notebook));
			tasks.add(new HashExtractorWrapper(notebook));
		}
		List<Future<Object>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		
		Writer codeCellsWriter = new FileWriter(outputDir + "/code_cells" + LocalDateTime.now() + ".csv");
		codeCellsWriter.write(numCodeCellsHeader());
		Writer LOCWriter = new FileWriter(outputDir + "/loc" + LocalDateTime.now() + ".csv");
		LOCWriter.write(LOCHeader());
		Writer langWriter = new FileWriter(outputDir + "/languages" + LocalDateTime.now() + ".csv");
		langWriter.write(languagesHeader());
		Writer allLangWriter = new FileWriter(outputDir + "/all_languages" + LocalDateTime.now() + ".csv");
		allLangWriter.write(allLanguagesHeader());
		Map<Notebook, SnippetCode[]> snippets = new HashMap<Notebook, SnippetCode[]>();
		
		for (int i=0; i<notebooks.size(); i++) {
			Notebook notebook = notebooks.get(i);
			int index = i * TASKS_PER_NOTEBOOK;
			writeCodeCellsLine(result.get(index), notebook, codeCellsWriter);
			writeLocLine(result.get(index+1), result.get(index+2), result.get(index+3), notebook, LOCWriter);
			writeLanguagesLine(result.get(index+4), notebook, langWriter);
			writeAllLanguagesLine(result.get(index+5), notebook, allLangWriter);
			storeHashes(result.get(index+6), notebook, snippets);
		}
		
		/* Language summary is not printed here, since the information can
		   easily be extracted from the CSV file. */
		codeCellsWriter.close();
		LOCWriter.close();
		langWriter.close();
		allLangWriter.close();
		
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		new CloneFileWriter(outputDir).write(snippets, clones, CONNECTION_NOTEBOOKS);
	}
	
	/**
	 * Compute the MD5 hash of each snippet in each notebook. Return a map with
	 * the key being the snippet code and the values being lists of all
	 * snippets containing that code.
	 * For each notebook, print the name of the notebook followed by a list of
	 * the hash of each snippet in the notebook to the file 
	 * file2hashes<current-date-time>.csv.
	 * Print each hash and the corresponding snippets (name, index) on a
	 * separate line in the file hash2files<current-date-time>.csv.
	 * Print the number of clones and unique snippets, and the clone frequency,
	 * for each notebook to cloneFrequency<current-date-time>.csv.
	 * Print the number of connections for each notebook to
	 * connections <current-date-time>.csv. See header of the csv file for
	 * details. Start each csv file with a header.
	 * @return The map described above
	 * @throws IOException On problems handling the output file.
	 */
	public Map<SnippetCode, List<Snippet>> clones() throws IOException {
		Map<Notebook, SnippetCode[]> snippets = getSnippets();
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		new CloneFileWriter(outputDir).write(snippets, clones, CONNECTION_NOTEBOOKS);
		return clones;
	}
	
	/**
	 * @return A map from file names to snippets
	 */
	private Map<Notebook, SnippetCode[]> getSnippets() throws IOException {
		List<Callable<SnippetCode[]>> tasks = new ArrayList<Callable<SnippetCode[]>>(notebooks.size());
		for (Notebook notebook: notebooks) {
			tasks.add(new HashExtractor(notebook));
		}
		List<Future<SnippetCode[]>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		Map<Notebook, SnippetCode[]> snippets = new HashMap<Notebook, SnippetCode[]>();
		for (int i=0; i<notebooks.size(); i++) {
			Notebook notebook = notebooks.get(i);
			if (0 == i%10000) {
				System.out.println("Retrieving hashes in " + notebook.getName());
			}
			storeHashes(result.get(i), notebook, snippets);
		}
		return snippets;
	}
	
	/**
	 * Store the hash array wrapped in result with notebook as key in the map snippets.
	 * @param hashes Wrapper around hashes
	 * @param notebook Key to store in the map
	 * @param snippets Map to store the result in
	 */
	private<T> void storeHashes(Future<T> hashes, Notebook notebook, Map<Notebook, SnippetCode[]> snippets) {
		SnippetCode[] hashValues;
		try {
			hashValues = (SnippetCode[])hashes.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get snippets for " + notebook.getName() + ": " + e);
			hashValues = new SnippetCode[0];
		}
		snippets.put(new Notebook(notebook), hashValues);
	}
	
	/**
	 * @return A map from snippet (hash and loc) to location in a notebook
	 */
	private Map<SnippetCode, List<Snippet>> getClones(Map<Notebook, SnippetCode[]> fileMap) throws IOException {
		int numAnalyzed = 0;
		Map<SnippetCode, List<Snippet>> clones = new HashMap<SnippetCode, List<Snippet>>();
		for (Notebook notebook: fileMap.keySet()) {
			if (0 == numAnalyzed%10000) {
				System.out.println("Finding clones in " + notebook.getName());
			}
			SnippetCode[] snippetCodes = fileMap.get(notebook);
			for (int j=0; j<snippetCodes.length; j++) {
				if (clones.containsKey(snippetCodes[j])) {
					clones.get(snippetCodes[j]).add(new Snippet(notebook, j));
				} else {
					List<Snippet> snippets = new ArrayList<Snippet>();
					snippets.add(new Snippet(notebook, j));
					clones.put(snippetCodes[j], snippets);
				}
			}
			numAnalyzed++;
		}
		return clones;
	}
	
	/**
	 * Create a file all_languages<current-date-time>.csv with a header line followed by the language defined in each language specification field for each notebook. The file contains one line per notebook, on the format
	 * <filename>,<language found in metadata.language>,<language found in metadata.language_info.name>,<language found in metadata.kernelspec.language>,<language found in metadata.kernelspec.name>,<language found in code cells>
	 * If any field is missing in a notebook, "UNKNOWN" is written for that field.
	 * @throws IOException 
	 */
	public void allLanguageValues() throws IOException {
		List<Callable<Map<LangSpec, LangName>>> tasks = new ArrayList<>(notebooks.size());
		for (Notebook notebook: notebooks) {
			tasks.add(new AllLanguagesExtractor(notebook));
		}
		List<Future<Map<LangSpec, LangName>>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		Writer writer = new FileWriter(outputDir + "/all_languages" + LocalDateTime.now() + ".csv");
		writer.write(allLanguagesHeader());
		for (int i=0; i<notebooks.size(); i++) {
			Notebook notebook = notebooks.get(i);
			writeAllLanguagesLine(result.get(i), notebook, writer);
		}
		writer.close();
	}
	
	/**
	 * @return Header for the all_languages csv file
	 */
	private static String allLanguagesHeader() {
		String result = "file";
		for (LangSpec field : langSpecFields) {
			result += ", " + field.toString();
		}
		result += "\n";
		return result;
	}
	
	/**
	 * Write the data for one notebook to the all_languages file.
	 * @param languages Wrapper around language values to write
	 * @param notebook Notebook to write information for
	 * @param writer Writer that appends text to the all_languages file
	 */
	private<T> void writeAllLanguagesLine(Future<T> languages, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName());
		try {
			@SuppressWarnings("unchecked")
			Map<LangSpec, LangName> languageValues = (Map<LangSpec, LangName>) languages.get();
			for (LangSpec field: langSpecFields) {
				writer.write(", " + languageValues.get(field));
			}
		} catch (InterruptedException | ExecutionException e) {
			for (int j=0; j<langSpecFields.length; j++) {
				writer.write(", UNKNOWN");
			}
			System.err.println("Could not get language values for " + notebook.getName() + ": " + e);
		}
		writer.write("\n");
	}

	/**
	 * Create a file languages<current-date-time>.csv with a header line
	 * followed by the language and the element from which is was extracted
	 * from the notebook file. The file contains one line per notebook, on the
	 * format <filename>,<language>,<location of language>.
	 * @return A map with the different languages as keys and the number of files written in this language as value
	 * @throws IOException On problems with handling the output file
	 */
	public Map<LangName, Integer> languages() throws IOException {
		Map<LangName, Integer> languages = new HashMap<LangName, Integer>();
		for (LangName language: LangName.values()) {
			languages.put(language, 0);
		}
		List<Callable<Language>> tasks = new ArrayList<>(notebooks.size());
		for (Notebook notebook: notebooks) {
			tasks.add(new LanguageExtractor(notebook));
		}
		List<Future<Language>> langResult = ThreadExecutor.getInstance().invokeAll(tasks);
		Writer writer = new FileWriter(outputDir + "/languages" + LocalDateTime.now() + ".csv");
		writer.write(languagesHeader());
		for (int i=0; i<notebooks.size(); i++) {
			LangName language = writeLanguagesLine(langResult.get(i), notebooks.get(i), writer);
			languages.put(language, languages.get(language) + 1);
		}
		writer.close();
		return languages;
	}
	
	/**
	 * Write the data for one notebook to the languages file.
	 * @param language Wrapper around languages value
	 * @param notebook Notebook to write information for
	 * @param writer Writer that appends text to the languages file
	 */
	private<T1,T2> LangName writeLanguagesLine(Future<T1> language, Notebook notebook, Writer writer) throws IOException {
		Language languageValue;
		try {
			languageValue = (Language) language.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get language information for " + notebook.getName() + ": " + e);
			languageValue = new Language();
		}
		writer.write(notebook.getName() + ", " + languageValue.toString() + "\n");
		return languageValue.getName();
	}
	
	/**
	 * @return Header for the languages csv file
	 */
	private static String languagesHeader() {
		return "file, language, language spec in\n";
	}

	/**
	 * Create a file loc<current-date-time>.csv with the header line
	 * followed by the number of lines of code for each notebook on the format
	 * <total loc>, <non-blank loc>, <blank loc>
	 * The data for each notebook is print on a separate line.
	 * @return Total number of LOC in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int LOC() throws IOException {
		List<Callable<Integer>> locTasks = new ArrayList<Callable<Integer>>(notebooks.size());
		List<Callable<Integer>> nonBlankLocTasks = new ArrayList<Callable<Integer>>(notebooks.size());
		List<Callable<Integer>> blankLocTasks = new ArrayList<Callable<Integer>>(notebooks.size());
		for (Notebook notebook: notebooks) {
			locTasks.add(new TotalLOCCounter(notebook));
			nonBlankLocTasks.add(new NonBlankLOCCounter(notebook));
			blankLocTasks.add(new BlankLOCCounter(notebook));
		}
		List<Future<Integer>> locResults = ThreadExecutor.getInstance().invokeAll(locTasks);
		List<Future<Integer>> nonBlankLocResults = ThreadExecutor.getInstance().invokeAll(nonBlankLocTasks);
		List<Future<Integer>> blankLocResults = ThreadExecutor.getInstance().invokeAll(blankLocTasks);
		
		Writer writer = new FileWriter(outputDir + "/loc" + LocalDateTime.now() + ".csv");
		writer.write(LOCHeader());
		int totalLoc = 0;
		for (int i=0; i<notebooks.size(); i++) {
			totalLoc += writeLocLine(locResults.get(i), nonBlankLocResults.get(i), blankLocResults.get(i), notebooks.get(i), writer);
		}
		writer.close();
		return totalLoc;
	}
	
	/**
	 * Write the data for one notebook to the loc file.
	 * @param loc Wrapper around total number of lines in the notebook
	 * @param nonBlankLoc Wrapper around the number of non blank lines in the notebook
	 * @param blankLoc Wrapper around the number of blank lines in the notebook
	 * @param notebook Notebook to write information for
	 * @param writer Writer that appends text to the loc file
	 */
	private<T> int writeLocLine(Future<T> loc, Future<T> nonBlankLoc, Future<T> blankLoc, Notebook notebook, Writer writer) throws IOException {
		int locValue = 0, nonBlankLocValue = 0, blankLocValue = 0;
		try {
			locValue = (Integer) loc.get();
			nonBlankLocValue = (Integer) nonBlankLoc.get();
			blankLocValue = (Integer) blankLoc.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get line count for " + notebook.getName() + ": " + e);
		}
		writer.write(notebook.getName() + ", " + locValue + ", " + nonBlankLocValue + ", " + blankLocValue + "\n");
		return locValue;
	}

	/**
	 * @return Header for the LOC csv file
	 */
	private static String LOCHeader() {
		return "file, total LOC, non-blank LOC, blank LOC\n";
	}
	
	/**
	 * Count the number of code cells in each notebook. Print each value on a
	 * separate line in the file code_cells<current-date-time>.csv. Start the
	 * csv file with the header "file, code cells".
	 * @return Total number of code cells in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int numCodeCells() throws IOException {
		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>(notebooks.size());
		for (Notebook notebook: notebooks) {
			tasks.add(new CodeCellCounter(notebook));
		}
		Writer writer = new FileWriter(outputDir + "/code_cells" + LocalDateTime.now() + ".csv");
		writer.write(numCodeCellsHeader());
		int totalNumCodeCells = 0;
		List<Future<Integer>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		for (int i=0; i<notebooks.size(); i++) {
			totalNumCodeCells += writeCodeCellsLine(result.get(i), notebooks.get(i), writer);
		}
		writer.close();
		return totalNumCodeCells;
	}
	
	/**
	 * Write the data for one notebook to the code_cells file.
	 * @param numCodeCells Wrapper around total number of code cells in the notebook
	 * @param notebook Notebook to write information for
	 * @param writer Writer that appends text to the code_cells file
	 */
	private<T> int writeCodeCellsLine(Future<T> numCodeCells, Notebook notebook, Writer writer) throws IOException {
		int numCodeCellsValue = 0;
		try {
			numCodeCellsValue =(Integer) numCodeCells.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get cell count for " + notebook.getName() + ": " + e);
		}
		writer.write(notebook.getName() + ", " + numCodeCellsValue + "\n");
		return numCodeCellsValue;
	}
	
	/**
	 * @return Header for the file2hashes csv file
	 */
	private String numCodeCellsHeader() {
		return "file, code cells\n";
	}
	
	/**
	 * @return Number of notebooks stored in analyzer
	 */
	public int numNotebooks() {
		return this.notebooks.size();
	}
	
	/**
	 * Parse command line arguments and perform actions accordingly.
	 */
	void analyze(String[] args) {
		boolean all = false,
				count = false,
				lang = false,
				loc = false,
				clones = false,
				langAll = false;
		String reproFile = null;
		String nbPath = null;
		
		// Read arguments
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--nb_path")) {
				nbPath = getValueFromArgument(arg);
			} else if (arg.startsWith("--output_dir")) {
				this.outputDir = getValueFromArgument(arg);
			} else if (arg.startsWith("--repro_file")) {
				reproFile = getValueFromArgument(arg);
			} else {
				switch (arg) {
				case "--all":
					all = true;
					break;
				case "--count":
					count = true;
					break;
				case "--lang":
					lang = true;
					break;
				case "--loc":
					loc = true;
					break;
				case "--clones":
					clones = true;
					break;
				case "--lang_all":
					langAll = true;
					break;
				default:
					System.err.println("Unknown argument: " + arg);
				}
			}
		}
			
		// Set up
		if (null != nbPath && "" != nbPath) {
			this.initializeNotebooksFrom(nbPath);
		} else {
			System.err.println("Warning! Notebook path not set! No notebooks will be analyzed.");
		}
		if (null != reproFile && "" != reproFile) {
			try {
				this.initializeReproInfo(reproFile);
			} catch (FileNotFoundException e) {
				System.err.println("Repro file not found: " + e.getMessage());
				System.err.println("Repro information not initialized!");
			}
		} else if (all || clones) {
			System.err.println("Warning! Clone analysis run without repro information!");
		}
			
		// Perform analyzes
		try {
			if (all) {
				this.allAnalyzes();
				System.out.println("All analyzes made for " + this.numNotebooks() + " notebooks.");
			}
			if (count) {
				System.out.println("Notebooks parsed: " + this.numNotebooks());
				System.out.println("Code cells: " + this.numCodeCells());
			}
			if (lang) {
				Map<LangName, Integer> languages = this.languages();
				printLanguageSummary(languages);
			}
			if (loc) {
				System.out.println("Lines of code: " + this.LOC());
			}
			if (clones) {
				this.clones();
				System.out.println("Clone files created!");
			}
			if (langAll) {
				this.allLanguageValues();
				System.out.println("File with all language values created!");
			}
		} catch (IOException e) {
			System.err.println("I/O error: " + e.getMessage() + ". Operation interrupted.");
		}
	}

	private void printLanguageSummary(Map<LangName, Integer> languages) {
		System.out.println("\nLANGUAGES:");
		for (LangName language: languages.keySet()) {
			System.out.println(language + ": " + languages.get(language));
		}
		System.out.println("");
	}

	public static void main(String[] args) {
		NotebookAnalyzer analyzer = new NotebookAnalyzer();
		analyzer.analyze(args);
		ThreadExecutor.tearDown();
	}
}
