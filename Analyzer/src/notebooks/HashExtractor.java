package notebooks;

import notebooks.Worker;

class HashExtractor extends Worker<String[]> {
	public HashExtractor(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public String[] call() throws Exception {
		return notebook.hashes();
	}

	@Override
	protected String[] defaultValue() {
		return new String[0];
	}
}