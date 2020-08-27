package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class SnippetCodeTest {
	SnippetCode code;
	private final int LOC = 4;
	private final String hash = "ABCD";
	
	@Before
	public void setUp() {
		code = new SnippetCode(LOC, hash);
	}
	
	@Test
	public void testConstructor() {
		assertEquals("Wrong LOC in SnippetCode!", LOC, code.getLOC());
		assertEquals("Wrong hash in SnippetCode!", hash, code.getHash());
	}
	
	@Test
	public void testCopyConstructor() {
		SnippetCode copy = new SnippetCode(code);
		assertEquals("Copy constructor returns different object!", code, copy);
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
		assertTrue("Snippet codes considered different when hash is the same but LOC differ!",
				code.equals(different));
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
	
	@Test
	public void testIsEmpty() {
		assertFalse("Non-empty snippet code  considered empty!", code.isEmpty());
	}

	@Test
	public void testIsEmpty_emptyHash() {
		SnippetCode emptyCode = new SnippetCode(1, "D41D8CD98F00B204E9800998ECF8427E");
		assertTrue("Empty snippet code considered non-empty!", emptyCode.isEmpty());
	}
	
	@Test
	public void testIsEmpty_0loc() {
		SnippetCode emptyCode = new SnippetCode(0, hash);
		assertTrue("Empty snippet code considered non-empty!", emptyCode.isEmpty());
	}
	
	@Test
	public void testIsIntraClone_unique() {
		SnippetCode[] nbSnippets = {
			new SnippetCode(1, "DEFG"),
			new SnippetCode(code),
			new SnippetCode(1, "DEFG"),
			new SnippetCode(5, "HIJK")
		};
		assertFalse("Incorrect intra clone found!", code.isIntraClone(nbSnippets));
	}
	
	@Test
	public void testIsIntraClone_1copy() {
		SnippetCode[] nbSnippets = {
			new SnippetCode(code),
			new SnippetCode(3, "BLAHA"),
			new SnippetCode(code)
		};
		assertTrue("Intra clone missed!", code.isIntraClone(nbSnippets));
	}
	
	@Test
	public void testIsIntraClone_2copies() {
		SnippetCode[] nbSnippets = {
			new SnippetCode(code),
			new SnippetCode(code),
			new SnippetCode(code)
		};
		assertTrue("Intra clone missed!", code.isIntraClone(nbSnippets));
	}
	
	@Test
	public void testSetLOC() {
		int newLOC  = 10;
		code.setLOC(newLOC);
		assertEquals("LOC not set correctly!", newLOC, code.getLOC());
	}
	
	@Test
	public void testToString() {
		assertEquals("Wrong string representation!", hash, code.toString());
	}
}