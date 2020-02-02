package notebooks;

class LanguageExtractor extends Worker<Language> {
	public LanguageExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Language call() throws Exception {
		return notebook.language();
	}
}