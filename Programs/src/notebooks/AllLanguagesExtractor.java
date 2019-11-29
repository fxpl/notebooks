package notebooks;

import java.util.HashMap;
import java.util.Map;

class AllLanguagesExtractor extends Worker<Map<LangSpec, Language>> {
	public AllLanguagesExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Map<LangSpec, Language> call() throws Exception {
		return notebook.allLanguageValues();
	}

	@Override
	protected Map<LangSpec, Language> defaultValue() {
		Map<LangSpec, Language> result
			= new HashMap<LangSpec, Language>(LangSpec.values().length - 1);
		result.put(LangSpec.METADATA_LANGUAGE, Language.UNKNOWN);
		result.put(LangSpec.METADATA_LANGUAGEINFO_NAME, Language.UNKNOWN);
		result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.UNKNOWN);
		result.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.UNKNOWN);
		result.put(LangSpec.CODE_CELLS, Language.UNKNOWN);
		return result;
	}
}