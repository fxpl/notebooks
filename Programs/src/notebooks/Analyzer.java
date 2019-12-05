package notebooks;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
	 * the key being the snippet code and the values being lists of all
	 * snippets containing that code.
	 * For each notebook, print the name of the notebook followed by a list of
	 * the hash of each snippet in the notebook to the file 
	 * file2hashes<current-date-time>.csv.
	 * Print each hash and the corresponding snippets (name, index) on a
	 * separate line in the file hash2files<current-date-time>.csv. Start the
	 * csv files with a header.
	 * @return The map described above
	 * @throws IOException On problems handling the output file.
	 */
	public Map<SnippetCode, List<Snippet>> clones() throws IOException {
		Map<String, SnippetCode[]> snippets = getSnippets();
		Map<SnippetCode, List<Snippet>> clones = getClones(snippets);
		printFile2Hashes(snippets);
		printHash2Files(clones);
		printCloneFrequencies(snippets, clones);
		return clones;
	}

	/**
	 * @return A map from file names to snippets
	 */
	private Map<String, SnippetCode[]> getSnippets() throws IOException {
		Map<String, SnippetCode[]> snippets = new HashMap<String, SnippetCode[]>();
		for (int i=0; i<notebooks.size(); i++) {
			if (0 == i%10000) {
				System.out.println("Hashing snippets in notebook " + i);
			}
			Notebook currentNotebook = notebooks.get(i);
			String fileName = currentNotebook.getName();
			SnippetCode[] snippetsInNotebook = employ(new HashExtractor(currentNotebook));
			snippets.put(fileName, snippetsInNotebook);
		}
		return snippets;
	}
	
	/**
	 * return A map from snippets to files
	 */
	private Map<SnippetCode, List<Snippet>> getClones(Map<String, SnippetCode[]> fileMap) throws IOException {
		int numAnalyzed = 0;
		Map<SnippetCode, List<Snippet>> clones = new HashMap<SnippetCode, List<Snippet>>();
		for (String fileName: fileMap.keySet()) {
			if (0 == numAnalyzed%10000) {
				System.out.println("Finding clones in notebook " + numAnalyzed);
			}
			SnippetCode[] snippetCodes = fileMap.get(fileName);
			for (int j=0; j<snippetCodes.length; j++) {
				if (clones.containsKey(snippetCodes[j])) {
					clones.get(snippetCodes[j]).add(new Snippet(fileName, j));
				} else {
					List<Snippet> snippets = new ArrayList<Snippet>();
					snippets.add(new Snippet(fileName, j));
					clones.put(snippetCodes[j], snippets);
				}
			}
			numAnalyzed++;
		}
		return clones;
	}

	private void printHash2Files(Map<SnippetCode, List<Snippet>> clones) throws IOException {
		Writer writer = new FileWriter("hash2files" + LocalDateTime.now() + ".csv");
		writer.write("hash, file, index, ...\n");
		for (SnippetCode code: clones.keySet()) {
			String hash = code.getHash();
			writer.write(hash);
			for (Snippet s: clones.get(code)) {
				writer.write(", " + s.toString());
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	private void printFile2Hashes(Map<String, SnippetCode[]> files) throws IOException {
		Writer writer = new FileWriter("file2hashes" + LocalDateTime.now() + ".csv");
		writer.write("file, snippets\n");
		for (String fileName: files.keySet()) {
			writer.write(fileName);
			SnippetCode[] code = files.get(fileName);
			for (SnippetCode snippet: code) {
				writer.write(", " + snippet.getHash());
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	private void printCloneFrequencies(Map<String, SnippetCode[]> file2Hashes,
			Map<SnippetCode, List<Snippet>> hash2Files) throws IOException {
		Writer writer = new FileWriter("cloneFrequency" + LocalDateTime.now() + ".csv");
		writer.write("file, clones, unique, clone frequency\n");
		for (String fileName: file2Hashes.keySet()) {
			int numClones = 0, numUnique = 0;
			SnippetCode[] code = file2Hashes.get(fileName);
			for (SnippetCode snippet: code) {
				if(isClone(snippet, hash2Files)) {
					numClones++;
				} else {
					numUnique++;
				}
			}
			writer.write(fileName + ", " + numClones + ", " + numUnique + ", ");
			int numSnippets = numClones + numUnique;
			if (0 != numSnippets) {
				double cloneFrequency = (double)numClones / numSnippets;
				writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + "\n");
			} else {
				writer.write("0\n");
			}
		}
		writer.close();
	}
	
	/**
	 * Look in clones to decide whether snippet is a clone or a unique snippet
	 * (that is, if the list of snippets is at least 2).
	 * @return true if snippet is a clone, false otherwise
	 */
	private boolean isClone(SnippetCode snippet, Map<SnippetCode, List<Snippet>> clones) {
		List<Snippet> snippets = clones.get(snippet);
		return snippets.size() >= 2;
	}
	
	/**
	 * Create a file all_languages<current-date-time>.csv with a header line followed by the language defined in each language specification field for each notebook. The file contains one line per notebook, on the format
	 * <filename>,<language found in metadata.language>,<language found in metadata.language_info.name>,<language found in metadata.kernelspec.language>,<language found in metadata.kernelspec.name>,<language found in code cells>
	 * If any field is missing in a notebook, "UNKNOWN" is written for that field.
	 * @throws IOException 
	 */
	public void allLanguageValues() throws IOException {
		LangSpec[] langSpecFields = {LangSpec.METADATA_LANGUAGE , LangSpec.METADATA_LANGUAGEINFO_NAME, 
				LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_NAME,
				LangSpec.CODE_CELLS};
		Writer writer = new FileWriter("all_languages" + LocalDateTime.now() + ".csv");
		// Write header
		writer.write("file");
		for (LangSpec field: langSpecFields) {
			writer.write(", " + field.toString());
		}
		writer.write("\n");
		
		// Write language values
		for(Notebook notebook: notebooks) {
			Map<LangSpec, Language> languages = employ(new AllLanguagesExtractor(notebook));
			String name = notebook.getName();
			writer.write(name);
			for (LangSpec field: langSpecFields) {
				writer.write(", " + languages.get(field));
			}
			writer.write("\n");
		}
		writer.close();
	}

	/**
	 * Create a file languages<current-date-time>.csv with a header line
	 * followed by the language and the element from which is was extracted
	 * from the notebook file. The file contains one line per notebook, on the
	 * format <filename>,<language>,<location of language>.
	 * @return A map with the different languages as keys and the number of files written in this language as value
	 * @throws IOException On problems with handling the output file
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
	
	/**
	 * Count the number of code cells in each notebook. Print each value on a
	 * separate line in the file snippets<current-date-time>.csv. Start the
	 * csv file with the header "file, snippets".
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
	
	/**
	 * @return Number of notebooks stored in analyzer
	 */
	public int numNotebooks() {
		return this.notebooks.size();
	}
	
	/**
	 * Parse command line arguments and perform actions accordingly.
	 */
	private void analyze(String[] args) {
		for (String arg: args) {
			try {
				switch (arg) {
					case "-count":
						System.out.println("Notebooks parsed: " + this.numNotebooks());
						System.out.println("Code snippets: " + this.numCodeCells());
						break;
					case "-lang":
						Map<Language, Integer> languages = this.languages();
						System.out.println("\nLANGUAGES:");
						for (Language language: languages.keySet()) {
							System.out.println(language + ": " + languages.get(language));
						}
						System.out.println("");
						break;
					case "-loc":
						System.out.println("Lines of code: " + this.LOC());
						break;
					case "-clones":
						this.clones();
						System.out.println("Clone files created!");
						break;
					case "-lang_all":
						this.allLanguageValues();
						System.out.println("File with all language values created!");
						break;
					default:
						System.err.println("Unknown argument: " + arg);
				}
			} catch (IOException e) {
				System.err.println("I/O error: " + e.getMessage() + ". Operation interrupted.");
			}
		}
	}
	
	private <T> T employ(Worker<T> worker) {
		try {
			Future<T> result = executor.submit(worker);
			return result.get();
		} catch (ExecutionException e) {
			System.err.println(e.getMessage() + " Skipping notebook!");
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
