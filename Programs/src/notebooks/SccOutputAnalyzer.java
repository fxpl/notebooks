package notebooks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SccOutputAnalyzer extends Analyzer {
	Map<String, Set<SccSnippetId>> file2snippets;	// TODO: Int istf String?
	Map <SccSnippetId, SccSnippet> snippets;
	// Information about each snippet
	Map<SccSnippetId, Notebook> notebookNumbers;	// TODO: Behövs denna?
	// Information about each notebook
	private Map<String, String> repros = null;	// TODO: Behövs denna?
	
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
	 * @param pairFile: Path to output file with clone pairs from the SourcererCC clone detection
	 * @return A map from snippets to files
	 * @throws IOException
	 */
	public void clones(String statsFile, String reproFile, String pairFile) throws IOException {
		initializeSnippetInfo(statsFile);
		initializeReproMap(reproFile);
		clones(pairFile);
		//return clones(pairFile);
	}
	
	/** TODO: Borde den returnera något?
	 * Perform the clone analysis based on SourcererCC output files. Write
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
	public void clones(String pairFile) throws IOException {
		System.out.println("Analyzing clones based on SourcererCC output files!");
		System.out.println("NOTE THAT NOTEBOOKS WITHOUT SNIPPETS ARE NOT INCLUDED");
		System.out.println("since they are not included in the SourcererCC data!");
		storeConnections(pairFile);
		CloneFileWriter writer = new CloneFileWriter(outputDir);
		writer.write(null, null, file2snippets, snippets);
		//return snippet2file;
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
		notebookNumbers = new HashMap<SccSnippetId, Notebook>();
		snippets = new HashMap<SccSnippetId, SccSnippet>();
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
			notebookNumbers.put(id, new Notebook(notebookName));	// TODO: NotebookID istället?
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
						Notebook notebook1 = notebookNumbers.get(id1);
						Notebook notebook2 = notebookNumbers.get(id2);
						if (null != notebook1 && null != notebook2) {
							String repro1 = repros.get(notebook1.getName());
							String repro2 = repros.get(notebook2.getName());
							boolean intraNotebook = notebook1.equals(notebook2);
							boolean intraRepro = repro1.equals(repro2);
							snippet1.addConnection(intraNotebook, intraRepro, repro2);
							snippet2.addConnection(intraNotebook, intraRepro, repro1);
						} else {
							if (null == notebook1) {
								System.err.println("Notebook missing for snippet " + id1 + ").");
							} if (null == notebook2) {
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
