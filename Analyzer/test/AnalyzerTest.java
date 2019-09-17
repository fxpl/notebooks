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

	/**
	 * Verify that the right number of notebooks are found under a directory.
	 */
	@Test
	public void testReadNotebooks() {
		String dataDirectory = "testdata/count";
		analyzer.readNotebooksFrom(new File(dataDirectory));
		assertEquals("Wrong number of notebooks read", 6, analyzer.numNotebooks());
	}

}
