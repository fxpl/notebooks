package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class PythonModuleTest {
	private PythonModule module;
	private final String name = "someModule";
	private final ImportType importedWith = ImportType.ORDINARY;

	
	@Before
	public void setUp() {
		module = new PythonModule(name, importedWith);
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned by getName!", name, module.getName());
	}
	
	@Test
	public void testEquals_equal() {
		PythonModule other = new PythonModule(name, ImportType.ALIAS);
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
		PythonModule other = new PythonModule("differentName", ImportType.ALIAS);
		assertFalse("Objects with different names considered equal!", module.equals(other));
	}
	
	@Test
	public void testHashCode() {
		int expectedHash = Objects.hashCode(name);
		assertEquals("Wrong hash code returned!", expectedHash, module.hashCode());
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("Wrong import type retured!", importedWith, module.importedWith());
	}
	
	@Test
	public void testToString() {
		String expected = name;
		assertEquals("Wrong String representation returned!", expected, module.toString());
	}
}
