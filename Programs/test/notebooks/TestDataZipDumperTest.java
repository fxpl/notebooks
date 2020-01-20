package notebooks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataZipDumperTest extends DumperTest {

	public TestDataZipDumperTest() {
		super();
	}
	
	@Before
	public void setUp() {
		super.setUp();
		dumper = new TestDataZipDumper();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}

	/**
	 * Verify that dump creates one zip file per Python notebook.
	 */
	@Test
	public void testDump_singleFile() throws NotebookException, IOException {
		String src = "test/data/dump";
		String[] expectedZipFiles = {"dump/nb1.zip", "dump/nb1_str.zip",
				"dump/nb2.zip", "dump/nb3.zip", "dump/sub/nb4.zip", "dump/nbR.zip"};
		
		dumper.dumpAll(src, targetDir);
		
		for (String fileName: expectedZipFiles) {
			File output = new File(targetDir + "/" + fileName);
			assertTrue(fileName + " is missing!", output.exists());
			// The content of the file is checked by NotebookTest.
		}
	}
	
}
