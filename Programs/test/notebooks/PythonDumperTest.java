package notebooks;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PythonDumperTest extends DumperTest {
	
	public PythonDumperTest() {
		super();
	}
	
	@Before
	public void setUp() {
		super.setUp();
		dumper = new PythonDumper();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	/**
	 * Verify that dumper does not dump non-notebook files.
	 */
	@Test
	public void testDump_nonNotebookFile() {
		String src = "test/data/dump/non-notebook.ipynb";
		dumper.dumpAll(src, targetDir);
		assertTrue("Output file created from non-notebook file " + src,
				new File(targetDir).list().length == 0);
		;
	}
	
	/**
	 * Verify that the snippets of a notebook are not dumped by dumper if the
	 * notebook is not written in Python.
	 */
	@Test
	public void testDump_nonPythonFile() throws IOException {
		String src = "test/data/dump/nbR.ipynb";
		File outputFile = new File(targetDir + "/nbR_0.py");
		
		dumper.dumpAll(src, targetDir);
		
		assertFalse("Output file created from non-Python file " + src,
				outputFile.exists());
	}

	/**
	 * Verify that dump dumps the snippets of a single notebook correctly.
	 */
	@Test
	public void testDump_singleFile() throws IOException {
		String src = "test/data/dump/nb1.ipynb";
		String[] expectedOutputs = {"nb1_0.py", "nb1_1.py"};
		
		dumper.dumpAll(src, targetDir);
		
		for (String fileName: expectedOutputs) {
			File output = new File(targetDir + File.separator + fileName);
			assertTrue(fileName + " is missing!", output.exists());
			// The content of the file is checked by NotebookTest.
		}
	}
	
	/**
	 * Verify that dumper does not try to dump files that do not end with ipynb.
	 */
	@Test
	public void testDump_txtFile() {
		String src = "test/data/dump/txt.txt";
		dumper.dumpAll(src, targetDir);
		assertFalse("Output file created from non-notebook file " + src,
				new File(targetDir).exists());
	}
	
	/**
	 * Verify that dump dumps the snippets of the notebooks i a whole directory
	 * correctly.
	 */
	@Test
	public void testDump_wholeDir() throws IOException {
		String dir = "dump";
		String src = "test/data/" + dir;
		String[] expectedOutputs = {"nb1_0.py", "nb1_1.py", "nb2_0.py", "nb3_0.py"};
		
		dumper.dumpAll(src, targetDir);
		
		for (String fileName: expectedOutputs) {
			File output = new File(targetDir + File.separator + dir + File.separator + fileName);
			assertTrue(fileName + " is missing!", output.exists());
			// The content of the file is checked by NotebookTest.
			output.delete();
		}
	}

	/**
	 * Verify that dumper dumps notebooks in subdirectories correctly. 
	 */
	@Test
	public void testDump_subDir() throws IOException {
		String dir = "dump";
		String subDir = "sub";
		String src = "test/data/" + dir;
		String expectedOutput = "nb4_0.py";
		
		dumper.dumpAll(src, targetDir);
		// Files in test/tmp/dir already checked by another test
		
		String outputFileName = targetDir + File.separator + dir + File.separator + subDir + File.separator + expectedOutput;
		File outputFile = new File(outputFileName);
		assertTrue(outputFile + " is missing!", outputFile.exists());
	}
}
