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
		assertTrue("Inter connected repros set not empty at creation",
				notebook.getReprosInterConnected().isEmpty());
		
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
		assertTrue("Inter connected repros set not empty after addition of intra repro connections",
				notebook.getReprosInterConnected().isEmpty());
		assertTrue("Inter connected repros set for argument not empty after addition of intra repro connections",
				sameRepro.getReprosInterConnected().isEmpty());
		
		int otherReproNumber = 99;
		SccNotebook otherRepro = new SccNotebook("otherRepro", otherReproNumber);
		Set<Integer> myExpectedRepros = new HashSet<Integer>();
		myExpectedRepros.add(otherReproNumber);
		Set<Integer> otherExpectedRepros = new HashSet<Integer>();
		otherExpectedRepros.add(reproNumber);
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
		
		SccNotebook otherReproAgain = new SccNotebook("otherReproAgain", otherReproNumber);
		notebook.connect(otherReproAgain);
		assertEquals("Number of inter repro connections !=2 after addition of two",
				2, notebook.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition of two connections to the same repro",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of inter connected repro set for argument after addition of two connections to the same repro",
				otherExpectedRepros, otherReproAgain.getReprosInterConnected());
		
		int newReproNumber = 447;
		SccNotebook newRepro = new SccNotebook("newRepro.ipynb", newReproNumber);
		myExpectedRepros.add(newReproNumber);
		notebook.connect(newRepro);
		assertEquals("Wrong content of repro set after addition of inter connections to two different repros",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of repro set for argument after addition of inter connections to two different repros",
				otherExpectedRepros, newRepro.getReprosInterConnected());
				
		int newReproNumber2 = 448;
		SccNotebook newRepro2 = new SccNotebook("otherReproAgain.ipynb", newReproNumber2);
		myExpectedRepros.add(newReproNumber2);
		notebook.connect(newRepro2);
		assertEquals("Wrong content of repro set after addition of inter connections to three different repros",
				myExpectedRepros, notebook.getReprosInterConnected());
		
		// Final check of number of connections, to be on the safe side. (Everything should be checked above.)
		assertEquals("Wrong final number of intra repro connections",
				1 , notebook.numIntraReproConnections());
		assertEquals("Wrong final number of inter repro connections",
				4, notebook.numInterReproConnections());
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
}
