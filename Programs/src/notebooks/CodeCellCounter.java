package notebooks;

class CodeCellCounter extends IntegerWorker {
	public CodeCellCounter(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Integer call() throws Exception {
		return notebook.numCodeCells();
	}
}