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
	
	private String path;
	
	public Notebook(String path) {
		this.path = path;
	}
	
	/**
	 * @return Number of code cells in notebook
	 * @throws NotebookException if the file could not be parsed 
	 */
	public int numCodeCells() throws NotebookException {
		int numCodeCells = 0;
		Reader reader;
		try {
			reader = new FileReader(this.path);
		} catch (FileNotFoundException e) {
			throw new NotebookException("Could not read " + this.path + ": " + e.getMessage());
		}
		JSONObject notebook;
		try {
			notebook = (JSONObject)new JSONParser().parse(reader);
		} catch (IOException | ParseException e) {
			throw new NotebookException("Could not parse " + this.path + ": " + e.getMessage());
		}
		
		JSONArray cells = getCellArray(notebook);
		for (int i=0; i<cells.size(); i++) {
			JSONObject cell = (JSONObject) cells.get(i);
			String type = (String) cell.get("cell_type");
			if (type.equals("code")) {
				numCodeCells++;
			}
		}
		return numCodeCells;
	}
	
	/**
	 * Extract cell array from notebook. Handles cell array on the top level
	 * but also an array of worksheets, each containing a cell array.
	 * @param notebook Notebook/worksheet to extract cells from
	 * @return Array containing all cells of the notebook
	 */
	@SuppressWarnings("unchecked")	// The JSON library uses raw types internally
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
