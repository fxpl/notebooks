package notebooks;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

public class Analyzer {
	protected String outputDir = ".";

	/**
	 * Get the part of arg located after the (first) '=' sign. If the '=' is
	 * missing, print an error message and return an empty string.
	 */
	protected String getValueFromArgument(String arg) {
		int eqIndex = arg.indexOf('=');
		if (-1 == eqIndex) {
			System.err.println("Argument " + arg + " must be followed by '=' and value!");
			return "";
		} else {
			return arg.substring(eqIndex + 1);
		}
	}
	
	/**
	 * @return Header for the cloneFrequency csv file
	 */
	protected static String cloneFrequencyHeader() {
		return "file, unique, clones, empty, clone frequency, non-empty clone frequency, "
				+ "intra clones, non-empty intra clones\n";
	}
	
	/**
	 * Print one line (=info for one notebook) to the clone frequency output file
	 * @param writer Writer that writes to the clone frequency output file
	 * @param notebook Name of actual notebook
	 * @param numClones Number of clones in notebook
	 * @param numUnique Number of unique snippets in the notebook
	 * @param numEmpty Number of empty snippets in the notebook
	 * @param numIntra Number of intra notebook connections
	 * @param numIntraNE Number of intra notebook connections, empty snippet excluded
	 */
	protected void printCloneFrequencyLine(Writer writer, String notebook, int numClones,
			int numUnique, int numEmpty, int numIntra, int numIntraNE) throws IOException {
		writer.write(notebook + ", " + numUnique + ", " + numClones + ", " + numEmpty + ", ");
		int numSnippets = numClones + numUnique;
		int numSnippetsNE = numSnippets - numEmpty;
		if (0 != numSnippets) {
			double cloneFrequency = (double)numClones / numSnippets;
			writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
		} else {
			writer.write("0, ");
		}
		if (0 != numSnippetsNE) {
			double cloneFrequency = (double)numClones / numSnippetsNE;
			writer.write(String.format(Locale.US, "%.4f", cloneFrequency) + ", ");
		} else {
			writer.write("0, ");
		}
		writer.write(numIntra + ", " + numIntraNE + "\n");
	}
}