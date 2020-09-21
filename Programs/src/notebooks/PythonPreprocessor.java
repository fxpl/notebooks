package notebooks;
import java.util.ArrayList;

import java.util.List;

import org.json.JSONArray;

public class PythonPreprocessor {
	JSONArray input;
	List<String> processed;
	
	public PythonPreprocessor(JSONArray input) {
		this.input = input;
		processed = new ArrayList<String>();
	}
	
	/**
	 * @param code A piece of code to remove strings from
	 * @param delimiters Existing string delimiters
	 * @return A copy of code, with all strings removed
	 */
	public String removeStrings(String code, String[] delimiters) {
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
			for (int i=0; i<numDelimiters; i++) {
				if (!otherTrue(inString, i) && delimiterPositions.get(i).contains(index) && previous != '\\') {
					inString[i] = !inString[i];
					index += delimiters[i].length(); // Skip delimiter
					if (!inString[i] && code.charAt(index) != '\n') {
						result += ";";
					}
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
		return result;
	}
	
	/**
	 * @param str String to check
	 * @param pos An index in the string
	 * @param seq Char sequence to look for
	 * @return true if the specified char sequence can be found at position pos in str
	 */
	private boolean charSequenceFound(String str, int pos, String seq) {
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
	private boolean anyTrue(boolean[] bools) {
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
	private boolean otherTrue(boolean[] bool, int current) {
		for (int i=0; i<bool.length; i++) {
			if (bool[i] && i != current) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove comments starting with '#'. (Strings are expected to be removed
	 * already!)
	 * @param code A piece of code to remove comments from
	 * @return A copy of code, with all comments removed
	 */
	public String removeComments(String code) {
		String result = "";
		int index = 0;
		char[] chars = code.toCharArray();
		while (index < chars.length) {
			char c = chars[index];
			if ('#' == c) {
				while ('\n' != chars[++index]);
			}
			result += chars[index++];
		}
		return result;
	}

	/**
	 * Remove strings from input, and split all elements at newline and ';'.
	 * @return A list of sublines
	 */
	public List<String> process() {
		// TODO: Bryt ut metod
		String code = "";
		for (int i=0; i<input.length(); i++) {
			code += input.getString(i);
		}
		
		code = removeStrings(code, new String[]{"\"\"\"", "'''"});
		code = removeStrings(code, new String[]{"\"", "'"});
		code = removeComments(code);

		// TODO: Bryt ut metod
		String[] lines = code.split("\n");
		for (String line: lines) {
			line += "\n";
			String[] statements = line.split(";");
			for (String statement: statements) {
				processed.add(statement);
			}
		}
		return processed;
	}
}
