package notebooks;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CloneFileWriter {
	private String outputDir;
	
	public CloneFileWriter(String outputDir) {
		this.outputDir = outputDir;
	}
	
	/**
	 * Create and fill file2Hashes, hash2Files cloneFrequencies and connections
	 * files
	 * @param snippets A map from file names to snippets
	 * @param clones A map from snippets to files
	 * @throws IOException On problems handling the output files
	 */
	public void write(Map<Notebook, SnippetCode[]> snippets,
			Map<SnippetCode, List<Snippet>> clones) throws IOException {
		printFile2hashes(snippets);
		printHash2files(clones);
		printCloneFrequencies(snippets, clones);
		printConnectionsFile(snippets, clones);
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
	 * connections<current-date-time>.csv.
	 * Note that when computing the mean, only repros for which there is a
	 * connection are included.
	 * @param file2snippets Mapping from notebook name to snippets
	 * @param snippet2files Mapping from snippets to position in notebooks
	 */
	private void printConnectionsFile(Map<Notebook, SnippetCode[]> file2snippets,
			Map<SnippetCode, List<Snippet>> snippet2files) throws IOException {
		Writer writer = new FileWriter(outputDir + "/connections" + LocalDateTime.now() + ".csv");
		writer.write(connectionsHeader());
		for (Notebook notebook: file2snippets.keySet()) {
			printConnections(notebook, file2snippets, snippet2files, writer);
		}
		writer.close();
	}

	/**
	 * @param notebook Notebook to print connections for
	 * @param file2snippets Mapping from notebook to snippets
	 * @param snippets2files Mapping from snippets to position in notebooks
	 * @param writer Writer that will print the result
	 */
	private void printConnections(Notebook notebook, Map<Notebook, SnippetCode[]> file2snippets,
			Map<SnippetCode, List<Snippet>> snippets2files, Writer writer)
			throws IOException {
		int connections = 0;
		int nonEmptyConnections = 0;	// Connections excluding empty snippets
		int intraReproConnections = 0;
		int nonEmtpyIntraReproConnections = 0;
		int interReproConnections = 0;
		int nonEmptyInterReproConnections = 0;
		String currentRepro = notebook.getRepro();
		SnippetCode[] snippets = file2snippets.get(notebook);
		Set<String> otherRepros = new HashSet<String>();
		Set<String> otherNonEmptyRepros = new HashSet<String>();	// Other repros with non-empty friends
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
		
		writer.write(notebook.getName() + ", " + connections + ", "
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
	private static int connections(List<Snippet> locations) {
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
			String friendRepro = friend.getRepro();
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
			String friendRepro = friend.getRepro();

      // Guard against stupid mistakes
      assert(friendRepro == friendRepro.intern());

			if (friendRepro != currentRepro) {
				connections++;
				otherRepros.add(friendRepro);
			}
		}
		return connections;
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
	 * Normalize numerator by dividing it by denominator, unless the denominator is 0.
	 * Then return 0.
	 * @param numerator
	 * @param denominator
	 * @return numerator normalized according to description above
	 */
	private static double normalized(int numerator, int denominator) {
		if (0 == denominator) {
			return 0;
		} else {
			return (double)numerator/denominator;
		}
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
