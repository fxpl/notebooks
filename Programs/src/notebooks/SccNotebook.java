package notebooks;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SccNotebook {
	private String name;
	private int repro;
	private int intraReproConnections;
	private int interReproConnections;
	// Repros that this notebook has an inter connections to
	private Set<Integer> reprosInterConnected;
	
	public SccNotebook(String name, int repro) {
		this.name = name;
		this.repro = repro;
		reprosInterConnected = new HashSet<Integer>();
	}
	
	public SccNotebook(SccNotebook model) {
		this(model.name, model.repro);
	}
	
	/**
	 * The notebook given as an argument should be connected to this notebook.
	 * Update connection info with this connection.
	 * @param connected Notebook that should be connected
	 */
	public void connect(SccNotebook connected) {
		if (this.repro == connected.repro) {
			this.intraReproConnections++;
			connected.intraReproConnections++;
		} else {
			this.interReproConnections++;
			connected.interReproConnections++;
			this.reprosInterConnected.add(connected.repro);
			connected.reprosInterConnected.add(this.repro);
		}
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SccNotebook) {
			SccNotebook otherNotebook = (SccNotebook)other;
			return this.name.equals(otherNotebook.name);
		} else {
			return false;
		}
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}
	
	public int numInterReproConnections() {
		return interReproConnections;
	}
	
	public int numIntraReproConnections() {
		return intraReproConnections;
	}
	
	/**
	 * @return The number of repros to which this notebook is connected, except the one it self lives in
	 */
	public int numReprosInterConnected() {
		return reprosInterConnected.size();
	}
}
