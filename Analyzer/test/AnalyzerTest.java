import static org.junit.Assert.*;

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
	 */
	@Test
	public void testNumCodeCells() {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of cells found in notebooks:", 9, analyzer.numCodeCells());
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
