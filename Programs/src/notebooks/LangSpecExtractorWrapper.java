package notebooks;

public class LangSpecExtractorWrapper extends Worker<Object> {
	public LangSpecExtractorWrapper(Notebook notebook) {
		super(notebook);
	}

	@Override
	public Object call() throws Exception {
		return new LangSpecExtractor(notebook).call();
	}
}
