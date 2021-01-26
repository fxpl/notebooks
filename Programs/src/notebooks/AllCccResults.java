package notebooks;

import java.util.Map;

public class AllCccResults {
	private int numCodeCells;
	private int totalLOC, nonBlankLOC, blankLOC;
	private Language language;
	private Map<LangSpec, LangName> allLanguages;
	SnippetCode[] hashes;
	
	public AllCccResults() {
		this.setNumCodeCells(0);
		this.setLOC(0, 0, 0);
		this.setLanguage(new Language());
		this.setLanguageMap(null);
		this.setHashes(new SnippetCode[0]);
	}
	
	public AllCccResults(int numCodeCells,
			int totalLOC,
			int nonBlankLOC,
			int blankLOC,
			Language language,
			Map<LangSpec, LangName> allLanguages,
			SnippetCode[] hashes) {
		this.setNumCodeCells(numCodeCells);
		this.setLOC(totalLOC, nonBlankLOC, blankLOC);
		this.setLanguage(language);
		this.setLanguageMap(allLanguages);
		this.setHashes(hashes);
	}
	
	public int getNumCodeCells() {
		return numCodeCells;
	}
	
	public int getTotalLOC() {
		return totalLOC;
	}
	
	public int getNonBlankLOC() {
		return nonBlankLOC;
	}
	
	public int getBlankLOC() {
		return blankLOC;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public Map<LangSpec, LangName> getAllLanguages() {
		return allLanguages;
	}
	
	public SnippetCode[] getHashes() {
		return hashes;
	}
	
	public void setNumCodeCells(int numCodeCells) {
		this.numCodeCells = numCodeCells;
	}
	
	public void setLOC(int totalLOC, int nonBlankLOC, int blankLOC) {
		this.totalLOC = totalLOC;
		this.nonBlankLOC = nonBlankLOC;
		this.blankLOC = blankLOC;
	}
	
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	public void setLanguageMap(Map<LangSpec, LangName> map) {
		this.allLanguages = map;
	}
	
	public void setHashes(SnippetCode[] hashes) {
		this.hashes = hashes;
	}
}
