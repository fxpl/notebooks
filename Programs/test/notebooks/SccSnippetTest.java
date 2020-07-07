package notebooks;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

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
	public void testConnect() {
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
		snippet.connect(sameNotebook);
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
		snippet.connect(sameRepro);
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
		snippet.connect(otherRepro);
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
		snippet.connect(otherRepro2);
		assertEquals("Wrong content of inter connected repro set after addition of two connections to the same repro",
				expectedRepros, snippet.getReprosInterConnected());
		
		String newReproName = "newRepro";
		SccSnippet newRepro = new SccSnippet(101, new Notebook("newRepro.ipynb", newReproName));
		expectedRepros.add(newReproName);
		snippet.connect(newRepro);
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
		
		// Also check snippets given as arguments
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				1, sameNotebook.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				0, sameNotebook.numInterNotebookConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				1, sameNotebook.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				0, sameNotebook.numInterReproConnections());
		assertEquals("Wrong number of elements in inter connected repros set for argument snippet",
				0, sameNotebook.getReprosInterConnected().size());
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				0, sameRepro.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				1, sameRepro.numInterNotebookConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				1, sameRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				0, sameRepro.numInterReproConnections());
		assertEquals("Wrong number of elements in inter connected repros set for argument snippet",
				0, sameRepro.getReprosInterConnected().size());
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				0, otherRepro.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				1, otherRepro.numInterNotebookConnections());
		assertEquals("Wrong number of intra repro connections for argument snippet",
				0, otherRepro.numIntraReproConnections());
		assertEquals("Wrong number of inter repro connections for argument snippet",
				1, otherRepro.numInterReproConnections());
		assertEquals("Wrong number of elements in inter connected repros set for argument snippet",
				1, otherRepro.getReprosInterConnected().size());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when notebook info is missing.
	 */
	@Test
	public void testConnect_nullNotebook() {
		boolean thrown = false;
		SccSnippet nullNbSnippet = new SccSnippet(loc, null);
		try {
			snippet.connect(nullNbSnippet);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when notebook info is missing.", thrown);
		assertEquals("Intra notebook connection stored despite missing notebook info",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Intra notebook connection stored despite missing notebook info",
				0, nullNbSnippet.numIntraNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing notebook info",
				0, snippet.numInterNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing notebook info",
				0, nullNbSnippet.numInterNotebookConnections());
		assertEquals("Intra repro connection stored despite missing notebook info",
				0, snippet.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing notebook info",
				0, nullNbSnippet.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing notebook info",
				0, snippet.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing notebook info",
				0, nullNbSnippet.numInterReproConnections());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when notebook info is missing in argument snippet.
	 */
	@Test
	public void testConnect_nullNotebookArg() {
		boolean thrown = false;
		SccSnippet nullNbSnippet = new SccSnippet(loc, null);
		try {
			nullNbSnippet.connect(snippet);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when notebook info is missing in argument snippet.", thrown);
		assertEquals("Intra repro connection stored despite missing notebook info",
				0, snippet.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing notebook info",
				0, nullNbSnippet.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing notebook info",
				0, snippet.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing notebook info",
				0, nullNbSnippet.numInterReproConnections());
		assertEquals("Intra notebook connection stored despite missing notebook info",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Intra notebook connection stored despite missing notebook info",
				0, nullNbSnippet.numIntraNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing notebook info",
				0, snippet.numInterNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing notebook info",
				0, nullNbSnippet.numInterNotebookConnections());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when repro info is missing.
	 */
	@Test
	public void testConnect_nullRepro() {
		boolean thrown = false;
		SccSnippet nullReproSnippet = new SccSnippet(loc, new Notebook("nullReproNb.ipynb", null));
		try {
			snippet.connect(nullReproSnippet);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when repro info is missing.", thrown);
		assertEquals("Intra notebook connection stored despite missing repro info",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Intra notebook connection stored despite missing repro info",
				0, nullReproSnippet.numIntraNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing repro info",
				0, snippet.numInterNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing repro info",
				0, nullReproSnippet.numInterNotebookConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, snippet.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, nullReproSnippet.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, snippet.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, nullReproSnippet.numInterReproConnections());
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when repro info is missing in argument snippet.
	 */
	@Test
	public void testConnect_nullReproArg() {
		boolean thrown = false;
		SccSnippet nullReproSnippet = new SccSnippet(loc, new Notebook("nullReproNb.ipynb", null));
		try {
			nullReproSnippet.connect(snippet);
		} catch (NullPointerException e) {
			thrown = true;
		}
		assertTrue("No NullPointerException thrown when repro info is missing in argument snippet.", thrown);
		assertEquals("Intra notebook connection stored despite missing repro info",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Intra notebook connection stored despite missing repro info",
				0, nullReproSnippet.numIntraNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing repro info",
				0, snippet.numInterNotebookConnections());
		assertEquals("Inter notebook connection stored despite missing repro info",
				0, nullReproSnippet.numInterNotebookConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, snippet.numIntraReproConnections());
		assertEquals("Intra repro connection stored despite missing repro info",
				0, nullReproSnippet.numIntraReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, snippet.numInterReproConnections());
		assertEquals("Inter repro connection stored despite missing repro info",
				0, nullReproSnippet.numInterReproConnections());
		
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
		snippet.connect(sameNotebook);
		assertTrue("Snippet not considered clone after addition of addition of inter notebook connection", snippet.isClone());
		assertTrue("Snippet not considered clone after addition of addition of inter notebook connection", sameNotebook.isClone());
	}
	
	/**
	 * Verify that a snippet is considered clone after addition of intra
	 * notebook connection, but not before.
	 */
	@Test
	public void testIsClone_intra() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		SccSnippet otherNotebook = new SccSnippet(32, new Notebook("otherRepro.ipynb", ""));
		snippet.connect(otherNotebook);
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", snippet.isClone());
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", otherNotebook.isClone());
	}
}
