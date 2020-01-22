package notebooks;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
	private Map<String, String> repros = null;	// TODO: Only needed for scc. Get rid of!
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
	void initializeReproMap(String fileName) throws FileNotFoundException {
		this.repros = createReproMap(fileName);
		for (Notebook nb: notebooks) {
			nb.setRepro(repros.get(nb.getName()));
		}
	}
	
	/**
	 * Create a map from notebook name to repro.
	 * @param fileName Name of file with mapping from notebook number to repro
	 * @return The map from notebook name to repro
	 */
	private static Map<String, String> createReproMap(String fileName) throws FileNotFoundException {
		Map<String, String> result = new HashMap<String, String>();
		Scanner scanner = new Scanner(new File(fileName));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
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
		}
		scanner.close();
		return result;
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
		new CloneFileWriter(outputDir).write(snippets, clones);
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
		new CloneFileWriter(outputDir).write(snippets, clones);
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
					// TODO Konstruktor som tar notebook som argument istället?! Eller Lagra notebook i!
					clones.get(snippetCodes[j]).add(new Snippet(notebook.getName(), notebook.getRepro(), j));
				} else {
					List<Snippet> snippets = new ArrayList<Snippet>();
					snippets.add(new Snippet(notebook.getName(), notebook.getRepro(), j));
					clones.put(snippetCodes[j], snippets);
				}
			}
			numAnalyzed++;
		}
		return clones;
	}
	
	 /** TODO: Bryt ut SCC-analysen till en separat klass!
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * file2hashes<current-date-time>.csv, hash2files<current-date-time>.csv,
	 * cloneFrequencies<current-date-time>.csv and
	 * connections<current-date-time>.csv accordingly.
	 * Note that the ''hashes'' written by this method are not the MD5 hashes
	 * of the snippets, but just the value of a counter. However, all instances
	 * of the ''hash'' of a snippet are the same.
	 * @param sccPairFile: Output file with clone pairs from the SourcererCC clone detection
	 * @param sccStatsFile: File stats file created by the SourcererCC tokenizer
	 * @return A map from snippets to files
	 * @throws IOException
	 */
	public Map<SnippetCode, List<Snippet>> clones(String sccPairFile, String sccStatsFile) throws IOException {
		System.out.println("Analyzing clones based on SourcererCC output files!");
		System.out.println("NOTE THAT NOTEBOOKS WITHOUT SNIPPETS ARE NOT INCLUDED!");
		Map<String, Integer> snippetsPerFile = new HashMap<String, Integer>();
		Map<SnippetCode, List<Snippet>> snippet2file = getClones(sccPairFile, sccStatsFile, snippetsPerFile);
		Map<Notebook, SnippetCode[]> file2snippet = getSnippets(snippet2file, snippetsPerFile);
		// TODO: Lös detta snyggare!
		for (Notebook notebook: file2snippet.keySet()) {
			notebook.setRepro(repros.get(notebook.getName()));
		}
		new CloneFileWriter(outputDir).write(file2snippet, snippet2file);
		return snippet2file;
	}

	/**
	 * Create a mapping från snippets to notebooks (hash2files) using output
	 * files from SourcererCC. Also count the number of snippets for each
	 * notebook and store the result in snippetsPerFile (the third argument)
	 */
	private Map<SnippetCode, List<Snippet>> getClones(String sccPairFile, String sccStatFile,
			Map<String, Integer> snippetsPerFile) throws IOException {
		List<List<SccSnippetId>> clones = getCloneLists(sccPairFile);
		return getCloneMap(clones, sccStatFile, snippetsPerFile);
	}
	
	private List<List<SccSnippetId>> getCloneLists(String sccPairFile) throws FileNotFoundException {
		List<List<SccSnippetId>> clones = new ArrayList<List<SccSnippetId>>();
		Scanner scanner = new Scanner(new File(sccPairFile));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			assert(line.matches("[0-9]+,[0-9]+,[0-9]+,[0-9]+"));
			String[] numbers = line.split(",");
			SccSnippetId id1 = new SccSnippetId(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));
			SccSnippetId id2 = new SccSnippetId(Integer.parseInt(numbers[2]), Integer.parseInt(numbers[3]));
			boolean bothStored = false;
			Iterator<List<SccSnippetId>> it = clones.iterator();
			while (!bothStored && it.hasNext()) {
				List<SccSnippetId> existing = it.next();
				boolean id1stored = existing.contains(id1);
				boolean id2stored = existing.contains(id2); 
				if(id1stored && id2stored) {
					bothStored = true;
				} else if (id1stored && !id2stored) {
					existing.add(id2);
					bothStored = true;
				} else if(id2stored && !id1stored) {
					existing.add(id1);
					bothStored = true;
				}
			}
			if (!bothStored) {
				List<SccSnippetId> newCloneList = new ArrayList<SccSnippetId>();
				newCloneList.add(id1);
				newCloneList.add(id2);
				clones.add(newCloneList);
			}
		}
		scanner.close();
		return clones;
	}
	
	private Map<SnippetCode, List<Snippet>> getCloneMap(
			List<List<SccSnippetId>> clones, String sccStatsFile, Map<String, Integer> snippetsPerFile)
			throws FileNotFoundException {
		// Maps needed for analysis
		Map<SccSnippetId, Integer> notebookNumbers = new HashMap<SccSnippetId, Integer>();
		Map<SccSnippetId, Integer> snippetIndices = new HashMap<SccSnippetId, Integer>();
		Map<SccSnippetId, Integer> linesOfCode = new HashMap<SccSnippetId, Integer>();
		initializeMapsFromStatsFile(sccStatsFile, notebookNumbers, snippetIndices, linesOfCode);
		return getCloneMap(clones, snippetsPerFile, notebookNumbers, snippetIndices, linesOfCode);
	}

	private Map<SnippetCode, List<Snippet>> getCloneMap(
			List<List<SccSnippetId>> clones,
			Map<String, Integer> snippetsPerFile,
			Map<SccSnippetId, Integer> notebookNumbers,
			Map<SccSnippetId, Integer> snippetIndices,
			Map<SccSnippetId, Integer> linesOfCode) {
		Map<SnippetCode, List<Snippet>> result = new HashMap<SnippetCode, List<Snippet>>(clones.size());
		int hashIndex = 0;
		
		// Cloned snippets
		for (List<SccSnippetId> cloned: clones) {
			List<Snippet> snippets = new ArrayList<Snippet>();
			for (SccSnippetId id: cloned) {
				String nbName = "nb_" + notebookNumbers.remove(id) + ".ipynb";
				addOrIncrease(snippetsPerFile, nbName);
				int snippetIndex = snippetIndices.remove(id);
				Snippet snippet = new Snippet(nbName, repros.get(nbName), snippetIndex);
				snippets.add(snippet);
			}
			int loc = linesOfCode.get(cloned.get(0));	 // TODO: Borde göras på något smartare sätt. Max? Min? Medel?
			SnippetCode hash = new SnippetCode(loc, Integer.toString(hashIndex++));
			result.put(hash, snippets);
		}
		
		// TODO: Get rid of duplication!
		// Remaining snippets are unique. Add them!
		for (SccSnippetId id: notebookNumbers.keySet()) {
			List<Snippet> snippets = new ArrayList<>(1);
			String nbName = "nb_" + notebookNumbers.get(id) + ".ipynb";
			addOrIncrease(snippetsPerFile, nbName);
			int snippetIndex = snippetIndices.remove(id);
			Snippet snippet = new Snippet(nbName, repros.get(nbName), snippetIndex);
			snippets.add(snippet);
			int loc = linesOfCode.get(id);
			SnippetCode hash = new SnippetCode(loc, Integer.toString(hashIndex++));
			result.put(hash, snippets);
		}
		return result;
	}

	/**
	 * @param sccStatsFile File stats file produced by the SourcererCC tokenizer
	 * @param notebookNumbers Notebook number for each snippet
	 * @param snippetIndices Index for each snippet
	 * @param linesOfCode LOC for each snippet
	 * @throws FileNotFoundException If the stats file doesn't exist
	 */
	private void initializeMapsFromStatsFile(String sccStatsFile,
			Map<SccSnippetId, Integer> notebookNumbers,
			Map<SccSnippetId, Integer> snippetIndices,
			Map<SccSnippetId, Integer> linesOfCode)
			throws FileNotFoundException {
		Scanner statsScanner = new Scanner(new File(sccStatsFile));
		while(statsScanner.hasNextLine()) {
			String line = statsScanner.nextLine();
			String[] columns = line.split(",");
			int id1 = Integer.parseInt(columns[0]);
			int id2 = Integer.parseInt(columns[1]);
			SccSnippetId id = new SccSnippetId(id1, id2);
			String path = columns[2];
			// Remove directories from filename
			String snippetFileName = path.substring(path.lastIndexOf('/') + 1);
			// Remove suffix
			snippetFileName = snippetFileName.substring(0, snippetFileName.lastIndexOf('.'));
			String[] snippetSubStrings = snippetFileName.split("_");
			notebookNumbers.put(id, Integer.parseInt(snippetSubStrings[1]));
			snippetIndices.put(id, Integer.parseInt(snippetSubStrings[2]));
			/* Here we use the number of lines of source code (comments
			   excluded), which is inconsistent with the clone analysis of the 
			   notebook files, but so is the clone detection -SourcererCC
			   doesn't consider comments in clone analysis. */
			int loc = Integer.parseInt(columns[8]);
			linesOfCode.put(id, loc);
		}
		statsScanner.close();
	}

	/**
	 * If map contains a value for key, increase it with 1. Else add an entry
	 * with for key with the value 1.
	 * @param map Map to modify as stated above
	 * @param key Key for the entry that will be changed/added
	 */
	private void addOrIncrease(Map<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1);
		} else {
			map.put(key, 1);
		}
	}

	private Map<Notebook, SnippetCode[]> getSnippets(Map<SnippetCode, List<Snippet>> snippet2file,
			Map<String, Integer> snippetsPerFile) {
		Map<Notebook, SnippetCode[]> result = new HashMap<Notebook, SnippetCode[]>(snippetsPerFile.size());
		// Create arrays for snippets
		for (String notebook: snippetsPerFile.keySet()) {
			// TODO: Är det en dålig idé att skapa alla notebooks en gång till?!
			result.put(new Notebook(notebook), new SnippetCode[snippetsPerFile.get(notebook)]);
		}
		// Put snippet in notebook to snippet map
		for (SnippetCode hash: snippet2file.keySet()) {
			for (Snippet snippet: snippet2file.get(hash)) {
				// TODO: Lägg notebooken i snippet istället!
				SnippetCode[] snippetsInFile = result.get(new Notebook(snippet.getFileName()));
				snippetsInFile[snippet.getSnippetIndex()] = new SnippetCode(hash);
			}
		}

		return result;
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
		// TODO: Bättre testtäckning!
		// TODO: mer enhetliga flagg/metod/variabelnamn
		boolean all = false,
				count = false,
				lang = false,
				loc = false,
				clones = false,
				langAll = false,
				clonesScc = false;
		String reproFile = null;
		String nbPath = null;
		String outputDir = null;
		String sccStatsFile = null;
		String sccPairFile = null;

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
					System.err.println("Argument '-output_dir' must be followed by the path to the output dir!");
					System.err.println("Default output directory is used!");
				}
				break;
			case "-scc_stats_file":
				try {
					sccStatsFile = args[++i];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Argument '-scc_stats_file' must be followed by the path to the SourcererCC file stats file!");
				}
				break;
			case "-scc_clones_file":
				try {
					sccPairFile = args[++i];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.println("Argument -scc_clones_file must be followed by the path to the SourcererCC clone pairs file!");
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
			case "-clones_scc":
				clonesScc = true;
				break;
			case "-lang_all":
				langAll = true;
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
			if (clonesScc) {
				if (null != sccStatsFile && null != sccPairFile && null !=reproFile) {
					this.clones(sccPairFile, sccStatsFile);
					System.out.println("Clone files created!");
				} else {
					if (null == sccPairFile) {
						System.err.println("SourcererCC clones pair file path not set!");
					}
					if (null == sccStatsFile) {
						System.err.println("SourcererCC file statistics file path not set!");
					}
					if (null == reproFile) {
						System.err.println("Repro mapping file path not set!");
					}
					System.err.println("Clone analysis will not be run!");
				}
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
