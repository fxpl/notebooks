package notebooks;

class TotalLOCCounter extends Worker<Integer> {
	public TotalLOCCounter(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Integer call() throws Exception {
		return notebook.LOC();
	}
}