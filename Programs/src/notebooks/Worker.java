package notebooks;

import java.util.concurrent.Callable;

abstract class Worker<T> implements Callable<T> {
	protected Notebook notebook;
	
	Worker(Notebook notebook) {
		this.notebook = notebook;
	}
	
	// TODO: Kan troligtvis tas bort så småningom!
	String getNotebookName() {
		return this.notebook.getName();
	}
	
	/**
	 * @return The value to return on failure.
	 */
	protected abstract T defaultValue();
}