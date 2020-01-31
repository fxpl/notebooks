package notebooks;

import java.io.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipOutputStream;

import java.util.zip.ZipEntry;
import java.util.*;

import org.json.*;

/**
 * A Jupyter notebook.
 */
public class Notebook {
	private String path;
	private String repro = "";
	private int locTotal;		// Total number of lines of code
	private int locBlank;		// Number of empty code lines
	private int locContents;	// Number of non-empty code lines
	private volatile boolean locCounted = false;
	private LangSpec languageSpecIn;
	private JSONObject contents;
	
	public Notebook(String path) {
		this(path, "");
	}
	
	public Notebook(String path, String repro) {
		this.path = path;
		this.repro = repro;
	}
	
	public Notebook(Notebook model) {
		this(model.path, model.repro);
	}
	
	/**
	 * @return true iff is a notebook with the same name as this
	 */
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass()) {
			return false;
		}
		Notebook otherNotebook = (Notebook)other;
		return this.getName().equals(otherNotebook.getName());
	}
	
	/**
	 * @return The file name of the notebook, without preceding path
	 */
	public String getName() {
		int namePos = path.lastIndexOf('/') + 1;
		return path.substring(namePos);
	}
	
	public String getRepro() {
		return repro;
	}
	
	public void setRepro(String reproName) {
		this.repro = reproName;
	}
	
	/**
	 * Reset the stored contents. This metod cat be used to decrease the
	 * memory load.
	 */
	public void clearContents() {
		contents = null;
	}
	
	/**
	 * Dump each code snippet to a separate file in the directory whose name is
	 * given as an argument of the method. Each file will be named
	 * <name>_<id>.<suffix> where <name> is the name of this notebook (except
	 * its suffix, if any), id is the index of the snippet in the notebook file
	 * and suffix is given as an argument.
	 */
	public void dumpCode(String location, String suffix) throws IOException {
		List<JSONObject> cells = this.getCodeCells();
		String noteBookName = getNameWithoutSuffix();
		for (int i=0; i<cells.size(); i++) {
			String outputFile = location + "/" + noteBookName + "_" + i + "." + suffix;
			Writer writer = new FileWriter(outputFile);
			JSONArray lines = getSource(cells.get(i));
			for (int j=0; j<lines.length(); j++) {
				writer.write(lines.getString(j));
			}
			writer.close();
		}
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName());
	}

	/**
	 * Works like dumpCode except that each notebook is turned into
	 * a zip file with one file entry per snippet.
	 */
	public void dumpCodeAsZip(String location, String suffix) throws IOException {
		String noteBookName = getNameWithoutSuffix();
		List<JSONObject> cells = this.getCodeCells();
		try {
			FileOutputStream fos = new FileOutputStream(location + "/" + noteBookName + ".zip");
			ZipOutputStream zos = new ZipOutputStream(fos);
			for (int i=0; i<cells.size(); i++) {
				ZipEntry entry = new ZipEntry(noteBookName + "_" + i + "." + suffix); 
				zos.putNextEntry(entry);
				JSONArray lines = getSource(cells.get(i));
				for (int j=0; j<lines.length(); j++) {
					zos.write(lines.getString(j).getBytes());
				}
				zos.closeEntry();
			}
			zos.close();
			fos.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Return the code of all snippets of the notebook. For memory consumption
	 * reasons, the result contains only the length (LOC) and the hash of the
	 * code in each snippet, not the code itself.
	 * @return Array containing a representation of the code of the snippets of the notebook
	 */
	public SnippetCode[] snippetCodes() {
		MessageDigest hasher;
		try {
			hasher = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			System.err.println("MessageDigest cannot hash using MD5!");
			return null;
		}
		List<JSONObject> codeCells = getCodeCells();
		int numSnippets = codeCells.size();
		SnippetCode[] code = new SnippetCode[numSnippets];
		for (int i=0; i<numSnippets; i++) {
			String snippet = "";
			JSONArray lines = getSource(codeCells.get(i));
			int loc = 0;
			for (int j=0; j<lines.length(); j++) {
				String line = lines.getString(j);
				line = line .replaceAll("\\s", "");
				if(!"".equals(line)) {
					loc++;	// Count non-empty lines
				}
				snippet += line;
			}
			String hash = toHexString(hasher.digest(snippet.getBytes()));
			code[i] = new SnippetCode(loc, hash);
		}
		return code;
	}
	
	/**
	 * Fetch the language defined by each of the fields defined by LangSpec
	 * (except NONE). Store in a Map with the LangSpec value being the key.
	 * @return The map described above
	 */
	public Map<LangSpec, Language> allLanguageValues() {
		Map<LangSpec, Language> result
			= new HashMap<LangSpec, Language>(LangSpec.values().length-1);
		LangSpec langSpecIn = this.languageSpecIn;
		JSONObject notebook = this.getNotebook();
		if (!notebook.has("metadata")) {
			result.put(LangSpec.METADATA_LANGUAGE, Language.UNKNOWN);
			result.put(LangSpec.METADATA_LANGUAGEINFO_NAME, Language.UNKNOWN);
			result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.UNKNOWN);
			result.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.UNKNOWN);
		} else {
			JSONObject metadata = notebook.getJSONObject("metadata");
			result.put(LangSpec.METADATA_LANGUAGE, getLanguageFromLanguage(metadata));
			result.put(LangSpec.METADATA_LANGUAGEINFO_NAME, getLanguageFromLanguageinfo(metadata));
			if (!metadata.has("kernelspec")) {
				result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.UNKNOWN);
				result.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.UNKNOWN);
			} else {
				JSONObject kernelspec = metadata.getJSONObject("kernelspec");
				result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, getLanguageFromKernelspecLanguage(kernelspec));
				result.put(LangSpec.METADATA_KERNELSPEC_NAME, getLanguageFromKernelSpecName(kernelspec));
			}
		}
		result.put(LangSpec.CODE_CELLS, getLanguageFromCodeCells(notebook));
		
		// Reset langSpecIn
		this.languageSpecIn = langSpecIn;
		return result;
	}
	
	/**
	 * This updates instance variables, but it will always set the same values
	 * for a given object, so it doesn't have to be synchronized. (The same
	 * holds for its private helper methods.) 
	 * @return The language of the notebook
	 */
	public Language language() {
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
	public LangSpec langSpec() {
		if (null == languageSpecIn) {
			language();
		}
		return languageSpecIn;
	}
	
	/**
	 * @return Total lines of code for all code cells in the notebook
	 */
	public int LOC() {
		countLinesIfNotDone();
		return locTotal;
	}
	
	/**
	 * @return Number of blank code lines in the notebook
	 */
	public int LOCBlank() {
		countLinesIfNotDone();
		return locBlank;
	}
	
	/**
	 * @return Number of non-blank code lines in the notebook
	 */
	public int LOCNonBlank() {
		countLinesIfNotDone();
		return locContents;
	}
	
	/**
	 * @return Number of code cells in notebook
	 */
	public int numCodeCells() {
		return getCodeCells().size();
	}
	
	/**
	 * @return An array with the number of characters in each snippet
	 */
	public int[] numCodeChars() {
		List<JSONObject> codeCells = this.getCodeCells();
		int[] result = new int[codeCells.size()];
		for (int i=0; i<codeCells.size(); i++) {
			JSONObject cell = codeCells.get(i);
			JSONArray lines = getSource(cell);
			result[i] = 0;
			for (int j=0; j<lines.length(); j++) {
				String line = lines.getString(j);	// Type is checked in getSource.
				result[i] += line.length();
			}
			
		}
		return result;
	}
	
	/**
	 * Print the snippet whose index is given as an argument to the method,
	 * followed by an empty line.
	 * @param Index of snippet to print
	 */
	public void printSnippet(int index) {
		JSONArray snippet = getSource(this.getCodeCells().get(index));
		for (int i=0; i<snippet.length(); i++) {
			System.out.print(snippet.getString(i));
		}
	}
	
	
	/**
	 * Count lines of code and set all loc variables.
	 */
	private synchronized void countLinesIfNotDone() {
		if (!locCounted) {
			locTotal = 0;	// May have been partly counted before.
			locBlank = 0;
			locContents = 0;
			List<JSONObject> codeCells = getCodeCells();
			for (JSONObject cell: codeCells) {
				JSONArray source = getSource(cell);
				countLines(source);
			}
			locCounted = true;
		}
	}

	/**
	 * Count lines in source.
	 */
	private synchronized void countLines(JSONArray source) {
		locTotal += source.length();
		for (int i=0; i<source.length(); i++) {
			String line = (source.getString(i)).trim();
			if ("".equals(line)) {
				locBlank++;
			} else {
				locContents++;
			}
		}
	}
	
	/**
	 * Extract cell array from notebook. Handles cell array on the top level
	 * but also an array of worksheets, each containing a cell array.
	 * @param notebook Notebook/worksheet to extract cells from
	 * @return Array containing all cells of the notebook
	 */
	private /*static*/ JSONArray getCellArray(JSONObject notebook) {
		JSONArray cells;
		if (notebook.has("cells")) {
			cells = notebook.getJSONArray("cells");
		} else {
			cells = new JSONArray();
		}
		if (notebook.has("worksheets")) {
			// Not according to spec, but occurring
			JSONArray worksheets = notebook.getJSONArray("worksheets");
			for (int i=0; i<worksheets.length(); i++) {
				JSONArray workSheetCells = getCellArray(worksheets.getJSONObject(i));
				for (int j=0; j<workSheetCells.length(); j++) {
					cells.put(workSheetCells.getJSONObject(j));
				}
			}
		}
		return cells;
	}
	
	/**
	 * @return A list containing all code cells in the notebook represented by this object
	 */
	private List<JSONObject> getCodeCells() {
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
		for (int i=0; i<cells.length(); i++) {
			JSONObject cell = cells.getJSONObject(i);
			if (cell.has("cell_type")) {
				String type = cell.getString("cell_type");
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
		} else if (spec.trim().equals("")) {
			return Language.UNKNOWN;
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
			if (cell.has("language")) {
				languageSpecIn = LangSpec.CODE_CELLS;
				language = cell.getString("language");
				for (int i=1; i<codeCells.size(); i++) {
					cell = codeCells.get(i);
					if (!cell.has("language") || !(cell.getString("language")).equals(language)) {
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
		if (notebook.has("metadata")) {
			JSONObject metadata = notebook.getJSONObject("metadata");
			language = getLanguageFromLanguageinfo(metadata);
			if (Language.UNKNOWN == language) {
				language = getLanguageFromLanguage(metadata);
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
	 * @return The language specified in Get source code metadata->kernelspec. UNKNOWN otherwise.
	 */
	private Language getLanguageFromKernelspec(JSONObject metadata) {
		if (metadata.has("kernelspec")) {
			JSONObject kernelspec = metadata.getJSONObject("kernelspec");
			return getLanguageFromKernelspecLanguage(kernelspec);
		}
		return Language.UNKNOWN;
	}

	/**
	 * @param kernelspec
	 */
	private Language getLanguageFromKernelSpecName(JSONObject kernelspec) {
		if (kernelspec.has("name")) {
			languageSpecIn = LangSpec.METADATA_KERNELSPEC_NAME;
			return getLanguage(kernelspec.getString("name"));
		}
		return Language.UNKNOWN;
	}

	/**
	 * @param kernelspec
	 */
	private Language getLanguageFromKernelspecLanguage(JSONObject kernelspec) {
		if (kernelspec.has("language")) {
			languageSpecIn = LangSpec.METADATA_KERNELSPEC_LANGUAGE;
			return getLanguage(kernelspec.getString("language"));
		}
		return Language.UNKNOWN;
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromLanguage(JSONObject metadata) {
		if (metadata.has("language")) {
			languageSpecIn = LangSpec.METADATA_LANGUAGE;
			return getLanguage(metadata.getString("language"));
		}
		return Language.UNKNOWN;
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language_info, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromLanguageinfo(JSONObject metadata) {
		if (metadata.has("language_info")) {
			JSONObject languageinfo = metadata.getJSONObject("language_info");
			if (languageinfo.has("name")) {
				languageSpecIn = LangSpec.METADATA_LANGUAGEINFO_NAME;
				return getLanguage(languageinfo.getString("name"));
			}
		}
		return Language.UNKNOWN;
	}
	
	/**
	 * @return The name of the notebook, with everything after the last "." removed
	 */
	private String getNameWithoutSuffix() {
		String fullName = this.getName();
		int separatorIndex = fullName.lastIndexOf(".");
		if (-1 == separatorIndex) {
			return fullName;
		} else {
			return fullName.substring(0, separatorIndex);
		}
	}
	
	/**
	 * @return A JSONObject containing the contents of the notebook
	 */
	private JSONObject getNotebook() {
		if (null == contents) {
			try {
				InputStream input = new DataInputStream(new FileInputStream(new File(this.path)));
				JSONTokener tokener = new JSONTokener(input);
				contents = new JSONObject(tokener);
			} catch (FileNotFoundException e) {
				System.err.println("Could not read " + this.path + ": " + e.toString());
				contents = new JSONObject();
			}
		}
		return contents;
	}
	
	/**
	 * @param cell Code cell to fetch source code from
	 * @return The source code stored in cell (with each line as a separate element), empty array if source code is missing 
	 */
	private JSONArray getSource(JSONObject cell) {
		// Source can be either a JSONArray or a string. :-/
		Object source = null;
		if (cell.has("source")) {
			source = cell.get("source");
		} else if (cell.has("input")) {
			source = cell.get("input");
		} else {
			System.err.println("Keys \"source\" and \"input\" are missing in a cell in " + this.path);
			source = new JSONArray();
		}
		if (source instanceof JSONArray) {
			JSONArray result = (JSONArray)source;
			if (!result.isEmpty()) {
				String lastLine = result.getString(result.length()-1);
				result.put(result.length()-1, lastLine + "\n");
			}
			return result;
		} else if (source instanceof String) {
			JSONArray result = new JSONArray();
			String[] lines = ((String) source).split("\\n");
			for (String line: lines) {
				result.put(line + "\n");
			}
			return result;
		} else {
			System.err.println("Unknown source type in " + this.path
					+ ": " + source.getClass() + "! Ignoring source.");
			return new JSONArray();
		}
	}
	
	/**
	 * Convert a byte array to hexadecimal string
	 * @param bytes Byte array to convert to hex string
	 * @return Hex string representation of bytes
	 */
	private String toHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int i=0; i<bytes.length; i++) {
			char hexRepr = Character.forDigit((bytes[i] >> 4) & 0xF, 16);
			hexChars[2*i] = Character.toUpperCase(hexRepr);
			hexRepr = Character.forDigit(bytes[i] & 0xF, 16);
			hexChars[2*i+1] = Character.toUpperCase(hexRepr);
		}
		return new String(hexChars);
	}
}
