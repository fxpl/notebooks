package notebooks;

import static org.junit.Assert.assertFalse;

import java.io.File;

public class DumperTest {
	private boolean deleteTargetAtTearDown = false;
	protected String targetDir = "test/PythonDumperTestOutput";
	protected Dumper dumper;

	/**
	 * Create dumper and make sure that targetDir is empty.
	 */
	public void setUp() {
		dumper = new PythonDumper();
		File target = new File(targetDir);
		assertFalse("Target direcory " + targetDir + " existed at set up!", target.exists());
		deleteTargetAtTearDown = true;
	}

	/**
	 * Delete newly created output files.
	 */
	public void tearDown() {
		if (deleteTargetAtTearDown) {
			deleteRecursively(targetDir);
		}
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