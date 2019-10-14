import static org.junit.Assert.*;

import org.junit.Test;

public class NotebookTest {
	
	/**
	 * Verify that getName returns the name of the notebook (without preceding
	 * path).
	 */
	@Test
	public void testGetName() {
		Notebook notebook = new Notebook("made/up/path/empty.ipynb");
		assertEquals("Wrong name of notebook!" , "empty.ipynb", notebook.getName());
	}
	
	/**
	 * Verify that the correct total number of lines of code are found in JSON
	 * files.
	 */
	@Test
	public void testLOCTotal() {
		String dataDir = "test/data/loc";
		String[] files = {"markdownCells.ipynb", "one_codeCell_6loc.ipynb",
				"two_codeCells_13loc.ipynb", "three_codeCells_2loc.ipynb",
				"code_and_md_3loc.ipynb", "missing_source.ipynb",
				"two_codeCells_26loc_worksheet.ipynb"};
		int[] LOC = {0, 6, 13, 2, 3, 2, 23};
		for (int i=0; i<files.length; i++) {
			String fileName = files[i];
			Notebook notebook = new Notebook(dataDir + "/" + fileName);
			try {
				assertEquals("Wrong LOC!", LOC[i], notebook.LOC());
			} catch (NotebookException e) {
				fail("Could not count LOC: " + e.getMessage());
			}
		}
	}
	
	/**
	 * Verify that the correct total number of blank code lines are found in
	 * JSON files.
	 */
	@Test
	public void testLOCBlank() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		try {
			assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
		} catch(NotebookException e) {
			fail("Could not count blank LOC: " + e.getMessage());
		}
	}
	
	/**
	 * Verify that the correct total number of non-blank code lines are found
	 * in JSON files.
	 */
	@Test
	public void testLOCNonBlank() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		try {
			assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
		} catch (NotebookException e) {
			fail("Could not count non-blank LOC: " + e.getMessage());
		}
		
	}
	
	/**
	 * Verify that all kinds of lines of code are counted correctly, also when
	 * the methods are called after each other.
	 */
	@Test
	public void testLOCAll() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		try {
			assertEquals("Wrong LOC!", 13, notebook.LOC());
			assertEquals("Wrong second LOC!", 13, notebook.LOC());
			assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
			assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
		} catch (NotebookException e) {
			fail("Could not count code lines: " + e.getMessage());
		}
	}
	
	/**
	 * Verify that the correct number of code cells are found in JSON files.
	 */
	@Test
	public void testNumCodeCells() {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "two.ipynb",
				"three_with_md.ipynb", "three_in_worksheets.ipynb",
				"missing_cells.ipynb", "missing_cells_in_worksheet.ipynb",
				"missing_cell_type.ipynb", "four_cells_and_worksheets.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3, 3, 0, 0, 2, 4};
		for (int i=0; i<files.length; i++) {
			String fileName = files[i];
			Notebook notebook = new Notebook(dataDir + "/" + fileName);
			try {
				assertEquals("Wrong number of code cells!",
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
