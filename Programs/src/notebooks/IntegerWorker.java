package notebooks;

abstract class IntegerWorker extends Worker<Integer> {
	public IntegerWorker(Notebook notebook) {
		super(notebook);
	}
	
	public Integer defaultValue() {
		return 0;
	}
}