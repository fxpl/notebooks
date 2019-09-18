import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

public class NotebookTest {
	
	/**
	 * Verify that the correct number of code cells are found in JSON files.
	 */
	@Test
	public void testNumCodeCells() {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "two.ipynb", "three_with_md.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3};
		for (int i=0; i<files.length; i++) {
			String fileName = files[i];
			Notebook notebook = null;
			try {
				notebook = new Notebook(new File(dataDir + "/" + fileName));
			} catch (ParseException e) {
				fail("ParseException when parsing " + fileName +": " + e.getMessage());
			} catch (IOException e) {
				fail("IOException when parsing file: " + e.getMessage());
			}
			assertEquals("One code cell expected!", numCodeCells[i], notebook.numCodeCells());
		}
	}

	/**
	 * Verify that an IOException is thrown if we try to create a notebook from
	 * a missing file.
	 */
	@Test (expected=IOException.class)
	public void testParsingMissingFile() throws IOException, ParseException {
		new Notebook(new File("nonexistent_file.txt"));
	}
	
	/**
	 * Verify that a ParseException is thrown if we try to create a notebook
	 * from a file not containing JSON data.
	 */
	@Test (expected=ParseException.class)
	public void testParsingEmptyFile() throws IOException, ParseException {
		new Notebook(new File("test/data/count/empty.ipynb"));
	}
}
