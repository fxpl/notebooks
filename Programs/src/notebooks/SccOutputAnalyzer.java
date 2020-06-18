package notebooks;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SccOutputAnalyzer extends Analyzer {
	// Information about each snippet
	Map<SccSnippetId, Integer> notebookNumbers;
	Map<SccSnippetId, Integer> snippetIndices;
	Map<SccSnippetId, Integer> linesOfCode;
	// Information about each notebook
	private Map<String, String> repros = null;
	Map<String, Integer> snippetsPerNotebook = null;
	
	/**
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * file2hashesA<current-date-time>.csv, hash2filesA<current-date-time>.csv,
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
	 * file2hashesA<current-date-time>.csv, hash2filesA<current-date-time>.csv,
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
		Map<SnippetCode, List<Snippet>> snippet2file = getClones(pairFile);
		Map<Notebook, SnippetCode[]> file2snippet = getSnippets(snippet2file);
		new CloneFileWriter(outputDir).write(file2snippet, snippet2file);
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
		linesOfCode = new HashMap<SccSnippetId, Integer>();
		snippetsPerNotebook = new HashMap<String, Integer>();
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
			linesOfCode.put(id, loc);
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
		List<List<SccSnippetId>> clones = getCloneLists(pairFile);
		return getCloneMap(clones);
	}
	
	private List<List<SccSnippetId>> getCloneLists(String pairFile) throws IOException {
		List<List<SccSnippetId>> clones = new ArrayList<List<SccSnippetId>>();
		BufferedReader reader = new BufferedReader(new FileReader(pairFile));
		int numRead = 0;
		String line = reader.readLine();
		while (null != line) {
			assert(line.matches("[0-9]+,[0-9]+,[0-9]+,[0-9]+"));
			String[] numbers = line.split(",");
			SccSnippetId id1 = new SccSnippetId(numbers[0], numbers[1]);
			SccSnippetId id2 = new SccSnippetId(numbers[2], numbers[3]);
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
			numRead++;
			if (0 == numRead%1000000) {
				System.out.println(numRead + " clone pairs read.");
			}
			line = reader.readLine();
		}
		reader.close();
		return clones;
	}
	
	private Map<SnippetCode, List<Snippet>> getCloneMap(List<List<SccSnippetId>> clones)
			throws FileNotFoundException {
		Map<SnippetCode, List<Snippet>> result = new HashMap<SnippetCode, List<Snippet>>(clones.size());
		Set<SccSnippetId> snippetIdsToAdd = notebookNumbers.keySet();
		int hashIndex = 0;
		
		// Cloned snippets
		for (List<SccSnippetId> cloned: clones) {
			if (0 == hashIndex%10000) {
				System.out.println("Creating entry  for " + hashIndex + " in snippet-to-files-map.");
			}
			List<Snippet> snippets = new ArrayList<Snippet>();
			int numClones = cloned.size();
			List<Integer> loc = new ArrayList<Integer>(numClones);
			for (int i=0; i<numClones; i++) {
				SccSnippetId id = cloned.get(i);
				addSnippet(id, snippets);
				snippetIdsToAdd.remove(id);
				loc.add(linesOfCode.get(cloned.get(i)));
			}
			int medianLoc = Utils.median(loc, "Different line count for snippet " + Integer.toString(hashIndex));
			SnippetCode hash = new SnippetCode(medianLoc, Integer.toString(hashIndex++));
			result.put(hash, snippets);
		}
		
		// Remaining snippets are unique. Add them!
		for (SccSnippetId id: snippetIdsToAdd) {
			if (0 == hashIndex%10000) {
				System.out.println("Creating entry  for " + hashIndex + " in snippet-to-files-map.");
			}
			List<Snippet> snippets = new ArrayList<>(1);
			addSnippet(id, snippets);
			int loc = linesOfCode.get(id);
			SnippetCode hash = new SnippetCode(loc, Integer.toString(hashIndex++));
			result.put(hash, snippets);
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
		String notebookName = getNotebookNameFromNumber(notebookNumbers.get(id)); 
		int snippetIndex = snippetIndices.get(id);
		snippets.add(new Snippet(notebookName, repros.get(notebookName), snippetIndex));
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
			if (0 == numAdded%10000) {
				System.out.println("Adding snippet " + hash + " to notebook-to-snippet-map.");
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
		
		// Run
		// (If notebookNumbers is null, none of the snippet info maps are initialized.)
		if (null != pairFile && "" != pairFile && null != notebookNumbers &&  null !=this.repros) {
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
