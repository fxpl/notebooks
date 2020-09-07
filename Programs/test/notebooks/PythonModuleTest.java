package notebooks;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

public class PythonModuleTest {
	private PythonModule module;
	private final String name = "someModule";
	private final String alias = "m";
	private final ImportType importedWith = ImportType.ALIAS;
	private String parentModuleName = "parentModule";
	private final PythonModule parent = new PythonModule(parentModuleName, ImportType.ORDINARY);

	
	@Before
	public void setUp() {
		module = new PythonModule(name, alias, importedWith, parent);
	}
	
	@Test
	public void testConstructor_oneParam() {
		PythonModule oneParamModule = new PythonModule(name);
		PythonModule expectedModule = new PythonModule(name, null, null, null);
		assertEquals("Wrong name returned!", name, oneParamModule.getName());
		assertNull("Import type returned for module without import type!", oneParamModule.importedWith());
		assertEquals("Alias or parent set!", expectedModule, oneParamModule);
	}
	
	@Test
	public void testConstructor_twoParam() {
		PythonModule twoParamModule = new PythonModule(name, ImportType.ORDINARY);
		PythonModule expectedModule = new PythonModule(name, null, ImportType.ORDINARY, null);
		assertEquals("Wrong name returned!", name, twoParamModule.getName());
		assertEquals("Wrong import type returned for module!",
				ImportType.ORDINARY, twoParamModule.importedWith());
		assertEquals("Alias or parent set!", expectedModule, twoParamModule);
	}
	
	@Test
	public void testConstructor_fourParam() {
		PythonModule expectedModule = new PythonModule(name, alias, importedWith, parent);
		// Import type is checked by testImportedWith.
		assertEquals("Wrong name, alias or parent set!", expectedModule, module);
	}
	
	@Test
	public void testConstructor_suspicious() {
		PythonModule withoutAlias = new PythonModule(name, ImportType.ALIAS);
		PythonModule expectedModule = new PythonModule(name, null, ImportType.ALIAS, null);
		assertEquals("Wrong name set!", name, withoutAlias.getName());
		assertEquals("Wrong import type set!", ImportType.ALIAS, withoutAlias.importedWith());
		assertEquals("Wrong alias or parent set!", expectedModule, withoutAlias);
		PythonModule withAlias = new PythonModule(name, alias, ImportType.FROM);
		expectedModule = new PythonModule(name, alias, ImportType.ALIAS, null);
		assertEquals("Wrong name set!", name, withAlias.getName());
		assertEquals("Wrong import type set!", ImportType.FROM, withAlias.importedWith());
		assertEquals("Wrong alias or parent set!", expectedModule, withAlias);
	}
	
	@Test
	public void testConstructorWithoutAlias() {
		PythonModule moduleWithoutAlias = new PythonModule(name, ImportType.ORDINARY, parent);
		PythonModule expectedModule = new PythonModule(name, null, ImportType.ORDINARY, parent);
		assertEquals("Wrong name returned!", name, moduleWithoutAlias.getName());
		assertEquals("Wrong import type returned for module!",
				ImportType.ORDINARY, moduleWithoutAlias.importedWith());
		assertEquals("Wrong alias or parent set!", expectedModule, moduleWithoutAlias);
	}
	
	@Test
	public void testConstructorWithoutParent() {
		PythonModule moduleWithoutParent = new PythonModule(name, alias, importedWith);
		PythonModule expectedModule = new PythonModule(name, alias, importedWith, null);
		assertEquals("Wrong name returned!", name, moduleWithoutParent.getName());
		assertEquals("Wrong import type returned for module!", importedWith, moduleWithoutParent.importedWith());
		assertEquals("Wrong alias or parent set!", expectedModule, moduleWithoutParent);
	}
	
	// Four parameter constructor implicitly tested by other getter tests
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name returned by getName!", name, module.getName());
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
		PythonModule other = new PythonModule(name, ImportType.ORDINARY);
		assertFalse("Python module with alias considered equal to python module without alias!",
				module.equals(other));
		assertFalse("Python module without alias considered equal to python module with alias!",
				other.equals(module));
	}
	
	@Test
	public void testEquals_nullParent() {
		PythonModule other = new PythonModule(name, alias, importedWith);
		assertFalse("Python modules with different parents considered equal!", module.equals(other));
		assertFalse("Python modules with different parents considered equal!", other.equals(module));
	}
	
	@Test
	public void testEquals_nullParent_sameAlias() {
		PythonModule other = new PythonModule(name, alias, importedWith, null);
		assertFalse("Python modules with different parents considered equal!", other.equals(module));
	}
	
	@Test
	public void testEquals_diffParent() {
		PythonModule otherParent = new PythonModule("otherParentModule");
		PythonModule other = new PythonModule(name, alias, importedWith, otherParent);
		assertFalse("Python modules with different parents considered equal!", module.equals(other));
	}
	
	@Test
	public void testEquals_diffGrandParent() {
		PythonModule grandParent1 = new PythonModule("farfar");
		PythonModule parent1 = new PythonModule("pappa", null, grandParent1);
		PythonModule module1 = new PythonModule(name, ImportType.ORDINARY, parent1);
		PythonModule grandParent2 = new PythonModule("farmor");
		PythonModule parent2 = new PythonModule("pappa", null, grandParent2);
		PythonModule module2 = new PythonModule(name, ImportType.ORDINARY, parent2);
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
		PythonModule other = new PythonModule("otherName", alias, importedWith);
		assertFalse("Modules with different names considered same!", module.is(other));
	}
	
	@Test
	public void testIs_diffParent() {
		PythonModule otherParent = new PythonModule("mammi");
		PythonModule other = new PythonModule(name, alias, importedWith, otherParent);
		assertFalse("Modules with different parents considered same!", module.is(other));
	}
	
	@Test
	public void testIs_nullParents() {
		PythonModule other = new PythonModule(name, alias, importedWith);
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
		PythonModule module1 = new PythonModule(name, alias, importedWith, parent1);
		PythonModule grandParent2 = new PythonModule(grandParentName);
		PythonModule parent2 = new PythonModule(parentName, null, grandParent2);
		PythonModule module2 = new PythonModule(name, alias, importedWith, parent2);
		assertTrue("Modules with same grand parents considered different!",
				module1.is(module2));
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("Wrong import type retured!", importedWith, module.importedWith());
	}
	
	@Test
	public void testMerge() {
		module.functionUsages.put("fun0", 5);
		module.functionUsages.put("fun1", 2);
		module.functionUsages.put("fun2", 1);
		PythonModule moduleToMerge = new PythonModule(name, ImportType.ORDINARY, parent);
		moduleToMerge.functionUsages.put("fun0", 3);
		moduleToMerge.functionUsages.put("fun1", 2);
		moduleToMerge.functionUsages.put("fun3", 6);
		
		Map <String, Integer> expectedFunctionUsages = new HashMap<String, Integer>(4);
		expectedFunctionUsages.put("fun0", 8);
		expectedFunctionUsages.put("fun1", 4);
		expectedFunctionUsages.put("fun2", 1);
		expectedFunctionUsages.put("fun3", 6);
		
		module.merge(moduleToMerge);
		assertEquals("Modules merged incorrectly.", expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testMerge_diffModule() {
		PythonModule moduleToMerge = new PythonModule(name); // No parent => not the same module
		moduleToMerge.functionUsages.put("fun0", 3);
		
		module.merge(moduleToMerge);
		assertTrue("Modules merged despite representing different modules.",
				module.functionUsages.isEmpty());
	}
	
	@Test
	public void testRegisterUsage_alias() {
		// import parentModuleName.name as alias
		module.registerUsage(alias + ".fun0()");
		module.registerUsage("a = " + alias + ".fun0()");
		module.registerUsage(alias + ".fun1(3)");
		module.registerUsage(alias + ".fun3(8, 6, 2)");
		module.registerUsage(alias + ".fun3( 9 , 3 , 5 )");
		module.registerUsage(alias + " . fun3(13, 22 , 0)");
		module.registerUsage("otherModule.fun32(" + alias + ".funX().)");
		module.registerUsage("[1, 2, 3, " + alias + ".funX(), 5]");
		module.registerUsage(name + ".funY()");	// Should not be registered
		module.registerUsage(parentModuleName + "." + name + ".funZ()"); // Should not be registered
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("fun0", 2);
		expectedFunctionUsages.put("fun1", 1);
		expectedFunctionUsages.put("fun3", 3);
		expectedFunctionUsages.put("funX", 2);
		
		assertEquals("Wrong function usages stored for module imported with alias.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_mult_occurrences() {
		module.registerUsage("[1, " + alias + ".f(8), 6, 2, " + alias + ".f(6), 0]");
		module.registerUsage("[1, " + alias + ".fun1(8), 6, 2, " + alias + ".fun2(6), 0]");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("f", 2);
		expectedFunctionUsages.put("fun1", 1);
		expectedFunctionUsages.put("fun2", 1);
		
		assertEquals("Wrong function usages stored when function is called multiple times.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_nested() {
		module.registerUsage(alias + ".f(" + alias + ".f(x))");
		module.registerUsage(alias + ".fun1(" + alias + ".fun2(a,b,c))");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("f", 2);
		expectedFunctionUsages.put("fun1", 1);
		expectedFunctionUsages.put("fun2", 1);
		
		assertEquals("Wrong function usages stored for nested function calls.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage() {
		// import name
		PythonModule functionsModule = new PythonModule(name, ImportType.ORDINARY);
		functionsModule.registerUsage(name + ".fun( arg1, arg2, a=arg3 )");
		functionsModule.registerUsage(name + ".fun( \"apa\", arg2, \"kossa\" )");
		functionsModule.registerUsage(name + ".fun( \'apa\', arg2, \'kossa\' )");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("fun", 3);
		
		assertEquals("Wrong function usages stored for module without alias.",
				expectedFunctionUsages, functionsModule.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_parent() {
		// import parentModuleName.name
		PythonModule parent = new PythonModule(parentModuleName, ImportType.ORDINARY);
		PythonModule module = new PythonModule(name, ImportType.ORDINARY, parent);
		
		module.registerUsage("funA()");	// Not fun from this module
		module.registerUsage(name + ".funB()");	// Not fun from this module
		module.registerUsage(parentModuleName + "." + name + ".funC()"); // Should be registered
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("funC", 1);
		
		assertEquals("Wrong function usages stored for module with parent.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_from() {
		// from parentModuleName import name
		PythonModule parent = new PythonModule(parentModuleName, ImportType.FROM);
		PythonModule module = new PythonModule(name, ImportType.ORDINARY, parent);
		
		module.registerUsage(name + ".fun0()"); // Should be registered
		module.registerUsage("funX()"); // Should not be registered
		module.registerUsage(parentModuleName + "." + name + ".funY()"); // Should not be registered
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("fun0", 1);
		
		assertEquals("Wrong function usages stored for module imported from parent.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_submodule_from() {
		// from parentModuleName import name.subModuleName
		String subModuleName = "subModule";
		PythonModule grandParent = new PythonModule(parentModuleName, ImportType.FROM);
		PythonModule parent = new PythonModule(name, ImportType.ORDINARY, grandParent);
		PythonModule module = new PythonModule(subModuleName, ImportType.ORDINARY, parent);
		
		module.registerUsage("funA()"); // Should not be registered
		module.registerUsage(subModuleName + ".funB()");	// Should not be registered
		module.registerUsage(name + "." + subModuleName + ".funC()");	// Should be registered
		module.registerUsage(parentModuleName + "." + name + "." + subModuleName + ".funD()");	// Should not be registered
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("funC", 1);
		
		assertEquals("Wrong function usages stored.", expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_from_submodule() {
		// from parentModuleName.subModuleName import name
		PythonModule parent = new PythonModule("parentModuleName.subModuleName", ImportType.FROM);
		PythonModule module = new PythonModule(name, ImportType.ORDINARY, parent);
		
		module.registerUsage(name + ".fun()");
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("fun", 1);
		
		assertEquals("Wrong function usages stored for from import where parent has a submodule.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_submodule_from_with_alias() {
		// from parentModuleName import name.subModuleName as alias
		String subModuleName = "subModule";
		PythonModule grandParent = new PythonModule(parentModuleName, ImportType.FROM);
		PythonModule parent = new PythonModule(name, ImportType.ORDINARY, grandParent);
		PythonModule module = new PythonModule(subModuleName, alias, ImportType.ALIAS, parent);
		
		module.registerUsage("funA()"); // Should not be registered
		module.registerUsage(subModuleName + ".funB()");	// Should not be registered
		module.registerUsage(name + "." + subModuleName + ".funC()");	// Should not be registered
		module.registerUsage(parentModuleName + "." + name + "." + subModuleName + ".funD()");	// Should not be registered
		module.registerUsage(alias + ".funE()");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put("funE", 1);
		
		assertEquals("Wrong function usages stored.", expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_function_from() {
		// from parentModule import function
		PythonModule parent = new PythonModule(parentModuleName, ImportType.FROM);
		PythonModule module = new PythonModule(name, ImportType.ORDINARY, parent);
		
		module.registerUsage(name + "(x, y=z)");
		module.registerUsage("[1, 5, 0, " + name + "(x, y=z), 3, " + name + "(\"apa\")]");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>();
		expectedFunctionUsages.put(name, 3);
		
		assertEquals("Wrong function usages stored when function is imported.",
				expectedFunctionUsages, module.functionUsages);
	}
	
	@Test
	public void testSetOldestAncestor_noParents() {
		PythonModule newAncestor = new PythonModule("Lucy", ImportType.FROM);
		PythonModule expectedDescendant = new PythonModule(name, null, ImportType.ORDINARY, newAncestor);
		PythonModule descendant = new PythonModule(name, ImportType.ORDINARY);
		descendant.setOldestAncestor(newAncestor);
		assertEquals("Parents not set correctly by setOldestAncestor!", expectedDescendant, descendant);
	}
	
	@Test
	public void testSetOldestAncestor_existingAncestors() {
		String grandParentName = "morfar";
		String parentName = "mamma";
		String childName = "me";
		PythonModule newAncestor = new PythonModule("Lucy", ImportType.FROM);
		PythonModule expectedGrandParent = new PythonModule(grandParentName, ImportType.ORDINARY, newAncestor);
		PythonModule expectedParent =new PythonModule(parentName, ImportType.ORDINARY, expectedGrandParent);
		PythonModule expectedChild = new PythonModule(childName, ImportType.ORDINARY, expectedParent);
		PythonModule grandParent = new PythonModule(grandParentName, ImportType.ORDINARY);
		PythonModule parent = new PythonModule(parentName, ImportType.ORDINARY, grandParent);
		PythonModule child = new PythonModule(childName, ImportType.ORDINARY, parent);
		child.setOldestAncestor(newAncestor);
		assertEquals("Oldest ancestor not set correctly when ancestors are already present!", expectedChild, child);
	}
	
	@Test
	public void testToString() {
		String expected = parentModuleName + "." + name + "(" + alias + ")";
		assertEquals("Wrong string representation returned!", expected, module.toString());
	}
	
	@Test
	public void testToString_noAlias() {
		PythonModule moduleWithoutAlias = new PythonModule(name, ImportType.ORDINARY, parent);
		String expected = parentModuleName + "." + name;
		assertEquals("Wrong string representation returned for module without alias!",
				expected, moduleWithoutAlias.toString());
	}
	
	@Test
	public void testPedigreeString() {
		String expected = parentModuleName + "." + name;
		assertEquals("Wrong pedigree string returned.", expected, module.pedigreeString());
	}
	
	@Test
	public void testPedigreeString_noParent() {
		PythonModule moduleWithoutParent = new PythonModule(name, alias, importedWith);
		String expected = name;
		assertEquals("Wrong pedigree string returned for module without parent!",
				expected, moduleWithoutParent.pedigreeString());
	}
	
	@Test
	public void testPedigreeString_grandParent() {
		final String parentName = "pappa";
		final String grandParentName = "farmor";
		PythonModule grandParent = new PythonModule(grandParentName);
		PythonModule parent = new PythonModule(parentName, null, grandParent);
		PythonModule module = new PythonModule(name, alias, importedWith, parent);
		String expected = grandParentName + "." + parentName + "." + name;
		assertEquals("Wrong pedigree string returned for module with grand parent!",
				expected, module.pedigreeString());
	}
}
