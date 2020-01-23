package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import notebooks.Notebook;
import notebooks.Snippet;

public class SnippetTest {
	private Snippet snippet;
	private String name = "nb_1000000";
	private int index = 2;
	private String repro = "someRepro";
	
	
	@Before
	public void setUp() {
		this.snippet = new Snippet(this.name, repro, this.index);
	}
	
	@Test
	public void testConstructor_notebook() {
		Notebook notebook = new Notebook(name, repro);
		Snippet notebookSnippet = new Snippet(notebook, index);
		assertEquals("Notebook name not initialized/fetched correctly!", name, notebookSnippet.getFileName());
		assertEquals("Repro name not initialized/fetched correctly!", repro, notebookSnippet.getRepro());
		assertEquals("Index not initialized/fetched correctly!", index, notebookSnippet.getSnippetIndex());
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
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		Notebook different = new Notebook("");
		assertFalse("Snippet and notebook considered equal!", snippet.equals(different));
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong file name returned for snippet!",  name, snippet.getFileName());
	}
	
	@Test
	public void testGetSnippetIndex() {
		assertEquals("Wrong snippet id returned for snippet!", index, snippet.getSnippetIndex());
	}
	
	@Test
	public void testHashCode() {
		int expectedHashCode = Objects.hash(name, index);
		assertEquals("Wrong hash code returned!", expectedHashCode, snippet.hashCode());
	}
	
	@Test
	public void testToString() {
		String expected = name + ", " + index;
		assertEquals("Wrong string representation of snippet:", expected, snippet.toString());
	}
}
