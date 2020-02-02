package notebooks;

public class AllLanguagesExtractorWrapper extends Worker<Object> {
	public AllLanguagesExtractorWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new AllLanguagesExtractor(notebook).call();
	}
}
