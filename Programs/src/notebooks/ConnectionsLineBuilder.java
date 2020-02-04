package notebooks;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Callable;

/**
 * Class for building a line to be written in a connections CSV file for a
 * certain notebook.
 */
public class ConnectionsLineBuilder implements Callable<String> {
	private Notebook notebook;
	private Map <Notebook, SnippetCode[]> file2hashes;
	private Map<SnippetCode, List<Snippet>> hash2files;
	
	/**
	 * @param notebook Notebook to print connections for
	 * @param file2hashes Mapping from notebook to snippets
	 * @param hash2files Mapping from snippets to position in notebooks
	 */
	public ConnectionsLineBuilder(Notebook notebook, Map<Notebook, SnippetCode[]> file2hashes,
			Map<SnippetCode, List<Snippet>> hash2files) {
		this.notebook = notebook;
		this.file2hashes = file2hashes;
		this.hash2files = hash2files;
	}

	@Override
	public String call() throws Exception {
		int connections = 0;
		int nonEmptyConnections = 0;	// Connections excluding empty snippets
		int intraReproConnections = 0;
		int nonEmtpyIntraReproConnections = 0;
		int interReproConnections = 0;
		int nonEmptyInterReproConnections = 0;
		String currentRepro = notebook.getRepro();
		SnippetCode[] snippets = file2hashes.get(notebook);
		Set<String> otherRepros = new HashSet<String>();
		Set<String> otherNonEmptyRepros = new HashSet<String>();	// Other repros with non-empty friends
		int numNonEmptySnippets = 0;
		for (SnippetCode snippet: snippets) {
			// Locations where the current snippet can be found
			List<Snippet> locations = hash2files.get(snippet);
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
		
		return notebook.getName() + ", " + connections + ", "
				+ String.format(Locale.US, "%.4f", normalizedConnections) + ", "
				+ nonEmptyConnections + ", "
				+ String.format(Locale.US, "%.4f", normalizedNonEmptyConnections) + ", "
				+ intraReproConnections + ", " + nonEmtpyIntraReproConnections + ", "
				+ String.format(Locale.US, "%.4f", meanInterReproConnections) + ", "
				+ String.format(Locale.US, "%.4f", meanNonEmptyInterReproConnections) + "\n";
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
			if (!friendRepro.equals(currentRepro)) {
				connections++;
				otherRepros.add(friendRepro);
			}
		}
		return connections;
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
}
