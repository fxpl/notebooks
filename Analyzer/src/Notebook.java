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
	String path;
	
	public Notebook(File file)
			throws IOException, ParseException {
		codeCells = new ArrayList<JSONObject>();
		this.path = file.getAbsolutePath();
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
	 * Extract and store all code cells
	 * @param notebook JSONObject parsed from input file
	 */
	private void extractCodeCells(JSONObject notebook) {
		JSONArray cells = getCellArray(notebook);
		for (int i=0; i<cells.size(); i++) {
			JSONObject cell = (JSONObject) cells.get(i);
			String type = (String) cell.get("cell_type");
			if (type.equals("code")) {
				codeCells.add(cell);
			}
		}
	}

	/**
	 * Extract cell array from notebook. Handles cell array on the top level
	 * but also an array of worksheets, each containing a cell array.
	 * @param notebook Notebook/worksheet to extract cells from
	 * @return Array containing all cells of the notebook
	 */
	private JSONArray getCellArray(JSONObject notebook) {
		JSONArray cells;
		if (notebook.containsKey("cells")) {
			// According to spec
			cells = (JSONArray) notebook.get("cells");
		} else {
			cells = new JSONArray();
			if(notebook.containsKey("worksheets")) {
				// Not according to spec, but occuring
				JSONArray worksheets = (JSONArray) notebook.get("worksheets");
				for (int i=0; i<worksheets.size(); i++) {
					JSONArray worksSheetCells = getCellArray((JSONObject) worksheets.get(i));
					cells.addAll(worksSheetCells);
				}
			} else {
				System.err.println("No cells found in " + notebook.toString() +
						" (" + this.path + ")!");
			}
		}
		return cells;
	}
}
