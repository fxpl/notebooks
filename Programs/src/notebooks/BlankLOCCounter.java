package notebooks;

class BlankLOCCounter extends Worker<Integer> {
	public BlankLOCCounter(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Integer call() throws Exception {
		return notebook.LOCBlank();
	}
}