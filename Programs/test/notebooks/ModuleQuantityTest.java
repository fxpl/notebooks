package notebooks;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ModuleQuantityTest {
	private ModuleQuantity moduleQuantity;
	private String name = "nb_100.ipynb";
	private int quantity = 92;	
	
	@Before
	public void setUp() {
		moduleQuantity = new ModuleQuantity(name, quantity);
	}
	
	@Test
	public void testCompare() {
		ModuleQuantity same = new ModuleQuantity("nb_101.ipynb", quantity);
		ModuleQuantity larger = new ModuleQuantity("nb_102.ipynb", 10456);
		assertEquals("Equal module quantities considered different!", 0, moduleQuantity.compareTo(same));
		assertTrue("Smaller quantity not identified!", moduleQuantity.compareTo(larger) < 0);
		assertTrue("Larger quantity not identified!", larger.compareTo(moduleQuantity) > 0);
	}
	
	@Test
	public void testToString() {
		String expected = name + ": " + quantity;
		assertEquals("Wrong string representation of ModuleQuantity!", expected, moduleQuantity.toString());
	}
}
