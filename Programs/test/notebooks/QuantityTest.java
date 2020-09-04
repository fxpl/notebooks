package notebooks;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class QuantityTest {
	private Quantity quantity;
	private String name = "nb_100.ipynb";
	private int count = 92;
	
	@Before
	public void setUp() {
		quantity = new Quantity(name, count);
	}
	
	@Test
	public void testCompare() {
		Quantity same = new Quantity("nb_101.ipynb", count);
		Quantity larger = new Quantity("nb_102.ipynb", 10456);
		assertEquals("Equal quantities considered different!", 0, quantity.compareTo(same));
		assertTrue("Smaller quantity not identified!", quantity.compareTo(larger) < 0);
		assertTrue("Larger quantity not identified!", larger.compareTo(quantity) > 0);
	}
	
	@Test
	public void testToString() {
		String expected = name + ": " + count;
		assertEquals("Wrong string representation of quantity!", expected, quantity.toString());
	}
}
