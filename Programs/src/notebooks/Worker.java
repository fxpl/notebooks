package notebooks;

import java.util.concurrent.Callable;

abstract class Worker<T> implements Callable<T> {
	protected Notebook notebook;
	
	Worker(Notebook notebook) {
		this.notebook = notebook;
	}

	/**
	 * @return The value to return on failure.
	 */
	protected abstract T defaultValue();
}