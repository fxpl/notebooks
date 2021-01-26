package notebooks;

import java.util.Map;

class AllCccAnalyzer extends Worker<AllCccResults> {
	private int numCodeCells;
	private int totalLOC;
	private int nonBlankLOC;
	private int blankLOC;
	private Language language;
	private Map<LangSpec, LangName> allLanguages;
	private SnippetCode[] hashes;
	
	public AllCccAnalyzer(Notebook notebook) {
		super(notebook);
	}	

	@Override
	public AllCccResults call() throws Exception {
		callNumCodeCells();
		callLOC();
		callLanguage();
		callAllLanguageValues();
		callSnippetCodes();
		return new AllCccResults(numCodeCells, totalLOC, nonBlankLOC, blankLOC, language, allLanguages, hashes);
	}
	
	private void callNumCodeCells() {
		try {
			numCodeCells = notebook.numCodeCells();
		} catch (Exception e) {
			System.err.println("Could not get cell count for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			numCodeCells = 0;
		}
	}
	
	private void callLOC() {
		try {
			totalLOC = notebook.LOC();
			nonBlankLOC = notebook.LOCNonBlank();
			blankLOC = notebook.LOCBlank();
		} catch (Exception e) {
			System.err.println("Could not get line count for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			totalLOC = 0;
			nonBlankLOC = 0;
			blankLOC = 0;
		}
	}
	
	private void callLanguage() {
		try {
			language = notebook.language();
		} catch (Exception e) {
			System.err.println("Could not get language information for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			language = new Language();
		}
	}
	
	private void callAllLanguageValues() {
		try {
			allLanguages = notebook.allLanguageValues();
		} catch (Exception e) {
			System.err.println("Could not get language values for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			allLanguages = null;
		}
	}

	private void callSnippetCodes() {
		try {
			hashes = notebook.snippetCodes();
		} catch (Exception e) {
			System.err.println("Could not get snippets for " + notebook.getName() + ": " + e);
			e.printStackTrace();
			hashes = new SnippetCode[0];
		}
	}
}
