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
		boolean escaped = false;
		while (index < code.length()-1) {
			if ('\\' == code.charAt(index)) {
				escaped = !escaped;
			} else {
				escaped = false;
			}
			if (escaped && '\n' == code.charAt(index+1)) {
				index += 2;
				result += ' ';
			} else {
				result += code.charAt(index++);
			}
		}
		code = result;
	}
	
	/**
	 * Remove everything that should only be removed when it is not inside a
	 * string, that is comments (starting with '#') and the newlines that occur
	 * between brackets, from the code handled by the preprocessor.
	 * @param delimiters Existing string delimiters. I a delimiter a is a substring of a delimiter b, b must precede a in the array.
	 */
	public void removeOutsideString(String[] delimiters) {
		CodeState state = new CodeState(code, delimiters);
		
		// Do the actual cleaning
		String result = "";
		int bracketLevel = 0;
		while (state.inCode()) {
			char current = state.currentChar();
			if (!state.inString()) { // We are not inside a string. Do clean.
				if('#' == current && !state.escaped()) {
					state.stepPast('\n');
				} else if ('(' == current) {
					bracketLevel++;
				} else if (')' == current) {
					bracketLevel--;
				} else if ('\n' == current && 0 < bracketLevel) {
					state.stepPast();
					continue;
				}
			}
			result += state.currentChar();
			state.step();
		}
		code = result;
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
		removeOutsideString(new String[]{"\"\"\"", "'''", "\"", "'"});
		removeEscapedNewLines();
		splitCode();
		return processed;
	}
}
