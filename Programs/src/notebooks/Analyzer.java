package notebooks;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;

/**
 * Analyzer for Jupyter notebooks.
 */
public class Analyzer {
	private ExecutorService executor;
	private List<Notebook> notebooks;
	private static LangSpec[] langSpecFields = {LangSpec.METADATA_LANGUAGE , LangSpec.METADATA_LANGUAGEINFO_NAME, 
			LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_NAME,
			LangSpec.CODE_CELLS};
	private Map<String, String> repros = null;
	private String outputDir = ".";	// Default value
	
	/**
	 * Note that when you are done with this Analyzer, you must call the method
	 * shutDown!
	 */
	public Analyzer() {
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
	 * TODO: Lägga reproinfo i notebook istället, och använd notebookobjekt i file2hash-map!? Isf måste Snippetobjektet innehålla en notebook istf ett filnamn, för att man ska kunna hämta info om reprot!
	 * Initialize the map from notebook name to repro.
	 * @param fileName Name of file with mapping from notebook number to repro
	 */
	void initializeReproMap(String fileName) throws FileNotFoundException {
		repros = new HashMap<String, String>();
		Scanner scanner = new Scanner(new File(fileName));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] subStrings = line.split(",");
			try {
				int notebookNumber = Integer.parseInt(subStrings[0]);
				String notebookName = "nb_" + notebookNumber + ".ipynb";
				String reproName = subStrings[1];
				repros.put(notebookName, reproName);
			} catch (NumberFormatException e) {
				System.err.println("Notebook numbers in repro file must be integers! Notebook with \"number\" '"
						+ subStrings[0] + "' is excluded from mapping!");
			}
		}
		scanner.close();
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
		
		Map<String, SnippetCode[]> snippets = new HashMap<String, SnippetCode[]>();
		for (Notebook notebook: this.notebooks) {
			numCodeCellsIn(notebook, codeCellsWriter);
			LOCIn(notebook, LOCWriter);
			languageIn(notebook, langWriter);
			allLanguageValuesIn(notebook, allLangWriter);
			getSnippetsFrom(notebook, snippets);
		}
		/* Language summary is not printed here, since the information can
		   easily be extracted from the CSV file. */
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		printCloneFiles(snippets, clones);
		
		codeCellsWriter.close();
		LOCWriter.close();
		langWriter.close();
		allLangWriter.close();
	}
	
	/**
	 * Compute the MD5 hash of each snippet in each notebook. Return a map with
	 * the key being the snippet code and the values being lists of all
	 * snippets containing that code.
	 * For each notebook, print the name of the notebook followed by a list of
	 * the hash of each snippet in the notebook to the file 
	 * file2hashes<current-date-time>.csv.
	 * Print each hash and the corresponding snippets (name, index) on a
	 * separate line in the file hash2files<current-date-time>.csv. Start the
	 * csv files with a header.
	 * @return The map described above
	 * @throws IOException On problems handling the output file.
	 */
	public Map<SnippetCode, List<Snippet>> clones() throws IOException {
		Map<String, SnippetCode[]> snippets = getSnippets();
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		printCloneFiles(snippets, clones);
		return clones;
	}

	/**
	 * @return A map from file names to snippets
	 */
	private Map<String, SnippetCode[]> getSnippets() throws IOException {
		Map<String, SnippetCode[]> snippets = new HashMap<String, SnippetCode[]>();
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
	private void getSnippetsFrom(Notebook notebook, Map<String, SnippetCode[]> snippets) {
		String fileName = notebook.getName();
		SnippetCode[] snippetsInNotebook = employ(new HashExtractor(notebook));
		snippets.put(fileName, snippetsInNotebook);
	}
	
	/**
	 * @return A map from snippets to files
	 */
	private Map<SnippetCode, List<Snippet>> getClones(Map<String, SnippetCode[]> fileMap) throws IOException {
		int numAnalyzed = 0;
		Map<SnippetCode, List<Snippet>> clones = new HashMap<SnippetCode, List<Snippet>>();
		for (String fileName: fileMap.keySet()) {
			if (0 == numAnalyzed%10000) {
				System.out.println("Finding clones in notebook " + numAnalyzed);
			}
			SnippetCode[] snippetCodes = fileMap.get(fileName);
			for (int j=0; j<snippetCodes.length; j++) {
				if (clones.containsKey(snippetCodes[j])) {
					clones.get(snippetCodes[j]).add(new Snippet(fileName, j));
				} else {
					List<Snippet> snippets = new ArrayList<Snippet>();
					snippets.add(new Snippet(fileName, j));
					clones.put(snippetCodes[j], snippets);
				}
			}
			numAnalyzed++;
		}
		return clones;
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files and cloneFrequencies files
	 * @param snippets A map from file names to snippets
	 * @param clones A map from snippets to files
	 * @throws IOException On problems handling the output files
	 */
	private void printCloneFiles(Map<String, SnippetCode[]> snippets,
			Map<SnippetCode, List<Snippet>> clones) throws IOException {
		printFile2hashes(snippets);
		printHash2files(clones);
		printCloneFrequencies(snippets, clones);
		printConnectionsFile(snippets, clones);
	}

	private void printHash2files(Map<SnippetCode, List<Snippet>> clones) throws IOException {
		Writer writer = new FileWriter(outputDir + "/hash2files" + LocalDateTime.now() + ".csv");
		writer.write(hash2filesHeader());
		for (SnippetCode code: clones.keySet()) {
			writer.write(code.getHash() + ", " + code.getLOC());
			for (Snippet s: clones.get(code)) {
				writer.write(", " + s.toString());
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	/**
	 * @return Header for the file2hashes csv file
	 */
	private String hash2filesHeader() {
		return "hash, LOC, file, index, ...\n";
	}
	
	private void printFile2hashes(Map<String, SnippetCode[]> files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/file2hashes" + LocalDateTime.now() + ".csv");
		writer.write(file2hashesHeader());
		for (String fileName: files.keySet()) {
			writer.write(fileName);
			SnippetCode[] code = files.get(fileName);
			for (SnippetCode snippet: code) {
				writer.write(", " + snippet.getHash());
			}
			writer.write("\n");
		}
		writer.close();
	}

	/**
	 * @return Header for the file2hashes csv file
	 */
	private String file2hashesHeader() {
		return "file, snippets\n";
	}
	
	private void printCloneFrequencies(Map<String, SnippetCode[]> file2Hashes,
			Map<SnippetCode, List<Snippet>> hash2Files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write(cloneFrequencyHeader());
		for (String fileName: file2Hashes.keySet()) {
			int numClones = 0, numUnique = 0;
			SnippetCode[] code = file2Hashes.get(fileName);
			for (SnippetCode snippet: code) {
				if(isClone(snippet, hash2Files)) {
					numClones++;
				} else {
					numUnique++;
				}
			}
			writer.write(fileName + ", " + numClones + ", " + numUnique + ", ");
			int numSnippets = numClones + numUnique;
			if (0 != numSnippets) {
				double cloneFrequency = (double)numClones / numSnippets;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + "\n");
			} else {
				writer.write("0\n");
			}
		}
		writer.close();
	}

	/**
	 * @return Header for the cloneFrequency csv file
	 */
	private String cloneFrequencyHeader() {
		return "file, clones, unique, clone frequency\n";
	}
	
	/**
	 * Imagine a graph where the nodes are the notebooks and each snippet that
	 * is shared between two notebooks constitutes an edge between these
	 * notebooks. For each node, let 'edges' be the number of edges
	 * starting/ending at this node and 'repro' be the repro where the current
	 * notebook resides. For every node/notebook, count
	 * - edges, incl. empty snippets
	 * - edges, excl. empty snippets
	 * - normalized edges (that is, edges/number of snippets, or 0 if edges=0)
	 * - normalized edges for non-empty snippets
	 * - edges inside current repro
	 * - edges inside current repro, incl. those representing empty snippets
	 * - edges inside current repro, excl. those representing empty snippets
	 * - mean number edges to other repros, incl. those representing empty snippets
	 * - mean number edges to other repros, excl. those representing empty snippets
	 * Print the values, in the order mentioned, separated with commas to the file
	 * connections<current-date-time>.csv.
	 * Note that when computing the mean, only repros for which there is a
	 * connection are included.
	 * @param file2snippets Mapping from notebook name to snippets
	 * @param snippet2files Mapping from snippets to position in notebooks
	 */
	private void printConnectionsFile(Map<String, SnippetCode[]> file2snippets,
			Map<SnippetCode, List<Snippet>> snippet2files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		writer.write(connectionsHeader());
		for (String fileName: file2snippets.keySet()) {
			printConnections(fileName, file2snippets, snippet2files, writer);
		}
		writer.close();
	}

	/**
	 * @param fileName Name of notebook file
	 * @param file2snippets Mapping from notebook name to snippets
	 * @param snippets2files Mapping from snippets to position in notebooks
	 * @param writer Writer that will print the result
	 */
	private void printConnections(String fileName, Map<String, SnippetCode[]> file2snippets,
			Map<SnippetCode, List<Snippet>> snippets2files, Writer writer)
			throws IOException {
		int connections = 0;
		int nonEmptyConnections = 0;	// Connections excluding empty snippets
		int intraReproConnections = 0;
		int nonEmtpyIntraReproConnections = 0;
		int interReproConnections = 0;
		int nonEmptyInterReproConnections = 0;
		String currentRepro = repros.get(fileName);
		SnippetCode[] snippets = file2snippets.get(fileName);
		Set<String> otherRepros = new TreeSet<String>();
		Set<String> otherNonEmptyRepros = new TreeSet<String>();	// Other repros with non-empty friends
		int numNonEmptySnippets = 0;
		for (SnippetCode snippet: snippets) {
			// Locations where the current snippet can be found
			List<Snippet> locations = snippets2files.get(snippet);
			int connectionsForSnippet = connections(locations);
			int intraReproConnectionsForSnippet = intraReproConnections(locations, currentRepro); 
			connections += connectionsForSnippet;
			intraReproConnections += intraReproConnectionsForSnippet;
			interReproConnections += interReproConnections(locations, currentRepro, otherRepros);
			if (0 < snippet.getLOC()) {	// Non-empty snippet
				numNonEmptySnippets++;
				nonEmptyConnections += connectionsForSnippet;
				nonEmtpyIntraReproConnections += intraReproConnectionsForSnippet;
				nonEmptyInterReproConnections += interReproConnections(locations, currentRepro, otherNonEmptyRepros);
			}
		}
		int numSnippets = snippets.length;
		double normalizedConnections = normalized(connections, numSnippets);
		double normalizedNonEmptyConnections = normalized(nonEmptyConnections, numNonEmptySnippets);
		double meanInterReproConnections = normalized(interReproConnections, otherRepros.size());
		double meanNonEmptyInterReproConnections = normalized(nonEmptyInterReproConnections, otherNonEmptyRepros.size());
		
		writer.write(fileName + ", " + connections + ", "
				+ String.format(Locale.US, "%.4f", normalizedConnections) + ", "
				+ nonEmptyConnections + ", "
				+ String.format(Locale.US, "%.4f", normalizedNonEmptyConnections) + ", "
				+ intraReproConnections + ", " + nonEmtpyIntraReproConnections + ", "
				+ String.format(Locale.US, "%.4f", meanInterReproConnections) + ", "
				+ String.format(Locale.US, "%.4f", meanNonEmptyInterReproConnections) + "\n");
	}
	
	/**
	 * Count the number of connections from a snippet in locations to other
	 * snippets in locations.
	 * @param locations Locations where the current snippet can be found
	 */
	private int connections(List<Snippet> locations) {
		return locations.size() - 1;	// -1 for current notebook
	}
	
	/**
	 * Count the number of connection from a snippet in locations to other
	 * snippets in locations that reside in the same repro.
	 * @param location Locations where the current snippet can be found
	 * @param currentRepro Name of the repro where the snippet for which we count connections reside
	 */
	private int intraReproConnections(List<Snippet> locations, String currentRepro) {
		int connections = 0;
		for (Snippet friend: locations) {
			String friendRepro = repros.get(friend.getFileName());
			if (friendRepro.equals(currentRepro)) {
				connections++;
			}
		}
		// Don't count connections from the current snippet to itself!
		return connections - 1;
	}
	
	/**
	 * Count the number of connections from a snippet in locations to other
	 * snippets in locations that reside in another repro. Make sure that the
	 * name of each repro where any of the locations reside (except the current
	 * one) are stored in the set otherRepros.
	 * @param location Locations where the current snippet can be found
	 * @param currentRepro Name of the repro where the current snippet reside
	 * @param otherRepros Set that will contain all other repros that the snippet is connected to
	 */
	private int interReproConnections(List<Snippet> locations, String currentRepro, Set<String> otherRepros) {
		int connections = 0;
		for (Snippet friend: locations) {
			String friendFileName = friend.getFileName();
			String friendRepro = repros.get(friendFileName);
			if (!friendRepro.equals(currentRepro)) {
				connections++;
				otherRepros.add(friendRepro);
			}
		}
		return connections;
	}
	
	/**
	 * Normalize numerator by dividing it by denominator, unless the denominator is 0.
	 * Then return 0.
	 * @param numerator
	 * @param denominator
	 * @return numerator normalized according to description above
	 */
	private double normalized(int numerator, int denominator) {
		if (0 == denominator) {
			return 0;
		} else {
			return (double)numerator/denominator;
		}
	}
	
	/**
	 * @return Header for the connections csv file
	 */
	private String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized"
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections\n";
	}
	
	/**
	 * Look in clones to decide whether snippet is a clone or a unique snippet
	 * (that is, if the list of snippets is at least 2).
	 * @return true if snippet is a clone, false otherwise
	 */
	private boolean isClone(SnippetCode snippet, Map<SnippetCode, List<Snippet>> clones) {
		List<Snippet> snippets = clones.get(snippet);
		return snippets.size() >= 2;
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
	private void analyze(String[] args) {
		boolean all = false,
				count = false,
				lang = false,
				loc = false,
				clones = false,
				langAll = false;
		String reproFile = null;
		String nbPath = null;
		String outputDir = null;

		// Read arguments
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			switch (arg) {
			case "-nb_path":
				try {
					nbPath = args[++i];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Argument '-nb_path' must be followed by the path of the notebook(s)!");
					System.err.println("No notebooks will be analyzed!");
				}
				break;
			case "-repro_file":
				try {
					reproFile = args[++i];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Argument '-repro_file' must be followed by the path to the repro file!");
					System.err.println("Repro information not initialized!");
				}
				break;
			case "-output_dir":
				try {
					outputDir = args[++i];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Argument '-output_dir' must be followed the the path to the output dir!");
					System.err.println("Default output directory is used!");
				}
				break;
			case "-all":
				all = true;
				break;
			case "-count":
				count = true;
				break;
			case "-lang":
				lang = true;
				break;
			case "-loc":
				loc = true;
				break;
			case "-clones":
				clones = true;
				break;
			case "-lang_all":
				lang = true;
				break;
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
			
		// Perform analyzes
		try {
			if (null != nbPath) {
				this.initializeNotebooksFrom(nbPath);
			}
			if (null != reproFile) {
				try {
					this.initializeReproMap(reproFile);
				} catch (FileNotFoundException e) {
					System.err.println("Repro file not found: " + e.getMessage());
					System.err.println("Repro information not initialized!");
				}
			}
			if (null != outputDir) {
				this.outputDir = outputDir;
			}
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
		Analyzer analyzer = new Analyzer();
		analyzer.analyze(args);
		analyzer.shutDown();
	}
}
