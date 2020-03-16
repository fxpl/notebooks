package notebooks;

import java.util.Collections;
import java.util.List;

public class Utils {
	/**
	 * Log to stdout if the minimum and the maximum values are different.
	 * Return the median.
	 * @param values to check and compute median from
	 * @param msg Specialized part of log message
	 * @return median of values
	 */
	public static int median(List<Integer> values, String msg) {
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
