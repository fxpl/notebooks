import static org.junit.Assert.*;

import org.junit.Test;

public class NotebookTest {
	
	/**
	 * Verify that the correct number of code cells are found in JSON files.
	 */
	@Test
	public void testNumCodeCells() {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "two.ipynb",
				"three_with_md.ipynb", "three_in_worksheets.ipynb",
				"missing_cells.ipynb", "missing_cells_in_worksheet.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3, 3, 0, 0};
		for (int i=0; i<files.length; i++) {
			String fileName = files[i];
			Notebook notebook = new Notebook(dataDir + "/" + fileName);
			try {
				assertEquals(numCodeCells[i] + " code cell(s) expected!",
						numCodeCells[i], notebook.numCodeCells());
			} catch (NotebookException e) {
				fail("Could not count code cells: " + e.getMessage());
			}
		}
	}

	/**
	 * Verify that a NotebookException is thrown if we try to count cells in a
	 * notebook created from a file not containing JSON data.
	 */
	@Test (expected=NotebookException.class)
	public void testParsingEmptyFile() throws NotebookException {
		Notebook notebook = new Notebook("test/data/count/empty.ipynb");
		notebook.numCodeCells();
	}
	
	/**
	 * Verify that a NotebookException is thrown if we try to count cells in a
	 * notebook created from a missing file.
	 */
	@Test (expected=NotebookException.class)
	public void testParsingMissingFile() throws NotebookException  {
		Notebook notebook = new Notebook("nonexistent_file.txt");
		notebook.numCodeCells();
	}

}
