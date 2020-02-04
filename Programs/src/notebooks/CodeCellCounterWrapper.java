package notebooks;

class CodeCellCounterWrapper extends Worker<Object> {
	public CodeCellCounterWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Integer call() throws Exception {
		return new CodeCellCounter(notebook).call();
	}
}