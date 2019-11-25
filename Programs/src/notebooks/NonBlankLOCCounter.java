package notebooks;

class NonBlankLOCCounter extends IntegerWorker {
	public NonBlankLOCCounter(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Integer call() throws Exception {
		return notebook.LOCNonBlank();
	}
}