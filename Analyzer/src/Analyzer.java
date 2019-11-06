import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Analyzer for Jupyter notebooks.
 */
public class Analyzer {
	private ExecutorService executor;
	private List<Notebook> notebooks;
	
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
	
	/**
	 * Compute the MD5 hash of each snippet in each notebook. Return a map with
	 * the key being the hashes and the values being lists of all snippets
	 * containing the corresponding code.
	 * Print each hash and the corresponding snippets (name, index) on a
	 * separate line in the file clones<current-date-time>.csv. Start the csv
	 * file with a header.
	 * @return The map described above
	 * @throws IOException On problems handling the output file.
	 */
	public Map<String, List<Snippet>> clones() throws IOException {
		// Identify clones
		Map<String, List<Snippet>> clones = new HashMap<String, List<Snippet>>();
		for (int i=0; i<notebooks.size(); i++) {
			Notebook currentNotebook = notebooks.get(i);
			String fileName = currentNotebook.getName();
			if (0 == i%10000) {
				System.out.println("Looking for clones in notebook " + i);
			}
			String[] hashes = employ(new HashExtractor(currentNotebook));
			for (int j=0; j<hashes.length; j++) {
				if (clones.containsKey(hashes[j])) {
					clones.get(hashes[j]).add(new Snippet(fileName, j));
				} else {
					List<Snippet> snippets = new ArrayList<Snippet>();
					snippets.add(new Snippet(fileName, j));
					clones.put(hashes[j], snippets);
				}
			}
		}
		
		// Print to file
		Writer writer = new FileWriter("clones" + LocalDateTime.now() + ".csv");
		writer.write("hash, file, index, ...\n");
		for (String hash: clones.keySet()) {
			writer.write(hash);
			for (Snippet s: clones.get(hash)) {
				writer.write(", " + s);
			}
			writer.write("\n");
		}
		writer.close();
		return clones;
	}
	
	private class HashExtractor extends Worker<String[]> {
		public HashExtractor(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public String[] call() throws Exception {
			return notebook.hashes();
		}

		@Override
		protected String[] defaultValue() {
			return null;
		}
	}
	
	/**
	 * TODO
	 */
	public Map<Language, Integer> languages() throws IOException {
		Map<Language, Integer> languages = new HashMap<Language, Integer>();
		for (Language language: Language.values()) {
			languages.put(language, 0);
		}
		Writer writer = new FileWriter("languages" + LocalDateTime.now() + ".csv");
		writer.write("file, language\n");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Extracting language from notebook " + i);
			}
			Language language = employ(new LanguageExtractor(notebooks.get(i)));
			languages.put(language, languages.get(language) + 1);
			LangSpec langSpec = employ(new LangSpecExtractor(notebooks.get(i)));
			String name = notebooks.get(i).getName();
			writer.write(name + ", " + language + ", " + langSpec + "\n");
		}
		writer.close();
		return languages;
	}
	
	private class LanguageExtractor extends Worker<Language> {
		public LanguageExtractor(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public Language call() throws Exception {
			return notebook.language();
		}

		@Override
		protected Language defaultValue() {
			return Language.UNKNOWN;
		}
	}
	
	private class LangSpecExtractor extends Worker<LangSpec> {
		public LangSpecExtractor(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public LangSpec call() throws Exception {
			return notebook.langSpec();
		}
		
		@Override
		protected LangSpec defaultValue() {
			return LangSpec.NONE;
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
		writer.write("file, total, non-blank, blank\n");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Counting LOC in notebook " + i);
				System.out.println(totalLOC + " lines of code found so far.");
			}
			int LOC = employ(new TotalLOCCounter(notebooks.get(i)));
			int LOCNonBlank = employ(new NonBlankLOCCounter(notebooks.get(i)));
			int LOCBlank = employ(new BlankLOCCounter(notebooks.get(i)));
			String name = notebooks.get(i).getName();
			writer.write(name + ", " + LOC + ", " + LOCNonBlank + ", " + LOCBlank + "\n");
			totalLOC += LOC;
		}
		writer.close();
		return totalLOC;
	}
	
	private class TotalLOCCounter extends IntegerWorker {
		public TotalLOCCounter(Notebook notebook) {
			super(notebook);
		}

		@Override
		public Integer call() throws Exception {
			return notebook.LOC();
		}
	}
	
	private class NonBlankLOCCounter extends IntegerWorker {
		public NonBlankLOCCounter(Notebook notebook) {
			super(notebook);
		}
		
		@Override
		public Integer call() throws Exception {
			return notebook.LOCNonBlank();
		}
	}
	
	private class BlankLOCCounter extends IntegerWorker {
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
		writer.write("file, snippets\n");
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Counting code cells in notebook " + i);
				System.out.println(totalNumCodeCells + " code cells found so far.");
			}
			int numCodeCells = employ(new CodeCellCounter(notebooks.get(i)));
			writer.write(notebooks.get(i).getName() + ", "+ numCodeCells + "\n");
			totalNumCodeCells += numCodeCells;
		}
		writer.close();
		return totalNumCodeCells;
	}
	
	private class CodeCellCounter extends IntegerWorker {
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
					Map<Language, Integer> languages = this.languages();
					System.out.println("\nLANGUAGES:");
					for (Language language: languages.keySet()) {
						System.out.println(language + ": " + languages.get(language));
					}
					System.out.println("");
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
			case "-clones":
				// TODO
				break;
			default:
				System.err.println("Unknown argument: " + arg);
			}
		}
	}
	
	private abstract class Worker<T> implements Callable<T> {
		protected Notebook notebook;
		
		Worker(Notebook notebook) {
			this.notebook = notebook;
		}
		
		/**
		 * @return The value to return on failure.
		 */
		protected abstract T defaultValue();
	}
	
	private abstract class IntegerWorker extends Worker<Integer> {
		public IntegerWorker(Notebook notebook) {
			super(notebook);
		}
		
		public Integer defaultValue() {
			return 0;
		}
	}
	
	private <T> T employ(Worker<T> worker) {
		try {
			Future<T> result = executor.submit(worker);
			return result.get();
		} catch (ExecutionException e) {
			System.err.println(e.getMessage() + " Skipping!");
			return worker.defaultValue();
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
