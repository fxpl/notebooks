package notebooks;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class UtilsTest {
	
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
