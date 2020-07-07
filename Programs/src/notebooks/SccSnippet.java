package notebooks;

import java.util.HashSet;
import java.util.Set;

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
		boolean intraNotebook = false;
		boolean intraRepro = false;
		Notebook otherNotebook = other.getNotebook();
		if (this.notebook.equals(otherNotebook)) {
			intraNotebook = true;
			intraRepro = true;
		} else {
			String myRepro = this.notebook.getRepro();
			// We call toString because we want a NPE to be thrown if repro is null.
			String otherRepo = otherNotebook.getRepro().toString();
			intraRepro = myRepro.equals(otherRepo);
		}
		if (intraNotebook) {
			this.intraNotebookConnections++;
		} else {
			interNotebookConnections++;
		}
		if (intraRepro) {
			intraReproConnections++;
		} else {
			interReproConnections++;
			reprosInterConnected.add(otherNotebook.getRepro());
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
