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
	 * Verify that the right number of cells are found in the notebooks under a
	 * directory.
	 * @throws IOException on errors when handling snippets.csv
	 */
	@Test
	public void testNumCodeCells_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of cells found in notebooks:", 9, analyzer.numCodeCells());
		lastSnippetFile().delete();
	}
	
	/**
	 * Verify that the output file snippets.csv is created and filled correctly
	 * when the number of cells are counted.
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
		for (int expectedCells: numCodeCells) {
			assertEquals("Wrong number of snippets written to output file:", ""
					+ expectedCells , outputReader.readLine());
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
		assertEquals("Wrong number of notebooks found:", 10, analyzer.numNotebooks());
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
