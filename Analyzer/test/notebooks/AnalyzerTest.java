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
	
	// TODO: mappar i andra test, mindre kodduplicering!
	
	/**
	 * Verify that snippets are stored correctly in the clone hash map.
	 * @throws IOException 
	 */
	@Test
	public void testClones() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/hash");
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
		
		// Actual values
		Map<String, List<Snippet>> clones = analyzer.clones();
		
		// Check values
		assertTrue(expectedClones.keySet().equals(clones.keySet()));
		for (String hash: expectedClones.keySet()) {
			List<Snippet> expectedSnippets = expectedClones.get(hash);
			List<Snippet> actualSnippets = clones.get(hash);
			assertEquals("Wrong number of snippets stored for " + hash + ":", expectedSnippets.size(), actualSnippets.size());
			assertTrue("Wrong snippets stored for " + hash, actualSnippets.containsAll(expectedSnippets));
		}
		lastOutputFile("snippets").delete();
		lastOutputFile("clones").delete();
	}
	
	/**
	 * Verify that the output files snippets<current-date-time>.csv and
	 * clones<current-date-time>.csv have the right headers after clone analysis.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_headers() throws IOException {
		analyzer.clones();
		checkHeader("snippets", "file, snippets");
		checkHeader("clones", "hash, file, index, ...");
		lastOutputFile("snippets").delete();
		lastOutputFile("clones").delete();
	}
	
	/**
	 * Verify that the output files snippets<current-date-time>.csv and
	 * clones<current-date-time>.csv have the right content after clone analysis
	 * of a notebook with a single snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_singleSnippet() throws IOException {
		String fileName = "single_import.ipynb";
		analyzer.initializeNotebooksFrom("test/data/hash/" + fileName);
		String hash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedClonesLines = {hash + ", " + fileName + ", 0"};
		String[] expectedSnippetsLines = {fileName + ", " + hash};
		
		analyzer.clones();
		
		checkLines("clones", expectedClonesLines);
		checkLines("snippets", expectedSnippetsLines);
		lastOutputFile("clones").delete();
		lastOutputFile("snippets").delete();
	}
	
	/**
	 * Verify that the output files snippets<current-date-time>.csv and
	 * clones<current-date-time>.csv have the right content after clone 
	 * analysis of a two notebooks with a clone.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_emptySnippets() throws IOException {
		String fileName1 = "empty_code_string.ipynb";
		String fileName2 = "empty_code_strings.ipynb";
		analyzer.initializeNotebooksFrom("test/data/hash/" + fileName1);
		analyzer.initializeNotebooksFrom("test/data/hash/" + fileName2);
		String hash = "D41D8CD98F00B204E9800998ECF8427E";
		String[] expectedSnippetLines = {
				fileName1 + ", " + hash,
				fileName2 + ", " + hash
		};
		String[] expectedClonesLines = {
				hash + ", " + fileName1 + ", 0, " + fileName2 + ", 0"
		};
		
		analyzer.clones();
		
		checkLines("snippets", expectedSnippetLines);
		checkLines("clones", expectedClonesLines);
		
		lastOutputFile("snippets").delete();
		lastOutputFile("clones").delete();
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
		// Analyze files
		String dataDir = "test/data/lang";
		String[] files = {"k_l_cpp.ipynb", "k_l_R.ipynb", "li_n_python.ipynb"};
		Language[] languages = {Language.OTHER, Language.R, Language.PYTHON};
		LangSpec[] langSpecs = {LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_LANGUAGEINFO_NAME};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.languages();
		
		// Check results
		File outputFile = lastOutputFile("languages");
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		assertEquals("Wrong header in language csv!", "file, language", outputReader.readLine());
		for (int i=0; i<languages.length; i++) {
			String expectedLine = files[i] + ", " + languages[i] + ", " + langSpecs[i];
			assertEquals("Wrong language written to output file:",
					"" + expectedLine , outputReader.readLine());
		}
		
		// Clean up
		outputReader.close();
		outputFile.delete();
	}
	
	/**
	 * Verify that the right number of code lines are found in the notebooks
	 * under a directory.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/loc");
		assertEquals("Wrong LOC!", 49, analyzer.LOC());
		lastOutputFile("loc").delete();
	}
	
	/** TODO: Extract methods to reduce code duplication!
	 * Verify that the output file loc<current-date-time>.csv is created and
	 * filled correctly when the number of LOC are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_csv() throws IOException {
		// Analyze files
		String dataDir = "test/data/loc";
		String[] files = {"code_and_md_3loc.ipynb", "markdownCells.ipynb",
				"two_codeCells_13loc.ipynb"};
		int[] LOC = {3, 0, 13};
		int[] emptyLOC = {0, 0, 2};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.LOC();
		
		// Check results
		File outputFile = lastOutputFile("loc");
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		assertEquals("Wrong header in LOC csv!", "file, total, non-blank, blank", outputReader.readLine());
		for (int i=0; i<LOC.length; i++) {
			String expectedLine = files[i] + ", " + LOC[i] + ", " + (LOC[i]-emptyLOC[i]) + ", " + emptyLOC[i];
			assertEquals("Wrong LOC written to output file:",
					"" + expectedLine , outputReader.readLine());
		}
		
		// Clean up
		outputReader.close();
		outputFile.delete();
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
		lastOutputFile("num_snippets").delete();
	}
	
	/**
	 * Verify that the output file num_snippets<current-date-time>.csv is
	 * created and filled correctly when the number of cells are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_csv() throws IOException {
		// Anlayze files
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "three_with_md.ipynb"};
		int[] numCodeCells = {0, 1, 3};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.numCodeCells();
		
		// Check results
		File outputFile = lastOutputFile("num_snippets");
		BufferedReader outputReader = new BufferedReader(
				new FileReader(outputFile));
		assertEquals("Wrong header in number of snippets csv!", "file, snippets", outputReader.readLine());
		for (int i =0; i<numCodeCells.length; i++) {
			assertEquals("Wrong output in number of snippets file:",
					files[i] + ", " + numCodeCells[i], outputReader.readLine());
		}
		
		// Clean up
		outputReader.close();
		outputFile.delete();
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
	 * content (except header).
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file
	 */
	private void checkLines(String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		outputReader.readLine();	// Skip header
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			assertEquals("Wrong line number " + (i+1) + " for " + prefix + " csv!", expectedLine, outputReader.readLine());
		}
		outputReader.close();
		
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv contains the
	 * right header.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedHeader Expected header of the file
	 */
	private void checkHeader(String prefix, String expectedHeader) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		assertEquals("Wrong header in " + prefix + " csv!", expectedHeader, outputReader.readLine());
		outputReader.close();
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
