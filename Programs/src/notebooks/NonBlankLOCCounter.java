package notebooks;

class NonBlankLOCCounter extends Worker<Integer> {
	public NonBlankLOCCounter(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Integer call() throws Exception {
		return notebook.LOCNonBlank();
	}
}