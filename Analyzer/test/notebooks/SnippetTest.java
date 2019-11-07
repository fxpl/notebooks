package notebooks;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import notebooks.Notebook;
import notebooks.Snippet;

public class SnippetTest {
	private Snippet snippet;
	private String name = "nb_1000000";
	private int index = 2;
	
	
	@Before
	public void setUp() {
		this.snippet = new Snippet(this.name, this.index);
	}

	@Test
	public void testEquals_equal() {
		Snippet equal = new Snippet(name, index);
		assertTrue("Equal snippets considered different!", snippet.equals(equal));
	}
	
	@Test
	public void testEquals_diffName() {
		Snippet different = new Snippet("nb_0.ipynb", 2);
		assertFalse("Different snippets considered equal!", snippet.equals(different));
	}
	
	@Test
	public void testEquals_diffIndex() {
		Snippet different = new Snippet(name, 10);
		assertFalse("Different snippets considered equal!", snippet.equals(different));
	}
	
	@Test
	public void testEquals_diffType() {
		Notebook different = new Notebook("");
		assertFalse("Snippet and notebook considered equal!", snippet.equals(different));
	}
	
	@Test
	public void testToString() {
		String expected = name + ", " + index;
		assertEquals("Wrong string representation of snippet:", expected, snippet.toString());
	}
}
