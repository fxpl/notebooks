package notebooks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SccOutputAnalyzer extends Analyzer {
	Map<String, Set<SccSnippetId>> file2snippets;	// TODO: Int istf String?
	Map <SccSnippetId, SccSnippet> snippets;
	// Information about each snippet
	Map<SccSnippetId, Integer> notebookNumbers;
	Map<SccSnippetId, Integer> snippetIndices;
	// Information about each notebook
	private Map<String, String> repros = null;
	Map<String, Integer> snippetsPerNotebook = null;
	
	/**
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * file2hashes[A|NE]<<current-date-time>.csv, hash2filesA<current-date-time>.csv,
	 * cloneFrequencies<current-date-time>.csv and
	 * connections<current-date-time>.csv accordingly.
	 * This methods initializes snippet and repro information, so you shouldn't
	 * do it explicitly before the call to this method.
	 * Note that the ''hashes'' written by this method are not the MD5 hashes
	 * of the snippets, but just the value of a counter. However, all instances
	 * of the ''hash'' of a snippet are the same.
	 * @param statsFile Path to file stats file produced by the SourcererCC tokenizer
	 * @param reproFile Path to file with mapping from notebook number to repro
	 * @param pairFile: Path to output file with clone pairs from the SourcererCC clone detection
	 * @return A map from snippets to files
	 * @throws IOException
	 */
	public Map<SnippetCode, List<Snippet>> clones(String statsFile, String reproFile, String pairFile) throws IOException {
		initializeSnippetInfo(statsFile);
		initializeReproMap(reproFile);
		return clones(pairFile);
	}
	
	/**
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * file2hashes[A|NE]<<current-date-time>.csv, hash2filesA<current-date-time>.csv,
	 * cloneFrequencies<current-date-time>.csv and
	 * connections<current-date-time>.csv accordingly.
	 * Note that you have to initialize the snippet and repro information, by
	 * calling initializeSnippetInfo and initializeReproMap respectively before
	 * calling this method!
	 * Note that the ''hashes'' written by this method are not the MD5 hashes
	 * of the snippets, but just the value of a counter. However, all instances
	 * of the ''hash'' of a snippet are the same.
	 * @param pairFile: Path to output file with clone pairs from the SourcererCC clone detection
	 * @return A map from snippets to files
	 * @throws IOException
	 */
	public Map<SnippetCode, List<Snippet>> clones(String pairFile) throws IOException {
		System.out.println("Analyzing clones based on SourcererCC output files!");
		System.out.println("NOTE THAT NOTEBOOKS WITHOUT SNIPPETS ARE NOT INCLUDED");
		System.out.println("since they are not included in the SourcererCC data!");
		Map<SnippetCode, List<Snippet>> snippet2file;
		snippet2file = getClones(pairFile);
		Map<Notebook, SnippetCode[]> file2snippet = getSnippets(snippet2file);	// TODO: Ska bort!
		storeConnections(pairFile);
		CloneFileWriter writer = new CloneFileWriter(outputDir);
		//writer.write(file2snippet, snippet2file);
		writer.write(file2snippet, snippet2file, file2snippets, snippets);
		return snippet2file;
	}
	
	/**
	 * Initialize repro information for each notebook.
	 * @param fileName Path to file with mapping from notebook number to repro
	 */
	public void initializeReproMap(String fileName) throws IOException {
		repros = createReproMap(fileName);
	}
	
	/**
	 * Initialize the maps containing information about each snippet
	 * @param statsFile Path to file stats file produced by the SourcererCC tokenizer
	 */
	public void initializeSnippetInfo(String statsFile) throws IOException {
		BufferedReader statsReader = new BufferedReader(new FileReader(statsFile));
		notebookNumbers = new HashMap<SccSnippetId, Integer>();
		snippetIndices = new HashMap<SccSnippetId, Integer>();
		snippets = new HashMap<SccSnippetId, SccSnippet>();
		snippetsPerNotebook = new HashMap<String, Integer>();	// TODO: Int istf String?
		file2snippets = new HashMap<String, Set<SccSnippetId>>();
		String line = statsReader.readLine();
		while(null != line) {
			String[] columns = line.split(",");
			SccSnippetId id = new SccSnippetId(columns[0], columns[1]);
			String path = columns[2];
			// Remove directories from filename
			String snippetFileName = path.substring(path.lastIndexOf('/') + 1);
			// Remove suffix
			snippetFileName = snippetFileName.substring(0, snippetFileName.lastIndexOf('.'));
			String[] snippetSubStrings = snippetFileName.split("_");
			int notebookNumber = Integer.parseInt(snippetSubStrings[1]);
			String notebookName = getNotebookNameFromNumber(notebookNumber);
			addOrIncrease(snippetsPerNotebook, notebookName);
			notebookNumbers.put(id, notebookNumber);
			snippetIndices.put(id, Integer.parseInt(snippetSubStrings[2]));
			/* Here we use the number of lines of source code (comments
			   excluded), which is inconsistent with the clone analysis of the 
			   notebook files, but so is the clone detection -SourcererCC
			   doesn't consider comments in clone analysis. */
			int loc = Integer.parseInt(columns[8]);
			snippets.put(id, new SccSnippet(loc));
			Set<SccSnippetId> snippetsForNotebook = file2snippets.get(notebookName);
			if (null == snippetsForNotebook) {
				snippetsForNotebook = new HashSet<SccSnippetId>();
			}
			snippetsForNotebook.add(id);
			file2snippets.put(notebookName, snippetsForNotebook);
			line = statsReader.readLine();
		}
		statsReader.close();
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

	/**
	 * Create a mapping from snippets to notebooks (hash2files) using output
	 * files from SourcererCC.
	 */
	private Map<SnippetCode, List<Snippet>> getClones(String pairFile) throws IOException {
		HashMap<CloneGroup, List<SccSnippetId>> intermediateCloneMap = getCloneMap(pairFile);
		return getClones(intermediateCloneMap);
	}
	
	private void storeConnections(String pairFile) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(pairFile));
		long numRead = 0;
		String line = reader.readLine();
		while (null != line) {
			if (0 == numRead%100000000) {
				Utils.heartBeat("Reading clone pair " + numRead + ".");
			}
			String[] numbers = line.split(",");
			if (4 != numbers.length) {
				System.err.println("Invalid line number " + (numRead + 1) + " in pair file: " + line);
				System.err.println(" Skipping line!");
			} else {
				try {
					// TODO: Tester!
					SccSnippetId id1 = new SccSnippetId(numbers[0], numbers[1]);
					SccSnippetId id2 = new SccSnippetId(numbers[2], numbers[3]);
					SccSnippet snippet1 = snippets.get(id1);
					SccSnippet snippet2 = snippets.get(id2);
					if (null == snippet1) {
						System.err.println("ID for nonexistent snippet (" + id1 + ") found on line \""
								+ line + "\". Skipping clone pair!");
					}
					if (null == snippet2) {
						System.err.println("ID for nonexistent snippet (" + id2 + ") found on line \""
								+ line + "\". Skipping clone pair!");
					}
					if (null != snippet1 && null != snippet2) {
						// TODO: Spara repro i snippet istället!?
						Integer notebook1Number = notebookNumbers.get(id1);
						Integer notebook2Number = notebookNumbers.get(id2);
						if (null != notebook1Number && null != notebook2Number) {
							String notebook1Name = getNotebookNameFromNumber(notebook1Number);
							String notebook2Name = getNotebookNameFromNumber(notebook2Number);
							String repro1 = repros.get(notebook1Name);
							String repro2 = repros.get(notebook2Name);
							boolean intraNotebook = notebook1Name.equals(notebook2Name);
							boolean intraRepro = repro1.equals(repro2);
							snippet1.addConnection(intraNotebook, intraRepro, repro2);
							snippet2.addConnection(intraNotebook, intraRepro, repro1);
						} else {
							if (null == notebook1Number) {
								System.err.println("Notebook missing for snippet " + id1 + ").");
							} if (null == notebook1Number) {
								System.err.println("Notebook missing for snippet " + id2 + ").");
							}
							System.err.println("Clone pair " + line + " skipped!");
						}
					}
				} catch (NumberFormatException e) {
					// We just skip this line
					System.err.println("Number format exception when parsing line \""
							+ line + "\": " + e.getMessage());
				}
			}
			numRead++;
			line = reader.readLine();
		}
		reader.close();
	}
	
	private HashMap<CloneGroup, List<SccSnippetId>> getCloneMap(String pairFile) throws IOException {
		HashMap<SccSnippetId, CloneGroup> clones = new HashMap<SccSnippetId, CloneGroup>();
		final BufferedReader reader = new BufferedReader(new FileReader(pairFile));
		long numRead = 0;
		String line = reader.readLine();
		while (null != line) {
			if (0 == numRead%100000000) {
				Utils.heartBeat("Reading clone pair " + numRead + ".");
			}
			String[] numbers = line.split(",");
			if (4 != numbers.length) {
				System.err.println("Invalid line number " + (numRead + 1) + " in pair file: " + line);
				System.err.println(" Skipping line!");
			} else {
				SccSnippetId id1, id2;
				try {
					id1 = new SccSnippetId(numbers[0], numbers[1]);
					id2 = new SccSnippetId(numbers[2], numbers[3]);
					ensureClonePairStored(id1, id2, clones);
				} catch (NumberFormatException e) {
					// We just skip this line
					System.err.println("Number format exception when parsing line \""
							+ line + "\": " + e.getMessage());
				}
			}
			numRead++;
			if (0 == numRead%5000000) {
				compact(clones);
			}
			line = reader.readLine();
		}
		reader.close();
		return invertMap(clones);
	}

	/**
	 * Ensure that two snippets are stored as a clone pair in the provided map
	 * @param id1 ID of first snippet
	 * @param id2 ID of second snippet
	 * @param clones Map containing clone groups
	 */
	private void ensureClonePairStored(SccSnippetId id1, SccSnippetId id2,
			HashMap<SccSnippetId, CloneGroup> clones) {
		CloneGroup id1Clones = clones.get(id1);
		CloneGroup id2Clones = clones.get(id2);
		if (id1Clones == id2Clones) {
			if (id1Clones != null) {
				/// We already had them marked as clones
			} else {
				/// Create a new clone set with this data
				CloneGroup top = new CloneGroup();
				clones.put(id1, top);
				clones.put(id2, top);
			}
		} else {
			/// Merge the sets as they are both clones, and point both to same set
			if (id1Clones == null) {
				clones.put(id1, id2Clones.top());
			} else if (id2Clones == null) {
				clones.put(id2, id1Clones.top());
			} else {
				CloneGroup top = id1Clones.merge(id2Clones);
				if (id1Clones != top) {
					clones.put(id1, top);
				}
				if (id2Clones != top) {
					clones.put(id2, top);
				}
			}
		}
	}
	
	/**
	 * Merge entries in clones so that each clone group is only stored once.
	 */
	private static void compact(HashMap<SccSnippetId, CloneGroup> clones) {
		for(Map.Entry<SccSnippetId, CloneGroup> entry : clones.entrySet()){
			CloneGroup group = entry.getValue();
			if (null != group) {
				entry.setValue(group.top());
			}
		}
	}
	
	private static HashMap<CloneGroup, List<SccSnippetId>> invertMap(HashMap<SccSnippetId, CloneGroup> clones) {
		// Required for correctness
		compact(clones);

		final HashMap<CloneGroup, List<SccSnippetId>> outerResult = new HashMap<CloneGroup, List<SccSnippetId>>();
		final Set<SccSnippetId> keySet = clones.keySet();
		int keyNum = 0;
		for (SccSnippetId key : keySet) {
			if (0 == keyNum % 10000000) {
				Utils.heartBeat("Inverting data for key " + keyNum + ".");
			}
			keyNum++;
			CloneGroup cg = clones.get(key);
			List<SccSnippetId> list = outerResult.get(cg);
			if (list == null) {
				list = new ArrayList<SccSnippetId>();
				list.add(key);
				outerResult.put(cg, list);
			} else {
				list.add(key);
			}
		}
		return outerResult;
	}
	
	// TODO: Hela den här lär tas bort!
	private Map<SnippetCode, List<Snippet>> getClones(HashMap<CloneGroup, List<SccSnippetId>> intermediateCloneMap)
			throws FileNotFoundException {
		Map<SnippetCode, List<Snippet>> result = new HashMap<SnippetCode, List<Snippet>>(intermediateCloneMap.size());
		Set<SccSnippetId> snippetIdsToAdd = new HashSet<SccSnippetId>(notebookNumbers.keySet());
		int hashIndex = 0;
		
		// Cloned snippets
		for (List<SccSnippetId> cloned: intermediateCloneMap.values()) {
			if (0 == hashIndex%1000000) {
				Utils.heartBeat("Creating entry for " + hashIndex + " in snippet-to-files-map.");
			}
			List<Snippet> snippetsTmp = new ArrayList<Snippet>();
			int numClones = cloned.size();
			List<Integer> loc = new ArrayList<Integer>(numClones);
			for (int i=0; i<numClones; i++) {
				SccSnippetId id = cloned.get(i);
				addSnippet(id, snippetsTmp);
				snippetIdsToAdd.remove(id);
				SccSnippet snippet = snippets.get(id);
				if (null == snippet) {	// TODO: Lär inte hända!?
					System.err.println("Nonexistent snippet (" + id + ") skipped!");
				} else {
					int currentLoc = snippet.getLoc();
					loc.add(currentLoc);
				}
			}
			int medianLoc = Utils.median(loc, "Different line count for snippet " + Integer.toString(hashIndex));
			SnippetCode hash = new SnippetCode(medianLoc, Integer.toString(hashIndex++));
			result.put(hash, snippetsTmp);
		}
		
		// Remaining snippets are unique. Add them!
		for (SccSnippetId id: snippetIdsToAdd) {
			if (0 == hashIndex%1000000) {
				Utils.heartBeat("Creating entry for " + hashIndex + " in snippet-to-files-map.");
			}
			List<Snippet> snippetsTmp = new ArrayList<>(1);
			addSnippet(id, snippetsTmp);
			int loc = snippets.get(id).getLoc();
			SnippetCode hash = new SnippetCode(loc, Integer.toString(hashIndex++));
			result.put(hash, snippetsTmp);
		}
		return result;
	}

	/**
	 * Add the snippet with the specified SourcererCC snippet id to the list
	 * specified.
	 * @param id SourcererCC snippet id of snippet to add
	 * @param snippets List of snippets, to which the snippet will be added
	 */
	private void addSnippet(SccSnippetId id, List<Snippet> snippets) {
		Integer notebookNumber = notebookNumbers.get(id);
		if (null == notebookNumber) {
			System.err.println("Snippet without notebook (" + id + ") skipped!");
		} else {
			String notebookName = getNotebookNameFromNumber(notebookNumber);
			int snippetIndex = snippetIndices.get(id);
			snippets.add(new Snippet(notebookName, repros.get(notebookName), snippetIndex));
		}
	}
	
	private Map<Notebook, SnippetCode[]> getSnippets(Map<SnippetCode, List<Snippet>> snippet2file) {
		Map<Notebook, SnippetCode[]> result = new HashMap<Notebook, SnippetCode[]>(snippetsPerNotebook.size());
		// Create arrays for snippets
		for (String notebookName: snippetsPerNotebook.keySet()) {
			String repro = repros.get(notebookName);
			result.put(new Notebook(notebookName, repro), new SnippetCode[snippetsPerNotebook.get(notebookName)]);
			
		}
		// Put snippet in notebook-to-snippet-map
		int numAdded = 0;
		for (SnippetCode hash: snippet2file.keySet()) {
			if (0 == numAdded%1000000) {
				Utils.heartBeat("Adding snippet " + hash + " to notebook-to-snippet-map.");
			}
			for (Snippet snippet: snippet2file.get(hash)) {
				SnippetCode[] snippetsInFile = result.get(new Notebook(snippet.getFileName()));
				snippetsInFile[snippet.getSnippetIndex()] = new SnippetCode(hash);
			}
			numAdded++;
		}
		return result;
	}
	
	private static String getNotebookNameFromNumber(int notebookNumber) {
		return "nb_" + notebookNumber + ".ipynb";
	}
	
	void analyze(String[] args) {
		String pairFile = null;
		
		// Set up
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--stats_file")) {
				String statsFile = getValueFromArgument(arg);
				try {
					initializeSnippetInfo(statsFile);
				} catch (IOException e) {
					System.err.println("I/O error when initializing snippet info: " + e.getMessage());
				}
			} else if (arg.startsWith("--repro_file")) {
				String reproFile = getValueFromArgument(arg);
				try {
					this.initializeReproMap(reproFile);
				} catch (IOException e) {
					System.err.println("I/O error when initializing repro info: " + e.getMessage());
					System.err.println("Repro information not initialized!");
				}
			} else if (arg.startsWith("--pair_file")) {
				pairFile = getValueFromArgument(arg);
			} else if (arg.startsWith("--output_dir")) {
				outputDir = getValueFromArgument(arg);
			} else {
				System.err.println("Unknown argument: " + arg);
			}
		}
		
		boolean pairFileSet = null != pairFile && !("".equals(pairFile));
		// Run
		// (If notebookNumbers is null, none of the snippet info maps are initialized.)
		if (pairFileSet && null != notebookNumbers &&  null !=this.repros) {
			try {
				this.clones(pairFile);
				System.out.println("Clone files created!");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Operation interrupted!");
			}
		} else {
			if (null == pairFile || "" == pairFile) {
				System.err.println("SourcererCC clones pair file path not set!");
			}
			if (null == notebookNumbers) {
				System.err.println("Snippet information is not initialized!");
			}
			if (null == this.repros) {
				System.err.println("Repro information is not initialized!");
			}
			System.err.println("Analysis will not be run!");
		}
	}
	
	public static void main(String[] args) {
		SccOutputAnalyzer analyzer = new SccOutputAnalyzer();
		analyzer.analyze(args);
		ThreadExecutor.tearDown();
	}
}
