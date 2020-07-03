package notebooks;

import java.util.HashSet;
import java.util.Set;

// TODO(?!) Mergea olika snippetklasser
public class SccSnippet {
	private final int loc;
	private int intraNotebookConnections;
	private int interNotebookConnections;
	private int intraReproConnections;
	private int interReproConnections;
	// Repros that this snippet has an inter connection to
	private Set<String> reprosInterConnected;
	
	public SccSnippet(int loc) {
		this.loc = loc;
		reprosInterConnected = new HashSet<String>();
	}
	
	public SccSnippet(String loc) {
		this(Integer.parseInt(loc));
	}
	
	// TODO: Osnygg signatur
	public void addConnection(boolean intraNotebook, boolean intraRepro, String otherRepro) {
		if (intraNotebook) {
			intraNotebookConnections++;
		} else {
			interNotebookConnections++;
		}
		if (intraRepro) {
			intraReproConnections++;
		} else {
			interReproConnections++;
			reprosInterConnected.add(otherRepro);
		}
	}
	
	public boolean isClone() {
		return interNotebookConnections > 0 || intraNotebookConnections > 0;
	}
	
	/**
	 * @return Number of lines of code for snippet
	 */
	public int getLoc() {
		return loc;
	}
	
	/**
	 * @return The set of all repros to which this snippet is connected, except the one it self lives in
	 */
	public Set<String> getReprosInterConnected() {
		return reprosInterConnected;
	}
	
	public int numInterNotebookConnections() {
		return interNotebookConnections;
	}
	
	public int numInterReproConnections() {
		return interReproConnections;
	}
	
	public int numIntraNotebookConnections() {
		return intraNotebookConnections;
	}
	
	public int numIntraReproConnections() {
		return intraReproConnections;
	}
}
