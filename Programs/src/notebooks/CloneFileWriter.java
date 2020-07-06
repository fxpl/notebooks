package notebooks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CloneFileWriter {
	private String outputDir;
	
	public CloneFileWriter(String outputDir) {
		this.outputDir = outputDir;
	}
	
	/**
	 * TODO: Kommentar
	 * TODO: Oanvända argument
	 */
	public void write(Map<Notebook, SnippetCode[]> file2hashes, Map<SnippetCode,
			List<Snippet>> hash2files, Map<String, Set<SccSnippetId>> file2snippets,
			Map<SccSnippetId, SccSnippet> snippets) throws IOException {
		printCloneFrequencies2(file2snippets, snippets);
		printConnectionsFile(file2snippets, snippets);
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files cloneFrequencies and connections
	 * files with data for all notebooks.
	 * @param file2hashes A map from file names to snippets
	 * @param hash2files A map from snippets to files
	 * @throws IOException On problems handling the output files
	 */
	public void write(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files) throws IOException {
		write(file2hashes, hash2files, file2hashes.keySet().size());
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
	public void write(Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files, int CONNECTION_NOTEBOOKS) throws IOException {
		printFile2hashes(file2hashes);
		printHash2files(hash2files);
		printCloneFrequencies(file2hashes, hash2files);
		printConnectionsFile(file2hashes, hash2files, CONNECTION_NOTEBOOKS);
	}
	
	private void printFile2hashes(Map<Notebook, SnippetCode[]> file2hashes) throws IOException {
		String timeStamp = LocalDateTime.now().toString();
		Writer writerA = new FileWriter(outputDir + "/file2hashesA" + timeStamp + ".csv");
		Writer writerNE = new FileWriter(outputDir + "/file2hashesNE" + timeStamp + ".csv");
		writerA.write(file2hashesHeader());
		writerNE.write(file2hashesHeader());
		for (Notebook notebook: file2hashes.keySet()) {
			writerA.write(notebook.getName());
			writerNE.write(notebook.getName());
			SnippetCode[] code = file2hashes.get(notebook);
			for (SnippetCode snippet: code) {
				writerA.write(", " + snippet.getHash());
				if (0 != snippet.getLOC()) {
					writerNE.write(", " + snippet.getHash());
				}
			}
			writerA.write("\n");
			writerNE.write("\n");
		}
		writerA.close();
		writerNE.close();
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
			int numClones = 0, numUnique = 0, numEmpty = 0;
			int numIntra = 0, numIntraNE = 0;	// # intra notebook clones
			SnippetCode[] code = file2hashes.get(notebook);
			for (SnippetCode snippet: code) {
				if(isClone(snippet, hash2files)) {
					numClones++;
					boolean intra = snippet.isIntraClone(code);
					if (intra) {
						numIntra++;
					}
					if (!snippet.isEmpty()) {
						numEmpty++;
						if (intra) {
							numIntraNE++;
						}
					}
				} else {
					numUnique++;
				}
			}
			writer.write(notebook + ", " + numUnique + ", " + numClones + ", " + numEmpty + ", ");
			int numSnippets = numClones + numUnique;
			int numSnippetsNE = numSnippets - numEmpty;
			if (0 != numSnippets) {
				double cloneFrequency = (double)numClones / numSnippets;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
			} else {
				writer.write("0, ");
			}
			if (0 != numSnippetsNE) {
				int numClonesNE = numClones - numEmpty;
				double cloneFrequency = (double)numClonesNE / numSnippetsNE;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
			} else {
				writer.write("0, ");
			}
			writer.write(numIntra + ", " + numIntraNE + "\n");
		}
		writer.close();
	}
	
	// TODO: Bättre namn! (Annan klass?)
	// TODO: Mindre duplicering!
	private void printCloneFrequencies2(Map<String, Set<SccSnippetId>> file2snippets,
			Map<SccSnippetId, SccSnippet> snippets) throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write(cloneFrequencyHeader());
		for (String notebook: file2snippets.keySet()) {
			int numClones = 0, numUnique = 0, numEmpty = 0;
			int numIntra = 0, numIntraNE = 0;
			Set<SccSnippetId> snippetsInNotebook = file2snippets.get(notebook);
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
				numIntra += snippet.numIntraNotebookConnections();
			}
			numIntraNE = numIntra;	// No empty clones for Scc data!
			writer.write(notebook + ", " + numUnique + ", " + numClones + ", " + numEmpty + ", ");
			int numSnippets = numClones + numUnique;
			int numSnippetsNE = numSnippets - numEmpty;
			if (0 != numSnippets) {
				double cloneFrequency = (double)numClones / numSnippets;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
			} else {
				writer.write("0, ");
			}
			if (0 != numSnippetsNE) {
				double cloneFrequency = (double)numClones / numSnippetsNE;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
			} else {
				writer.write("0, ");
			}
			writer.write(numIntra + ", " + numIntraNE + "\n");
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
			} catch (InterruptedException e) {
				System.err.println("Printing of connections for notebook "
						+ notebooks.get(i).getName() + " was interrupted!");
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.err.println("Printing connections for notebook "
						+ notebooks.get(i).getName() + " failed!");
				e.printStackTrace();
			}
		}
		writer.close();
	}
	
	// TODO: Annan file-avbildning
	private void printConnectionsFile(Map<String, Set<SccSnippetId>> file2snippets, Map<SccSnippetId, SccSnippet> snippets) throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		// TODO: Separat metod
		writer.write("file, non-empty connections, non-empty connections normalized, non-empty intra repro connections, mean non-empty inter repro connections\n");
		Set<String> notebooks = file2snippets.keySet();
		// TODO: Behöver detta parallelliseras?
		for (String notebook: notebooks) {
			Set<String> interConnectedRepros = new HashSet<String>();
			int interConnections = 0;
			int intraConnections = 0;
			int nonEmptySnippets = 0;
			Set<SccSnippetId> snippetsForNotebook = file2snippets.get(notebook);
			for (SccSnippetId id: snippetsForNotebook) {
				SccSnippet snippet = snippets.get(id);
				interConnections += snippet.numInterReproConnections();
				intraConnections += snippet.numIntraReproConnections();
				if (0 != snippet.getLoc()) {
					nonEmptySnippets++;
				}
				interConnectedRepros.addAll(snippet.getReprosInterConnected());
			}
			int connections = interConnections + intraConnections;
			// Empty snippets are considered unique by SourcererCC
			double normalizedNonEmptyConnections = ConnectionsLineBuilder.normalized(connections, nonEmptySnippets);
			double meanNonEmptyInterReproConnections = ConnectionsLineBuilder.normalized(interConnections, interConnectedRepros.size());
			writer.write(notebook + ", "
					+ connections + ", " + String.format(Locale.US, "%.4f", normalizedNonEmptyConnections) + ", "
					+ intraConnections + ", " + String.format(Locale.US, "%.4f", meanNonEmptyInterReproConnections) + "\n");
		}
		writer.close();
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
	 * @return Header for the cloneFrequency csv file
	 */
	private static String cloneFrequencyHeader() {
		return "file, unique, clones, empty, clone frequency, non-empty clone frequency, "
				+ "intra clones, non-empty intra clones\n";
	}
	
	/**
	 * @return Header for the connections csv file
	 */
	private static String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized, "
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections\n";
	}
}
