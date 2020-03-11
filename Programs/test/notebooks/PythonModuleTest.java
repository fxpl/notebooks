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
	private String parentModuleName = "parentModule";
	private final PythonModule parent = new PythonModule(parentModuleName, ImportType.ORDINARY);

	
	@Before
	public void setUp() {
		module = new PythonModule(name, alias, importedWith, parent);
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
	public void testConstructor_oneParam() {
		PythonModule oneParamModule = new PythonModule(name);
		assertEquals("Wrong name returned!", name, oneParamModule.getName());
		assertNull("Import type returned for module without import type!", oneParamModule.importedWith());
		assertNull("Alias returned for module without alias!", oneParamModule.alias());
		assertNull("Non-null parent returned from module without parent!", oneParamModule.getParent());
	}
	
	@Test
	public void testConstructor_twoParam() {
		PythonModule twoParamModule = new PythonModule(name, importedWith);
		assertEquals("Wrong name returned!", name, twoParamModule.getName());
		assertEquals("Wrong import type returned for module!", importedWith, twoParamModule.importedWith());
		assertNull("Alias returned for module without alias!", twoParamModule.alias());
		assertNull("Non-null parent returned from module without parent!", twoParamModule.getParent());
	}
	
	@Test
	public void testConstructorWithoutAlias() {
		PythonModule moduleWithoutAlias = new PythonModule(name, importedWith, parent);
		assertEquals("Wrong name returned!", name, moduleWithoutAlias.getName());
		assertEquals("Wrong import type returned for module!", importedWith, moduleWithoutAlias.importedWith());
		assertNull("Alias returned for module without alias!", moduleWithoutAlias.alias());
		assertEquals("Wrong parent module returned!", parent, module.getParent());
	}
	
	@Test
	public void testConstructorWithoutParent() {
		PythonModule moduleWithoutParent = new PythonModule(name, alias, importedWith);
		assertEquals("Wrong name returned!", name, moduleWithoutParent.getName());
		assertEquals("Wrong import type returned for module!", importedWith, moduleWithoutParent.importedWith());
		assertEquals("Wrong alias returned!", alias, moduleWithoutParent.alias());
		assertNull("Non-null parent returned from module without parent!", moduleWithoutParent.getParent());
	}
	
	// Four parameter constructor implicitly tested by other getter tests
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned by getName!", name, module.getName());
	}
	
	@Test
	public void testGetParent() {
		assertEquals("Wrong module returned by getParent!", parent, module.getParent());
	}
	
	@Test
	public void testGetParent_null() {
		PythonModule moduleWithoutParent = new PythonModule(name);
		assertNull("Non-null parent returned from module without parent!", moduleWithoutParent.getParent());
	}
	
	@Test
	public void testEquals_equal() {
		PythonModule other = new PythonModule(name, alias, ImportType.ALIAS, parent);
		assertTrue("Equal pyton modules considered different!", module.equals(other));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		String other = "different";
		assertFalse("Objects or different types considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_null() {
		assertFalse("Module considered equal to null!", module.equals(null));
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
	public void testEquals_nullParent() {
		PythonModule other = new PythonModule(name, importedWith);
		assertFalse("Python modules with different parents considered equal!", module.equals(other));
		assertFalse("Python modules with different parents considered equal!", other.equals(module));
	}
	
	@Test
	public void testEquals_diffParent() {
		PythonModule otherParent = new PythonModule("otherParentMOdule");
		PythonModule other = new PythonModule(name, alias, importedWith, otherParent);
		assertFalse("Python modules with different parents considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_diffGrandParent() {
		PythonModule grandParent1 = new PythonModule("farfar");
		PythonModule parent1 = new PythonModule("pappa", null, grandParent1);
		PythonModule module1 = new PythonModule(name, importedWith, parent1);
		PythonModule grandParent2 = new PythonModule("farmor");
		PythonModule parent2 = new PythonModule("pappa", null, grandParent2);
		PythonModule module2 = new PythonModule(name, importedWith, parent2);
		assertFalse("Modules with different grand parents considered equal!", module1.equals(module2));
	} 
	
	@Test
	public void testHashCode() {
		int expectedHash = Objects.hash(name, alias) * Objects.hash(parentModuleName, null);
		assertEquals("Wrong hash code returned!", expectedHash, module.hashCode());
	}
	
	@Test
	public void testHashCode_nullParent() {
		PythonModule moduleWithoutParent = new PythonModule(name);
		int expectedHash = Objects.hash(name, null);
		assertEquals("Wrong hash code returned!", expectedHash, moduleWithoutParent.hashCode());
	}
	
	@Test
	public void testIs_same() {
		PythonModule parent = new PythonModule(parentModuleName);
		PythonModule same = new PythonModule(name, ImportType.FROM, parent);
		assertTrue("Same module considered different!", module.is(same));
	}
	
	@Test
	public void testIs_diffName() {
		PythonModule other = new PythonModule("otherName", importedWith);
		assertFalse("Modules with different names considered same!", module.is(other));
	}
	
	@Test
	public void testIs_diffParent() {
		PythonModule otherParent = new PythonModule("mammi");
		PythonModule other = new PythonModule(name, importedWith, otherParent);
		assertFalse("Modules with different parents considered same!", module.is(other));
	}
	
	@Test
	public void testIs_nullParents() {
		PythonModule other = new PythonModule(name, importedWith);
		assertFalse("Module with different parent considered same!", module.is(other));
		assertFalse("Module with different parent considered same!", other.is(module));
	}
	
	@Test
	public void testIs_same_diffGrandParent() {
		String parentName = "mamma";
		String diffParentName = "mormor";
		PythonModule diffGrandParent = new PythonModule(diffParentName);
		PythonModule diffParent = new PythonModule(parentName, null, diffGrandParent);
		PythonModule other = new PythonModule(name, null, diffParent);
		assertFalse("Modules with different grand parents considered same!", module.is(other));
	}
	
	@Test
	public void testIs_sameGrandParent() {
		final String parentName = "pappa";
		final String grandParentName = "farmor";
		PythonModule grandParent1 = new PythonModule(grandParentName);
		PythonModule parent1 = new PythonModule(parentName, null, grandParent1);
		PythonModule module1 = new PythonModule(name, importedWith, parent1);
		PythonModule grandParent2 = new PythonModule(grandParentName);
		PythonModule parent2 = new PythonModule(parentName, null, grandParent2);
		PythonModule module2 = new PythonModule(name, importedWith, parent2);
		assertTrue("Modules with same grand parents considered different!",
				module1.is(module2));
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("Wrong import type retured!", importedWith, module.importedWith());
	}
	
	@Test
	public void testSetParent() {
		PythonModule newParent = new PythonModule("styvmor");
		module.setParent(newParent);
		assertEquals("Parent not set correctly by setParent!", newParent, module.getParent());
	}
	
	@Test
	public void testToString() {
		String expected = parentModuleName + "." + name + "(" + alias + ")";
		assertEquals("Wrong string representation returned!", expected, module.toString());
	}
	
	@Test
	public void testToString_noAlias() {
		PythonModule moduleWithoutAlias = new PythonModule(name, importedWith, parent);
		String expected = parentModuleName + "." + name;
		assertEquals("Wrong string representation returned for module without alias!",
				expected, moduleWithoutAlias.toString());
	}
	
	@Test
	public void testToString_noParent() {
		PythonModule moduleWithoutParent = new PythonModule(name, alias, importedWith);
		String expected = name + "(" + alias + ")";
		assertEquals("Wrong string representation returned for module without parent!",
				expected, moduleWithoutParent.toString());
	}
	
	@Test
	public void testToString_grandParent() {
		final String parentName = "pappa";
		final String grandParentName = "farmor";
		PythonModule grandParent = new PythonModule(grandParentName);
		PythonModule parent = new PythonModule(parentName, null, grandParent);
		PythonModule module = new PythonModule(name, importedWith, parent);
		String expected = grandParentName + "." + parentName + "." + name;
		assertEquals("Wrong string representation returned for module with grand parent!",
				expected, module.toString());
	}
}
