package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class SccNotebookTest {
	String notebookName = "nb_777.ipynb";
	int reproNumber = 77;
	SccNotebook notebook;
	
	@Before
	public void setUp() {
		notebook = new SccNotebook(notebookName, reproNumber);
	}
	
	@Test
	public void testCopyConstructor() {
		SccNotebook notebookCopy = new SccNotebook(notebook);
		assertEquals("Wrong name returned for notebook created using copy constructor",
				notebookName, notebookCopy.getName());
	}
	
	@Test
	public void testConnect() {
		assertEquals("Number of intra repro connections != 0 on creation",
				0, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 on creation",
				0, notebook.numInterReproConnections());
		assertEquals("Inter connected repros set not empty at creation",
				0, notebook.numReprosInterConnected());
		
		SccNotebook sameRepro = new SccNotebook("sameRepro", reproNumber);
		notebook.connect(sameRepro);
		assertEquals("Number of intra repro connections !=1 after addition of one",
				1, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 after addition of intra repro connection",
				0, notebook.numInterReproConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				1, sameRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				0, sameRepro.numInterReproConnections());
		assertEquals("Inter connected repros set not empty after addition of intra repro connections",
				0, notebook.numReprosInterConnected());
		assertEquals("Inter connected repros set for argument not empty after addition of intra repro connections",
				0, sameRepro.numReprosInterConnected());
		
		int otherReproNumber = 99;
		SccNotebook otherRepro = new SccNotebook("otherRepro", otherReproNumber);
		notebook.connect(otherRepro);
		assertEquals("Number of intra repro connections changed by addition of inter repro connection",
				1, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections !=1 after addition of one",
				1, notebook.numInterReproConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				0, otherRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				1, otherRepro.numInterReproConnections());
		assertEquals("Wrong number of inter connected repros after addition one inter repro connection",
				1, notebook.numReprosInterConnected());
		assertEquals("Wrong number of inter connected repros for argument after addition one inter repro connection",
				1, otherRepro.numReprosInterConnected());
		
		SccNotebook otherReproAgain = new SccNotebook("otherReproAgain", otherReproNumber);
		notebook.connect(otherReproAgain);
		assertEquals("Number of inter repro connections !=2 after addition of two",
				2, notebook.numInterReproConnections());
		assertEquals("Wrong number of inter connected repros after addition of two connections to the same repro",
				1, notebook.numReprosInterConnected());
		assertEquals("Wrong nymber of inter connected repros for argument after addition of two connections to the same repro",
				1, otherReproAgain.numReprosInterConnected());
		
		int newReproNumber = 447;
		SccNotebook newRepro = new SccNotebook("newRepro.ipynb", newReproNumber);
		notebook.connect(newRepro);
		assertEquals("Wrong number of inter connected repros after addition of inter connections to two different repros",
				2, notebook.numReprosInterConnected());
		assertEquals("Wrong number of inter connected repros after addition of inter connections to two different repros",
				1, newRepro.numReprosInterConnected());
				
		int newReproNumber2 = 448;
		SccNotebook newRepro2 = new SccNotebook("otherReproAgain.ipynb", newReproNumber2);
		notebook.connect(newRepro2);
		assertEquals("Wrong number of inter repro connections after addition of inter connections to three different repros",
				3, notebook.numReprosInterConnected());
		
		// Final check of number of connections, to be on the safe side. (Everything should be checked above.)
		assertEquals("Wrong final number of intra repro connections",
				1 , notebook.numIntraReproConnections());
		assertEquals("Wrong final number of inter repro connections",
				4, notebook.numInterReproConnections());
	}
	
	@Test
	public void testDumping() {
		SccNotebook.setDumpDir("scc_notebook_dump_dir_unittest");
		int otherReproNumber = 99;
		SccNotebook otherRepro = new SccNotebook("otherRepro", otherReproNumber);
		notebook.connect(otherRepro);
		int yetAnotherReproNumber = 1002;
		SccNotebook yetAnotherRepro = new SccNotebook("yetAnotherRepro", yetAnotherReproNumber);
		notebook.connect(yetAnotherRepro);
		notebook.dumpReprosIfLargerOrEq(2);
		assertEquals("Dumped repros don't seem to be read correctly", 2, notebook.numReprosInterConnected());
		SccNotebook.removeDumpDirContents();
	}
	
	@Test
	public void testEquals_equal() {
		SccNotebook otherNotebook = new SccNotebook(notebookName, reproNumber);
		assertTrue("Equal notebooks considered different", notebook.equals(otherNotebook));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		assertFalse("String considered equal to notebook", notebook.equals("Random string"));
	}
	
	@Test
	public void testEquals_diffName() {
		SccNotebook otherNotebook = new SccNotebook("otherName", reproNumber);
		assertFalse("Notebook with other name considered equal", notebook.equals(otherNotebook));
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned for notebook", notebookName, notebook.getName());
	}
	
	@Test
	public void testHashCode() {
		int expectedHashCode = Objects.hash(notebookName);
		assertEquals("Wrong hash code returned for notebook", expectedHashCode, notebook.hashCode());
	}
	
	@Test
	public void testSetDumpDir() {
		String dirName = "scc_notebook_dump_dir_unittest";
		SccNotebook.setDumpDir(dirName);
		File dir = new File(dirName);
		assertTrue("Dump dir not created", dir.exists());
		SccNotebook.removeDumpDirContents();
		File subDir = new File(dirName + "/inter_connected_repros_dump");
		assertFalse("Dump dir contents not removed", subDir.exists());
		dir.delete();
	}
	
	@Test
	public void testRemoveDumpDirContents() {
		String dirName = "scc_notebook_dump_dir_unittest";
		SccNotebook.setDumpDir(dirName);
		String subDirName = dirName + "/inter_connected_repros_dump";
		try {
			new File(subDirName + "/empty_dump_file.repros").createNewFile();
		} catch (IOException e) {
			fail("Could not create test dump file: " + e.getMessage());
		}
		SccNotebook.removeDumpDirContents();
		File subDir = new File(subDirName);
		assertFalse("Dump dir contents not removed", subDir.exists());
		new File(dirName).delete();
	}
}
