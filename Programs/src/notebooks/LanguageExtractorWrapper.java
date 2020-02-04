package notebooks;

public class LanguageExtractorWrapper extends Worker<Object> {
	public LanguageExtractorWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new LanguageExtractor(notebook).call();
	}
}
