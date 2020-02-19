package notebooks;

public class Utils {
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
