package notebooks;

public class NonBlankLOCCounterWrapper extends Worker<Object> {
	public NonBlankLOCCounterWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new NonBlankLOCCounter(notebook).call();
	}
}
