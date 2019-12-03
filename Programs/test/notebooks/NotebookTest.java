package notebooks;

import static org.junit.Assert.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import notebooks.LangSpec;
import notebooks.Language;
import notebooks.Notebook;
import notebooks.NotebookException;

public class NotebookTest {
	
	/**
	 * Verify that each snippet in the input file is dumped  to a separate
	 * output file, line by line, and that the output files are named
	 * correctly.
	 */
	@Test
	public void testDumpCode() throws IOException, NotebookException {
		String dataDir = "test/data/dump";
		String[] inFiles = {"nb1.ipynb", "nb1_str.ipynb", "nb2.ipynb", "nb3.ipynb"};
		String outputDir = ".";
		String suffix = "py";
		String[] expectedOutFiles = {"nb1_0.py", "nb1_1.py",
				"nb1_str_0.py", "nb1_str_1.py", "nb2_0.py", "nb3_0.py"};
		String[][] expectedLines = {
				{"import numpy", "\t"},
				{"def my_function", "\ta = 2", "\tb = 2"},
				{"import numpy", "\t"},
				{"def my_function", "\ta = 2", "\tb = 2"},
				{"import pandas"},
				{"import something"}
		};
		
		// Run
		for (String nbFile: inFiles) {
			Notebook nb = new Notebook(dataDir + "/" + nbFile);
			nb.dumpCode(outputDir, suffix);
		}
		
		// Check output
		for (int i=0; i<expectedOutFiles.length; i++) {
			String fileName = expectedOutFiles[i];
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			for (int j=0; j<expectedLines[i].length; j++) {
				assertEquals("Wrong code dumped to " + fileName, expectedLines[i][j], reader.readLine());
			}
			assertNull("Too many lines in " + fileName, reader.readLine());
			reader.close();
		}
		
		// Clean up
		for (String outFile: expectedOutFiles) {
			new File(outFile).delete();
		}
	}
	
	/**
	 * Verify that getName returns the name of the notebook (without preceding
	 * path).
	 */
	@Test
	public void testGetName() {
		Notebook notebook = new Notebook("made/up/path/empty.ipynb");
		assertEquals("Wrong name of notebook!" , "empty.ipynb", notebook.getName());
	}
	
	/**
	 * Verify that code snippets are hashed correctly.
	 * @throws NotebookException
	 */
	@Test
	public void testHashes() throws NotebookException {
		String dataDir = "test/data/hash";
		String[] files = {"empty_code_string.ipynb", "empty_code_strings.ipynb",
				"single_import.ipynb", "two_import_cells.ipynb"};
		String[][] expectedHashStrings = {
				{"D41D8CD98F00B204E9800998ECF8427E"},
				{"D41D8CD98F00B204E9800998ECF8427E"},
				{"33BE8D72467938FBB23EF42CF8C9E85F"},
				{"33BE8D72467938FBB23EF42CF8C9E85F", "6CABFDBC20F69189D4B8894A06C78F49"}
		};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(dataDir + "/" + files[i]);
			String[] hashes = notebook.hashes();
			for (int j=0; j<expectedHashStrings[i].length; j++) {
				assertEquals("Wrong hash:", expectedHashStrings[i][j], hashes[j]);
			}
		}
	}
	
	/**
	 * Verify that language is extracted correctly from notebooks.
	 * @throws NotebookException 
	 */
	@Test
	public void testLanguage() throws NotebookException {
		String dataDir = "test/data/lang";
		String[] files = {"empty.ipynb", "empty_metadata.ipynb", "k_l_cpp.ipynb",
				"k_l_python.ipynb", "k_l_python3.ipynb", "k_l_julia.ipynb",
				"k_l_R.ipynb", "k_l_r.ipynb",
				"k_l_scala.ipynb", "k_l_scala210.ipynb", "k_n_python.ipynb",
				"l_julia.ipynb", "l_Julia.ipynb", "l_Python.ipynb", "l_Scala.ipynb",
				"li_n_python.ipynb", "code_cells_amb.ipynb", "code_cells_amb2.ipynb",
				"code_cells_nolang.ipynb", "code_cells_python.ipynb"};
		Language[] languages = {Language.UNKNOWN, Language.UNKNOWN, Language.OTHER,
				Language.PYTHON, Language.PYTHON, Language.JULIA,
				Language.R, Language.R,
				Language.SCALA, Language.SCALA, Language.PYTHON,
				Language.JULIA, Language.JULIA, Language.PYTHON, Language.SCALA,
				Language.PYTHON, Language.UNKNOWN, Language.UNKNOWN,
				Language.UNKNOWN, Language.PYTHON};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(dataDir + "/" + files[i]);
			assertEquals("Wrong language:", languages[i], notebook.language());
		}
	}
	
	/**
	 * Verify that the correct language specification field is returned by
	 * langSpec().
	 * @throws NotebookException
	 */
	@Test
	public void testLangSpec() throws NotebookException {
		String dataDir = "test/data/lang";
		String[] files = {"empty.ipynb", "empty_metadata.ipynb",
				"k_l_cpp.ipynb", "k_l_python.ipynb",
				"k_n_python.ipynb", "l_julia.ipynb",
				"li_n_python.ipynb", "code_cells_amb.ipynb", "code_cells_python.ipynb"};
		LangSpec[] langSpecs = {LangSpec.NONE, LangSpec.NONE,
				LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE,
				LangSpec.METADATA_KERNELSPEC_NAME, LangSpec.METADATA_LANGUAGE,
				LangSpec.METADATA_LANGUAGEINFO_NAME, LangSpec.CODE_CELLS, LangSpec.CODE_CELLS};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(dataDir + "/" + files[i]);
			assertEquals("Wrong language specification location:", langSpecs[i], notebook.langSpec());
		}
	}
	
	/**
	 * Verify that the right language is found in each language specification
	 * field in a notebook where all such fields are initialized.
	 * @throws NotebookException
	 */
	@Test
	public void testAllLanguageValues_initialized() throws NotebookException {
		String fileName = "test/data/langFields/all_lang_specs.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, Language> expected
			= new HashMap<LangSpec, Language>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, Language.JULIA);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, Language.PYTHON);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.R);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.OTHER);
		expected.put(LangSpec.CODE_CELLS, Language.SCALA);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that "UNKNOWN" is set as the language by langFieldValues when the
	 * language specification fields in a notebook are missing.
	 * @throws NotebookException
	 */
	@Test
	public void testAllLanguageValues_empty() throws NotebookException {
		String fileName = "test/data/langFields/empty.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, Language> expected
			= new HashMap<LangSpec, Language>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, Language.UNKNOWN);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, Language.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.UNKNOWN);
		expected.put(LangSpec.CODE_CELLS, Language.UNKNOWN);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that "UNKNOWN" is set as the language for the KERNELSPEC fields by
	 * langFieldValues when a notebook contains metadata, but not kernelspec.
	 * @throws NotebookException
	 */
	@Test
	public void testAllLanguageValues_noKernelSpec() throws NotebookException {
		String fileName = "test/data/langFields/no_kernelspec.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, Language> expected
			= new HashMap<LangSpec, Language>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, Language.R);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, Language.JULIA);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, Language.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, Language.UNKNOWN);
		expected.put(LangSpec.CODE_CELLS, Language.SCALA);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that the language spec value is not changed by langFieldValues.
	 * @throws NotebookException
	 */
	@Test
	public void testAllLanguageValues_langSpec() throws NotebookException {
		String fileName = "test/data/langFields/all_lang_specs.ipynb";
		Notebook notebook = new Notebook(fileName);
		notebook.language();
		notebook.allLanguageValues();
		assertEquals("Language specification field changed by langFieldValues",
				LangSpec.METADATA_LANGUAGE, notebook.langSpec());
	}
	
	/**
	 * Verify that the correct total number of lines of code are found in JSON
	 * files.
	 * @throws NotebookException 
	 */
	@Test
	public void testLOCTotal() throws NotebookException {
		String dataDir = "test/data/loc";
		String[] files = {"markdownCells.ipynb", "one_codeCell_6loc_arr.ipynb",
				"one_codeCell_6loc_str.ipynb", "two_codeCells_13loc.ipynb",
				"three_codeCells_2loc.ipynb", "code_and_md_3loc.ipynb",
				"missing_source.ipynb", "two_codeCells_26loc_worksheet.ipynb"};
		int[] LOC = {0, 6, 6, 13, 2, 3, 2, 23};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(dataDir + "/" + files[i]);
			assertEquals("Wrong LOC!", LOC[i], notebook.LOC());
		}
	}
	
	/**
	 * Verify that the correct total number of blank code lines are found in
	 * JSON files.
	 * @throws NotebookException 
	 */
	@Test
	public void testLOCBlank() throws NotebookException {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
	}
	
	/**
	 * Verify that the correct total number of non-blank code lines are found
	 * in JSON files.
	 * @throws NotebookException 
	 */
	@Test
	public void testLOCNonBlank() throws NotebookException {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
	}
	
	/**
	 * Verify that all kinds of lines of code are counted correctly, also when
	 * the methods are called after each other.
	 * @throws NotebookException 
	 */
	@Test
	public void testLOCAll() throws NotebookException {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong LOC!", 13, notebook.LOC());
		assertEquals("Wrong second LOC!", 13, notebook.LOC());
		assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
		assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
	}
	
	/**
	 * Verify that the correct number of code cells are found in JSON files.
	 * @throws NotebookException 
	 */
	@Test
	public void testNumCodeCells() throws NotebookException {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "two.ipynb",
				"three_with_md.ipynb", "three_in_worksheets.ipynb",
				"missing_cells.ipynb", "missing_cells_in_worksheet.ipynb",
				"missing_cell_type.ipynb", "four_cells_and_worksheets.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3, 3, 0, 0, 2, 4};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(dataDir + "/" + files[i]);
			assertEquals("Wrong number of code cells!",
					numCodeCells[i], notebook.numCodeCells());
		}
	}
	
	@Test
	public void testPrintSnippet() throws NotebookException, IOException {
		// Redirect stdout
		PrintStream stdout = System.out;
		OutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output));
		
		// Test data
		String dataDir = "test/data/dump";
		String file = "nb1.ipynb";
		int snippetIndex = 1;
		String expectedOutput = "def my_function\n\ta = 2\n\tb = 2\n";
		
		// Verify behavior
		Notebook notebook = new Notebook(dataDir + "/" + file);
		try {
			notebook.printSnippet(snippetIndex);
			assertEquals("Wrong code printed", expectedOutput, output.toString());
		} catch (NotebookException e) {
			// Before failing, we have to reset stdout!
			System.setOut(stdout);
			throw e;
		}
		
		// Reset stdout
		System.setOut(stdout);
	}
	
	/**
	 * Verify that a NotebookException is thrown if we try to extract a source
	 * value that is not a string or an array.
	 */
	@Test (expected=NotebookException.class)
	public void testParsingUnknownSourceType() throws NotebookException {
		Notebook notebook = new Notebook("test/data/loc/unknown_source_type.ipynb");
		notebook.LOC();
	}

	/**
	 * Verify that a NotebookException is thrown if we try to count cells in a
	 * notebook created from a file not containing JSON data.
	 */
	@Test (expected=NotebookException.class)
	public void testParsingEmptyFile() throws NotebookException {
		Notebook notebook = new Notebook("test/data/count/empty.ipynb");
		notebook.numCodeCells();
	}
	
	/**
	 * Verify that a NotebookException is thrown if we try to count cells in a
	 * notebook created from a missing file.
	 */
	@Test (expected=NotebookException.class)
	public void testParsingMissingFile() throws NotebookException  {
		Notebook notebook = new Notebook("nonexistent_file.txt");
		notebook.numCodeCells();
	}
}
