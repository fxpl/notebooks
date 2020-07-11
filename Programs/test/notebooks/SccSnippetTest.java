package notebooks;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SccSnippetTest {
	private SccSnippet snippet;
	private final int loc = 21;
	private final String notebookName = "nb_86.ipynb";
	private final String reproName = "testRepro";
	private SccNotebook notebook;
	
	@Before
	public void setUp() {
		notebook = new SccNotebook(notebookName, reproName);
		snippet = new SccSnippet(loc, notebook);
	}
	
	@Test
	public void testStringConstructor() {
		String locString = Integer.toString(loc);
		SccSnippet stringSnippet = new SccSnippet(locString, notebook);
		assertEquals("Wrong loc for snippet ID created using string constructor", loc, stringSnippet.getLoc());
		assertFalse("Snippet created using string constructor marked as clone", stringSnippet.isClone());
	}
	
	@Test
	public void testConnect() {
		assertEquals("Number of intra notebook connections != 0 on creation",
				0, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 0 on creation",
				0, snippet.numInterNotebookConnections());
		SccSnippet sameNotebook = new SccSnippet(5, new SccNotebook(notebook));
		snippet.connect(sameNotebook);
		assertEquals("Number of intra notebook connections !=1 after addition of one",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 0 after addition of intra notebook connection",
				0, snippet.numInterNotebookConnections());
		
		SccSnippet sameRepro = new SccSnippet(7, new SccNotebook("sameRepro.ipynb", reproName));
		snippet.connect(sameRepro);
		assertEquals("Number of intra notebook connections changed by addition of intra repro connection",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections !=1 after addition of one",
				1, snippet.numInterNotebookConnections());
		
		String otherReproName = "otherRepro";
		SccSnippet otherRepro = new SccSnippet(32, new SccNotebook("otherRepro.ipynb", otherReproName));
		snippet.connect(otherRepro);
		assertEquals("Number of intra notebook connections changed by addition of inter repro connection",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections !=2 after addition of two",
				2, snippet.numInterNotebookConnections());
		
		// Final check of number of connections, to be on the safe side. (Every case should be checked above.)
		assertEquals("Wrong final number of intra notebook connections",
				1, snippet.numIntraNotebookConnections());
		assertEquals("Wrong final number of inter notebook connections",
				2, snippet.numInterNotebookConnections());
		
		// Also check snippets given as arguments
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				1, sameNotebook.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				0, sameNotebook.numInterNotebookConnections());
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				0, sameRepro.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				1, sameRepro.numInterNotebookConnections());
		assertEquals("Wrong number of intra notebook connections for argument snippet",
				0, otherRepro.numIntraNotebookConnections());
		assertEquals("Wrong number of inter notebook connections for argument snippet",
				1, otherRepro.numInterNotebookConnections());
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
	}
	
	/**
	 * Verify that a NullPointerException is thrown and no connections are added
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
		SccSnippet nullReproSnippet = new SccSnippet(loc, new SccNotebook("nullReproNb.ipynb", null));
		try {
			nullReproSnippet.connect(snippet);
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
	}
	
	/**
	 * Verify that a NullPointerException is throw and no connections are added
	 * by addConnection when repro info is missing in argument snippet.
	 */
	@Test
	public void testConnect_nullReproArg() {
		boolean thrown = false;
		SccSnippet nullReproSnippet = new SccSnippet(loc, new SccNotebook("nullReproNb.ipynb", null));
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
	}
	
	@Test
	public void testGetLoc() {
		assertEquals("Wrong loc for snippet", loc, snippet.getLoc());
	}
	
	/**
	 * Verify that a snippet is considered clone after addition of inter
	 * notebook connection, but not before.
	 */
	@Test
	public void testIsClone_inter() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		SccSnippet sameNotebook = new SccSnippet(5, new SccNotebook(notebook));
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
		SccSnippet otherNotebook = new SccSnippet(32, new SccNotebook("otherRepro.ipynb", ""));
		snippet.connect(otherNotebook);
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", snippet.isClone());
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", otherNotebook.isClone());
	}
	
	/**
	 * Verify that a snippet is considered an intra notebook clone if and only
	 * if an intra notebook connection is added.
	 */
	@Test
	public void testIsIntraNotebookClone() {
		assertFalse("Snippet considered intra notebook clone on creation", snippet.isIntraNotebookClone());
		SccSnippet otherNotebook = new SccSnippet(35, new SccNotebook("otherRepro.ipynb", ""));
		snippet.connect(otherNotebook);
		assertFalse("Snippet considered intra clone after addition of inter notebook connection", snippet.isIntraNotebookClone());
		SccSnippet sameNotebook = new SccSnippet(17, new SccNotebook(notebook));
		snippet.connect(sameNotebook);
		assertTrue("Snippet not considered intra clone after addition of intra notebook connection", snippet.isIntraNotebookClone());
	}
}
