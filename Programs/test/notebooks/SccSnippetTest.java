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
	public void testGetLoc() {
		assertEquals("Wrong loc for snippet", loc, snippet.getLoc());
	}
	
	@Test
	public void testInterConnections() {
		String repro1 = "repro1";
		String repro2 = "repro2";
		assertEquals("Number of inter connections != 0 on creation", 0, snippet.numInterConnections());
		assertTrue("Inter connected repros set not empty at creation", snippet.getReprosInterConnected().isEmpty());
		snippet.addInterConnection(repro1);
		assertEquals("Wrong number of inter connections after one addition", 1, snippet.numInterConnections());
		Set<String> expectedRepros = new HashSet<String>();
		expectedRepros.add(repro1);
		assertEquals("Wrong inter connections set after one addition", expectedRepros, snippet.getReprosInterConnected());
		snippet.addInterConnection(repro2);
		expectedRepros.add(repro2);
		assertEquals("Wrong inter connections set after two additions", expectedRepros, snippet.getReprosInterConnected());
		snippet.addInterConnection(repro2);
		assertEquals("Wrong number of inter connections after three additions", 3, snippet.numInterConnections());
		assertEquals("Wrong inter connections set after addition of connection to the same repro", expectedRepros, snippet.getReprosInterConnected());
	}
	
	@Test
	public void testIntraConnections() {
		assertEquals("Number of intra connections != 0 on creation", 0, snippet.numIntraConnections());
		snippet.addIntraConnection();
		assertEquals("Wrong number of intra connections after one addition", 1, snippet.numIntraConnections());
		snippet.addIntraConnection();
		snippet.addIntraConnection();
		assertEquals("Wrong number of intra connections after three additions", 3, snippet.numIntraConnections());
	}
	
	@Test
	public void testIsClone_inter() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		snippet.addInterConnection("foo");
		assertTrue("Snippet not considered clone after addition of addition of inter connection", snippet.isClone());
	}
	
	@Test
	public void testIsClone_intra() {
		assertFalse("Snippet considered clone on creation", snippet.isClone());
		snippet.addIntraConnection();
		assertTrue("Snippet not considered clone after addition of addition of intra connection", snippet.isClone());
	}
}
