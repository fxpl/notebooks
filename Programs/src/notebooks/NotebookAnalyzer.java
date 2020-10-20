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
			Utils.heartBeat("Traversing " + file.getPath());
			// The file is a directory. Traverse it.
			String[] subFiles = file.list();
			for (String subFileName: subFiles) {
				initializeNotebooksFrom(file.getPath() + File.separator + subFileName);
			}
		}
	}
	
	/**
	 * Initialize the map from notebook name to repro, and add information about repro to each notebook.
	 * @param fileName Name of file with mapping from notebook number to repro
	 */
	void initializeReproInfo(String fileName) throws IOException {
		Map<String, String> reproMap = createReproMap(fileName);
		for (Notebook nb: notebooks) {
			nb.setRepro(reproMap.get(nb.getName()));
		}
	}
	
	/**
	 * Create a map from notebook name to repro name.
	 * @param fileName Name of file with mapping from notebook number to repro
	 * @return The map from notebook name to repro name
	 */
	protected static Map<String, String> createReproMap(String fileName) throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = reader.readLine();
		while (null != line) {
			String[] subStrings = line.split(",");
			try {
				int notebookNumber = Integer.parseInt(subStrings[0]);
				String notebookName = "nb_" + notebookNumber + ".ipynb";
				String reproName = subStrings[1];
				result.put(notebookName, reproName);
			} catch (NumberFormatException e) {
				System.err.println("Notebook numbers in repro file must be integers! Notebook with \"number\" '"
						+ subStrings[0] + "' is excluded from mapping!");
			}
			line = reader.readLine();
		}
		reader.close();
		return result;
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
		writeCloneFiles(snippets, clones);
	}
	
	/**
	 * Compute the MD5 hash of each snippet in each notebook. Return a map with
	 * the key being the snippet code and the values being lists of all
	 * snippets containing that code.
	 * For each notebook, print the name of the notebook followed by a list of
	 * the hash of each snippet in the notebook to the file 
	 * file2hashes[A|NE]<<current-date-time>.csv.
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
		writeCloneFiles(snippets, clones);
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
				Utils.heartBeat("Retrieving hashes in " + notebook.getName());
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
				Utils.heartBeat("Finding clones in " + notebook.getName());
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
			int medianLoc = Utils.median(locValues, "Different line count for snippet " + snippet.getHash());
			snippet.setLOC(medianLoc);
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
	 * Create and fill file2Hashes, hash2Files cloneFrequencies and connections
	 * files with data for all notebooks.
	 * @param file2hashes A map from file names to snippets
	 * @param hash2files A map from snippets to files
	 * @throws IOException On problems handling the output files
	 */
	public void writeCloneFiles(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files) throws IOException {
		writeCloneFiles(file2hashes, hash2files, file2hashes.keySet().size());
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files cloneFrequencies  files with
	 * data for all notebooks. Create and fill connections file for
	 * CONNECTION_NOTEBOOKS notebooks
	 * @param file2hashes A map from file names to snippets
	 * @param hash2files A map from snippets to files
	 * @param CONNECTION_NOTEBOOKS Number of notebooks to print connection data for
	 * @throws IOException On problems handling the output files
	 */
	private void writeCloneFiles(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files, int CONNECTION_NOTEBOOKS) throws IOException {
		printFile2hashes(file2hashes);
		printHash2files(hash2files);
		printCloneFrequencies(file2hashes, hash2files);
		printConnectionsFile(file2hashes, hash2files, CONNECTION_NOTEBOOKS);
	}
	
	private void printFile2hashes(Map<Notebook, SnippetCode[]> file2hashes) throws IOException {
		String timeStamp = LocalDateTime.now().toString();
		Writer writerA = new FileWriter(outputDir + "/file2hashesA" + timeStamp + ".csv");
		writerA.write(file2hashesHeader());
		for (Notebook notebook: file2hashes.keySet()) {
			writerA.write(notebook.getName());
			SnippetCode[] code = file2hashes.get(notebook);
			for (SnippetCode snippet: code) {
				writerA.write(", " + snippet.getHash());
			}
			writerA.write("\n");
		}
		writerA.close();
	}
	
	private void printHash2files(Map<SnippetCode, List<Snippet>> hash2files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/hash2filesA" + LocalDateTime.now() + ".csv");
		writer.write(hash2filesHeader());
		for (SnippetCode code: hash2files.keySet()) {
			writer.write(code.getHash() + ", " + code.getLOC());
			for (Snippet s: hash2files.get(code)) {
				writer.write(", " + s.toString());
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	private void printCloneFrequencies(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write(cloneFrequencyHeader());
		for (Notebook notebook: file2hashes.keySet()) {
			int numClones = 0, numUnique = 0, numEmpty = 0, numClonesNE = 0;
			int numIntra = 0, numIntraNE = 0;	// # intra notebook clones
			SnippetCode[] code = file2hashes.get(notebook);
			for (SnippetCode snippet: code) {
				if(isClone(snippet, hash2files)) {
					numClones++;
					boolean intra = snippet.isIntraClone(code);
					if (intra) {
						numIntra++;
					}
					if (snippet.isEmpty()) {
						numEmpty++;
					} else {
						if (intra) {
							numIntraNE++;
						}
					}
				} else {
					numUnique++;
				}
			}
			numClonesNE = numClones - numEmpty;
			printCloneFrequencyLine(writer, notebook.getName(), numClones, numUnique, numEmpty, numClonesNE, numIntra, numIntraNE);
		}
		writer.close();
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
	 * connections<current-date-time>.csv for a random sample of NUM_CONNECTIONS
	 * notebooks in file2hashes (unless NUM_CONNECTIONS > number of notebooks
	 * in the analysis --then data is printed for the whole set of notebooks).
	 * Note that when computing the mean, only repros for which there is a
	 * connection are included.
	 * @param file2hashes Mapping from notebook name to snippets
	 * @param hash2files Mapping from snippets to position in notebooks
	 * @param NUM_NOTEBOOKS Maximum number of notebooks to print connection information for
	 */
	private void printConnectionsFile(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files, final int NUM_CONNECTIONS) throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		writer.write(connectionsHeader());
		List<Notebook> notebooks = new ArrayList<Notebook>(file2hashes.keySet());
		Collections.shuffle(notebooks);
		int connectionsToPrint = Math.min(NUM_CONNECTIONS, file2hashes.size());
		List<Callable<String>> tasks = new ArrayList<Callable<String>>(connectionsToPrint);
		for (int i=0; i<connectionsToPrint; i++) {
			boolean heartBeat = 0 == i%10000;
			tasks.add(new ConnectionsLineBuilder(notebooks.get(i), file2hashes, hash2files, heartBeat));
		}
		List<Future<String>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		for (int i=0; i<connectionsToPrint; i++) {
			try {
				writer.write(result.get(i).get());
			} catch (InterruptedException | ExecutionException e) {
				System.err.println("Printing connections for notebook "
						+ notebooks.get(i).getName() + " failed!");
				e.printStackTrace();
			}
		}
		writer.close();
	}
	
	/**
	 * @return Header for the file2hashes csv file
	 */
	private static String file2hashesHeader() {
		return "file, snippets\n";
	}
	
	/**
	 * @return Header for the hash2files csv file
	 */
	private static String hash2filesHeader() {
		return "hash, LOC, file, index, ...\n";
	}
	
	/**
	 * @return Header for the connections csv file
	 */
	private static String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized, "
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections\n";
	}
	
	/**
	 * Look in clones to decide whether snippet is a clone or a unique snippet
	 * (that is, if the list of snippets is at least 2).
	 * @return true if snippet is a clone, false otherwise
	 */
	private static boolean isClone(SnippetCode snippet, Map<SnippetCode, List<Snippet>> hash2files) {
		List<Snippet> snippets = hash2files.get(snippet);
		return snippets.size() >= 2;
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
		Language languageValue = getLanguage(language, notebook);
		writeLanguagesLine(languageValue, notebook, writer);
		return languageValue.getName();
	}
	
	private Language getLanguage(Future<Language> language, Notebook notebook) {
		try {
			return language.get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get language information for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			return new Language();
		}
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
	 * The data for each notebook is printed on a separate line.
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
	 * Create a file modules<timestamp>.csv with the header line
	 * followed by the modules imported in each notebook as a comma separated
	 * list. The data for each notebooks is printed on a separate line.
	 * For each of the 10 most commonly imported modules, create a file
	 * <moduleName>-functions<timestamp>.csv containing the functions for this
	 * module that are used in the corpus, sorted on number of usages in
	 * descending order.
	 * @return @return A list of the all modules used and their count, sorted on count in descending order
	 * @throws IOException On problems handling the output file
	 */
	public List<Quantity> modules() throws IOException {
		List<List<PythonModule>> allModules = this.listModules();
		List<Quantity> modulesSorted = sortedModules(allModules);
		this.functionUsages(allModules, modulesSorted, 10);
		return modulesSorted;
	}
	
	/**
	 * Create a file modules<timestamp>.csv with the header line followed by
	 * the modules imported in each notebook as a comma separated list. The
	 * data for each notebooks is printed on a separate line.
	 * @return A list where each element is a list of the modules found in the corresponding notebook
	 * @throws IOException On problems with handling the output file
	 */
	List<List<PythonModule>> listModules() throws IOException {
		List<Notebook> pythonNotebooks = getPythonNotebooks();
		List<Callable<List<PythonModule>>> moduleTasks
			= new ArrayList<Callable<List<PythonModule>>>();
		for (Notebook notebook: pythonNotebooks) {
			moduleTasks.add(new ModulesIdentifier(notebook));
		}
		List<Future<List<PythonModule>>> modules = ThreadExecutor.getInstance().invokeAll(moduleTasks);
		
		List<List<PythonModule>> result = new ArrayList<List<PythonModule>>(pythonNotebooks.size());
		Writer writer = new FileWriter(outputDir + "/modules" + LocalDateTime.now() + ".csv");
		writer.write(modulesHeader());
		for (int i=0; i<modules.size(); i++) {
			Notebook notebook = pythonNotebooks.get(i);
			if (0 == i%10000) {
				Utils.heartBeat("Identifying modules in " + notebook.getName());
			}
			result.add(i, writeModuleLine(modules.get(i), notebook, writer));
		}
		writer.close();
		return result;
	}
	
	/**
	 * @return A list of all Python notebooks in the corpus
	 */
	private List<Notebook> getPythonNotebooks() {
		List<Callable<Language>> languageTasks
			= new ArrayList<Callable<Language>>(notebooks.size());
		for (Notebook notebook: notebooks) {
			languageTasks.add(new LanguageExtractor(notebook));
		}
		List<Future<Language>> languages = ThreadExecutor.getInstance().invokeAll(languageTasks);
	
		List<Notebook> pythonNotebooks = new ArrayList<Notebook>();
		for (int i=0; i<notebooks.size(); i++) {
			Notebook notebook = notebooks.get(i);
			LangName langName = getLanguage(languages.get(i), notebook).getName();
			if (LangName.PYTHON == langName) {
				pythonNotebooks.add(notebook);
			}
		}
		return pythonNotebooks;
	}

	/**
	 * Write the data for one notebook to the modules file.
	 * @param modules Wrapper around the modules of the notebook
	 * @param notebook Notebook to write information for
	 * @param writer Writer that appends text to the modules file
	 */
	private List<PythonModule> writeModuleLine(Future<List<PythonModule>> modules, Notebook notebook, Writer writer) throws IOException {
		List<PythonModule> result;
		try {
			result = modules.get();
		} catch(InterruptedException | ExecutionException e) {
			System.err.println("Could not get modules for " + notebook.getName() + ":" + e);
			e.printStackTrace();
			result = new ArrayList<PythonModule>(0);
		}
		writeModuleLine(result, notebook, writer);
		return result;
	}

	private void writeModuleLine(List<PythonModule> modules, Notebook notebook, Writer writer) throws IOException {
		writer.write(notebook.getName());
		for (PythonModule module: modules) {
			writer.write(", " + module);
		}
		writer.write("\n");
	}
	
	private String modulesHeader() {
		return "notebook, modules ...\n";
	}
	
	/**
	 * Count how many times each module has been imported (its count). Return a
	 * list of quantities (name + count) representing the modules, sorted on
	 * count in descending order.
	 * @param modules List of list of all imported modules
	 * @return A list of the all modules used and their count, sorted on count in descending order
	 */
	static List<Quantity> sortedModules(List<List<PythonModule>> modules) {
		Map<String, Integer> moduleQuantities = new HashMap<String, Integer>();
		for (List<PythonModule> notebookModules: modules) {
			for (PythonModule module: notebookModules) {
				Utils.addOrIncrease(moduleQuantities, module.pedigreeString());
			}
		}
		return sortedQuantities(moduleQuantities);
	}
	
	/**
	 * Create a string containing the most common modules and their count
	 * (i.e. how many times they are imported).
	 * @param modules List of list of all imported modules, sorted in descending order
	 * @param maxNum Maximum number of modules to include in the string
	 * @return A string containing a list of the most common modules and their count
	 */
	static String mostCommonModulesAsString(List<Quantity> modules, int maxNum) {
		final int modulesToPrint = Math.min(maxNum, modules.size());
		String result ="";
		for (int i=0; i<modulesToPrint; i++) {
			result += (i+1) + ". " + modules.get(i) + "\n";
		}
		return result;
	}
	
	/**
	 * For each of the most commonly imported modules, create a file called
	 * <moduleName>-functions<timestamp>.csv, where moduleName is the name of
	 * the module. In the file, list all functions from this module that are
	 * used in the corpus. Sort on number of usages in descending order. If
	 * maxNum < the number of modules, only maxNum files are created. Else one
	 * file is created for every module.
	 * @param allModules A list containing lists of modules used in each notebook
	 * @param modulesSorted Contains one entry for each module, sorted on the number of usages of each module, in descencing order
	 * @param maxNum Maximum number of output files to create
	 * @throws IOException On problems handling the output files
	 */
	void functionUsages(List<List<PythonModule>> allModules, List<Quantity> modulesSorted, int maxNum) throws IOException {
		// Create list with top modules
		int numModules = Math.min(modulesSorted.size(), maxNum);
		List<PythonModule> topModulesWithFunctions = new ArrayList<PythonModule>(numModules);
		for (int i=0; i<numModules; i++) {
			Quantity quantity = modulesSorted.get(i);
			PythonModule module = getModuleFromPedigreeString(quantity.getIdentifier());
			topModulesWithFunctions.add(module);
		}
		// Merge all modules from allModules into the top modules (in parallel)
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(numModules);
		CountDownLatch counter = new CountDownLatch(numModules);
		for (PythonModule module: topModulesWithFunctions) {
			tasks.add(new ModulesMerger(module, allModules, counter));
		}
		ThreadExecutor.getInstance().invokeAll(tasks);
		try {
			counter.await();	// Block until all tasks have finished.
		} catch (InterruptedException e) {
			System.err.println("Thread was interrupted while merging modules:" + e);
			e.printStackTrace();
		}
		
		// Write result
		for (PythonModule module: topModulesWithFunctions) {
			List<Quantity> functionQuantities = sortedQuantities(module.functionUsages);
			String csvFileName = outputDir + File.separator + module.pedigreeString() + "-functions" + LocalDateTime.now() + ".csv";
			Writer writer = new FileWriter(csvFileName);
			writer.write(functionUsagesHeader());
			for (Quantity function: functionQuantities) {
				writer.write(function.toCsvString() + "\n");
			}
			writer.close();
		}
	}

	private static PythonModule getModuleFromPedigreeString(String pedigreeString) {
		PythonModule parent = null;
		PythonModule module;
		String[] identifierParts = pedigreeString.split("\\.");
		for (int j=0; j<identifierParts.length-1; j++) {
			module = new PythonModule(identifierParts[j], ImportType.ORDINARY, parent);
			parent = module;
		}
		String moduleName = identifierParts[identifierParts.length - 1];
		if ("*".equals(moduleName)) {
			module = new AllModules(parent);
		} else {
			module = new PythonModule(moduleName, ImportType.ORDINARY, parent);
		}
		return module;
	}


	/**
	 * @return Header for the function usage csv files
	 */
	private String functionUsagesHeader() {
		return "function, usages\n";
	}

	/**
	 * Create a list containing quantities (name + count) created from each
	 * key-value pair in the map passed as an argument. The list is sorted on
	 * the counts of the quantities, in descending order.
	 * @param quantities Map with name-value pairs
	 * @return Sorted list of quantities as described above.
	 */
	private static List<Quantity> sortedQuantities(Map<String, Integer> quantities) {
		List<Quantity> quantitesSorted = new ArrayList<Quantity>(quantities.size());
		for (String key: quantities.keySet()) {
			quantitesSorted.add(new Quantity(key, quantities.get(key)));
		}
		Collections.sort(quantitesSorted, Collections.reverseOrder());
		return quantitesSorted;
	}
	
	/**
	 * For each function listed in the file given as argument, create a file
	 * named function<current-date-time>.csv containing all calls to the
	 * function, one per line.
	 */
	public void listFunctionCalls(String functionsFile) throws IOException {
		List<PythonModule> functions = getListedFunctions(functionsFile);
		
		List<Callable<Map<PythonModule, List<String>>>> tasks
			= new ArrayList<Callable<Map<PythonModule, List<String>>>>(notebooks.size());
		for (int i=0; i<notebooks.size(); i++) {
			Notebook nb = notebooks.get(i);
			boolean heartBeat = 0 == i%10000;
			tasks.add(new FunctionCallsGetter(nb, functions, heartBeat));
		}
		List<Future<Map<PythonModule, List<String>>>> functionCalls = ThreadExecutor.getInstance().invokeAll(tasks);
		
		Map<PythonModule, List<String>> calls = getFunctionCalls(functions, functionCalls);
		writeFunctionLists(calls, functions);
	}
	
	private List<PythonModule> getListedFunctions(String functionsFile)
			throws FileNotFoundException, IOException {
		List<PythonModule> functions = new ArrayList<PythonModule>();
		BufferedReader reader = new BufferedReader(new FileReader(functionsFile));
		String line = reader.readLine();
		while (null != line) {
			functions.add(PythonModule.fromPedigreeString(line));
			line = reader.readLine();
		}
		reader.close();
		return functions;
	}

	private Map<PythonModule, List<String>> getFunctionCalls(List<PythonModule> functions,
			List<Future<Map<PythonModule, List<String>>>> functionCalls) {
		Map<PythonModule, List<String>> calls = new HashMap<PythonModule, List<String>>(functions.size());
		// Put keys
		for (PythonModule function: functions) {
			calls.put(function, new ArrayList<String>());
		}
		
		// Put values
		for (int i=0; i<notebooks.size(); i++) {
			Map<PythonModule, List<String>> callsInNotebook = getCallsForNotebook(i, functionCalls);
			for (PythonModule function: functions) {
				if (callsInNotebook.containsKey(function)) {
					calls.get(function).addAll(callsInNotebook.get(function));
				}
			}
		}
		return calls;
	}
	
	private Map<PythonModule, List<String>> getCallsForNotebook(int notebookIndex,
			List<Future<Map<PythonModule, List<String>>>> functionCalls) {
		try {
			return functionCalls.get(notebookIndex).get();
		} catch (InterruptedException | ExecutionException e) {
			System.err.println("Could not get function calls from " + notebooks.get(notebookIndex).getName() + ":" + e);
			e.printStackTrace();
			return new HashMap<PythonModule, List<String>>(0);
		}
	}

	private void writeFunctionLists(Map<PythonModule, List<String>> calls,
			List<PythonModule> functions) throws IOException {
		for (PythonModule function: functions) {
			Writer writer = new FileWriter(outputDir + "/" + function.pedigreeString() + "-calls" + LocalDateTime.now() + ".csv");
			for (String call: calls.get(function)) {
				writer.write(call + "\n");
			}
			writer.close();
		}
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
		List<Future<Integer>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		Writer writer = new FileWriter(outputDir + "/code_cells" + LocalDateTime.now() + ".csv");
		writer.write(numCodeCellsHeader());
		int totalNumCodeCells = 0;
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
				langAll = false,
				modules = false;
		String reproFile = null;
		String nbPath = null;
		String listFunctionsFile = null;
		
		// Read arguments
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--nb_path")) {
				nbPath = getValueFromArgument(arg);
			} else if (arg.startsWith("--output_dir")) {
				this.outputDir = getValueFromArgument(arg);
			} else if (arg.startsWith("--repro_file")) {
				reproFile = getValueFromArgument(arg);
			} else if (arg.startsWith("--functions")) {
				listFunctionsFile = getValueFromArgument(arg);
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
				case "--modules":
					modules = true;
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
			} catch (IOException e) {
				System.err.println("I/O error when initializing repro info: " + e.getMessage());
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
			if (modules) {
				List<Quantity> modulesSorted = modules();
				System.out.println("\nMost common modules:");
				System.out.print(mostCommonModulesAsString(modulesSorted, 100));
			}
			if (null != listFunctionsFile) {
				this.listFunctionCalls(listFunctionsFile);
				System.out.println("Function calls listed!");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Operation interrupted!");
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
