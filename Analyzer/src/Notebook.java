import java.io.*;
import java.util.ArrayList;

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
	
	private ArrayList<JSONObject> codeCells;
	
	public Notebook(File file)
			throws IOException, ParseException {
		codeCells = new ArrayList<JSONObject>();
		Reader reader = new FileReader(file);
		JSONParser parser = new JSONParser();
		JSONObject notebook = (JSONObject) parser.parse(reader);
		extractCodeCells(notebook);
	}
	
	/**
	 * @return Number of code cells in notebook
	 */
	public int numCodeCells() {
		return codeCells.size();
	}
	
	/**
	 * Store all code cells
	 * @param notebook JSONObject parsed from input file
	 */
	private void extractCodeCells(JSONObject notebook) {
		JSONArray cells = (JSONArray) notebook.get("cells");
		for (int i=0; i<cells.size(); i++) {
			JSONObject cell = (JSONObject) cells.get(i);
			String type = (String) cell.get("cell_type");
			if (type.equals("code")) {
				codeCells.add(cell);
			}
		}
	}
}
