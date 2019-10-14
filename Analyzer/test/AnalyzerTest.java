import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;

public class AnalyzerTest {
	
	Analyzer analyzer;
	
	@Before
	public void setUp() {
		analyzer = new Analyzer();
	}
	
	@After
	public void tearDown() {
		analyzer.shutDown();
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
		lastLOCFile().delete();
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
		File outputFile = lastLOCFile();
		BufferedReader outputReader = new BufferedReader(
				new FileReader(outputFile));
		assertEquals("Header missing in LOC csv!", "total, non-blank, blank", outputReader.readLine());
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
		lastSnippetFile().delete();
	}
	
	/**
	 * Verify that the output file snippets<current-date-time>.csv is created
	 * and filled correctly when the number of cells are counted.
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
		File outputFile = lastSnippetFile();
		BufferedReader outputReader = new BufferedReader(
				new FileReader(outputFile));
		assertEquals("Header missing in snippets csv!", "snippets", outputReader.readLine());
		for (int i =0; i<numCodeCells.length; i++) {
			assertEquals("Wrong output i snippets file:",
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
	
	// TODO: Mer generell metod for outputfiler!
	/**
	 * @return File handler to the LOC output file with greatest (latest) file name
	 */
	private File lastLOCFile() {
		File directory = new File(".");
		String outputFileName = "loc.csv";
		for (String currentFileName: directory.list()) {
			if (currentFileName.matches("loc\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\.csv")
					&& currentFileName.compareTo(outputFileName) > 0) {
				outputFileName = currentFileName;
			}
		}
		return new File(outputFileName);
	}
	
	/**
	 * @return File handler to the snippet output file with greatest (latest) file name
	 */
	private File lastSnippetFile() {
		File directory = new File(".");
		String outputFileName = "snippets.csv";
		for (String currentFileName: directory.list()) {
			if (currentFileName.matches("snippets\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\.csv")
					&& currentFileName.compareTo(outputFileName) > 0) {
				outputFileName = currentFileName;
			}
		}
		return new File(outputFileName);
	}

}
