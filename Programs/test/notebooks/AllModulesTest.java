package notebooks;

import static org.junit.Assert.assertEquals;
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
	public void testImportedWith() {
		assertEquals("AllModules objects not imported with FROM statment!",
				ImportType.FROM, module.importedWith());
	}
	
	@Test
	public void testConstructor() {
		// Check parent and alias via string representation.
		String expectedStringRepr = parent.toString() + ".*";
		assertEquals("Wrong parent, alias or name!", expectedStringRepr, module.toString());
	}
}
