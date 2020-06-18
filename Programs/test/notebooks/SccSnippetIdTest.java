package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class SccSnippetIdTest {
	private SccSnippetId id;
	private final int file = 7;
	private final int snippet = 78;

	
	@Before
	public void setUp() {
		id = new SccSnippetId(file, snippet);
	}
	
	@Test
	public void testEquals_equal() {
		SccSnippetId equal = new SccSnippetId(file, snippet);
		assertTrue("Equal ID:s considered different!", id.equals(equal));
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		String different = "blah";
		assertFalse("SccSnippetId and String considered equal!", id.equals(different));
	}
	
	@Test
	public void testEquals_diffFile() {
		SccSnippetId other = new SccSnippetId(1, snippet);
		assertFalse("Different SccSnippetId:s consdered equal!", id.equals(other));
	}

	@Test
	public void testEquals_diffSnippet() {
		SccSnippetId other = new SccSnippetId(file, 300);
		assertFalse("Different SccSnippetId:s consdered equal!", id.equals(other));
	}
	
	@Test
	public void testHashCode() {
		int expectedHashCode = Objects.hash(file, snippet);
		assertEquals("Wrong hash code returned!", expectedHashCode, id.hashCode());
	}
	
	@Test
	public void testToString() {
		assertEquals(file + ":" + snippet, id.toString());
	}
}
