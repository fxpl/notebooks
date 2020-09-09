package notebooks;

import static org.junit.Assert.assertEquals;
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
	public void testGetName() {
		assertEquals("Wrong name retued for AllModules instance!", "*", module.getName());
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
	
	@Test
	public void testToString() {
		String expected = "module.*";
		assertEquals("Wrong string representation returned for AllModules object!",
				expected, module.toString());
	}
}
