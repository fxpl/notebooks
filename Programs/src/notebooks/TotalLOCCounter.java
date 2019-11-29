package notebooks;

class TotalLOCCounter extends IntegerWorker {
	public TotalLOCCounter(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Integer call() throws Exception {
		return notebook.LOC();
	}
}