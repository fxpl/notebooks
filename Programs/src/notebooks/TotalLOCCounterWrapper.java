package notebooks;

class TotalLOCCounterWrapper extends Worker<Object> {
	public TotalLOCCounterWrapper(Notebook notebook) {
		super(notebook);
	}
	
	@Override
	public Object call() throws Exception {
		return new TotalLOCCounter(notebook).call();
	}
}