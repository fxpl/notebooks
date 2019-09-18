import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class AnalyzerTest {
	
	Analyzer analyzer;
	
	@Before
	public void setUp() {
		analyzer = new Analyzer();
	}
	
	@Test
	public void testNumCodeCells() {
		analyzer.readNotebooksFrom(new File("test/data/count"));
		assertEquals("Wrong number of cells found in notebooks:", 6, analyzer.numCodeCells());
	}

	/**
	 * Verify that the right number of notebooks are found under a directory.
	 */
	@Test
	public void testNumNotebooks() {
		String dataDirectory = "test/data/count";
		analyzer.readNotebooksFrom(new File(dataDirectory));
		assertEquals("Wrong number of notebooks read:", 6, analyzer.numNotebooks());
	}

}
