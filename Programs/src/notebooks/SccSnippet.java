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
	private final Notebook notebook;
	
	public SccSnippet(int loc, Notebook notebook) {
		this.loc = loc;
		this.notebook = notebook;
		reprosInterConnected = new HashSet<String>();
	}
	
	public SccSnippet(String loc, Notebook notebook) {
		this(Integer.parseInt(loc), notebook);
	}
	
	public void addConnection(SccSnippet other) {
		Notebook otherNotebook = other.getNotebook();
		if (null != this.notebook && null != otherNotebook) {
			if (this.notebook.equals(otherNotebook)) {
				intraNotebookConnections++;
				intraReproConnections++;
			} else {
				interNotebookConnections++;
				String myRepro = notebook.getRepro();
				if (null == myRepro) {
					// TODO: Bättre utskrift
					System.err.println("Null repro for notebook. Connection considered inter repro connection!");
					interReproConnections++;
				} else {
					String otherRepo = otherNotebook.getRepro();
					if (myRepro.equals(otherRepo)) {
						intraReproConnections++;
					} else {
						interReproConnections++;
						reprosInterConnected.add(otherRepo);
					}
				}
			}
		} else {
			// TODO: Bättre utskrift
			System.err.println("Notebook info missing. Connections not added!");
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
	
	public Notebook getNotebook() {
		return notebook;
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
