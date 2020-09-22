package notebooks;
import java.util.ArrayList;

import java.util.List;

import org.json.JSONArray;

public class PythonPreprocessor {
	// TODO: Är det rimligt att ha dessa som instansvariabler?
	JSONArray input;
	List<String> processed;
	
	public PythonPreprocessor(JSONArray input) {
		this.input = input;
		processed = new ArrayList<String>();
	}
	
	/**
	 * Merge all strings in input
	 * @return A single string containing all strings in input, in order
	 */
	public String mergeInputStrings() {
		String code = "";
		for (int i=0; i<input.length(); i++) {
			code += input.getString(i);
		}
		return code;
	}
	
	/**
	 * Remove all escaped newlines from code
	 * @param code A piece of code to remove escaped newlines from
	 * @return A copy of the code with all escaped newlines removed
	 */
	public String removeEscapedNewLines(String code) {
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
		return result;
	}
	
	/**
	 * Remove all newlines that occur between brackets
	 * @param code A piece of code to remove newlines from
	 * @return A copy of code with newlines between brackets removed
	 */
	public String removeNewlinesInBrackets(String code) {
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
		return result;
	}
	
	/**
	 * @param code A piece of code to remove strings and comments from
	 * @param delimiters Existing string delimiters
	 * @return A copy of code, with all strings and comments removed
	 */
	public String removeStringsAndComments(String code, String[] delimiters) {
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
	 * Split a piece of code at newlines and semicolons. Put each substring in
	 * the list processed. Keep newlines, but not semicolons.
	 * @param code A piece of code to split
	 */
	public void splitCode(String code) {
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
	 * @return A list of sublines
	 */
	public List<String> process() {
		String code = mergeInputStrings();
		
		code = removeStringsAndComments(code, new String[]{"\"\"\"", "'''"});
		code = removeStringsAndComments(code, new String[]{"\"", "'"});
		code = removeEscapedNewLines(code);
		code = removeNewlinesInBrackets(code);

		splitCode(code);
		return processed;
	}
}
