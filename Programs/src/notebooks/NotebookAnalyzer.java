package notebooks;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
		List<Callable<AllResults>> tasks = new ArrayList<>(notebooks.size());
		for (Notebook notebook: this.notebooks) {
			tasks.add(new AllAnalyzer(notebook));
		}
		List<Future<AllResults>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		
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
			AllResults results;
			try {
				results = result.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				/* This should only happen when a thread gets interrupted
				   Other exceptions are handled in AllAnalyzer. */
				System.err.println("Could not get results for " + notebook.getName() + ": " + e);
				e.printStackTrace();
				results = new AllResults();
			}
			writeCodeCellsLine(results.getNumCodeCells(), notebook, codeCellsWriter);
			writeLocLine(results.getTotalLOC(), results.getNonBlankLOC(), results.getBlankLOC(), notebook, LOCWriter);
			writeLanguagesLine(results.getLanguage(), notebook, langWriter);
			writeAllLanguagesLine(results.getAllLanguages(), notebook, allLangWriter);
			storeHashes(results.getHashes(), notebook, snippets);
		}
		
		/* Language summary is not printed here, since the information can
		   easily be extracted from the CSV file. */
		codeCellsWriter.close();
		LOCWriter.close();
		langWriter.close();
		allLangWriter.close();
		
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		new CloneFileWriter(outputDir).write(snippets, clones);
	}
	
	/**
	 * Compute the MD5 hash of each snippet in each notebook. Return a map with
	 * the key being the snippet code and the values being lists of all
	 * snippets containing that code.
	 * For each notebook, print the name of the notebook followed by a list of
	 * the hash of each snippet in the notebook to the file 
	 * file2hashesA<current-date-time>.csv.
	 * Print each hash and the corresponding snippets (name, index) on a
	 * separate line in the file hash2filesA<current-date-time>.csv.
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
		new CloneFileWriter(outputDir).write(snippets, clones);
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
	private void storeHashes(Future<SnippetCode[]> hashes, Notebook notebook, Map<Notebook, SnippetCode[]> snippets) {
		SnippetCode[] hashValues;
		try {
			hashValues = hashes.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get snippets for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			hashValues = new SnippetCode[0];
		}
		storeHashes(hashValues, notebook, snippets);
	}
	
	private void storeHashes(SnippetCode[] hashes, Notebook notebook, Map<Notebook, SnippetCode[]> snippets) {
		snippets.put(new Notebook(notebook), hashes);
	}
	
	/**
	 * @return A map from snippet (hash and loc) to location in a notebook
	 */
	private Map<SnippetCode, List<Snippet>> getClones(Map<Notebook, SnippetCode[]> fileMap) throws IOException {
		int numAnalyzed = 0;
		Map<SnippetCode, List<Snippet>> clones = new HashMap<SnippetCode, List<Snippet>>();
		Map<SnippetCode, List<Integer>> loc = new HashMap<SnippetCode, List<Integer>>();
		// Add all snippets to clone map
		for (Notebook notebook: fileMap.keySet()) {
			if (0 == numAnalyzed%10000) {
				System.out.println("Finding clones in " + notebook.getName());
			}
			SnippetCode[] snippetCodes = fileMap.get(notebook);
			for (int j=0; j<snippetCodes.length; j++) {
				addToMap(snippetCodes[j], new Snippet(notebook, j), clones);
				addToMap(snippetCodes[j], snippetCodes[j].getLOC(), loc);
			}
			numAnalyzed++;
		}
		// Update line count for snippets
		for (SnippetCode snippet: clones.keySet()) {
			List<Integer> locValues = loc.get(snippet);
			Collections.sort(locValues);
			int numCopies = locValues.size();
			int medianLoc = (locValues.get(numCopies/2) + locValues.get((numCopies-1)/2))/2;
			snippet.setLOC(medianLoc);
			int minLoc = locValues.get(0);
			int maxLoc = locValues.get(locValues.size()-1);
			if (minLoc != maxLoc) {
				System.out.println("Different line count for snippet " + snippet
						+ ". Min: " + minLoc + ". Max: " + maxLoc + ".");
			}
		}
		return clones;
	}
	
	/**
	 * If the specified key exists in map, add the value to the list stored for
	 * this key. Otherwise, create a new lost containing (only) the value and
	 * add to map with the actual key.
	 * @param key Key value
	 * @param value Value to store
	 * @param map Map to store the value in
	 */
	private<K, V> void addToMap(K key, V value, Map<K, List<V>> map) {
		if (map.containsKey(key)) {
			map.get(key).add(value);
		} else {
			List<V> values = new ArrayList<V>();
			values.add(value);
			map.put(key, values);
		}
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
	private void writeAllLanguagesLine(Future<Map<LangSpec, LangName>> languages, Notebook notebook, Writer writer) throws IOException {
		Map<LangSpec, LangName> languageValues = null;
		try {
			languageValues = languages.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get language values for " + notebook.getName() + ": " + e);
			e.printStackTrace();
		}
		writeAllLanguagesLine(languageValues, notebook, writer);
	}
	
	private void writeAllLanguagesLine(Map<LangSpec, LangName> languages, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName());
		if (null == languages) {
			for (int j=0; j<langSpecFields.length; j++) {
				writer.write(", UNKNOWN");
			}
		} else {
			for (LangSpec field: langSpecFields) {
				writer.write(", " + languages.get(field));
			}
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
	private LangName writeLanguagesLine(Future<Language> language, Notebook notebook, Writer writer) throws IOException {
		Language languageValue;
		try {
			languageValue = language.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get language information for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			languageValue = new Language();
		}
		writeLanguagesLine(languageValue, notebook, writer);
		return languageValue.getName();
	}
	
	private void writeLanguagesLine(Language language, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName() + ", " + language.toString() + "\n");
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
	private int writeLocLine(Future<Integer> loc, Future<Integer> nonBlankLoc, Future<Integer> blankLoc, Notebook notebook, Writer writer) throws IOException {
		int locValue = 0, nonBlankLocValue = 0, blankLocValue = 0;
		try {
			locValue = loc.get();
			nonBlankLocValue = nonBlankLoc.get();
			blankLocValue = blankLoc.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get line count for " + notebook.getName() + ": " + e);
			e.printStackTrace();
		}
		writeLocLine(locValue, nonBlankLocValue, blankLocValue, notebook, writer);
		return locValue;
	}
	
	private void writeLocLine(int loc, int nonBlankLoc, int blankLoc, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName() + ", " + loc + ", " + nonBlankLoc + ", " + blankLoc + "\n");
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
	private int writeCodeCellsLine(Future<Integer> numCodeCells, Notebook notebook, Writer writer) throws IOException {
		int numCodeCellsValue = 0;
		try {
			numCodeCellsValue = numCodeCells.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get cell count for " + notebook.getName() + ": " + e);
			e.printStackTrace();
		}
		writeCodeCellsLine(numCodeCellsValue, notebook, writer);
		return numCodeCellsValue;
	}
	
	private void writeCodeCellsLine(int numCodeCells, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName() + ", " + numCodeCells + "\n");
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
