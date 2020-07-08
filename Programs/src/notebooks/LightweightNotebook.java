package notebooks;

import java.util.Objects;

// TODO: Tester
public class LightweightNotebook {
	private String name;
	private String repro = "";
	
	public LightweightNotebook(String name, String repro) {
		this.name = name;
		this.repro = repro;
	}
	
	public LightweightNotebook(LightweightNotebook model) {
		this(model.name, model.repro);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof LightweightNotebook) {
			LightweightNotebook otherNotebook = (LightweightNotebook)other;
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
}
