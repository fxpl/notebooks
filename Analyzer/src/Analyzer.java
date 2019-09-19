import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Analyzer for Jupyter notebooks.
 */
public class Analyzer {
	ExecutorService executor;
	private ArrayList<Notebook> notebooks;
	int numSkippedNotebooks = 0;
	
	/**
	 * Note that when you are done with this Analyzer, you must call the method
	 * shutDown!
	 */
	public Analyzer() {
		this.notebooks = new ArrayList<Notebook>();
		executor = Executors.newCachedThreadPool();
	}
	
	public void shutDown() {
		executor.shutdown();
	}
	
	/**
	 * @return Total number of code cells in notebooks stored in analyzer
	 */
	public int numCodeCells() {
		int numCodeCells = 0;
		for (Notebook notebook: this.notebooks) {
			numCodeCells += notebook.numCodeCells();
		}
		return numCodeCells;
	}
	
	/**
	 * @return Number of notebooks stored in analyzer
	 */
	public int numNotebooks() {
		return this.notebooks.size();
	}
	
	/**
	 * Find each notebook file (i.e. file whose name ends with ".ipynb") in the
	 * file give as an argument to the method, or its sub directories
	 * (recursively). Create a Notebook from each file and store it in the
	 * field notebooks. (The argument may itself be a Jupyter notebook file. In
	 * that case, a Notebook is created from it.)
	 * @param file Directory or file to look in for notebooks
	 */
	public void readNotebooksFrom(File file) {
		if (!file.isDirectory()) {
			// Regular file
			if (file.getName().endsWith(".ipynb")) {
				createNotebook(file);
			}
		} else {
			// The file is a directory. Traverse it.
			String[] subFiles = file.list();
			for (String subFileName: subFiles) {
				readNotebooksFrom(new File(file.getPath() + "/" + subFileName));
			}
		}
	}
	
	/**
	 * Create a Notebook object from the file given as argument and store it.
	 * @param file File with notebook data
	 */
	private void createNotebook(File file) {
		try {
			Future<Notebook> result = executor.submit(new NotebookCreater(file));
			Notebook notebook = result.get();
			this.notebooks.add(notebook);
		} catch (ExecutionException e) {
			System.err.println("There was an error creating a notebook from " +
					file.getPath() + ": " + e.getMessage() + " Skipping!");
			numSkippedNotebooks++;
		} catch (InterruptedException e) {
			System.err.println("Thread creating a notebook from " +
					file.getPath() + " was interrupted: " + e.getMessage() + " Trying again!");
			createNotebook(file);
		}
	}
	
	private class NotebookCreater implements Callable<Notebook> {
		private File notebookFile;
		
		public NotebookCreater(File notebookFile) {
			this.notebookFile = notebookFile;
		}

		@Override
		public Notebook call() throws Exception {
			return new Notebook(notebookFile);
		}
	}
	
	private void analyze(String[] args) {
		for (String arg: args) {
			switch (arg) {
			case "-count":
				System.out.println("Notebooks parsed: " + this.numNotebooks());
				System.out.println("Notebooks skipped: " + this.numSkippedNotebooks);
				System.out.println("Code snippets: " + this.numCodeCells());
				break;
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
	}

	public static void main(String[] args) {
		File topDirectory = new File(args[0]);
		Analyzer analyzer = new Analyzer();
		analyzer.readNotebooksFrom(topDirectory);
		analyzer.analyze(Arrays.copyOfRange(args, 1, args.length));
		analyzer.shutDown();
	}
}
