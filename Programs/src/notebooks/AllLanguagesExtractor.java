package notebooks;

import java.util.Map;

class AllLanguagesExtractor extends Worker<Map<LangSpec, LangName>> {
	public AllLanguagesExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Map<LangSpec, LangName> call() throws Exception {
		return notebook.allLanguageValues();
	}
}