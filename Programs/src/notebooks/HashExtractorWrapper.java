package notebooks;

public class HashExtractorWrapper extends Worker<Object> {
	public HashExtractorWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new HashExtractor(notebook).call();
	}
}
