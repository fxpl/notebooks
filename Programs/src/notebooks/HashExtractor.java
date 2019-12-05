package notebooks;

import notebooks.Worker;

class HashExtractor extends Worker<SnippetCode[]> {
	public HashExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public SnippetCode[] call() throws Exception {
		return notebook.snippetCodes();
	}

	@Override
	protected SnippetCode[] defaultValue() {
		return new SnippetCode[0];
	}
}