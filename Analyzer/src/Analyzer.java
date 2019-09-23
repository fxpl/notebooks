import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Analyzer for Jupyter notebooks.
 */
public class Analyzer {
	ExecutorService executor;
	private ArrayList<Notebook> notebooks;
	
	/**
	 * Note that when you are done with this Analyzer, you must call the method
	 * shutDown!
	 */
	public Analyzer() {
		this.notebooks = new ArrayList<Notebook>();
		executor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Find each notebook file (i.e. file whose name ends with ".ipynb") in the
	 * file whose name is given as an argument to the method, and its sub
	 * directories (recursively). Create a Notebook from each file and store
	 * it. (The argument may be the path to a Jupyter notebook file. In that
	 * case, a Notebook is created from it.)
	 * @param fileName Name of directory or file to look in for notebooks
	 */
	public void initializeNotebooksFrom(String fileName) {
		File file = new File(fileName);
		if (!file.isDirectory()) {
			// Regular file
			if (fileName.endsWith(".ipynb")) {
				this.notebooks.add(new Notebook(fileName));
			}
		} else {
			System.out.println("Traversing " + file.getPath());
			// The file is a directory. Traverse it.
			String[] subFiles = file.list();
			for (String subFileName: subFiles) {
				initializeNotebooksFrom(file.getPath() + "/" + subFileName);
			}
		}
	}
	
	/**
	 * Shut the executor down. Must be called for each object when you are done
	 * with it!
	 */
	public void shutDown() {
		executor.shutdown();
	}
	
	/**
	 * Count the number of code cells in each notebook. Print each value on a
	 * separate line in the file snippets<current-date-time>.csv.
	 * @return Total number of code cells in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int numCodeCells() throws IOException {
		int totalNumCodeCells = 0;
		Writer writer = new FileWriter("snippets" + LocalDateTime.now() + ".csv");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%100000) {
				System.out.println("Counting code cells in notebook " + i);
				System.out.println(totalNumCodeCells + " code cells found so far.");
			}
			int numCodeCells = numCodeCellsIn(notebooks.get(i));
			writer.write(numCodeCells + "\n");
			totalNumCodeCells += numCodeCells;
		}
		writer.close();
		return totalNumCodeCells;
	}
	
	/**
	 * @param notebook Notebook of which to count code cells 
	 * @return The number of code cells found in the notebook given as argument (0 on error)
	 */
	private int numCodeCellsIn(Notebook notebook) {
		try {
			Future<Integer> result = executor.submit(new CodeCellCounter(notebook));
			return result.get();
		} catch (ExecutionException e) {
			System.err.println(e.getMessage() + " Skipping!");
			return 0;
		} catch (InterruptedException e) {
			System.err.println("A thread counting code cells was interrupted: " +
					e.getMessage() + " Trying again!");
			return numCodeCellsIn(notebook);
		}
	}
	
	private class CodeCellCounter implements Callable<Integer> {
		private Notebook notebook;
		
		public CodeCellCounter(Notebook notebook) {
			this.notebook = notebook;
		}

		@Override
		public Integer call() throws Exception {
			return notebook.numCodeCells();
		}
	}
	
	/**
	 * @return Number of notebooks stored in analyzer
	 */
	public int numNotebooks() {
		return this.notebooks.size();
	}
	
	/**
	 * Parse command line arguments and performs actions accordingly.
	 */
	private void analyze(String[] args) {
		for (String arg: args) {
			switch (arg) {
			case "-count":
				System.out.println("Notebooks parsed: " + this.numNotebooks());
				try {
					System.out.println("Code snippets: " + this.numCodeCells());
				} catch (IOException e) {
					System.err.println("I/O errors on handling output file for snippet counts." +
							"Snippets not counted!");
				}
				break;
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
	}

	public static void main(String[] args) {
		Analyzer analyzer = new Analyzer();
		analyzer.initializeNotebooksFrom(args[0]);
		analyzer.analyze(Arrays.copyOfRange(args, 1, args.length));
		analyzer.shutDown();
	}
}
