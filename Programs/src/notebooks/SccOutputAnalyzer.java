package notebooks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SccOutputAnalyzer extends Analyzer {
	private Map<Integer, Set<SccSnippetId>> notebook2snippets;
	private Map<Integer, SccNotebook> notebooks;
	private Map<String, Integer> repros;
	private Map <SccSnippetId, SccSnippet> snippets;
	private String tmpDir = ".";
	
	/**
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * cloneFrequencies<current-date-time>.csv and
	 * connections<current-date-time>.csv accordingly.
	 * This methods initializes snippet and repro information, so you shouldn't
	 * do it explicitly before the call to this method.
	 * Note that the ''hashes'' written by this method are not the MD5 hashes
	 * of the snippets, but just the value of a counter. However, all instances
	 * of the ''hash'' of a snippet are the same.
	 * @param statsFile Path to file stats file produced by the SourcererCC tokenizer
	 * @param reproFile Path to file with mapping from notebook number to repro
	 * @param pairFile Path to zipped output file with clone pairs from the SourcererCC clone detection
	 * @throws IOException
	 */
	public void clones(String statsFile, String reproFile, String pairFile) throws IOException {
		initializeSnippetInfo(statsFile, reproFile);
		clones(pairFile);
	}
	
	/**
	 * Perform the clone analysis based on SourcererCC output files. Write
	 * cloneFrequencies<current-date-time>.csv and
	 * connections<current-date-time>.csv accordingly.
	 * Note that you have to initialize the snippet and repro information, by
	 * calling initializeSnippetInfo before calling this method!
	 * Note that the ''hashes'' written by this method are not the MD5 hashes
	 * of the snippets, but just the value of a counter. However, all instances
	 * of the ''hash'' of a snippet are the same.
	 * @param pairFile Path to zipped output file with clone pairs from the SourcererCC clone detection
	 * @throws IOException
	 */
	public void clones(String pairFile) throws IOException {
		System.out.println("Analyzing clones based on SourcererCC output files!");
		System.out.println("NOTE THAT NOTEBOOKS WITHOUT SNIPPETS ARE NOT INCLUDED");
		System.out.println("since they are not included in the SourcererCC data!");
		storeConnections(pairFile);
		writeCloneFiles();
		SccNotebook.removeDumpDirContents();
	}
	
	/**
	 * Create and fill cloneFrequencies and connections csv files with data for
	 * all notebooks. Create the cloneLoc csv file and fill it with the line
	 * count for each clone instance.
	 * @throws IOException On problems handling the output files
	 */
	private void writeCloneFiles() throws IOException {
		printCloneLoc();
		printCloneFrequencies();
		printConnectionsFile();
	}
	
	/**
	 * Initialize the maps containing information about each snippet
	 * @param statsFile Path to file stats file produced by the SourcererCC tokenizer
	 * @param reproFile Path to file with mapping from notebook number to repro
	 */
	public void initializeSnippetInfo(String statsFile, String reproFile) throws IOException {
		createNotebookMaps(reproFile);
		BufferedReader statsReader = new BufferedReader(new FileReader(statsFile));
		snippets = new HashMap<SccSnippetId, SccSnippet>();
		notebook2snippets = new HashMap<Integer, Set<SccSnippetId>>();
		String line = statsReader.readLine();
		while(null != line) {
			String[] columns = line.split(",");
			SccSnippetId id = new SccSnippetId(columns[0], columns[1]);
			String path = columns[2];
			// Remove directories from filename
			String snippetFileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
			// Remove suffix
			snippetFileName = snippetFileName.substring(0, snippetFileName.lastIndexOf('.'));
			String[] snippetSubStrings = snippetFileName.split("_");
			// Create parent notebook
			int notebookNumber = Integer.parseInt(snippetSubStrings[1]);
			SccNotebook notebook = notebooks.get(notebookNumber);
			/* Here we use the number of lines of source code (comments
			   excluded), which is inconsistent with the clone analysis of the 
			   notebook files, but so is the clone detection -SourcererCC
			   doesn't consider comments in clone analysis. */
			int loc = Integer.parseInt(columns[8]);
			snippets.put(id, new SccSnippet(loc, notebook));
			Set<SccSnippetId> snippetsForNotebook = notebook2snippets.get(notebookNumber);
			if (null == snippetsForNotebook) {
				snippetsForNotebook = new HashSet<SccSnippetId>();
			}
			snippetsForNotebook.add(id);
			notebook2snippets.put(notebookNumber, snippetsForNotebook);
			line = statsReader.readLine();
		}
		statsReader.close();
	}

	/**
	 * Create a map from repro name to integer representation, and from
	 * notebook number to notebook
	 * @param fileName Name of file with mapping from notebook number to repro
	 */
	private void createNotebookMaps(String fileName) throws IOException {
		repros = new HashMap<String, Integer>();
		notebooks = new HashMap<Integer, SccNotebook>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line = reader.readLine();
		while (null != line) {
			String[] subStrings = line.split(",");
			try {
				int notebookNumber = Integer.parseInt(subStrings[0]);
				String notebookName = getNotebookNameFromNumber(notebookNumber);
				String reproName = subStrings[1];
				if (!repros.containsKey(reproName)) {
					repros.put(reproName, repros.size());
				}
				int reproNumber = repros.get(reproName);
				notebooks.put(notebookNumber, new SccNotebook(notebookName, reproNumber));
			} catch (NumberFormatException e) {
				System.err.println("Notebook numbers in repro file must be integers! Notebook with \"number\" '"
						+ subStrings[0] + "' is excluded from mapping!");
			}
			line = reader.readLine();
		}
		reader.close();
	}

	private void storeConnections(String pairFileName) throws IOException {
		SccNotebook.setDumpDir(tmpDir);
		ZipFile zippedPairFile = new ZipFile(pairFileName);
		Enumeration<? extends ZipEntry> pairFileSet = zippedPairFile.entries();
		while (pairFileSet.hasMoreElements()) {
			ZipEntry pairFile = pairFileSet.nextElement();
			InputStream zipStream = zippedPairFile.getInputStream(pairFile);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream));
			long numRead = 0;
			String line = reader.readLine();
			while (null != line) {
				if (0 == numRead%100000000) {
					Utils.heartBeat("Reading clone pair " + numRead + ".");
				}
				storeConnection(line);
				numRead++;
				line = reader.readLine();
			}
			reader.close();
		}
		zippedPairFile.close();
	}

	/**
	 * @param line A line from the clone pairs file from the SourcererCC output
	 */
	private void storeConnection(String line) {
		String[] numbers = line.split(",");
		if (4 != numbers.length) {
			System.err.print("Invalid line \"" + line + "\" in pair file.");
			System.err.println(" Skipping line!");
		} else {
			try {
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
					try {
						snippet1.connect(snippet2);
					} catch (NullPointerException e) {
						// Notebook or repro was null for one of the snippets
						System.err.println("Couldn't add connection between " + id1 + " and " + id2 + ". ");
						System.err.println("Notebook or repro info is missing. Skipping line " + line);
					}
				}
			} catch (NumberFormatException e) {
				// We just skip this line
				System.err.println("Number format exception when parsing line \""
						+ line + "\": " + e.getMessage());
			}
		}
	}
	
	private static String getNotebookNameFromNumber(int notebookNumber) {
		return "nb_" + notebookNumber + ".ipynb";
	}
	
	private void printCloneLoc() throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneLoc" + LocalDateTime.now() + ".csv");
		for (SccSnippet snippet: snippets.values()) {
			if (snippet.isClone()) {
				writer.write(snippet.getLoc() + "\n");
			}
		}
		writer.close();
	}
	
	private void printCloneFrequencies() throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write(cloneFrequencyHeader());
		for (Integer notebookNumber: notebook2snippets.keySet()) {
			int numClones = 0, numUnique = 0, numEmpty = 0;
			int numIntra = 0, numIntraNE = 0;	// # intra notebook clones
			Set<SccSnippetId> snippetsInNotebook = notebook2snippets.get(notebookNumber);
			for (SccSnippetId id: snippetsInNotebook) {
				SccSnippet snippet = snippets.get(id);
				if (snippet.isClone()) {
					numClones++;
				} else {
					numUnique++;
				}
				if (0 == snippet.getLoc()) {
					numEmpty++;
				}
				if (snippet.isIntraNotebookClone()) {
					numIntra++;
				}
			}
			numIntraNE = numIntra;	// No empty clones for Scc data!
			String notebookName = getNotebookNameFromNumber(notebookNumber);
			printCloneFrequencyLine(writer, notebookName, numClones, numUnique, numEmpty, numClones, numIntra, numIntraNE);
		}
		writer.close();
	}
	
	/**
	 * Imagine a graph where the nodes are the notebooks and each snippet that
	 * is shared between two notebooks constitutes an edge between these
	 * notebooks. For each node, let 'edges' be the number of edges
	 * starting/ending at this node and 'repro' be the repro where the current
	 * notebook resides. For every node/notebook, count:
	 * - edges
	 * - normalized edges (that is, edges/number of snippets, or 0 if edges=0)
	 * - edges inside current repro
	 * - mean number edges to other repros
	 * Ignore empty snippets in all computations. Print the values, in the
	 * order mentioned, separated with commas to the file
	 * connections<current-date-time>.csv. Note that when computing the mean,
	 * only repros for which there is a connection are included.
	 */
	private void printConnectionsFile() throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		writer.write(connectionsHeader());
		for (Integer notebookNumber: notebook2snippets.keySet()) {
			SccNotebook notebook = notebooks.get(notebookNumber);
			if (null == notebook) {
				String notebookName = getNotebookNameFromNumber(notebookNumber);
				System.err.print("No repro information stored for notebook " + notebookName + "! ");
				System.err.println("Connections will not be counted!");
			} else {
				int nonEmptySnippets = 0;
				Set<SccSnippetId> snippetsForNotebook = notebook2snippets.get(notebookNumber);
				for (SccSnippetId id: snippetsForNotebook) {
					SccSnippet snippet = snippets.get(id);
					if (0 != snippet.getLoc()) {
						nonEmptySnippets++;
					}
				}
				int interConnections = notebook.numInterReproConnections();
				int intraConnections = notebook.numIntraReproConnections();
				int numInterConnectedRepros = notebook.numReprosInterConnected();
				int connections = interConnections + intraConnections;
				// Empty snippets are considered unique by SourcererCC
				double normalizedNonEmptyConnections = ConnectionsLineBuilder.normalized(connections, nonEmptySnippets);
				double meanNonEmptyInterReproConnections = ConnectionsLineBuilder.normalized(interConnections, numInterConnectedRepros);
				writer.write(notebook.getName() + ", "
						+ connections + ", " + String.format(Locale.US, "%.4f", normalizedNonEmptyConnections) + ", "
					+ intraConnections + ", " + String.format(Locale.US, "%.4f", meanNonEmptyInterReproConnections) + "\n");
			}
		}
		writer.close();
	}
	
	/**
	 * @return Header for the connections csv file
	 */
	private String connectionsHeader() {
		return "file, non-empty connections, non-empty connections normalized, "
				+ "non-empty intra repro connections, mean non-empty inter repro connections\n";
	}
	
	void analyze(String[] args) {
		String pairFile = null;
		String statsFile = null;
		String reproFile = null;
		
		// Set up
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--stats_file")) {
				statsFile = getValueFromArgument(arg);
			} else if (arg.startsWith("--repro_file")) {
				reproFile = getValueFromArgument(arg);
			} else if (arg.startsWith("--pair_file")) {
				pairFile = getValueFromArgument(arg);
			} else if (arg.startsWith("--output_dir")) {
				outputDir = getValueFromArgument(arg);
			} else if (arg.startsWith("--tmp_dir")) {
				tmpDir = getValueFromArgument(arg);
			} else {
				System.err.println("Unknown argument: " + arg);
			}
		}
		
		boolean statsFileSet = null != statsFile && !("".equals(statsFile));
		boolean reproFileSet = null != reproFile && !("".equals(reproFile));
		boolean pairFileSet = null != pairFile && !("".equals(pairFile));
		// Run
		if (pairFileSet && statsFileSet &&  reproFileSet) {
			try {
				this.initializeSnippetInfo(statsFile, reproFile);
			} catch (IOException e) {
				System.err.println("I/O error when initializing snippet info: " + e.getMessage());
				e.printStackTrace();
				System.err.println("Analysis will not be run!");
				System.exit(1);
			}
			try {
				this.clones(pairFile);
				System.out.println("Clone files created!");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Operation interrupted!");
			}
		} else {
			if (!pairFileSet) {
				System.err.println("SourcererCC clones pair file path not set!");
			}
			if (!statsFileSet) {
				System.err.println("Snippet information is not initialized!");
			}
			if (!reproFileSet) {
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
