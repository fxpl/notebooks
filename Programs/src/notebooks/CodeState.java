package notebooks;

import java.util.ArrayList;
import java.util.List;

public class CodeState {
	String code;
	String[] delimiters;
	List<List<Integer>> delimiterPositions;
	boolean[] inString;
	boolean escaped;
	int index;
	
	public CodeState(String code, String[] delimiters) {
		this.code = code;
		this.delimiters = delimiters;
		delimiterPositions = delimiterPositions();
		
		escaped = false;
		inString = new boolean[delimiters.length];
		for (int i=0; i<delimiters.length; i++) {
			inString[i] = false;
		}
		index = 0;
	}
	
	/**
	 * @return Current character in code
	 */
	public char currentChar() {
		return code.charAt(index);
	}
	
	/**
	 * @return True if the current char is escaped, false otherwise
	 */
	public boolean escaped() {
		return escaped;
	}
	
	/**
	 * @return False if we have reached the end of the code, true otherwise
	 */
	public boolean inCode() {
		return index < code.length();
	}
	
	/**
	 * @return True if we are inside a string, false otherwise
	 */
	public boolean inString() {
		return anyTrue(inString);
	}
	
	/**
	 * Step to the next character (and update state accordingly)
	 */
	public void step() {
		// Keep track of whether we are inside a string
		for (int i=0; i<delimiters.length; i++) {
			if (!otherTrue(inString, i) && delimiterPositions.get(i).contains(index) && !escaped) {
				inString[i] = !inString[i];
			}
		}
		
		if ('\\' == code.charAt(index)) {
			escaped = !escaped;
		} else {
			escaped = false;
		}
		stepPast();
	}
	
	/**
	 * Step to the next character without updating the state
	 */
	public void stepPast() {
		index++;
	}
	
	/**
	 * Step to the character following the next instance of the specified
	 * character, without updating the state.
	 * @param character Character to step past
	 */
	public void stepPast(char character) {
		while (character != code.charAt(++index));
	}
	
	/**
	 * @param bools Array of booleans
	 * @return true if any of the values in the argument is true
	 */
	private static boolean anyTrue(boolean[] bools) {
		for (boolean inside: bools) {
			if (inside) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param str String to check
	 * @param pos An index in the string
	 * @param seq Char sequence to look for
	 * @return true if the specified char sequence can be found at position pos in str
	 */
	private static boolean charSequenceFound(String str, int pos, String seq) {
		for (int i=0; i<seq.length(); i++) {
			if (pos+i >= str.length() || str.charAt(pos+i) != seq.charAt(i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Find all positions of all delimiters. Return as a list containing one
	 * list per delimiter. These lists contain all indices on which the actual
	 * delimiter is located.
	 * @return
	 */
	private List<List<Integer>> delimiterPositions() {
		int numDelimiters = delimiters.length;
		List<List<Integer>> result = new ArrayList<List<Integer>>(numDelimiters);
		for (int i=0; i<numDelimiters; i++) {
			result.add(new ArrayList<Integer>());
		}
		for (int i=0; i<code.length(); i++) {
			for (int d=0; d<numDelimiters; d++) {
				if (charSequenceFound(code, i, delimiters[d])) {
					result.get(d).add(i);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * @param bools Array of booleans
	 * @param current Value to skip
	 * @return true if any of the values in bool, except bool[current], is true
	 */
	private static boolean otherTrue(boolean[] bool, int current) {
		for (int i=0; i<bool.length; i++) {
			if (bool[i] && i != current) {
				return true;
			}
		}
		return false;
	}

}
