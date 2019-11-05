import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.*;
import org.json.simple.parser.*;

/**
 * A Jupyter notebook.
 */
public class Notebook {
	private String path;
	private int locTotal;		// Total number of lines of code
	private int locBlank;		// Number of empty code lines
	private int locContents;	// Number of non-empty code lines
	private boolean locCounted = false;
	private LangSpec languageSpecIn;
	
	public Notebook(String path) {
		this.path = path;
	}
	
	/**
	 * @return The file name of the notebook, without preceding path
	 */
	public String getName() {
		int namePos = path.lastIndexOf('/') + 1;
		return path.substring(namePos);
	}

	/**
	 * @return Array containing the hash of the source code stored in each code cell
	 */
	public String[] hashes() throws NotebookException, NoSuchAlgorithmException {
		MessageDigest hasher = MessageDigest.getInstance("MD5");
		List<JSONObject> codeCells = getCodeCells();
		int numSnippets = codeCells.size();
		String[] hashes = new String[numSnippets];
		for (int i=0; i<numSnippets; i++) {
			String snippet = "";
			JSONArray lines = getSource(codeCells.get(i));
			for (int j=0; j<lines.size(); j++) {
				snippet += lines.get(j);
			}
			snippet = snippet.replaceAll("\\s", "");
			hashes[i] = DatatypeConverter.printHexBinary(hasher.digest(snippet.getBytes()));
		}
		return hashes;
	}
	
	/**
	 * @return The language of the notebook
	 */
	public Language language() throws NotebookException {
		JSONObject notebook = this.getNotebook();
		this.languageSpecIn = LangSpec.NONE;
		Language language = getLanguageFromMetadata(notebook);
		if (Language.UNKNOWN == language) {
			language = getLanguageFromCodeCells(notebook);
		}
		if (Language.UNKNOWN == language) {
			System.err.println("No language found in " + this.path);
		}
		return language;
	}
	
	/**
	 * @return Identifier of the location from which the language is extracted
	 */
	public LangSpec langSpec() throws NotebookException {
		if (null == languageSpecIn) {
			language();
		}
		return languageSpecIn;
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
		return getCodeCells().size();
	}
	
	
	/**
	 * Count lines of code and set all loc variables.
	 * @throws NotebookException if the file could not be parsed
	 */
	private void countLines() throws NotebookException {
		List<JSONObject> codeCells = getCodeCells();
		locTotal = 0;
		for (JSONObject cell: codeCells) {
			// Get source code
			JSONArray source = getSource(cell);
			// If source code exists, count lines
			// TODO: Extract method(?)
			if (null != source) {
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
				System.err.println("Keys \"source\" and \"input\" are missing in a cell in " + this.path);
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
	private static JSONArray getCellArray(JSONObject notebook) {
		JSONArray cells;
		if (notebook.containsKey("cells")) {
			// According to spec
			cells = (JSONArray) notebook.get("cells");
		} else {
			cells = new JSONArray();
		}
		if(notebook.containsKey("worksheets")) {
			// Not according to spec, but occurring
			JSONArray worksheets = (JSONArray) notebook.get("worksheets");
			for (int i=0; i<worksheets.size(); i++) {
				JSONArray worksSheetCells = getCellArray((JSONObject) worksheets.get(i));
				cells.addAll(worksSheetCells);
			}
		}
		return cells;
	}
	
	/**
	 * @return A list containing all code cells in the notebook represented by this object
	 */
	private List<JSONObject> getCodeCells() throws NotebookException {
		JSONObject notebook = this.getNotebook();
		return getCodeCells(notebook);
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
				System.err.println("Key \"cell_type\" is missing in a cell in " + this.path);
			}
		}
		return result;
	}
	
	/**
	 * Translate a string specifying the language to a Language value.
	 * @param spec String specifying the language
	 * @return The corresponding Language
	 */
	private static Language getLanguage(String spec) {
		if (spec.equals("julia") || spec.equals("Julia")) {
			return Language.JULIA;
		} else if (spec.startsWith("python") || spec.startsWith("Python")) {
			return Language.PYTHON;
		} else if (spec.equals("R") || spec.equals("r")) {
			return Language.R;
		} else if (spec.startsWith("scala") || spec.startsWith("Scala")) {
			return Language.SCALA;
		} else {
			return Language.OTHER;
		}
	}

	/**
	 * @param notebook Notebook to extract language from
	 * @return The language specified in code cells, if it exists and is consistent. UNKNOWN otherwise.
	 */
	private Language getLanguageFromCodeCells(JSONObject notebook) {
		List<JSONObject> codeCells = getCodeCells(notebook);
		if (0 < codeCells.size()) {
			String language = "";
			JSONObject cell = codeCells.get(0);
			if (cell.containsKey("language")) {
				languageSpecIn = LangSpec.CODE_CELLS;
				language = (String) cell.get("language");
				for (int i=1; i<codeCells.size(); i++) {
					cell = codeCells.get(i);
					if (!cell.containsKey("language") || !((String)cell.get("language")).equals(language)) {
						System.err.println("Ambiguous language in " + this.path);
						return Language.UNKNOWN;
					}
				}
				return getLanguage(language);
			}
		}
		return Language.UNKNOWN;
	}

	/**
	 * @param notebook Notebook to extract language from
	 * @return The language specified in metadata, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromMetadata(JSONObject notebook) {
		Language language = Language.UNKNOWN;
		if (notebook.containsKey("metadata")) {
			JSONObject metadata = (JSONObject) notebook.get("metadata");
			language = getLanguageFromLanguage(metadata);
			if (Language.UNKNOWN == language) {
				language = getLanguageFromLanguageinfo(metadata);
			}
			if (Language.UNKNOWN == language) {
				language = getLanguageFromKernelspec(metadata);
			}
		} else {
			System.err.println("No metadata in " + this.path);
		}
		return language;
	}

	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->kernelspec. UNKNOWN otherwise.
	 */
	private Language getLanguageFromKernelspec(JSONObject metadata) {
		if (metadata.containsKey("kernelspec")) {
			JSONObject kernelspec = (JSONObject)metadata.get("kernelspec");
			if (kernelspec.containsKey("language")) {
				languageSpecIn = LangSpec.METADATA_KERNELSPEC_LANGUAGE;
				return getLanguage((String) kernelspec.get("language"));
			}
			if (kernelspec.containsKey("name")) {
				languageSpecIn = LangSpec.METADATA_KERNELSPEC_NAME;
				return getLanguage((String)kernelspec.get("name"));
			}
		}
		return Language.UNKNOWN;
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromLanguage(JSONObject metadata) {
		if (metadata.containsKey("language")) {
			languageSpecIn = LangSpec.METADATA_LANGUAGE;
			return getLanguage((String)metadata.get("language"));
		}
		return Language.UNKNOWN;
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language_info, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromLanguageinfo(JSONObject metadata) {
		if (metadata.containsKey("language_info")) {
			JSONObject languageinfo = (JSONObject)metadata.get("language_info");
			if (languageinfo.containsKey("name")) {
				languageSpecIn = LangSpec.METADATA_LANGUAGEINFO_NAME;
				return getLanguage((String)languageinfo.get("name"));
			}
		}
		return Language.UNKNOWN;
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
	
	/**
	 * @param cell Code cell to fetch source code from
	 * @return The source code stored in cell (with each line as a separate element), null if source code is missing 
	 */
	private static JSONArray getSource(JSONObject cell) {
		JSONArray source = null;
		if (cell.containsKey("source")) {
			source = (JSONArray) cell.get("source");
		} else if (cell.containsKey("input")) {
			source = (JSONArray) cell.get("input");
		}
		return source;
	}
}
