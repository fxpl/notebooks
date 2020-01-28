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
	private ExecutorService executor;
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
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Running " + (2*cores) + " threads.");
		executor = Executors.newFixedThreadPool(2*cores);
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
	 * Shut the executor down. Must be called for each object when you are done
	 * with it!
	 */
	public void shutDown() {
		executor.shutdown();
	}
	
	/**
	 * Create CSV files with information about LOC, languages (actual one and
	 * all defined ones respectively) and clones.
	 * @throws IOException On problems handling the output file
	 */
	public void allAnalyzes() throws IOException {
		Writer codeCellsWriter = new FileWriter(outputDir + "/code_cells" + LocalDateTime.now() + ".csv");
		codeCellsWriter.write(numCodeCellsHeader());
		Writer LOCWriter = new FileWriter(outputDir + "/loc" + LocalDateTime.now() + ".csv");
		LOCWriter.write(LOCHeader());
		Writer langWriter = new FileWriter(outputDir + "/languages" + LocalDateTime.now() + ".csv");
		langWriter.write(languagesHeader());
		Writer allLangWriter = new FileWriter(outputDir + "/all_languages" + LocalDateTime.now() + ".csv");
		allLangWriter.write(allLanguagesHeader());
		
		Map<Notebook, SnippetCode[]> snippets = new HashMap<Notebook, SnippetCode[]>();
		for (Notebook notebook: this.notebooks) {
			numCodeCellsIn(notebook, codeCellsWriter);
			LOCIn(notebook, LOCWriter);
			languageIn(notebook, langWriter);
			allLanguageValuesIn(notebook, allLangWriter);
			getSnippetsFrom(notebook, snippets);
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
		Map<Notebook, SnippetCode[]> snippets = new HashMap<Notebook, SnippetCode[]>();
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Hashing snippets in notebook " + i);
			}
			getSnippetsFrom(notebooks.get(i), snippets);
		}
		return snippets;
	}

	/**
	 * Get all snippets from a notebook and put them in the snippets map.
	 * @param notebook Notebook to find snippets in
	 * @param snippets Map to put the snippets in
	 */
	private void getSnippetsFrom(Notebook notebook, Map<Notebook, SnippetCode[]> snippets) {
		SnippetCode[] snippetsInNotebook = employ(new HashExtractor(notebook));
		snippets.put(new Notebook(notebook), snippetsInNotebook);
	}
	
	private Map<SnippetCode, List<Snippet>> getClones(Map<Notebook, SnippetCode[]> fileMap) throws IOException {
		int numAnalyzed = 0;
		Map<SnippetCode, List<Snippet>> clones = new HashMap<SnippetCode, List<Snippet>>();
		for (Notebook notebook: fileMap.keySet()) {
			if (0 == numAnalyzed%10000) {
				System.out.println("Finding clones in notebook " + numAnalyzed);
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
		Writer writer = new FileWriter(outputDir + "/all_languages" + LocalDateTime.now() + ".csv");
		writer.write(allLanguagesHeader());
		for(Notebook notebook: notebooks) {
			allLanguageValuesIn(notebook, writer);
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
	 * Find all language values specified in a notebook and write them to a file
	 * on CSV format.
	 * @param notebook Notebook to read language from
	 * @param langSpecFields Fields that may specify language
	 * @param writer Open writer that writes to the file mentioned above
	 * @throws IOException On problems when writing to the CSV file
	 */
	private void allLanguageValuesIn(Notebook notebook, Writer writer) throws IOException {
		Map<LangSpec, Language> languages = employ(new AllLanguagesExtractor(notebook));
		String name = notebook.getName();
		writer.write(name);
		for (LangSpec field: langSpecFields) {
			writer.write(", " + languages.get(field));
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
	public Map<Language, Integer> languages() throws IOException {
		Map<Language, Integer> languages = new HashMap<Language, Integer>();
		for (Language language: Language.values()) {
			languages.put(language, 0);
		}
		Writer writer = new FileWriter(outputDir + "/languages" + LocalDateTime.now() + ".csv");
		writer.write(languagesHeader());
		for (int i=0; i<notebooks.size(); i++) {
			Language language = languageIn(notebooks.get(i), writer);
			languages.put(language, languages.get(language) + 1);
		}
		writer.close();
		return languages;
	}
	
	/**
	 * @return Header for the languages csv file
	 */
	private static String languagesHeader() {
		return "file, language, language spec in\n";
	}

	/**
	 * Identify the language of a notebook and write it to a file on CSV format.
	 * @param notebook Notebook to identify language in
	 * @param writer Open writer that writes to the languages CSV file
	 * @return Identified language in the notebook
	 * @throws IOException On problems when writing to the CSV file
	 */
	private Language languageIn(Notebook notebook, Writer writer)
			throws IOException {
		Language language = employ(new LanguageExtractor(notebook));
		LangSpec langSpec = employ(new LangSpecExtractor(notebook));
		String name = notebook.getName();
		writer.write(name + ", " + language + ", " + langSpec + "\n");
		return language;
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
		int totalLOC = 0;
		Writer writer = new FileWriter(outputDir + "/loc" + LocalDateTime.now() + ".csv");
		writer.write(LOCHeader());
		for (int i=0; i<notebooks.size(); i++) {
			totalLOC += LOCIn(notebooks.get(i), writer);
		}
		writer.close();
		return totalLOC;
	}

	/**
	 * @return Header for the LOC csv file
	 */
	private static String LOCHeader() {
		return "file, total LOC, non-blank LOC, blank LOC\n";
	}

	/**
	 * Count the number of lines of code in a notebook, and write it to a file on
	 * CSV format.
	 * @param notebook Notebook to count lines in 
	 * @param csvWriter Open writer that writes to the LOC CSV file
	 * @return Total number of LOC in notebook
	 * @throws IOException On problems when writing to the CSV file
	 */
	private int LOCIn(Notebook currentNotebook, Writer writer)
			throws IOException {
		int LOC = employ(new TotalLOCCounter(currentNotebook));
		int LOCNonBlank = employ(new NonBlankLOCCounter(currentNotebook));
		int LOCBlank = employ(new BlankLOCCounter(currentNotebook));
		String name = currentNotebook.getName();
		writer.write(name + ", " + LOC + ", " + LOCNonBlank + ", " + LOCBlank + "\n");
		return LOC;
	}
	
	/**
	 * Count the number of code cells in each notebook. Print each value on a
	 * separate line in the file code_cells<current-date-time>.csv. Start the
	 * csv file with the header "file, code cells".
	 * @return Total number of code cells in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int numCodeCells() throws IOException {
		int totalNumCodeCells = 0;
		Writer writer = new FileWriter(outputDir + "/code_cells" + LocalDateTime.now() + ".csv");
		writer.write(numCodeCellsHeader());
		for (int i=0; i<notebooks.size(); i++) {
			totalNumCodeCells += numCodeCellsIn(notebooks.get(i), writer);
		}
		writer.close();
		return totalNumCodeCells;
	}
	
	/**
	 * @return Header for the file2hashes csv file
	 */
	private String numCodeCellsHeader() {
		return "file, code cells\n";
	}

	/**
	 * Count the number of code cells in a notebook, and write it to a file on
	 * CSV format.
	 * @param notebook Notebook to count code cells in 
	 * @param csvWriter Open writer that writes to the code cells CSV file
	 * @return Number of code cells in notebook
	 * @throws IOException On problems when writing to the CSV file
	 */
	private int numCodeCellsIn(Notebook notebook, Writer csvWriter) 
			throws IOException {
		int numCodeCells = employ(new CodeCellCounter(notebook));
		csvWriter.write(notebook.getName() + ", " + numCodeCells + "\n");
		return numCodeCells;
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
				Map<Language, Integer> languages = this.languages();
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

	private void printLanguageSummary(Map<Language, Integer> languages) {
		System.out.println("\nLANGUAGES:");
		for (Language language: languages.keySet()) {
			System.out.println(language + ": " + languages.get(language));
		}
		System.out.println("");
	}
	
	private <T> T employ(Worker<T> worker) {
		try {
			Future<T> result = executor.submit(worker);
			return result.get();
		} catch (ExecutionException e) {
			System.err.println(e.getMessage() + " Skipping notebook!");
			return worker.defaultValue();
		} catch (InterruptedException e) {
			System.err.println("A thread was interrupted: " +
					e.getMessage() + " Trying again!");
			return employ(worker);
		}
	}

	public static void main(String[] args) {
		NotebookAnalyzer analyzer = new NotebookAnalyzer();
		analyzer.analyze(args);
		analyzer.shutDown();
	}
}
