package notebooks;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PythonDumperTest {
	private boolean deleteTargetAtTearDown = false;
	private String targetDir = "test/PythonDumperTestOutput";	// Should not exist on startup
	private PythonDumper dumper;

	/**
	 * Create dumper and make sure that targetDir is empty.
	 */
	@Before
	public void setUp() {
		dumper = new PythonDumper();
		File target = new File(targetDir);
		assertFalse("Target direcory " + targetDir + " existed at set up!", target.exists());
		deleteTargetAtTearDown = true;
	}

	/**
	 * Delete newly created output files.
	 */
	@After
	public void tearDown() {
		if (deleteTargetAtTearDown) {
			deleteRecursively(targetDir);
		}
	}
	
	/**
	 * Verify that the snippets of a notebook are not dumped by dumper if the
	 * notebook is not written in Python.
	 */
	@Test
	public void testDump_nonPythonFile() throws NotebookException, IOException {
		String src = "test/data/dump/nbR.ipynb";
		String targetDir = "test/tmp";
		File outputFile = new File(targetDir + "/nbR_0.py");
		
		dumper.dump(src, targetDir);
		
		assertFalse("Output file created from non-Python file " + src,
				outputFile.exists());
	}

	/**
	 * Verify that dump dumps the snippets of a single notebook correctly.
	 */
	@Test
	public void testDump_singleFile() throws NotebookException, IOException {
		String src = "test/data/dump/nb1.ipynb";
		String targetDir = "test/tmp";
		String[] expectedOutputs = {"nb1_0.py", "nb1_1.py"};
		
		dumper.dump(src, targetDir);
		
		for (String fileName: expectedOutputs) {
			File output = new File(targetDir + "/" + fileName);
			assertTrue(fileName + " is missing!", output.exists());
			// The content of the file is checked by NotebookTest.
		}
	}
	
	/**
	 * Verify that dump dumps the snippets of the notebooks i a whole directory
	 * correctly.
	 */
	@Test
	public void testDump_wholeDir() throws NotebookException, IOException {
		String dir = "dump";
		String src = "test/data/" + dir;
		String target = "test/tmp";
		String[] expectedOutputs = {"nb1_0.py", "nb1_1.py", "nb2_0.py", "nb3_0.py"};
		
		dumper.dump(src, target);
		
		for (String fileName: expectedOutputs) {
			File output = new File(target + "/" + dir + "/" + fileName);
			assertTrue(fileName + " is missing!", output.exists());
			// The content of the file is checked by NotebookTest.
			output.delete();
		}
	}

	/**
	 * Verify that dumper dumps notebooks in subdirectories correctly. 
	 */
	@Test
	public void testDump_subDir() throws NotebookException, IOException {
		String dir = "dump";
		String subDir = "sub";
		String src = "test/data/" + dir;
		String target = "test/tmp";
		String expectedOutput = "nb4_0.py";
		
		dumper.dump(src, target);
		// Files in test/tmp/dir already checked by another test
		
		String outputFileName = target + "/" + dir + "/" + subDir + "/" + expectedOutput;
		File outputFile = new File(outputFileName);
		assertTrue(outputFile + " is missing!", outputFile.exists());
	}
	
	/**
	 * Delete the contents of the directory named dirName recursively. If
	 * deleteDir, then also delete dirName itself.
	 */
	private void deleteRecursively(String dirName) {
		File dir = new File(dirName);
		if (dir.isDirectory()) {
			String[] contents = dir.list();
			for (String file: contents) {
				deleteRecursively(dirName + "/" + file);
			}
		}
		dir.delete();
	}
}
