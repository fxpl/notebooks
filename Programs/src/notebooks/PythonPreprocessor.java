package notebooks;
import java.util.ArrayList;

import java.util.List;

import org.json.JSONArray;

public class PythonPreprocessor {
	String code = "";
	List<String> processed;
	
	public PythonPreprocessor(JSONArray input) {
		mergeStrings(input);
		processed = new ArrayList<String>();
	}
	
	/**
	 * Merge all strings the JSON array given as argument. Store the result in
	 * the instance variable 'code'.
	 */
	private void mergeStrings(JSONArray strings) {
		for (int i=0; i<strings.length(); i++) {
			code += strings.getString(i);
		}
	}
	
	/**
	 * Remove all escaped newlines from the code handled by the preprocessor
	 */
	public void removeEscapedNewLines() {
		String result = "";
		int index = 0;
		while (index < code.length()-1) {
			if ('\\' == code.charAt(index) && '\n' == code.charAt(index+1)) {
				index += 2;
				result += ' ';
			} else {
				result += code.charAt(index++);
			}
		}
		code = result;
	}
	
	/**
	 * Remove all newlines that occur between brackets in the code handled by
	 * the preprocessor.
	 */
	public void removeNewlinesInBrackets() {
		String result = "";
		int bracketLevel = 0;
		int index = 0;
		// Comments and strings are already removed
		while (index < code.length()) {
			if ('(' == code.charAt(index)) {
				bracketLevel++;
			} else if (')' == code.charAt(index)) {
				bracketLevel--;
			} else if ('\n' == code.charAt(index) && 0 < bracketLevel) {
				index++;
				continue;
			}
			result += code.charAt(index++);
		}
		code = result;
	}
	
	/**
	 * Remove all strings and comments from the code handled by the
	 * preprocessor.
	 * @param delimiters Existing string delimiters
	 */
	public void removeStringsAndComments(String[] delimiters) {
		// Find positions of delimiters
		int numDelimiters = delimiters.length;
		List<List<Integer>> delimiterPositions = new ArrayList<List<Integer>>(numDelimiters);
		for (int i=0; i<numDelimiters; i++) {
			delimiterPositions.add(new ArrayList<Integer>());
		}
		for (int i=0; i<code.length(); i++) {
			for (int d=0; d<numDelimiters; d++) {
				if (charSequenceFound(code, i, delimiters[d])) {
					delimiterPositions.get(d).add(i);
				}
			}
		}
		
		// Remove strings
		String result = "";
		int index = 0;
		boolean[] inString = new boolean[numDelimiters];
		for (int i=0; i<numDelimiters; i++) {
			inString[i] = false;
		}
		char previous = ' ';
		while (index < code.length()) {
			char current = code.charAt(index);
			boolean isDelimiter = false;
			// Skip comments
			if (!anyTrue(inString) && '#' == current) {
				while ('\n' != code.charAt(++index));
			}
			// Remove strings
			for (int i=0; i<numDelimiters; i++) {
				if (!otherTrue(inString, i) && delimiterPositions.get(i).contains(index) && previous != '\\') {
					inString[i] = !inString[i];
					index += delimiters[i].length(); // Skip delimiter
					isDelimiter = true;
				}
			}
			if (!isDelimiter) {
				if (!anyTrue(inString)) {
					result += code.charAt(index);
				}
				index++;
			}
			previous = current;
		}
		code = result;
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
	
	/**
	 * Split a the code handled by the preprocessor at newlines and semicolons.
	 * Keep newlines, but not semicolons.
	 */
	private void splitCode() {
		String[] lines = code.split("\n");
		for (String line: lines) {
			line += "\n";
			String[] statements = line.split(";");
			for (String statement: statements) {
				processed.add(statement);
			}
		}
	}

	/**
	 * Remove strings from input, and split all elements at newline and ';'.
	 * @return A list of sub lines
	 */
	public List<String> process() {
		removeStringsAndComments(new String[]{"\"\"\"", "'''"});
		removeStringsAndComments(new String[]{"\"", "'"});
		removeEscapedNewLines();
		removeNewlinesInBrackets();

		splitCode();
		return processed;
	}
}
