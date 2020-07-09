package notebooks;

import java.util.HashSet;
import java.util.Set;

public class SccSnippet {
	private final int loc;
	private int intraNotebookConnections;
	private int interNotebookConnections;
	private int interReproConnections;
	// Repros that this snippet has an inter connection to
	private Set<String> reprosInterConnected;
	private final SccNotebook notebook;
	
	public SccSnippet(int loc, SccNotebook notebook) {
		this.loc = loc;
		this.notebook = notebook;
		reprosInterConnected = new HashSet<String>();
	}
	
	public SccSnippet(String loc, SccNotebook notebook) {
		this(Integer.parseInt(loc), notebook);
	}
	
	/**
	 * Add information about connections to the current snippet and the snippet
	 * given as argument.
	 * @param connected Snippet that has a connection to this snippet
	 */
	public void connect(SccSnippet connected) {
		boolean intraNotebook = false;
		boolean intraRepro = false;
		SccNotebook otherNotebook = connected.getNotebook();
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
			connected.intraNotebookConnections++;
		} else {
			this.interNotebookConnections++;
			connected.interNotebookConnections++;
		}
		if (!intraRepro) {
			this.interReproConnections++;
			connected.interReproConnections++;
			this.reprosInterConnected.add(otherNotebook.getRepro());
			connected.reprosInterConnected.add(this.notebook.getRepro());
		}
	}
	
	public boolean isClone() {
		return isIntraNotebookClone() || interNotebookConnections > 0;
	}
	
	public boolean isIntraNotebookClone() {
		return intraNotebookConnections > 0;
	}
	
	/**
	 * @return Number of lines of code for snippet
	 */
	public int getLoc() {
		return loc;
	}
	
	public SccNotebook getNotebook() {
		return notebook;
	}
	
	/** TODO: Lägg i notebook istället!
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
		return intraNotebookConnections + interNotebookConnections - interReproConnections;
	}
}
