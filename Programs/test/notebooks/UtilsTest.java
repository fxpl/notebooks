package notebooks;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class UtilsTest {
	
	/**
	 * Verify that addOrIncrease adds an entry with the specified value when
	 * the key is not in the map and increases its value with the specified
	 * number if it exists.
	 */
	@Test
	public void testAddOrIncrease() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String key1 = "key1";
		String key2 = "key2";
		Utils.addOrIncrease(map, key1, 5);
		assertEquals("Wrong value stored for new key.", new Integer(5), map.get(key1));
		Utils.addOrIncrease(map, key1, 3);
		assertEquals("Value increased incorrectly for existing key.", new Integer(8), map.get(key1));
		Utils.addOrIncrease(map, key2, 9);
		assertEquals("Existing key modified when new key was added.", new Integer(8), map.get(key1));
		assertEquals("Wrong value stored for new key.", new Integer(9), map.get(key2));
	}
	
	/**
	 * Verify that the addOrIncrease that does not take an increase value as
	 * argument behaves as the three parameter addOrIncrease but with the
	 * increase value 1.
	 */
	@Test
	public void testAddOrIncrease1() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String key1 = "key1";
		String key2 = "key2";
		Utils.addOrIncrease(map, key1);
		assertEquals("Wrong value stored for new key.", new Integer(1), map.get(key1));
		Utils.addOrIncrease(map, key1);
		assertEquals("Value increased incorrectly for existing key.", new Integer(2), map.get(key1));
		Utils.addOrIncrease(map, key2);
		assertEquals("Existing key modified when new key was added.", new Integer(2), map.get(key1));
		assertEquals("Wrong value stored for new key.", new Integer(1), map.get(key2));
	}
	
	/**
	 * Verify that the median is computed correctly for an odd number of
	 * numbers.
	 */
	@Test
	public void testMedian_odd() {
		List<Integer> numbers = new ArrayList<Integer>(3);
		numbers.add(9);
		numbers.add(2);
		numbers.add(5);
		assertEquals("Wrong median returned for odd number of numbers", 5, Utils.median(numbers, "Mediantest"));
	}
	
	/**
	 * Verify that the median is computed correctly for an even number of
	 * numbers.
	 */
	@Test
	public void testMedian_even() {
		List<Integer> numbers = new ArrayList<Integer>(4);
		numbers.add(22);
		numbers.add(57);
		numbers.add(20);
		numbers.add(7);
		assertEquals("Wrong median returned for even number of numbers", 21, Utils.median(numbers, "Mediantest"));
	}
	
	/**
	 * Verify that median returns 0 for an empty list.
	 */
	@Test
	public void testMedian_emptyList() {
		List<Integer> numbers = new ArrayList<Integer>();
		assertEquals("Wrong median returned for empty list", 0, Utils.median(numbers, "Mediantest"));
	}
	
	/**
	 * Verify that timeStamp precedes the specified message with
	 * ">>> (<seconds since program start> s) "
	 */
	@Test
	public void testTimeStamp() {
		final String msg = "Message";
		final String expected = ">>> \\([0-9]+ s\\) " + msg;
		final String actual = Utils.timeStamped(msg);
		final String errorMessage = "Incorrectly time stamped string. Expected \""
				+ expected + "\", but was \"" + actual + "\"";
		assertTrue(errorMessage, actual.matches(expected));
	}
}
