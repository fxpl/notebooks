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
		assertTrue("Inter connected repros set not empty at creation",
				notebook.getReprosInterConnected().isEmpty());
		SccNotebook sameRepro = new SccNotebook("sameRepro", reproName);
		notebook.connect(sameRepro);
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
		assertEquals("Wrong content of inter connected repro set after addition one inter repro connection",
				myExpectedRepros, notebook.getReprosInterConnected());
		assertEquals("Wrong content of inter connected repro set for argument after addition one inter repro connection",
				otherExpectedRepros, otherRepro.getReprosInterConnected());
		
		SccNotebook otherReproAgain = new SccNotebook("otherReproAgain", otherReproName);
		notebook.connect(otherReproAgain);
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
