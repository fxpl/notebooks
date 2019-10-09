import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Running " + (2*cores) + " threads.");
		executor = Executors.newFixedThreadPool(2*cores);
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
	
	public void languages() throws IOException {
		Map<String, Integer> languages = new HashMap<String, Integer>();
		for (int i=0; i<notebooks.size(); i++) {
			String language;
			try {
				language = notebooks.get(i).language();
			} catch (NotebookException e) {
				System.err.println("Notebook exception: " + e);
				language = "unparsed";
			}
			if (languages.containsKey(language)) {
				languages.put(language, languages.get(language) + 1);
			} else {
				languages.put(language, 1);
			}
		}
		
		System.out.println("\n\n LANGUAGES \n");
		for (String language: languages.keySet()) {
			System.out.println(language + " (" + languages.get(language) + ")");
		}
	}
	
	/**
	 * Create a file loc<current-date-time>.csv with the header line
	 * followed by the number of lines of code for each notebook on the format
	 * <total loc>, <non-blank loc>, <blank loc>
	 * The data for each notebook is print on a separate line.
	 * @return Total number of LOC in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int LOC() throws IOException {
		int totalLOC = 0;
		Writer writer = new FileWriter("loc" + LocalDateTime.now() + ".csv");
		writer.write("total, non-blank, blank\n");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Counting LOC in notebook " + i);
				System.out.println(totalLOC + " lines of code found so far.");
			}
			int LOC = employ(new TotalLOCCounter(notebooks.get(i)));
			int LOCNonBlank = employ(new NonBlankLOCCounter(notebooks.get(i)));
			int LOCBlank = employ(new BlankLOCCounter(notebooks.get(i)));
			writer.write(LOC + ", " + LOCNonBlank + ", " + LOCBlank + "\n");
			totalLOC += LOC;
		}
		writer.close();
		return totalLOC;
	}
	
	private class TotalLOCCounter extends Worker {
		public TotalLOCCounter(Notebook notebook) {
			super(notebook);
		}

		@Override
		public Integer call() throws Exception {
			return notebook.LOC();
		}
	}
	
	private class NonBlankLOCCounter extends Worker {
		public NonBlankLOCCounter(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public Integer call() throws Exception {
			return notebook.LOCNonBlank();
		}
	}
	
	private class BlankLOCCounter extends Worker {
		public BlankLOCCounter(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public Integer call() throws Exception {
			return notebook.LOCBlank();
		}
	}
	
	/**
	 * Count the number of code cells in each notebook. Print each value on a
	 * separate line in the file snippets<current-date-time>.csv. Start the csv
	 * file with the header "snippets".
	 * @return Total number of code cells in notebooks stored in analyzer
	 * @throws IOException On problems with handling the output file
	 */
	public int numCodeCells() throws IOException {
		int totalNumCodeCells = 0;
		Writer writer = new FileWriter("snippets" + LocalDateTime.now() + ".csv");
		writer.write("snippets\n");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Counting code cells in notebook " + i);
				System.out.println(totalNumCodeCells + " code cells found so far.");
			}
			int numCodeCells = employ(new CodeCellCounter(notebooks.get(i)));
			writer.write(numCodeCells + "\n");
			totalNumCodeCells += numCodeCells;
		}
		writer.close();
		return totalNumCodeCells;
	}
	
	private class CodeCellCounter extends Worker {
		public CodeCellCounter(Notebook notebook) {
			super(notebook);
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
			case "-lang":				
				try {
					this.languages();
				} catch (IOException e) {
					System.err.println("Exception: " + e);
					e.printStackTrace();
				}
				break;
			case "-loc":
				try {
					System.out.println("Lines of code: " + this.LOC());
				} catch(IOException e) {
					System.err.println("I/O errors on handling output file for LOC counts." +
							"LOC not counted!");
				}
				break;
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
	}
	
	private abstract class Worker implements Callable<Integer>{
		protected Notebook notebook;
		
		Worker(Notebook notebook) {
			this.notebook = notebook;
		}
	}
	
	private int employ(Worker worker) {
		try {
			Future<Integer> result = executor.submit(worker);
			return result.get();
		} catch (ExecutionException e) {
			System.err.println(e.getMessage() + " Skipping!");
			return 0;
		} catch (InterruptedException e) {
			System.err.println("A thread was interrupted: " +
					e.getMessage() + " Trying again!");
			return employ(worker);
		}
	}

	public static void main(String[] args) {
		Analyzer analyzer = new Analyzer();
		analyzer.initializeNotebooksFrom(args[0]);
		analyzer.analyze(Arrays.copyOfRange(args, 1, args.length));
		analyzer.shutDown();
	}
}
