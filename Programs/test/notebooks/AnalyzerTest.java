package notebooks;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;

import notebooks.Analyzer;
import notebooks.LangSpec;
import notebooks.Language;
import notebooks.Snippet;

public class AnalyzerTest {
	
	private Analyzer analyzer;
	
	@Before
	public void setUp() {
		analyzer = new Analyzer();
	}
	
	@After
	public void tearDown() {
		analyzer.shutDown();
	}

	/**
	 * Verify that snippets are stored correctly in the clone hash map.
	 * @throws IOException 
	 */
	@Test
	public void testClones() throws IOException {
		String dataDir = "test/data/hash";
		String[] files = {"empty_code_string.ipynb", "empty_code_strings.ipynb",
				"missing_cells.ipynb", "single_import.ipynb", "two_import_cells.ipynb",
				"intra_clones.ipynb", "intra_clones_and_unique.ipynb"
		};
		// Expected values
		Map<String, List<Snippet>> expectedClones = new HashMap<String, List<Snippet>>();
		List<Snippet> emptySnippets = new ArrayList<Snippet>(2);
		emptySnippets.add(new Snippet("empty_code_string.ipynb", 0));
		emptySnippets.add(new Snippet("empty_code_strings.ipynb", 0));
		expectedClones.put("D41D8CD98F00B204E9800998ECF8427E", emptySnippets);	// Empty
		List<Snippet> numpy = new ArrayList<Snippet>(2);
		numpy.add(new Snippet("single_import.ipynb", 0));
		numpy.add(new Snippet("two_import_cells.ipynb", 0));
		expectedClones.put("33BE8D72467938FBB23EF42CF8C9E85F", numpy); // import numpy
		List<Snippet> pandas = new ArrayList<Snippet>(1);
		pandas.add(new Snippet("two_import_cells.ipynb", 1));
		expectedClones.put("6CABFDBC20F69189D4B8894A06C78F49", pandas); // import pandas
		List<Snippet> kossa = new ArrayList<Snippet>(4);
		kossa.add(new Snippet("intra_clones.ipynb", 0));
		kossa.add(new Snippet("intra_clones.ipynb", 1));
		kossa.add(new Snippet("intra_clones_and_unique.ipynb", 0));
		kossa.add(new Snippet("intra_clones_and_unique.ipynb", 2));
		expectedClones.put("0120F99AA7C49E1CD5F4EE4A6BB1CC4A", kossa);
		List<Snippet> unique = new ArrayList<Snippet>(1);
		unique.add(new Snippet("intra_clones_and_unique.ipynb", 1));
		expectedClones.put("A2D53E3DA394A52271CF00632C961D2A", unique);
		
		// Actual values
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.initializeNotebooksFrom("test/data/hash");
		Map<String, List<Snippet>> clones = analyzer.clones();
		
		// Check values
		assertTrue(expectedClones.keySet().equals(clones.keySet()));
		for (String hash: expectedClones.keySet()) {
			List<Snippet> expectedSnippets = expectedClones.get(hash);
			List<Snippet> actualSnippets = clones.get(hash);
			assertEquals("Wrong number of snippets stored for " + hash + ":", expectedSnippets.size(), actualSnippets.size());
			assertTrue("Wrong snippets stored for " + hash, actualSnippets.containsAll(expectedSnippets));
		}
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv,
	 * hash2files<current-date-time>.csv and cloneFrequency<current-date-time>.csv
	 * have the right content after clone analysis of an empty notebook.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_emptyNotebook() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "missing_cells.ipynb";
		String[] expectedSnippetLines = {
				"file, snippets"
		};
		String[] expectedClonesLines = {
				"hash, file, index, ..."
		};
		String[] expectedFrequencyLiens = {
				"file, clones, unique, clone frequency",
				fileName + ", 0, 0, 0"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedSnippetLines);
		checkCsv("hash2files", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLiens);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv,
	 * hash2files<current-date-time>.csv and cloneFrequency<current-date-time>.csv
	 * have the right content after clone analysis of a notebook with a single
	 * snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_singleSnippet() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "single_import.ipynb";
		String hash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedSnippetLines = {
				"file, snippets",
				fileName + ", " + hash
		};
		String[] expectedClonesLines = {
				"hash, file, index, ...",
				hash + ", " + fileName + ", 0"
		};
		String[] expectedFrequencyLiens = {
				"file, clones, unique, clone frequency",
				fileName + ", 0, 1, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedSnippetLines);
		checkCsv("hash2files", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLiens);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv, 
	 * hash2files<current-date-time>.csv and cloneFrequency<current-date-time>.csv
	 * have the right content after clone analysis of a notebooks with a clone.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_intraClone() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "intra_clones.ipynb";
		String hash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String[] expectedFile2HashesLines = {
				"file, snippets",
				fileName + ", " + hash + ", " + hash
		};
		String[] expectedHash2FileLines = {
				"hash, file, index, ...",
				hash + ", " + fileName + ", 0, " + fileName + ", 1"
		};
		String[] expectedFrequencyLines = {
				"file, clones, unique, clone frequency",
				fileName + ", 2, 0, 1.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedFile2HashesLines);
		checkCsv("hash2files", expectedHash2FileLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv and
	 * cloneFrequency<current-date-time>.csv have the right content after clone
	 * analysis of a notebooks with both clones and a unique snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_mixed() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "intra_clones_and_unique.ipynb";
		String kossaHash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String uniqueHash = "A2D53E3DA394A52271CF00632C961D2A";
		String[] expectedFile2HashesLines = {
			"file, snippets",
			fileName + ", " + kossaHash + ", " + uniqueHash + ", " + kossaHash
		};
		// hash2Files is hard to test since we don't know in which order the hashes are stored
		String[] expectedFrequencyLines = {
			"file, clones, unique, clone frequency",
			fileName + ", 2, 1, 0.6667"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedFile2HashesLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		
		deleteCloneCsvs();
	}
	
	
	/**
	 * Verify that the right languages are found in the notebooks.
	 * @throws IOException
	 */
	@Test
	public void testLanguage_values() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/lang");
		Map<Language, Integer> expected = new HashMap<Language, Integer>();
		expected.put(Language.PYTHON, 6);
		expected.put(Language.JULIA, 3);
		expected.put(Language.R, 2);
		expected.put(Language.SCALA, 3);
		expected.put(Language.OTHER, 1);
		expected.put(Language.UNKNOWN, 6);
		Map<Language, Integer> actual = analyzer.languages();
		assertEquals("Error in language extraction:", expected, actual);
		lastOutputFile("languages").delete();
	}
	
	/**
	 * Verify that the output file languages<current-date-time>.csv is created
	 * and filled correctly when the languages are extracted.
	 * @throws IOException
	 */
	@Test
	public void testLanguage_csv() throws IOException {
		String dataDir = "test/data/lang";
		String[] files = {"k_l_cpp.ipynb", "k_l_R.ipynb", "li_n_python.ipynb"};
		Language[] languages = {Language.OTHER, Language.R, Language.PYTHON};
		LangSpec[] langSpecs = {LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_LANGUAGEINFO_NAME};
		String[] expectedLines = new String[languages.length+1];
		expectedLines[0] = "file, language";
		for (int i=0; i<languages.length; i++) {
			expectedLines[i+1] = files[i] + ", " + languages[i] + ", " + langSpecs[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.languages();
		
		checkCsv("languages", expectedLines);
		
		lastOutputFile("languages").delete();
	}
	
	/**
	 * Verify that the right number of code lines are found in the notebooks
	 * under a directory.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/loc");
		assertEquals("Wrong LOC!", 55, analyzer.LOC());
		lastOutputFile("loc").delete();
	}
	
	/**
	 * Verify that the output file loc<current-date-time>.csv is created and
	 * filled correctly when the number of LOC are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_csv() throws IOException {
		String dataDir = "test/data/loc";
		String[] files = {"code_and_md_3loc.ipynb", "markdownCells.ipynb",
				"two_codeCells_13loc.ipynb"};
		int[] LOC = {3, 0, 13};
		int[] emptyLOC = {0, 0, 2};
		String[] expectedLines = new String[LOC.length+1];
		expectedLines[0] = "file, total, non-blank, blank"; // header
		for (int i=0; i<LOC.length; i++) {
			expectedLines[i+1] = files[i] + ", " + LOC[i] + ", " + (LOC[i]-emptyLOC[i]) + ", " + emptyLOC[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.LOC();
		
		checkCsv("loc", expectedLines);	

		lastOutputFile("loc").delete();
	}
	
	/**
	 * Verify that the right number of cells are found in the notebooks under a
	 * directory.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of cells found in notebooks!", 15, analyzer.numCodeCells());
		lastOutputFile("snippets").delete();
	}
	
	/**
	 * Verify that the output file snippets<current-date-time>.csv is
	 * created and filled correctly when the number of cells are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_csv() throws IOException {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "three_with_md.ipynb"};
		int[] numCodeCells = {0, 1, 3};
		String[] expectedLines = new String[numCodeCells.length+1];
		expectedLines[0] = "file, snippets";
		for (int i=0; i<numCodeCells.length; i++) {
			expectedLines[i+1] = files[i] + ", " + numCodeCells[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.numCodeCells();
		
		checkCsv("snippets", expectedLines);
		
		lastOutputFile("snippets").delete();
	}

	/**
	 * Verify that the right number of notebooks are found under a directory.
	 */
	@Test
	public void testNumNotebooks() {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of notebooks found:", 12, analyzer.numNotebooks());
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv has the right
	 * content.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file
	 */
	private void checkCsv(String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			assertEquals("Wrong line number " + (i+1) + " for " + prefix + " csv!", expectedLine, outputReader.readLine());
		}
		outputReader.close();
	}
	
	/**
	 * Delete all CSV files created by the clone analysis. 
	 */
	private void deleteCloneCsvs() {
		lastOutputFile("file2hashes").delete();
		lastOutputFile("hash2files").delete();
		lastOutputFile("cloneFrequency").delete();
	}
	
	/**
	 * Find the output file <prefix><timestamp>.csv with the greatest (latest)
	 * time stamp.
	 * @param prefix First part of the output file
	 * @return Output file described above 
	 */
	private File lastOutputFile(String prefix) {
		File directory = new File(".");
		String outputFileName = prefix + ".csv";
		for (String currentFileName: directory.list()) {
			if (currentFileName.matches(prefix + "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\.csv")
					&& currentFileName.compareTo(outputFileName) > 0) {
				outputFileName = currentFileName;
			}
		}
		return new File(outputFileName);
	}

}
