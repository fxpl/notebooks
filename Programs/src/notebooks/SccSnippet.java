package notebooks;

public class SccSnippet {
	private final int loc;
	private int intraNotebookConnections;
	private int interNotebookConnections;
	private final SccNotebook notebook;
	
	public SccSnippet(int loc, SccNotebook notebook) {
		this.loc = loc;
		this.notebook = notebook;
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
		SccNotebook connectedNotebook = connected.notebook;
		this.notebook.connect(connectedNotebook);
		if (this.notebook.equals(connectedNotebook)) {
			this.intraNotebookConnections++;
			connected.intraNotebookConnections++;
		} else {
			this.interNotebookConnections++;
			connected.interNotebookConnections++;
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
	
	public int numInterNotebookConnections() {
		return interNotebookConnections;
	}
	
	public int numIntraNotebookConnections() {
		return intraNotebookConnections;
	}
}
