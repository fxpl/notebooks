package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class AllModulesTest {
	private PythonModule module;
	private final PythonModule parent = new PythonModule("module");
	
	@Before
	public void setUp() {
		module = new AllModules(parent);
	}
	
	@Test
	public void testGetName() {
		assertEquals("Wrong name retued for AllModules instance!", "*", module.getName());
	}
	
	@Test
	public void testAlias() {
		assertNull("Alias set for AllModules instance!", module.alias());
	}
	
	@Test
	public void testGetParent() {
		assertEquals("Wrong parent returned from AllModules instance!",
				parent, module.getParent());
	}
	
	@Test
	public void testImportedWith() {
		assertEquals("AllModules objects not imported with FROM statment!",
				ImportType.FROM, module.importedWith());
	}
}
