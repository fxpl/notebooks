package notebooks;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

// TODO: Testa med nullnotebook och nullrepro!

public class SccSnippetTest {
	private SccSnippet snippet;
	private final int loc = 21;
	private final String notebookName = "nb_86.ipynb";
	private final String reproName = "testRepro";
	private Notebook notebook;
	
	@Before
	public void setUp() {
		notebook = new Notebook(notebookName, reproName);
		snippet = new SccSnippet(loc, notebook);
	}
	
	@Test
	public void testStringConstructor() {
		String locString = Integer.toString(loc);
		SccSnippet stringSnippet = new SccSnippet(locString, notebook);
		assertEquals("Wrong loc for snippet ID created using string constructor", loc, stringSnippet.getLoc());
		assertFalse("Snippet created using string constructor marked as clone", stringSnippet.isClone());
		assertEquals("Wrong notebook retured for snippet ID created using string constructor", notebook, snippet.getNotebook());
	}
	
	@Test
	public void testAddConnections() {
		assertEquals("Number of intra notebook connections != 0 on creation",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 0 on creation",
				0, snippet.numInterNotebookConnections());
		assertEquals("Number of intra repro connections != 0 on creation",
				0, snippet.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 on creation",
				0, snippet.numInterReproConnections());
		assertTrue("Inter connected repros set not empty at creation",
				snippet.getReprosInterConnected().isEmpty());
		SccSnippet sameNotebook = new SccSnippet(5, new Notebook(notebook));
		snippet.addConnection(sameNotebook);
		assertEquals("Number of intra notebook connections !=1 after addition of one",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 0 after addition of intra notebook connection",
				0, snippet.numInterNotebookConnections());
		assertEquals("Number of intra repro connections !=1 after addition of one",
				1, snippet.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 0 after addition of intra repro connection",
				0, snippet.numInterReproConnections());
		assertTrue("Inter connected repros set not empty after addition of intra repro connection",
				snippet.getReprosInterConnected().isEmpty());
		SccSnippet sameRepro = new SccSnippet(7, new Notebook("sameRepro.ipynb", reproName));
		snippet.addConnection(sameRepro);
		assertEquals("Number of intra notebook connections changed by addition of intra repro connection",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections !=1 after addition of one",
				1, snippet.numInterNotebookConnections());
		assertEquals("Number of intra repro connections !=2 after addition of two",
				2, snippet.numIntraReproConnections());
		assertEquals("Number of inter repro connections changed when intra repro connection was added",
				0, snippet.numInterReproConnections());
		assertTrue("Inter connected repros set not empty after addition of intra repro connections",
				snippet.getReprosInterConnected().isEmpty());
		String otherReproName = "otherRepro";
		SccSnippet otherRepro = new SccSnippet(32, new Notebook("otherRepro.ipynb", otherReproName));
		Set<String> expectedRepros = new HashSet<String>();
		expectedRepros.add(otherReproName);
		snippet.addConnection(otherRepro);
		assertEquals("Number of intra notebook connections changed by addition of inter repro connection",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections !=2 after addition of two",
				2, snippet.numInterNotebookConnections());
		assertEquals("Number of intra repro connections changed by addition of inter repro connection",
				2, snippet.numIntraReproConnections());
		assertEquals("Number of inter repro connections !=1 after addition of one",
				1, snippet.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition one inter repro connection",
				expectedRepros, snippet.getReprosInterConnected());
		
		SccSnippet otherRepro2 = new SccSnippet(71, new Notebook("otherReproAgain.ipynb", otherReproName));
		snippet.addConnection(otherRepro2);
		assertEquals("Wrong content of inter connected repro set after addition of two connections to the same repro",
				expectedRepros, snippet.getReprosInterConnected());
		
		String newReproName = "newRepro";
		SccSnippet newRepro = new SccSnippet(101, new Notebook("newRepro.ipynb", newReproName));
		expectedRepros.add(newReproName);
		snippet.addConnection(newRepro);
		assertEquals("Wrong content of repro set after addition of inter connections to two different repros",
				expectedRepros, snippet.getReprosInterConnected());
		
		// Final check of number of connections, to be on the safe side. (Everything should be checked above.)
		assertEquals("Wrong final number of intra notebook connections",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Wrong final number of inter notebook connections",
				4, snippet.numInterNotebookConnections());
		assertEquals("Wrong final number of intra repro connections",
				2, snippet.numIntraReproConnections());
		assertEquals("Wrong final number of inter repro connections",
				3, snippet.numInterReproConnections());
	}
	
	/**
	 * Verify that connections are not added when notebook info is missing in
	 * any of the snippets.
	 */
	@Test
	public void testAddConnections_nullNotebook() {
		SccSnippet nullNbSnippet = new SccSnippet(loc, null);
		snippet.addConnection(nullNbSnippet);
		assertFalse("Snippet considered connected after addition of connection to snippet without notebook", snippet.isClone());
		nullNbSnippet.addConnection(snippet);
		assertFalse("Snippet considered connected despite missing notebook info", nullNbSnippet.isClone());
		SccSnippet otherNullNbSnippet = new SccSnippet(loc, null);
		nullNbSnippet.addConnection(otherNullNbSnippet);
		assertFalse("Snippet considered connected despite missing notebook info in both snippets", nullNbSnippet.isClone());
	}
	
	/**
	 * Verify that snippets are considered inter repro clones if repro
	 * information is missing for any of them.
	 */
	@Test
	public void testAddConnections_nullRepro() {
		SccSnippet nullReproSnippet = new SccSnippet(loc, new Notebook("nullReproNb.ipynb", null));
		snippet.addConnection(nullReproSnippet);
		assertEquals("Inter notebook connection not added when repro info missing in argument",
				1, snippet.numInterNotebookConnections());
		nullReproSnippet.addConnection(snippet);
		assertEquals("Inter notebook connection not added when repro info missing in caller",
				1, nullReproSnippet.numInterNotebookConnections());
		SccSnippet otherReproSnippet = new SccSnippet(loc, new Notebook("otherNullReproNb.ipynb", null));
		nullReproSnippet.addConnection(otherReproSnippet);
		assertEquals("Inter notebook connection not added when repro info missing in both",
				2, nullReproSnippet.numInterNotebookConnections());
	}
	
	@Test
	public void testGetLoc() {
		assertEquals("Wrong loc for snippet", loc, snippet.getLoc());
	}
	
	@Test
	public void testGetNotebook() {
		assertEquals("Wrong notebook for snippet", notebook, snippet.getNotebook());
	}
	
	/**
	 * Verify that a snippet is considered clone after addition of inter
	 * notebook connection, but not before.
	 */
	@Test
	public void testIsClone_inter() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		SccSnippet sameNotebook = new SccSnippet(5, new Notebook(notebook));
		snippet.addConnection(sameNotebook);
		assertTrue("Snippet not considered clone after addition of addition of inter notebook connection", snippet.isClone());
	}
	
	/**
	 * Verify that a snippet is considered clone after addition of intra
	 * notebook connection, but not before.
	 */
	@Test
	public void testIsClone_intra() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		SccSnippet otherNotebook = new SccSnippet(32, new Notebook("otherRepro.ipynb", ""));
		snippet.addConnection(otherNotebook);
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", snippet.isClone());
	}
}
