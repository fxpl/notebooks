import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * A Jupyter notebook.
 */
public class Notebook {
	// Enum for language
	private enum Language {
		PYTHON, JULIA, R, SCALA, OTHER, UNKNOWN
	}
	
	public Notebook(File file)
			throws FileNotFoundException, IOException, ParseException {
		Reader reader = new FileReader(file);
		JSONParser parser = new JSONParser();
		JSONObject data = (JSONObject) parser.parse(reader);
	}
}
