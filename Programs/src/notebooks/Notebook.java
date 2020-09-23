package notebooks;

import java.io.*;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipOutputStream;

import java.util.zip.ZipEntry;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private WeakReference<JSONObject> contents;
	private ReentrantLock contentsLock = new ReentrantLock();
	
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
		if (other instanceof Notebook) {
			Notebook otherNotebook = (Notebook)other;
			return this.getName().equals(otherNotebook.getName());
		} else {
			return false;
		}
	}
	
	/**
	 * @return A list of all modules imported in the notebook
	 */
	public List<PythonModule> modules() {
		List<PythonModule> modules = new ArrayList<PythonModule>();
		List<JSONObject> codeCells = getCodeCells();
		for (JSONObject cell: codeCells) {
			JSONArray lines = getSource(cell);
			List<String> splitLines = new PythonPreprocessor(lines).process();
			for (String line: splitLines) {
				if (line.trim().startsWith("import ") || line.trim().startsWith("from ")) {
					try {
						modules.addAll(modulesInImport(line));
					} catch (NotebookException e) {
						System.err.println("Could not add imported modules for " + this.path + ": " + e.getMessage());
					}
				} else {
					for (PythonModule module: modules) {
						module.registerUsage(line);
					}
				}
			}
		}
		return modules;
	}
	
	/**
	 * Identify all module(s) in a Python importStatement.
	 * @param importStatement Python import statement in which modules will be identified.
	 * @return A list of all modules found in importStatment
	 */
	private List<PythonModule> modulesInImport(String importStatement) throws NotebookException {
		final String MODULEIDENTIFIER = PythonModule.MODULE_IDENTIFIER;
		final String moduleDescr = "" + MODULEIDENTIFIER + "(\\s*as\\s+" + MODULEIDENTIFIER + "\\s*)?";
		final String moduleList = "(" + moduleDescr + "\\s*,\\s*)*" + moduleDescr;
		String importStatementTemplate = "\\s*import\\s+(" + moduleList + ")\\s*";
		Pattern importPattern = Pattern.compile(importStatementTemplate);
		Matcher importMatcher = importPattern.matcher(importStatement);
		Pattern fromPattern = Pattern.compile("\\s*from\\s+(" + MODULEIDENTIFIER + ")\\s+(" + importStatementTemplate + ")");
		Matcher fromMatcher = fromPattern.matcher(importStatement);
		Pattern allFromPattern = Pattern.compile("\\s*from\\s+(" + MODULEIDENTIFIER + ")\\s+import\\s*\\*\\s*");
		Matcher allFromMatcher = allFromPattern.matcher(importStatement);
		if (importMatcher.matches()) {
			return modulesInIdentifierList(importMatcher.group(1));
		} else if(allFromMatcher.matches()) {
			PythonModule parent = new PythonModule(allFromMatcher.group(1), ImportType.FROM);
			List <PythonModule> result = new ArrayList<PythonModule>(1);
			result.add(new AllModules(parent));
			return result;
		} else if (fromMatcher.matches()) {
			List<PythonModule> result = modulesInImport(fromMatcher.group(2));
			PythonModule parent = new PythonModule(fromMatcher.group(1), ImportType.FROM);
			for (PythonModule child: result) {
				child.setOldestAncestor(parent);
			}
			return result;
		} else {
			throw new NotebookException("Invalid import statement: " + importStatement);
		}
	}
	
	/**
	 * Identify all modules in the identifier list from a Python import (that
	 * is, everything stated after "import").
	 * @param identifierList the module list described above 
	 * @return A List of all modules in identifierList 
	 */
	private List<PythonModule> modulesInIdentifierList(String identifierList) {
		final String MODULEIDENTIFIER = PythonModule.MODULE_IDENTIFIER;
		final Pattern ordinaryPattern = Pattern.compile("(" + MODULEIDENTIFIER + ")");
		final Pattern asPattern = Pattern.compile("(" + MODULEIDENTIFIER + ")\\s+as\\s+(" + MODULEIDENTIFIER + ")");
		List<PythonModule> result = new ArrayList<PythonModule>();
		String[] identifiers = identifierList.split(",");
		for (int i=0; i<identifiers.length; i++) {
			String identifier = identifiers[i].trim();
			Matcher ordinaryMatcher = ordinaryPattern.matcher(identifier);
			Matcher asMatcher = asPattern.matcher(identifier);
			if (ordinaryMatcher.matches()) {
				String[] moduleNames = identifier.split("\\.");
				String name = lastElementOf(moduleNames);
				PythonModule parent = getParentModule(moduleNames);
				result.add(new PythonModule(name, ImportType.ORDINARY, parent));
			} else if (asMatcher.matches()) {
				String[] moduleNames = asMatcher.group(1).split("\\.");
				String name = lastElementOf(moduleNames);
				String alias = asMatcher.group(2);
				PythonModule parent = getParentModule(moduleNames);
				result.add(new PythonModule(name, alias, ImportType.ALIAS, parent));
			} else {
				System.out.println("Ignoring invalid identifier: " + identifier + "!");
			}
		}
		return result;
	}
	
	/**
	 * @return The last element of the array given as parameter
	 */
	private String lastElementOf(String[] array) {
		return array[array.length-1];
	}
	
	/**
	 * Build the parent of a Python module.
	 * @param names Array of names of all ancestors, and the module itself. Sorted with oldest first.
	 * @return The parent of the module with name = last element in names
	 */
	private PythonModule getParentModule(String[] names) {
		PythonModule parent = null;
		for (int j=0; j<names.length-1; j++) {
			parent = new PythonModule(names[j], ImportType.ORDINARY, parent);
		}
		return parent;
	}
	
	/**
	 * @return The file name of the notebook, without preceding path
	 */
	public String getName() {
		int namePos = path.lastIndexOf(File.separatorChar) + 1;
		return path.substring(namePos);
	}
	
	public String getRepro() {
		return repro;
	}
	
	public void setRepro(String reproName) {
		this.repro = reproName;
	}
	
	/**
	 * Dump each code snippet to a separate file in the directory whose name is
	 * given as an argument of the method. Each file will be named
	 * <name>_<id>.<suffix> where <name> is the name of this notebook (except
	 * its suffix, if any), id is the index of the snippet in the notebook file
	 * and suffix is given as an argument.
	 * @param location Directory where the output file will be put
	 * @param suffix Suffix of output files
	 */
	public void dumpCode(String location, String suffix) throws IOException {
		List<JSONObject> cells = this.getCodeCells();
		String noteBookName = getNameWithoutSuffix();
		for (int i=0; i<cells.size(); i++) {
			String outputFile = location + File.separator + noteBookName + "_" + i + "." + suffix;
			Writer writer = new FileWriter(outputFile);
			JSONArray lines = getSource(cells.get(i));
			for (int j=0; j<lines.length(); j++) {
				writer.write(lines.getString(j));
			}
			writer.close();
		}
	}
	
	/**
	 * Dump each notebook to a separate file wrapped in a zip file in the
	 * directory whose name is given as an argument of the method. The name of
	 * a wrapped file will be the notebook name followed by the suffix given as
	 * an argument. The name of the zip file will be the notebook name followed
	 * by ".zip".
	 * @param location Directory where the output file will be put
	 * @param suffix Suffix of output files (inside zip)
	 */
	public void dumpCodeAsZipWithSingleFile(String location, String suffix) throws IOException {
		List<JSONObject> cells = this.getCodeCells();
		String noteBookName = getNameWithoutSuffix();
		String outputFile = location + File.separator + noteBookName + ".zip";
		FileOutputStream zipFileStream = new FileOutputStream(outputFile);
		ZipOutputStream targetStream = new ZipOutputStream(zipFileStream);
		ZipEntry codeFile = new ZipEntry(noteBookName + "." + suffix);
		targetStream.putNextEntry(codeFile);
		for (int i=0; i<cells.size(); i++) {
			JSONArray lines = getSource(cells.get(i));
			for (int j=0; j<lines.length(); j++) {
				targetStream.write(lines.getString(j).getBytes());
			}
		}
		targetStream.close();
		zipFileStream.close();
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
			FileOutputStream fos = new FileOutputStream(location + File.separator + noteBookName + ".zip");
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
				line = line.replaceAll("\\s", "");
				if(!"".equals(line)) {
					loc++;	// Count non-empty lines
				}
				snippet += line;
			}
			String hash = Utils.toHexString(hasher.digest(snippet.getBytes()));
			code[i] = new SnippetCode(loc, hash);
		}
		return code;
	}
	
	/**
	 * Fetch the language name defined by each of the fields defined by LangSpec
	 * (except NONE). Store in a Map with the LangSpec value being the key.
	 * @return The map described above
	 */
	public Map<LangSpec, LangName> allLanguageValues() {
		Map<LangSpec, LangName> result
			= new HashMap<LangSpec, LangName>(LangSpec.values().length-1);
		JSONObject notebook = this.getNotebook();
		if (!notebook.has("metadata")) {
			result.put(LangSpec.METADATA_LANGUAGE, LangName.UNKNOWN);
			result.put(LangSpec.METADATA_LANGUAGEINFO_NAME, LangName.UNKNOWN);
			result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangName.UNKNOWN);
			result.put(LangSpec.METADATA_KERNELSPEC_NAME, LangName.UNKNOWN);
		} else {
			JSONObject metadata = notebook.getJSONObject("metadata");
			result.put(LangSpec.METADATA_LANGUAGE, getLanguageFromLanguage(metadata).getName());
			result.put(LangSpec.METADATA_LANGUAGEINFO_NAME, getLanguageFromLanguageinfo(metadata).getName());
			if (!metadata.has("kernelspec")) {
				result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangName.UNKNOWN);
				result.put(LangSpec.METADATA_KERNELSPEC_NAME, LangName.UNKNOWN);
			} else {
				JSONObject kernelspec = metadata.getJSONObject("kernelspec");
				result.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, getLanguageFromKernelspecLanguage(kernelspec).getName());
				result.put(LangSpec.METADATA_KERNELSPEC_NAME, getLanguageFromKernelSpecName(kernelspec).getName());
			}
		}
		result.put(LangSpec.CODE_CELLS, getLanguageFromCodeCells(notebook).getName());

		return result;
	}
	
	/** 
	 * @return The language of the notebook
	 */
	public Language language() {
		JSONObject notebook = this.getNotebook();
		
		Language language = getLanguageFromMetadata(notebook);
		if (!language.isSet()) {
			language = getLanguageFromCodeCells(notebook);
		}
		return language;
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
	
	@Override
	public String toString() {
		return this.getName();
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
	private static JSONArray getCellArray(JSONObject notebook) {
		JSONArray cells = new JSONArray();
		if (notebook.has("cells")) {
			JSONArray notebookCells = notebook.getJSONArray("cells");
			for (int i=0; i< notebookCells.length(); i++) {
				cells.put(notebookCells.getJSONObject(i));
			}
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
	 * Translate a string specifying the language to a LangName value.
	 * @param language String specifying the language
	 * @return The corresponding Language name
	 */
	private static LangName getLangName(String language) {
		if (language.equals("julia") || language.equals("Julia")) {
			return LangName.JULIA;
		} else if (language.startsWith("python") || language.startsWith("Python")) {
			return LangName.PYTHON;
		} else if (language.equals("R") || language.equals("r")) {
			return LangName.R;
		} else if (language.startsWith("scala") || language.startsWith("Scala")) {
			return LangName.SCALA;
		} else if (language.trim().equals("")) {
			return LangName.UNKNOWN;
		} else {
			return LangName.OTHER;
		}
	}

	/**
	 * @param notebook Notebook to extract language from
	 * @return The language specified in code cells, if it exists and is consistent. Unset language otherwise.
	 */
	private Language getLanguageFromCodeCells(JSONObject notebook) {
		List<JSONObject> codeCells = getCodeCells(notebook);
		if (0 < codeCells.size()) {
			String langName = "";
			JSONObject cell = codeCells.get(0);
			if (cell.has("language")) {
				langName = cell.getString("language");
				for (int i=1; i<codeCells.size(); i++) {
					cell = codeCells.get(i);
					if (!cell.has("language") || !(cell.getString("language")).equals(langName)) {
						System.err.println("Ambiguous language in " + this.path);
						return new Language(LangName.UNKNOWN, LangSpec.CODE_CELLS);
					}
				}
				return new Language (getLangName(langName), LangSpec.CODE_CELLS);
			}
		}
		return new Language();
	}

	/**
	 * @param notebook Notebook to extract language from
	 * @return The language specified in metadata, if any. Unset language otherwise.
	 */
	private Language getLanguageFromMetadata(JSONObject notebook) {
		if (notebook.has("metadata")) {
			JSONObject metadata = notebook.getJSONObject("metadata");
			Language language = getLanguageFromLanguageinfo(metadata);
			if (!language.isSet()) {
				language = getLanguageFromLanguage(metadata);
			}
			if (!language.isSet()) {
				language = getLanguageFromKernelspec(metadata);
			}
			if (language.isSet()) {
				return language;
			}
		} else {
			System.err.println("No metadata in " + this.path);
		}
		return new Language();
	}

	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in Get source code metadata->kernelspec. Unset language otherwise.
	 */
	private Language getLanguageFromKernelspec(JSONObject metadata) {
		if (metadata.has("kernelspec")) {
			JSONObject kernelspec = metadata.getJSONObject("kernelspec");
			return getLanguageFromKernelspecLanguage(kernelspec);
		}
		return new Language();
	}

	/**
	 * @param kernelspec
	 */
	private Language getLanguageFromKernelSpecName(JSONObject kernelspec) {
		if (kernelspec.has("name")) {
			LangName name = getLangName(kernelspec.getString("name"));
			return new Language(name, LangSpec.METADATA_KERNELSPEC_NAME);
		}
		return new Language();
	}

	/**
	 * @param kernelspec
	 */
	private Language getLanguageFromKernelspecLanguage(JSONObject kernelspec) {
		if (kernelspec.has("language")) {
			LangName name = getLangName(kernelspec.getString("language"));
			return new Language (name, LangSpec.METADATA_KERNELSPEC_LANGUAGE);
		}
		return new Language();
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language, if any. UNKNOWN otherwise.
	 */
	private Language getLanguageFromLanguage(JSONObject metadata) {
		if (metadata.has("language")) {
			LangName name = getLangName(metadata.getString("language"));
			return new Language(name, LangSpec.METADATA_LANGUAGE);
			
		}
		return new Language();
	}
	
	/**
	 * @param metadata Metadata to analyze
	 * @return The language specified in metadata->language_info, if any. Unset language otherwise.
	 */
	private  Language getLanguageFromLanguageinfo(JSONObject metadata) {
		if (metadata.has("language_info")) {
			JSONObject languageinfo = metadata.getJSONObject("language_info");
			if (languageinfo.has("name")) {
				LangName name = getLangName(languageinfo.getString("name"));
				return new Language(name, LangSpec.METADATA_LANGUAGEINFO_NAME);
			}
		}
		return new Language();
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
		contentsLock.lock();
		JSONObject contentsReferent = null;
		try {
			if (null != contents && null != contents.get()) {
				contentsReferent = contents.get();
			} else {
				InputStream input = new DataInputStream(new FileInputStream(new File(this.path)));
				JSONTokener tokener = new JSONTokener(input);
				contentsReferent = new JSONObject(tokener);
				contents = new WeakReference<JSONObject>(contentsReferent);
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not read " + this.path + ": " + e + ". Skipping notebook!");
			contentsReferent = new JSONObject();
			contents = new WeakReference<JSONObject>(contentsReferent);
		} finally {
			contentsLock.unlock();
		}
		return contentsReferent;
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
			// Add newlines if missing
			for (int i=0; i<result.length(); i++) {
				String line = result.getString(i);
				if (0 == line.length() || '\n' != line.charAt(line.length()-1)) {
					result.put(i, line + "\n");
				}
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
					+ ": " + source.getClass() + "! Skipping source.");
			return new JSONArray();
		}
	}
}
