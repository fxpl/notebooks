package notebooks;

import java.util.HashSet;
import java.util.Set;

// TODO(?!) Mergea olika snippetklasser
public class SccSnippet {
	private final int loc;
	private int intraConnections;
	private int interConnections;
	// Repros that this snippet has an inter connection to
	private Set<String> reprosInterConnected;
	
	public SccSnippet(int loc) {
		this.loc = loc;
		reprosInterConnected = new HashSet<String>();
	}
	
	public SccSnippet(String loc) {
		this(Integer.parseInt(loc));
	}
	
	/**
	 * Add one inter repro connection
	 * @param otherRepro Name of the repro to which this connection connects
	 */
	public void addInterConnection(String otherRepro) {
		reprosInterConnected.add(otherRepro);
		interConnections++;
	}
	
	/**
	 * Add one intra repro connection
	 */
	public void addIntraConnection() {
		intraConnections++;
	}
	
	public boolean isClone() {
		return interConnections > 0 || intraConnections > 0;
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
	
	public int numInterConnections() {
		return interConnections;
	}
	
	public int numIntraConnections() {
		return intraConnections;
	}
}
