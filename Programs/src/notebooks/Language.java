package notebooks;

public class Language {
	private final LangName name;
	private final LangSpec spec;
	
	public Language() {
		this.name = LangName.UNKNOWN;
		this.spec = LangSpec.NONE;
	}
	
	public Language(LangName name, LangSpec spec) {
		this.name = name;
		this.spec = spec;
	}
	
	public LangName getName() {
		return name;
	}
	
	public LangSpec getSpec() {
		return spec;
	}
	
	public boolean isSet() {
		return LangSpec.NONE != this.spec;
	}
	
	public String toString() {
		return this.name + ", " + this.spec;
	}
}
