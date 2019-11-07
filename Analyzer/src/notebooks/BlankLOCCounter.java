package notebooks;

class BlankLOCCounter extends IntegerWorker {
	public BlankLOCCounter(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Integer call() throws Exception {
		return notebook.LOCBlank();
	}
}