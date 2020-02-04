package notebooks;

class CodeCellCounter extends Worker<Integer> {
	public CodeCellCounter(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Integer call() throws Exception {
		return notebook.numCodeCells();
	}
}