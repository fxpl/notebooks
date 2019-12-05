package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class SnippetCodeTest {
	SnippetCode code;
	private int LOC = 4;
	private String hash = "ABCD";
	
	@Before
	public void setUp() {
		code = new SnippetCode(LOC, hash);
	}
	
	@Test
	public void testConstructor() {
		assertEquals("Wrong LOC in SnippetCode!", LOC, code.getLOC());
		assertEquals("Wrong hash in SnippetCode!", hash, code.getHash());
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		Notebook different = new Notebook("");
		assertFalse("Snippet code and notebook considered equal!", code.equals(different));
	}
	
	@Test
	public void testEquals_diffHash() {
		SnippetCode different = new SnippetCode(LOC, "FFFF");
		assertFalse("Snippet codes considered equal when hashes differ!", code.equals(different));
	}
	
	@Test
	public void testEquals_diffLOC() {
		SnippetCode different = new SnippetCode(1000, hash);
		assertFalse("Snippet codes considered equal when LOC differ!", code.equals(different));
	}
	
	@Test
	public void testEquals_equal() {
		SnippetCode equal = new SnippetCode(LOC, hash);
		assertTrue("Equal snippet codes considered different!", code.equals(equal));
	}
	
	@Test
	public void testHashCode() {
		assertEquals("Wrong hash code returned!", Objects.hash(hash), code.hashCode());
	}
}