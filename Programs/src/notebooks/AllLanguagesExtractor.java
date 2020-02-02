package notebooks;

import java.util.Map;

class AllLanguagesExtractor extends Worker<Map<LangSpec, Language>> {
	public AllLanguagesExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Map<LangSpec, Language> call() throws Exception {
		return notebook.allLanguageValues();
	}
}