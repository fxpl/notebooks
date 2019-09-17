import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.json.simple.parser.ParseException;
import org.junit.Test;

public class NotebookTest {

	@Test
	public void testTest() {
		assertTrue(true);
	}
	
	@Test
	public void testNumCodeCells() {		
		String dataDir = "testdata/codeCells";
		String[] files = {"empty.ipynb", "one.ipynb", "two.ipynb", "three_with_md.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3};
		for (int i=0; i<files.length; i++) {
			String fileName = files[i];
			Notebook notebook = null;
			try {
				notebook = new Notebook(new File(dataDir + "/" + fileName));
			} catch (ParseException e) {
				System.out.println("ParseException when parsing " + fileName +": " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IOException when parsing file: " + e.getMessage());
			}
			assertEquals("One code cell expected!", numCodeCells[i], notebook.numCodeCells());
		}
	}

}
