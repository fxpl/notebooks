package notebooks;

public class BlankLOCCounterWrapper extends Worker<Object> {
	public BlankLOCCounterWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new BlankLOCCounter(notebook).call();
	}
}
