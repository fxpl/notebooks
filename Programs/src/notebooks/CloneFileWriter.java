package notebooks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CloneFileWriter {
	private String outputDir;
	
	public CloneFileWriter(String outputDir) {
		this.outputDir = outputDir;
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files cloneFrequencies and connections
	 * files with data for all notebooks.
	 * @param snippets A map from file names to snippets
	 * @param clones A map from snippets to files
	 * @throws IOException On problems handling the output files
	 */
	public void write(Map<Notebook, SnippetCode[]> snippets,
			Map<SnippetCode, List<Snippet>> clones) throws IOException {
		write(snippets, clones, snippets.keySet().size());
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files cloneFrequencies  files with
	 * data for all notebooks. Create and fill connections file for
	 * CONNECTION_NOTEBOOKS notebooks
	 * @param snippets A map from file names to snippets
	 * @param clones A map from snippets to files
	 * @param CONNECTION_NOTEBOOKS Number of notebooks to print connection data for
	 * @throws IOException On problems handling the output files
	 */
	public void write(Map<Notebook, SnippetCode[]> snippets,
			Map<SnippetCode, List<Snippet>> clones, int CONNECTION_NOTEBOOKS) throws IOException {
		printFile2hashes(snippets);
		printHash2files(clones);
		printCloneFrequencies(snippets, clones);
		printConnectionsFile(snippets, clones, CONNECTION_NOTEBOOKS);
	}
	
	private void printFile2hashes(Map<Notebook, SnippetCode[]> files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/file2hashes" + LocalDateTime.now() + ".csv");
		writer.write(file2hashesHeader());
		for (Notebook notebook: files.keySet()) {
			writer.write(notebook.getName());
			SnippetCode[] code = files.get(notebook);
			for (SnippetCode snippet: code) {
				writer.write(", " + snippet.getHash());
			}
			writer.write("\n");
		}
		writer.close();
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
	
	private void printCloneFrequencies(Map<Notebook, SnippetCode[]> file2Hashes,
			Map<SnippetCode, List<Snippet>> hash2Files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write(cloneFrequencyHeader());
		for (Notebook notebook: file2Hashes.keySet()) {
			int numClones = 0, numUnique = 0;
			SnippetCode[] code = file2Hashes.get(notebook);
			for (SnippetCode snippet: code) {
				if(isClone(snippet, hash2Files)) {
					numClones++;
				} else {
					numUnique++;
				}
			}
			writer.write(notebook.getName() + ", " + numClones + ", " + numUnique + ", ");
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
	 * notebooks in file2snippets (unless NUM_CONNECTIONS > number of notebooks
	 * in the analysis --then data is printed for the whole set of notebooks).
	 * Note that when computing the mean, only repros for which there is a
	 * connection are included.
	 * @param file2snippets Mapping from notebook name to snippets
	 * @param snippet2files Mapping from snippets to position in notebooks
	 * @param NUM_NOTEBOOKS Maximum number of notebooks to print connection information for
	 */
	private void printConnectionsFile(Map<Notebook, SnippetCode[]> file2snippets,
			Map<SnippetCode, List<Snippet>> snippet2files, final int NUM_CONNECTIONS) throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		writer.write(connectionsHeader());
		List<Notebook> notebooks = new ArrayList<Notebook>(file2snippets.keySet());
		Collections.shuffle(notebooks);
		int connectionsToPrint = Math.min(NUM_CONNECTIONS, file2snippets.size());
		List<Callable<Void>> tasks = new ArrayList<>(connectionsToPrint);
		for (int i=0; i<connectionsToPrint; i++) {
			tasks.add(new ConnectionsWriter(notebooks.get(i), file2snippets, snippet2files, writer));
		}
		List<Future<Void>> result = ThreadExecutor.getInstance().invokeAll(tasks);
		// Wait for all tasks to finish
		for (int i=0; i<connectionsToPrint; i++) {
			try {
				result.get(i).get();
			} catch (InterruptedException e) {
				System.err.println("Printing of connections for notebook " + notebooks.get(i).getName()
						+ " was interrupted! " + e.getMessage());
			} catch (ExecutionException e) {
				System.err.println("Printing connections for notebook "
						+notebooks.get(i).getName() + " failed!" + e.toString());
			}
		}
		writer.close();
	}
	
	/**
	 * Look in clones to decide whether snippet is a clone or a unique snippet
	 * (that is, if the list of snippets is at least 2).
	 * @return true if snippet is a clone, false otherwise
	 */
	private static boolean isClone(SnippetCode snippet, Map<SnippetCode, List<Snippet>> clones) {
		List<Snippet> snippets = clones.get(snippet);
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
		return "file, clones, unique, clone frequency\n";
	}
	
	/**
	 * @return Header for the connections csv file
	 */
	private static String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized, "
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections\n";
	}
}
