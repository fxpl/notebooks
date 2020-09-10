package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AllModulesTest {
	private PythonModule module;
	private final PythonModule parent = new PythonModule("module", ImportType.FROM);
	
	@Before
	public void setUp() {
		module = new AllModules(parent);
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("AllModules objects not imported with ORDINARY statment!",
				ImportType.ORDINARY, module.importedWith());
	}
	
	@Test
	public void testConstructor() {
		/* If this tests fails, but testGetName and testImportedWith, the
		   problem is in the parent or with the alias. */
		PythonModule expected = new PythonModule("*", null, ImportType.ORDINARY, parent);
		assertEquals("Incorrect module returned by constructor!", expected, module);
	}
	
	@Test public void testIs_null() {
		assertFalse("Module instance considered being null.", module.is(null));
		assertFalse("Module instance considered being null.", module.is((AllModules)null));
	}
	
	@Test
	public void testIs_same() {
		PythonModule otherParent = new PythonModule("module", ImportType.FROM);
		PythonModule other = new AllModules(otherParent);
		assertTrue("Identical AllModules instances considered different.", other.is(module));
	}
	
	@Test
	public void testIs_sameButDiffTypes() {
		PythonModule other = new PythonModule("module", ImportType.ORDINARY);
		assertTrue("Modules representing the same module considered different when of different types.",
				module.is(other));
		assertTrue("Modules representing the same module considered different when of different types.",
				other.is(module));
	}
	
	@Test
	public void testIs_sameTypeButDifferent() {
		PythonModule otherParent = new PythonModule("otherParent", ImportType.FROM);
		PythonModule other = new AllModules(otherParent);
		assertFalse("Modules instances representing different modules considered same when being AllModules instances.",
				module.is(other));
		assertFalse("Modules instances representing different modules considered same when being AllModules instances.",
				other.is(module));
	}
	
	@Test
	public void testPedigreeString() {
		assertEquals("Wrong pedigree string returned for AllModule instance.",
				"module", module.pedigreeString());
	}
	
	@Test
	public void testRegisterUsage() {
		// from numpy import *
		PythonModule numpyModule = new PythonModule("numpy", ImportType.FROM);
		PythonModule numpyFunctions = new AllModules(numpyModule);
		
		numpyFunctions.registerUsage("nonNumpyFunction()");
		numpyFunctions.registerUsage("someOtherFunction(a=sin(1.85), b=sin(3.2), c=sin(5.7))");
		numpyFunctions.registerUsage("b = cos ( 1.85 ) \n");
		numpyFunctions.registerUsage("c = sin( 0 )");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>(2);
		expectedFunctionUsages.put("sin", 4);
		expectedFunctionUsages.put("cos", 1);
		
		assertEquals("numpy functions imported with \"*\" are not identified correctly.",
				expectedFunctionUsages, numpyFunctions.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_twoModules() {
		PythonModule pandasModule1 = new PythonModule("pandas", ImportType.FROM);
		PythonModule pandasFunctions1 = new AllModules(pandasModule1);
		pandasFunctions1.registerUsage("a = read_csv('my_data.csv')");
		
		PythonModule pandasModule2 = new PythonModule("pandas", ImportType.FROM);
		PythonModule pandasFunctions2 = new AllModules(pandasModule2);
		pandasFunctions2.registerUsage("b = read_json({data=7})");
		
		Map<String, Integer> expectedFunctionUsages1 = new HashMap<String, Integer>(1);
		expectedFunctionUsages1.put("read_csv", 1);
		Map<String, Integer> expectedFunctionUsages2 = new HashMap<String, Integer>(1);
		expectedFunctionUsages2.put("read_json", 1);
		
		assertEquals("pandas functions imported with \"*\" are not identified correctly when two pandas instances exist.",
				expectedFunctionUsages1, pandasFunctions1.functionUsages);
		assertEquals("pandas functions imported with \"*\" are not identified correctly when two pandas instances exist.",
				expectedFunctionUsages2, pandasFunctions2.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_unknownModule() {
		PythonModule userModule = new PythonModule("myLocallyDefinedModule", ImportType.FROM);
		PythonModule userModuleFunctions = new AllModules(userModule);
		
		userModuleFunctions.registerUsage("a = sin(1.85)");
		userModuleFunctions.registerUsage("someRandomFunction(a)");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>(0);
		
		assertEquals("Functions registered for unknown module!",
				expectedFunctionUsages, userModuleFunctions.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_nonFunction() {
		PythonModule matplotModule = new PythonModule("matplotlib", ImportType.FROM);
		PythonModule matplotFunctions = new AllModules(matplotModule);
		
		matplotFunctions.registerUsage("err = pyplot()");	// This is not valid, since pyplot is a sub module
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>(0);
		
		assertEquals("Incorrect call to sub module identified as function call.",
				expectedFunctionUsages, matplotFunctions.functionUsages);
	}
	
	@Test
	public void testToString() {
		String expected = "module.*";
		assertEquals("Wrong string representation returned for AllModules object!",
				expected, module.toString());
	}
}
