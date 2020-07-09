package notebooks;

import java.util.Objects;

public class SccNotebook {
	private String name;
	private String repro = "";
	
	public SccNotebook(String name, String repro) {
		this.name = name;
		this.repro = repro;
	}
	
	public SccNotebook(SccNotebook model) {
		this(model.name, model.repro);
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
}
