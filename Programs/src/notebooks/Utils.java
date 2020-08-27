package notebooks;

import java.util.Map;
import java.util.Collections;
import java.util.List;

public class Utils {
	private final static long startTime = System.currentTimeMillis();
	
	/**
	 * Print the given message to stdout, preceed by a time stamp.
	 * @param Heart beat message
	 */
	public static void heartBeat(String msg) {
		System.out.println(timeStamped(msg));
	}
	
	/**
	 * Add the string ">>> (T s)", where T is the elapsed time since program
	 * start, in seconds, at the beginning of the argument string
	 * @param msg String to preced by time stamp
	 * @return The resulting string
	 */
	public static String timeStamped(String msg) {
		StringBuilder sb = new StringBuilder();
		final long timeRun = (System.currentTimeMillis() - startTime) / 1000;
		sb.append(">>> (")
		.append(timeRun)
		.append(" s) ")
		.append(msg);
		return sb.toString();
	}
	
	/**
	 * Log to stdout if the minimum and the maximum values are different.
	 * Return the median.
	 * @param values to check and compute median from
	 * @param msg Specialized part of log message
	 * @return median of values, 0 if values is empty
	 */
	public static int median(List<Integer> values, String msg) {
		if (0 == values.size()) {
			System.err.println("Trying to compute median of empty list. Returning 0."
					+ " Line count message: " + msg);
			return 0;
		}
		Collections.sort(values);
		int min = values.get(0);
		int max = values.get(values.size()-1);
		if (min != max) {
			System.out.println(msg + ". Min: " + min + ". Max: " + max + ".");
		}
		int numValues = values.size();
		int median = (values.get(numValues/2) + values.get((numValues-1)/2))/2;
		return median;
	}
	
	/**
	 * If map contains a value for key, increase it with 1. Else add an entry
	 * with for key with the value 1.
	 * @param map Map to modify as stated above
	 * @param key Key for the entry that will be changed/added
	 */
	public static void addOrIncrease(Map<String, Integer> map, String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1);
		} else {
			map.put(key, 1);
		}
	}
	
	/**
	 * Convert a byte array to hexadecimal string
	 * @param bytes Byte array to convert to hex string
	 * @return Hex string representation of bytes
	 */
	public static String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int i=0; i<bytes.length; i++) {
			char hexRepr = Character.forDigit((bytes[i] >> 4) & 0xF, 16);
			hexChars[2*i] = Character.toUpperCase(hexRepr);
			hexRepr = Character.forDigit(bytes[i] & 0xF, 16);
			hexChars[2*i+1] = Character.toUpperCase(hexRepr);
		}
		return new String(hexChars);
	}
}
