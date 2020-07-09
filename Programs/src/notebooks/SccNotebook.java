package notebooks;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SccNotebook {
	private String name;
	private String repro = "";
	// Repros that this notebook has an inter connections to
	private Set<String> reprosInterConnected;
	
	public SccNotebook(String name, String repro) {
		this.name = name;
		this.repro = repro;
		reprosInterConnected = new HashSet<String>();
	}
	
	public SccNotebook(SccNotebook model) {
		this(model.name, model.repro);
	}
	
	public void connect(SccNotebook connected) {
		if (!this.repro.equals(connected.repro)) {
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
	
	@Override
	public int hashCode() {
		return Objects.hash(this.name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getRepro() {
		return repro;
	}
	
	/**
	 * @return The set of all repros to which this notebook is connected, except the one it self lives in
	 */
	public Set<String> getReprosInterConnected() {
		return reprosInterConnected;
	}
}
