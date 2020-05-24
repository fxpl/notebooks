package notebooks;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class Analyzer {
	protected String outputDir = ".";

	/**
	 * Create a map from notebook name to repro.
	 * @param fileName Name of file with mapping from notebook number to repro
	 * @return The map from notebook name to repro
	 */
    protected static Map<String, String> createReproMap(String fileName)
        throws FileNotFoundException {
        Map<String, String> result = new HashMap<String, String>();
        try {
            BufferedReader input = new BufferedReader(new FileReader(new File(fileName)));

            while (true) {
                final String line = input.readLine();
                if (line == null) break;
                
                String[] subStrings = line.split(",");
                try {
                    int notebookNumber = Integer.parseInt(subStrings[0]);
                    StringBuilder sb = new StringBuilder();
                    sb.append("nb_");
                    sb.append(notebookNumber);
                    sb.append(".ipynb");
                    String notebookName = sb.toString();
                    String reproName = subStrings[1];
                    result.put(notebookName, reproName);
                } catch (NumberFormatException e) {
                    System.err.println("Notebook numbers in repro file must be integers! Notebook with \"number\" '"
                                       + subStrings[0] + "' is excluded from mapping!");
                }
            }

            input.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return result;
    }
	
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
}
