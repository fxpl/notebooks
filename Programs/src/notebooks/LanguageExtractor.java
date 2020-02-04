package notebooks;

class LanguageExtractor extends Worker<LangName> {
	public LanguageExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public LangName call() throws Exception {
		return notebook.language().getName();
	}
}