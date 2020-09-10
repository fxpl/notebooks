package notebooks;

import static org.junit.Assert.*;

import java.util.Objects;

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
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		assertFalse("String considered equal to Quantity.", quantity.equals("hej"));
	}
	
	@Test
	public void testEquals_equal() {
		Quantity other = new Quantity(name, count);
		assertTrue("Equal quantities considered different.", quantity.equals(other));
	}
	
	@Test
	public void testEquals_diffName() {
		Quantity other = new Quantity("otherName", count);
		assertFalse("Quantities with different names considered equal.", quantity.equals(other));
	}
	
	@Test
	public void testEquals_diffCount() {
		Quantity other = new Quantity(name, 119);
		assertFalse("Quantities with different count considered equal.", quantity.equals(other));
	}
	
	@Test
	public void testGetIdentifier() {
		assertEquals("Wrong name returned for quantity.", name, quantity.getIdentifier());
	}
	
	@Test
	public void testHashCode() {
		int expectedHashCode = Objects.hash(name, count);
		assertEquals("Wrong hash code retured for quantity.",
				expectedHashCode, quantity.hashCode());
	}
	
	@Test
	public void testToString() {
		String expected = name + ": " + count;
		assertEquals("Wrong string representation of quantity!", expected, quantity.toString());
	}
	
	@Test
	public void testToCsvString() {
		String expected = name + ", " + count;
		assertEquals("Wrong CSV string representation of quantity!", expected, quantity.toCsvString());
	}
}
