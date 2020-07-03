package notebooks;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SccSnippetTest {
	private SccSnippet snippet;
	private final int loc = 21;
	
	@Before
	public void setUp() {
		snippet = new SccSnippet(loc);
	}
	
	@Test
	public void testStringConstructor() {
		String locString = Integer.toString(loc);
		SccSnippet stringSnippet = new SccSnippet(locString);
		assertEquals("Wrong loc for snippet ID created using string constructor", loc, stringSnippet.getLoc());
		assertFalse("Snippet created using string constructor marked as clone", stringSnippet.isClone());
	}
	
	@Test
	public void testAddConnections() {
		String repro1 = "repro1";
		String repro2 = "repro2";
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
		snippet.addConnection(true, true, null);
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
		snippet.addConnection(true, false, repro1);
		Set<String> expectedRepros = new HashSet<String>();
		expectedRepros.add(repro1);
		assertEquals("Number of intra notebook connections !=2 after addition of two",
				2, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 0 after addition of intra notebook connections",
				0, snippet.numInterNotebookConnections());
		assertEquals("Number of intra repro connections changed by addition of inter repro connection",
				1, snippet.numIntraReproConnections());
		assertEquals("Number of inter repro connections != 1 after addition of one",
				1, snippet.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition of one inter repro connection",
				expectedRepros, snippet.getReprosInterConnected());
		snippet.addConnection(false, false, repro1);
		assertEquals("Number of intra notebook connections changed by addition of inter notebook connection",
				2, snippet.numIntraNotebookConnections());
		assertEquals("Number of inter notebook connections != 1 after addition of one",
				1, snippet.numInterNotebookConnections());
		assertEquals("Number of inter repro connections != 2 after addition of two",
				2, snippet.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition of two connections to the same repro",
				expectedRepros, snippet.getReprosInterConnected());
		snippet.addConnection(false, false, repro2);
		expectedRepros.add(repro2);
		assertEquals("Number of inter notebook connections != 2 after addition of two",
				2, snippet.numInterNotebookConnections());
		assertEquals("Number of inter repro connections != 3 after addition of three", 3,
				snippet.numInterReproConnections());
		assertEquals("Wrong content of inter connected repro set after addition of connection to second repro",
				expectedRepros, snippet.getReprosInterConnected());
		snippet.addConnection(false, true, "apa");
		assertEquals("Content of inter connected repro set changed by addition of intra repro connection",
				expectedRepros, snippet.getReprosInterConnected());
		// Final check of number of connections, to be on the safe side. (Everything should be checked above.)
		assertEquals("Wrong final number of intra notebook connections",
				2, snippet.numIntraNotebookConnections());
		assertEquals("Wrong final number of inter notebook connections",
				3, snippet.numInterNotebookConnections());
		assertEquals("Wrong final number of intra repro connections",
				2, snippet.numIntraReproConnections());
		assertEquals("Wrong final number of inter repro connections",
				3, snippet.numInterReproConnections());
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
		snippet.addConnection(false, true, null);
		assertTrue("Snippet not considered clone after addition of addition of inter notebook connection", snippet.isClone());
	}
	
	/**
	 * Verify that a snippet is considered clone after addition of intra
	 * notebook connection, but not before.
	 */
	@Test
	public void testIsClone_intra() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		snippet.addConnection(true, true, null);
		assertTrue("Snippet not considered clone after addition of addition of intra notebook connection", snippet.isClone());
	}
}
