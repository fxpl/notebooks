package notebooks;

class LangSpecExtractor extends Worker<LangSpec> {
	public LangSpecExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public LangSpec call() throws Exception {
		return notebook.langSpec();
	}
}