package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SccNotebookTest {
	String notebookName = "nb_777.ipynb";
	String reproName = "repro77";
	SccNotebook notebook;
	
	@Before
	public void setUp() {
		notebook = new SccNotebook(notebookName, reproName);
	}
	
	@Test
	public void testCopyConstructor() {
		SccNotebook notebookCopy = new SccNotebook(notebook);
		assertEquals("Wrong name returned for notebook created using copy constructor",
				notebookName, notebookCopy.getName());
		assertEquals("Wrong repro name returned for notebook created using copy constructor",
				reproName, notebookCopy.getRepro());
	}
	
	@Test
	public void testConnect() {
		assertEquals("Number of intra repro connections != 0 on creation",
				0, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 on creation",
				0, notebook.numInterReproConnections());
		assertTrue("Inter connected repros set not empty at creation",
				notebook.getReprosInterConnected().isEmpty());
		
		SccNotebook sameRepro = new SccNotebook("sameRepro", reproName);
		notebook.connect(sameRepro);
		assertEquals("Number of intra repro connections !=1 after addition of one",
				1, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 after addition of intra repro connection",
				0, notebook.numInterReproConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				1, sameRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				0, sameRepro.numInterReproConnections());
		assertTrue("Inter connected repros set not empty after addition of intra repro connections",
				notebook.getReprosInterConnected().isEmpty());
		assertTrue("Inter connected repros set for argument not empty after addition of intra repro connections",
				sameRepro.getReprosInterConnected().isEmpty());
		
		String otherReproName = "repro99";
		SccNotebook otherRepro = new SccNotebook("otherRepro", otherReproName);
		Set<String> myExpectedRepros = new HashSet<String>();
		myExpectedRepros.add(otherReproName);
		Set<String> otherExpectedRepros = new HashSet<String>();
		otherExpectedRepros.add(reproName);
		notebook.connect(otherRepro);
		assertEquals("Number of intra repro connections changed by addition of inter repro connection",
				1, notebook.numIntraReproConnections());
		assertEquals("Number of inter repro connections !=1 after addition of one",
				1, notebook.numInterReproConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				0, otherRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				1, otherRepro.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition one inter repro connection",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of inter connected repro set for argument after addition one inter repro connection",
				otherExpectedRepros, otherRepro.getReprosInterConnected());
		
		SccNotebook otherReproAgain = new SccNotebook("otherReproAgain", otherReproName);
		notebook.connect(otherReproAgain);
		assertEquals("Number of inter repro connections !=2 after addition of two",
				2, notebook.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition of two connections to the same repro",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of inter connected repro set for argument after addition of two connections to the same repro",
				otherExpectedRepros, otherReproAgain.getReprosInterConnected());
		
		String newReproName = "newRepro";
		SccNotebook newRepro = new SccNotebook("newRepro.ipynb", newReproName);
		myExpectedRepros.add(newReproName);
		notebook.connect(newRepro);
		assertEquals("Wrong content of repro set after addition of inter connections to two different repros",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of repro set for argument after addition of inter connections to two different repros",
				otherExpectedRepros, newRepro.getReprosInterConnected());
				
		String newReproName2 = "yetAnotherRepro";
		SccNotebook newRepro2 = new SccNotebook("otherReproAgain.ipynb", newReproName2);
		myExpectedRepros.add(newReproName2);
		notebook.connect(newRepro2);
		assertEquals("Wrong content of repro set after addition of inter connections to three different repros",
				myExpectedRepros, notebook.getReprosInterConnected());
		
		// Final check of number of connections, to be on the safe side. (Everything should be checked above.)
		assertEquals("Wrong final number of intra repro connections",
				1 , notebook.numIntraReproConnections());
		assertEquals("Wrong final number of inter repro connections",
				4, notebook.numInterReproConnections());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by connect when repro info is missing.
	 */
	@Test
	public void testConnect_nullRepro() {
		boolean thrown = false;
		SccNotebook nullReproNb = new SccNotebook("nullReproNb.ipynb", null);
		try {
			nullReproNb.connect(notebook);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when repro info is missing.", thrown);
		assertEquals("Intra repro connection stored despite missing repro info",
				0, notebook.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, nullReproNb.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, notebook.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, nullReproNb.numInterReproConnections());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when repro info is missing in argument notebook.
	 */
	@Test
	public void testConnect_nullReproArg() {
		boolean thrown = false;
		SccNotebook nullReproNb = new SccNotebook("nullReproNb.ipynb", null);
		try {
			notebook.connect(nullReproNb);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when repro info is missing in argument notebook.", thrown);
		assertEquals("Intra repro connection stored despite missing repro info",
				0, notebook.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, nullReproNb.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, notebook.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, nullReproNb.numInterReproConnections());
	}
	
	@Test
	public void testEquals_equal() {
		SccNotebook otherNotebook = new SccNotebook(notebookName, reproName);
		assertTrue("Equal notebooks considered different", notebook.equals(otherNotebook));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		assertFalse("String considered equal to notebook", notebook.equals("Random string"));
	}
	
	@Test
	public void testEquals_diffName() {
		SccNotebook otherNotebook = new SccNotebook("otherName", reproName);
		assertFalse("Notebook with other name considered equal", notebook.equals(otherNotebook));
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned for notebook", notebookName, notebook.getName());
	}
	
	@Test
	public void testGetRepro() {
		assertEquals("Wrong repro returned for notebook", reproName, notebook.getRepro());
	}
	
	@Test
	public void testHashCode() {
		int expectedHashCode = Objects.hash(notebookName);
		assertEquals("Wrong hash code returned for notebook", expectedHashCode, notebook.hashCode());
	}
}
