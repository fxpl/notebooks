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
	}
	
	/**
	 * Verify that the output file snippets.csv is created and filled correctly
	 * when the number of cells are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_csv() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/count/zero.ipynb");
		analyzer.initializeNotebooksFrom("test/data/count/one.ipynb");
		analyzer.initializeNotebooksFrom("test/data/count/three_with_md.ipynb");
		analyzer.numCodeCells();
		BufferedReader outputReader = new BufferedReader(
				new FileReader("snippets.csv"));
		assertEquals("Wrong number of snippets written to output file:", "0", outputReader.readLine());
		assertEquals("Wrong number of snippets written to output file:", "1", outputReader.readLine());
		assertEquals("Wrong number of snippets written to output file:", "3", outputReader.readLine());
		outputReader.close();
	}

	/**
	 * Verify that the right number of notebooks are found under a directory.
	 */
	@Test
	public void testNumNotebooks() {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of notebooks found:", 10, analyzer.numNotebooks());
	}

}
