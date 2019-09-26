import java.io.*;
import java.util.*;

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
	private int locTotal;		// Total number of lines of code
	private int locBlank;		// Number of empty code lines
	private int locContents;	// Number of non-empty code lines
	private boolean locCounted = false;
	
	public Notebook(String path) {
		this.path = path;
	}
	
	/**
	 * @return Total lines of code for all code cells in the notebook
	 * @throws NotebookException if the notebook file could not be parsed
	 */
	public int LOC() throws NotebookException {
		if (!locCounted) {
			countLines();
		}
		return locTotal;
	}
	
	/**
	 * @return Number of blank code lines in the notebook
	 * @throws NotebookException if the notebook file could not be parsed
	 */
	public int LOCBlank() throws NotebookException {
		if (!locCounted) {
			countLines();
		}
		return locBlank;
	}
	
	/**
	 * @return Number of non-blank code lines in the notebook
	 * @throws NotebookException if the notebook file could not be parsed
	 */
	public int LOCNonBlank() throws NotebookException {
		if(!locCounted) {
			countLines();
		}
		return locContents;
	}
	
	/**
	 * @return Number of code cells in notebook
	 * @throws NotebookException if the file could not be parsed
	 */
	public int numCodeCells() throws NotebookException {
		JSONObject notebook = this.getNotebook();
		return getCodeCells(notebook).size();
	}
	
	
	/**
	 * Count lines of code and set all loc variables.
	 * @throws NotebookException if the file could not be parsed
	 */
	private void countLines() throws NotebookException {
		JSONObject notebook = this.getNotebook();
		List<JSONObject> codeCells = getCodeCells(notebook);
		locTotal = 0;
		for (JSONObject cell: codeCells) {
			if (cell.containsKey("source")) {
				JSONArray source = (JSONArray) cell.get("source");
				locTotal += source.size();
				for (int i=0; i<source.size(); i++) {
					String line = ((String)source.get(i)).trim();
					if ("".equals(line)) {
						locBlank++;
					} else {
						locContents++;
					}
				}
			} else {
				System.err.println("Key \"source\" is missing in a cell in "
						+ notebook.toString() + " (" + this.path + ")!");
			}
		}
		locCounted = true;
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
	
	/**
	 * @param notebook Notebook to extract code cells from
	 * @return A list containing all code cells in notebook
	 */
	private List<JSONObject> getCodeCells(JSONObject notebook) {
		JSONArray cells = getCellArray(notebook);
		List<JSONObject> result = new ArrayList<JSONObject>();
		for (int i=0; i<cells.size(); i++) {
			JSONObject cell = (JSONObject) cells.get(i);
			if (cell.containsKey("cell_type")) {
				String type = (String) cell.get("cell_type");
				if (type.equals("code")) {
					result.add(cell);
				}
			} else {
				System.err.println("Key \"cell_type\" is missing in a cell in "
						+ notebook.toString() + " (" + this.path + ")!");
			}
		}
		return result;
	}
	
	/**
	 * @return A JSONObject containing the contents of the notebook
	 * @throws NotebookException If the file this.path could not be parsed
	 */
	private JSONObject getNotebook() throws NotebookException {
		Reader reader;
		JSONObject result;
		try {
			reader = new FileReader(this.path);
		} catch (FileNotFoundException e) {
			throw new NotebookException("Could not read " + this.path + ": " + e.toString());
		}
		try {
			result = (JSONObject)new JSONParser().parse(reader);
		} catch (IOException | ParseException e) {
			throw new NotebookException("Could not parse " + this.path + ": " + e.toString());
		}
		try {
			reader.close();
		} catch (IOException e) {
			System.err.println("Warning: Could not close reader of " + this.path + ": " + e.toString());
		}
		return result;
	}
}
