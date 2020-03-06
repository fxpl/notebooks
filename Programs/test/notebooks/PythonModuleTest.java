package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class PythonModuleTest {
	private PythonModule module;
	private final String name = "someModule";
	private final String alias = "m";
	private final ImportType importedWith = ImportType.ORDINARY;

	
	@Before
	public void setUp() {
		module = new PythonModule(name, alias, importedWith);
	}
	
	@Test
	public void testAlias() {
		assertEquals("Wrong alias returned for module!", alias, module.alias());
	}
	
	@Test
	public void testAlias_empty() {
		PythonModule moduleWithoutAlias = new PythonModule(name, importedWith);
		assertNull("Alias returned for module without alias!", moduleWithoutAlias.alias());
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned by getName!", name, module.getName());
	}
	
	@Test
	public void testEquals_equal() {
		PythonModule other = new PythonModule(name, alias, ImportType.ALIAS);
		assertTrue("Equal pyton modules considered different!", module.equals(other));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		String other = "different";
		assertFalse("Objects or different types considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_diffName() {
		PythonModule other = new PythonModule("differentName", alias, ImportType.ALIAS);
		assertFalse("Python modules with different names considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_diffAlias() {
		PythonModule other = new PythonModule(name, "otherAlias", importedWith);
		assertFalse("Python modules with different alias considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_nullAlias() {
		PythonModule other = new PythonModule(name, importedWith);
		assertFalse("Python module with alias considered equal to python module without alias!",
				module.equals(other));
		assertFalse("Python module without alias considered equal to python module with alias!",
				other.equals(module));
	}
	
	@Test
	public void testHashCode() {
		int expectedHash = Objects.hash(name, alias);
		assertEquals("Wrong hash code returned!", expectedHash, module.hashCode());
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("Wrong import type retured!", importedWith, module.importedWith());
	}
	
	@Test
	public void testToString() {
		String expected = name + "(" + alias + ")";
		assertEquals("Wrong string representation returned for module with alias!", expected, module.toString());
	}
	
	@Test
	public void testToString_noAlias() {
		PythonModule moduleWithoutAlias = new PythonModule(name, importedWith);
		String expected = name;
		assertEquals("Wrong string representation returned for module without alias!",
				expected, moduleWithoutAlias.toString());
	}
}
