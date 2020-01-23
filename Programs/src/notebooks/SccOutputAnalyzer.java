package notebooks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class SccOutputAnalyzer extends Analyzer {
	private Map<String, String> repros = null;	// TODO: Går det att bli av med denna?
	
	void initializeReproMap(String fileName) throws FileNotFoundException {
		this.repros = createReproMap(fileName);
	}
	
	/**
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
		for (String notebookName: snippetsPerFile.keySet()) {
			// TODO: Är det en dålig idé att skapa alla notebooks en gång till?!
			String repro = repros.get(notebookName);
			result.put(new Notebook(notebookName, repro), new SnippetCode[snippetsPerFile.get(notebookName)]);
			
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
	
	// TODO: Se över vilka argument som ska skickas!
	void analyze(String[] args) {
		String sccStatsFile = null;
		String sccPairFile = null;
		String reproFile = null;
		
		// Read arguments
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			switch (arg) {	
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
				// TODO: Duplicerat från NotebookAnalyzer
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
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
		
		if (null != reproFile) {
			try {
				this.initializeReproMap(reproFile);
			} catch (FileNotFoundException e) {
				System.err.println("Repro file not found: " + e.getMessage());
				System.err.println("Repro information not initialized!");
			}
		}
		if (null != sccStatsFile && null != sccPairFile && null !=reproFile) {
			try {
				this.clones(sccPairFile, sccStatsFile);
				System.out.println("Clone files created!");
			} catch (IOException e) {
				System.err.println("I/O error: " + e.getMessage() + ". Operation interrupted.");
			}
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
	
	public static void main(String[] args) {
		
	}
}
