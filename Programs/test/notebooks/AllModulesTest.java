package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		// from math import *
		PythonModule mathModule = new PythonModule("math", ImportType.FROM);
		PythonModule mathFunctions = new AllModules(mathModule);
		
		mathFunctions.registerUsage("nonMathFunction()");
		mathFunctions.registerUsage("someOtherFunction(a=sin(1.85), b=sin(3.2), c=sin(5.7))");
		mathFunctions.registerUsage("b = cos ( 1.85 ) \n");
		mathFunctions.registerUsage("c = sin( 0 )");
		
		Map<String, Integer> expectedFunctionUsages = new HashMap<String, Integer>(2);
		expectedFunctionUsages.put("sin", 4);
		expectedFunctionUsages.put("cos", 1);
		
		assertEquals("math functions imported with \"*\" are not identified correctly.",
				expectedFunctionUsages, mathFunctions.functionUsages);
	}
	
	@Test
	public void testRegisterUsage_twoModules() {
		PythonModule osModule1 = new PythonModule("os", ImportType.FROM);
		PythonModule osFunctions1 = new AllModules(osModule1);
		osFunctions1.registerUsage("p = getcwd()");
		
		PythonModule osModule2 = new PythonModule("os", ImportType.FROM);
		PythonModule osFunctions2 = new AllModules(osModule2);
		osFunctions2.registerUsage("listdir()");
		
		Map<String, Integer> expectedFunctionUsages1 = new HashMap<String, Integer>(1);
		expectedFunctionUsages1.put("getcwd", 1);
		Map<String, Integer> expectedFunctionUsages2 = new HashMap<String, Integer>(1);
		expectedFunctionUsages2.put("listdir", 1);
		
		assertEquals("os functions imported with \"*\" are not identified correctly when two os instances exist.",
				expectedFunctionUsages1, osFunctions1.functionUsages);
		assertEquals("os functions imported with \"*\" are not identified correctly when two os instances exist.",
				expectedFunctionUsages2, osFunctions2.functionUsages);
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
	public void testCallsTo() {
		PythonModule mathModule = new PythonModule("math", ImportType.FROM);
		PythonModule mathFunctions = new AllModules(mathModule);
		
		List<String> expectedCalls = new ArrayList<String>(2);
		expectedCalls.add("sin(x)");
		expectedCalls.add("sin( y )");
		
		List<String> calls = mathFunctions.callsTo("sin", "a = sin(x) + cos(y) - tan(z) + sin( y )");
		assertEquals("Wrong call list returned!", expectedCalls, calls);
	}
	
	@Test
	public void testCallsTo_noMatch() {
		PythonModule mathModule = new PythonModule("math", ImportType.FROM);
		PythonModule mathFunctions = new AllModules(mathModule);
		
		List<String> expectedCalls = new ArrayList<String>(0);
		
		List<String> calls = mathFunctions.callsTo("sin", "a = otherModule.sin(2)");
		assertEquals("Calls returned when abscent.", expectedCalls, calls);
	}
	
	@Test
	public void testToString() {
		String expected = "module.*";
		assertEquals("Wrong string representation returned for AllModules object!",
				expected, module.toString());
	}
}
