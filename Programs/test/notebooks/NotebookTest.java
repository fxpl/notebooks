package notebooks;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.junit.Test;

import notebooks.LangSpec;
import notebooks.LangName;
import notebooks.Notebook;

public class NotebookTest {
	
	@Test
	public void testCopyConstructor() {
		String notebookName = "nb.ipynb";
		String path = "dir/subdir/" + notebookName;
		String reproName = "repro0";
		Notebook model = new Notebook(path);
		model.setRepro(reproName);
		Notebook copy = new Notebook(model);
		assertEquals("Copy constructor doesn't seem to set path correctly!",
				notebookName, copy.getName());
		assertEquals("Copy constructor doesn't set the repro name correctly!",
				reproName, copy.getRepro());
	}
	
	/**
	 * Verify that each snippet in the input file is dumped  to a separate
	 * output file, line by line, and that the output files are named
	 * correctly.
	 * @throws IOException 
	 */
	@Test
	public void testDumpCode() throws IOException {
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
			Notebook nb = new Notebook(getPath(dataDir, nbFile));
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
	
	@Test
	public void testDumpCodeAsZip_singleFile() throws IOException {
		String dataDir = "test/data/dump";
		String[] notebooks = {"nb1", "nb1_str", "nb2", "nb3"};
		String outputDir = ".";
		String suffix = "py";
		String[][] expectedLines = {
				{"import numpy", "\t", "def my_function", "\ta = 2", "\tb = 2"},
				{"import numpy", "\t", "def my_function", "\ta = 2", "\tb = 2"},
				{"import pandas"},
				{"import something"}
		};
		
		// Run
		for (String nbFile: notebooks) {
			Notebook nb = new Notebook(getPath(dataDir, nbFile + ".ipynb"));
			nb.dumpCodeAsZipWithSingleFile(outputDir, suffix);
		}
		
		// Check output
		for (int i=0; i<notebooks.length; i++) {
			String fileName = notebooks[i] + ".zip";
			ZipInputStream zipFileStream = new ZipInputStream(new FileInputStream(fileName));
			ZipEntry codeFile = zipFileStream.getNextEntry(); 	// There is only one per zip file
			assertEquals("Wrong fileName for codeFile in " + fileName,
					notebooks[i] + "." + suffix, codeFile.getName());
			BufferedReader codeLineReader = new BufferedReader(new InputStreamReader(zipFileStream));
			for (int j=0; j<expectedLines[i].length; j++) {
				assertEquals("Wrong code dumped to " + fileName, expectedLines[i][j], codeLineReader.readLine());
			}
			assertNull("Too many lines in " + fileName, codeLineReader.readLine());
			codeLineReader.close();
			zipFileStream.close();
		}
		
		// Clean up
		for (String notebook: notebooks) {
			new File(notebook + ".zip").delete();
		}
	}
	
	@Test
	public void testDumpCodeAsZip() throws IOException {
		String dataDir = "test/data/dump";
		String[] inFiles = {"nb1.ipynb", "nb1_str.ipynb", "nb2.ipynb", "nb3.ipynb"};
		String outputDir = ".";
		String suffix = "py";
		String[] expectedOutFiles = {"nb1.zip", "nb1_str.zip", "nb2.zip", "nb3.zip"};

		String[][] expectedSnippets = {
        {"nb1_0.py", "nb1_1.py"},
        {"nb1_str_0.py", "nb1_str_1.py"},
        {"nb2_0.py"},
        {"nb3_0.py"}
    };

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
			Notebook nb = new Notebook(getPath(dataDir, nbFile));
			nb.dumpCodeAsZip(outputDir, suffix);
		}

	    int totalSnippetId = 0;
			// Check output
			for (int i=0; i<expectedOutFiles.length; i++) {
				String fileName = "./"  +  expectedOutFiles[i];
	      
	      ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(fileName)));
	
	      System.err.printf("%s\n", fileName);
	          
	      for (int snippetId=0; snippetId < expectedSnippets[i].length; snippetId += 1) {
	          ZipEntry entry = zip.getNextEntry();
	          assertEquals("Wrong filename for code snippet in " + fileName, expectedSnippets[i][snippetId], entry.getName());
	
	          BufferedReader reader = new BufferedReader(new InputStreamReader(zip));
	          for (int lineId = 0; lineId < expectedLines[totalSnippetId].length; lineId += 1) {
	              assertEquals("Wrong code dumped to " + fileName + " in " + entry.getName(), expectedLines[totalSnippetId][lineId], reader.readLine());
	          }
	
	          assertNull("Too many lines in " + fileName, reader.readLine());
	
	          totalSnippetId += 1;
	      }
	      
	      zip.close();
	    }
	    
			// Clean up
	    for (String outFile: expectedOutFiles) {
	        new File(outFile).delete();
	    }
	}
	
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals_diffType() {
		Notebook notebook = new Notebook("");
		String other = "";
		assertFalse("Object of other type considered equal!", notebook.equals(other));
	}
	
	@Test
	public void testEquals_diffNames() {
		Notebook notebook1 = new Notebook("notebook1.ipynb");
		Notebook notebook2 = new Notebook("notebook2.ipynb");
		assertFalse("Notebooks with different names considered equal!",
				notebook1.equals(notebook2));
	}

	@Test
	public void testEquals_equal() {
		Notebook notebook1 = new Notebook("notebook.ipynb");
		Notebook notebook2 = new Notebook("notebook.ipynb");
		assertTrue("Equal notebooks considered different!",
				notebook1.equals(notebook2));
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
	
	@Test
	public void testHashCode() {
		String name = "notebook";
		String path = "some/path/" + name;
		Notebook notebook = new Notebook(path);
		assertEquals("Wrong hash code returned!", Objects.hash(name), notebook.hashCode());
	}
	
	/**
	 * Verify that setRepro and getRepro sets and gets the repro name
	 * correctly.
	 */
	@Test
	public void testSetGetRepro() {
		String reproName = "someRepro";
		Notebook notebook = new Notebook("");
		notebook.setRepro(reproName);
		assertEquals("Repro name not set or not fetched correctly!",
				reproName, notebook.getRepro());
	}
	
	/**
	 * Verify that the constructor that takes both path and repro as arguments
	 * initializes the name and repro correctly
	 */
	@Test
	public void testTwoArgConstructor() {
		String name = "nb_0.ipynb";
		String repro = "someRepro";
		Notebook notebook = new Notebook(name, repro);
		assertEquals("Name not initialized/fetched correctly!", name, notebook.getName());
		assertEquals("Repro not initialized/fetched correctly!", repro, notebook.getRepro());
	}
	
	/**
	 * Verify that code snippets are hashed correctly.
	 */
	@Test
	public void testSnippetCodes() {
		String dataDir = "test/data/hash";
		String[] files = {"nb_4.ipynb", "nb_5.ipynb",
				"nb_6.ipynb", "nb_7.ipynb"};
		String[][] expectedHashStrings = {
				{"D41D8CD98F00B204E9800998ECF8427E", "D41D8CD98F00B204E9800998ECF8427E"},
				{"D41D8CD98F00B204E9800998ECF8427E"},
				{"33BE8D72467938FBB23EF42CF8C9E85F"},
				{"33BE8D72467938FBB23EF42CF8C9E85F", "6CABFDBC20F69189D4B8894A06C78F49"}
		};
		int[][] expectedLOC = {{0, 0}, {0}, {1}, {1, 1}};
		
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			SnippetCode[] snippetCodes = notebook.snippetCodes();
			for (int j=0; j<expectedHashStrings[i].length; j++) {
				assertEquals("Wrong hash:", expectedHashStrings[i][j], snippetCodes[j].getHash());
				assertEquals("Wrong number of LOC:", expectedLOC[i][j], snippetCodes[j].getLOC());
			}
		}
	}
	
	/**
	 * Verify that language is extracted correctly from notebooks.
	 */
	@Test
	public void testLanguage() {
		String dataDir = "test/data/lang";
		String[] files = {"empty.ipynb", "empty_metadata.ipynb", "k_l_cpp.ipynb",
				"k_l_python.ipynb", "k_l_python3.ipynb", "k_l_julia.ipynb",
				"k_l_R.ipynb", "k_l_r.ipynb",
				"k_l_scala.ipynb", "k_l_scala210.ipynb", "k_n_python.ipynb",
				"l_julia.ipynb", "l_Julia.ipynb", "l_Python.ipynb", "l_Scala.ipynb",
				"li_n_python.ipynb", "l_li_n_python.ipynb",
				"li_n_empty.ipynb", "li_n_space.ipynb",
				"code_cells_amb.ipynb", "code_cells_amb2.ipynb",
				"code_cells_nolang.ipynb", "code_cells_python.ipynb"};
		LangName[] languages = {LangName.UNKNOWN, LangName.UNKNOWN, LangName.OTHER,
				LangName.PYTHON, LangName.PYTHON, LangName.JULIA,
				LangName.R, LangName.R,
				LangName.SCALA, LangName.SCALA, LangName.UNKNOWN,
				LangName.JULIA, LangName.JULIA, LangName.PYTHON, LangName.SCALA,
				LangName.PYTHON, LangName.PYTHON,
				LangName.UNKNOWN, LangName.UNKNOWN,
				LangName.UNKNOWN, LangName.UNKNOWN,
				LangName.UNKNOWN, LangName.PYTHON};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			assertEquals("Wrong language:", languages[i], notebook.language().getName());
		}
	}
	
	/**
	 * Verify that the correct language specification field is returned by
	 * langSpec().
	 */
	@Test
	public void testLangSpec() {
		String dataDir = "test/data/lang";
		String[] files = {"empty.ipynb", "empty_metadata.ipynb",
				"k_l_cpp.ipynb", "k_l_python.ipynb",
				"k_n_python.ipynb", "l_julia.ipynb",
				"li_n_python.ipynb", "l_li_n_python.ipynb",
				"li_n_empty.ipynb", "li_n_space.ipynb",
				"code_cells_amb.ipynb", "code_cells_python.ipynb"};
		LangSpec[] langSpecs = {LangSpec.NONE, LangSpec.NONE,
				LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE,
				LangSpec.NONE, LangSpec.METADATA_LANGUAGE,
				LangSpec.METADATA_LANGUAGEINFO_NAME, LangSpec.METADATA_LANGUAGEINFO_NAME,
				LangSpec.METADATA_LANGUAGEINFO_NAME, LangSpec.METADATA_LANGUAGEINFO_NAME,
				LangSpec.CODE_CELLS, LangSpec.CODE_CELLS};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			assertEquals("Wrong language specification location:", langSpecs[i], notebook.language().getSpec());
		}
	}
	
	/**
	 * Verify that the right language is found in each language specification
	 * field in a notebook where all such fields are initialized.
	 */
	@Test
	public void testAllLanguageValues_initialized() {
		String fileName = "test/data/langFields/all_lang_specs.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, LangName> expected
			= new HashMap<LangSpec, LangName>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, LangName.JULIA);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, LangName.PYTHON);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangName.R);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, LangName.OTHER);
		expected.put(LangSpec.CODE_CELLS, LangName.SCALA);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that "UNKNOWN" is set as the language by langFieldValues when the
	 * language specification fields in a notebook are missing.
	 */
	@Test
	public void testAllLanguageValues_empty() {
		String fileName = "test/data/langFields/empty.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, LangName> expected
			= new HashMap<LangSpec, LangName>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, LangName.UNKNOWN);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, LangName.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangName.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, LangName.UNKNOWN);
		expected.put(LangSpec.CODE_CELLS, LangName.UNKNOWN);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that "UNKNOWN" is set as the language for the KERNELSPEC fields by
	 * langFieldValues when a notebook contains metadata, but not kernelspec.
	 */
	@Test
	public void testAllLanguageValues_noKernelSpec() {
		String fileName = "test/data/langFields/no_kernelspec.ipynb";
		final int NUM_LANG_FIELDS = LangSpec.values().length - 1;
		Map<LangSpec, LangName> expected
			= new HashMap<LangSpec, LangName>(NUM_LANG_FIELDS);
		expected.put(LangSpec.METADATA_LANGUAGE, LangName.R);
		expected.put(LangSpec.METADATA_LANGUAGEINFO_NAME, LangName.JULIA);
		expected.put(LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangName.UNKNOWN);
		expected.put(LangSpec.METADATA_KERNELSPEC_NAME, LangName.UNKNOWN);
		expected.put(LangSpec.CODE_CELLS, LangName.SCALA);
		
		Notebook notebook = new Notebook(fileName);
		assertEquals("Wrong language field values returned",
				expected, notebook.allLanguageValues());
	}
	
	/**
	 * Verify that the language spec value is not changed by langFieldValues.
	 */
	@Test
	public void testAllLanguageValues_langSpec() {
		String fileName = "test/data/langFields/all_lang_specs.ipynb";
		Notebook notebook = new Notebook(fileName);
		notebook.language();
		notebook.allLanguageValues();
		assertEquals("Language specification field changed by langFieldValues",
				LangSpec.METADATA_LANGUAGEINFO_NAME, notebook.language().getSpec());
	}
	
	/**
	 * Verify that the correct total number of lines of code are found in JSON
	 * files.
	 */
	@Test
	public void testLOCTotal() {
		String dataDir = "test/data/loc";
		String[] files = {"markdownCells.ipynb", "one_codeCell_6loc_arr.ipynb",
				"one_codeCell_6loc_str.ipynb", "two_codeCells_13loc.ipynb",
				"three_codeCells_2loc.ipynb", "code_and_md_3loc.ipynb",
				"missing_source.ipynb", "two_codeCells_26loc_worksheet.ipynb"};
		int[] LOC = {0, 6, 6, 13, 2, 3, 2, 23};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			assertEquals("Wrong LOC!", LOC[i], notebook.LOC());
		}
	}
	
	/**
	 * Verify that the correct total number of blank code lines are found in
	 * JSON files.
	 */
	@Test
	public void testLOCBlank() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
	}
	
	/**
	 * Verify that the correct total number of non-blank code lines are found
	 * in JSON files.
	 */
	@Test
	public void testLOCNonBlank() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
	}
	
	/**
	 * Verify that all kinds of lines of code are counted correctly, also when
	 * the methods are called after each other.
	 */
	@Test
	public void testLOCAll() {
		Notebook notebook = new Notebook("test/data/loc/two_codeCells_13loc.ipynb");
		assertEquals("Wrong LOC!", 13, notebook.LOC());
		assertEquals("Wrong second LOC!", 13, notebook.LOC());
		assertEquals("Wrong number of blank lines!", 2, notebook.LOCBlank());
		assertEquals("Wrong number of non-blank lines!", 11, notebook.LOCNonBlank());
	}
	
	/**
	 * Verify that the correct number of code cells are found in JSON files.
	 */
	@Test
	public void testNumCodeCells() {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "two.ipynb",
				"three_with_md.ipynb", "three_in_worksheets.ipynb",
				"missing_cells.ipynb", "missing_cells_in_worksheet.ipynb",
				"missing_cell_type.ipynb", "four_cells_and_worksheets.ipynb"};
		int[] numCodeCells = {0, 1, 2, 3, 3, 0, 0, 2, 4};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			assertEquals("Wrong number of code cells!",
					numCodeCells[i], notebook.numCodeCells());
		}
	}
	
	/**
	 * Verify that the correct number of code characters are returned by
	 * numCodeChars.
	 */
	@Test
	public void testNumCodeChars() {
		String dataDir = "test/data/codeChars";
		int[][] expectedNumChars = {{15, 30}, {15, 30}, {14}};
		String[] files = {"nb1.ipynb", "nb1_str.ipynb", "nb2.ipynb"};
		for (int i=0; i<files.length; i++) {
			Notebook notebook = new Notebook(getPath(dataDir, files[i]));
			int[] numChars = notebook.numCodeChars();
			for (int j=0; j<numChars.length; j++) {
				assertEquals("Wrong number of code characters returned for " + files[i] + ", snippet " + j + "!", 
						expectedNumChars[i][j], numChars[j]);
			}
		}
	}
	
	/**
	 * Verify that printSnippet prints a snippet correctly.
	 */
	@Test
	public void testPrintSnippet() throws IOException {
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
		Notebook notebook = new Notebook(getPath(dataDir, file));
		notebook.printSnippet(snippetIndex);
		assertEquals("Wrong code printed", expectedOutput, output.toString());
		
		// Reset stdout
		System.setOut(stdout);
	}
	
	/**
	 * Verify that no lines are found if we try to extract a source value that
	 * is not a string or an array.
	 */
	@Test
	public void testParsingUnknownSourceType() {
		Notebook notebook = new Notebook("test/data/loc/unknown_source_type.ipynb");
		notebook.LOC();
	}

	/**
	 * Verify that a JSONException is thrown if we try to count cells in a
	 * notebook created from a file not containing JSON data.
	 */
	@Test (expected=JSONException.class)
	public void testParsingEmptyFile() {
		Notebook notebook = new Notebook("test/data/count/empty.ipynb");
		notebook.numCodeCells();
	}
	
	/**
	 * Verify that 0 code cells are found in a notebook created from a missing
	 * file.
	 */
	@Test
	public void testParsingMissingFile()  {
		Notebook notebook = new Notebook("nonexistent_file.txt");
		notebook.numCodeCells();
	}
	
	/**
	 * Verify that a single import can be identified.
	 */
	@Test
	public void testSingleImport() {
		String dataDir = "test/data/modules";
		String file = "nb_1.ipynb";
		PythonModule expectedModule = new PythonModule("kossan_mu", ImportType.ORDINARY);
		expectedModule.functionUsages.put("function", 2);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a single import can be verified also when newlines are
	 * missing in the end of the lines stored in the source code JSON array.
	 */
	@Test
	public void testSingleImport_noNewLines() {
		String dataDir = "test/data/modules";
		String file = "nb_1b.ipynb";
		PythonModule expectedModule = new PythonModule("kossan_mu", ImportType.ORDINARY);
		expectedModule.functionUsages.put("function", 2);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a single Python module import with alias can be identified.
	 */
	@Test
	public void testSingleImportWithAlias() {
		String dataDir = "test/data/modules";
		String file = "nb_2.ipynb";
		PythonModule expectedModule = new PythonModule("kalv", "naut", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that the module is identified in a "from module import *"
	 * statement.
	 */
	@Test
	public void testSingleImportAllFrom() {
		String dataDir = "test/data/modules";
		String file = "nb_3.ipynb";
		PythonModule parent = new PythonModule("ko", ImportType.FROM);
		PythonModule expectedModule = new AllModules(parent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that the module is identified in a "from module import*"
	 * statement --that is also when the space is missing after import.
	 */
	@Test
	public void testSingleImportAllFromNoSpace() {
		String dataDir = "test/data/modules";
		String file = "nb_50.ipynb";
		PythonModule parent = new PythonModule("ko", ImportType.FROM);
		PythonModule expectedModule = new AllModules(parent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a module in a "from" import statement can be identified
	 * correctly.
	 */
	@Test
	public void testImportOrdinaryFrom() {
		String dataDir = "test/data/modules";
		String file = "nb_6.ipynb";
		PythonModule expectedParent = new PythonModule("ko", ImportType.FROM);
		PythonModule expectedModule = new PythonModule("mage", ImportType.ORDINARY, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a module with alias in a "from" import statement can be
	 * identified correctly.
	 */
	@Test
	public void testImportWithAliasFrom() {
		String dataDir = "test/data/modules";
		String file = "nb_7.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(1);
		PythonModule expectedParent = new PythonModule("ko", ImportType.FROM);
		expectedModules.add(new PythonModule("vom", "mage", ImportType.ALIAS, expectedParent));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that a module in a "from" import statement can be identified
	 * correctly when parentheses are put around the module/function name.
	 */
	@Test
	public void testImportOrdinaryFromWithParentheses() {
		String dataDir = "test/data/modules";
		String file = "nb_8.ipynb";
		PythonModule expectedParent = new PythonModule("ko", ImportType.FROM);
		PythonModule expectedModule = new PythonModule("mage", ImportType.ORDINARY, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a module in a "from" import statement can be identified
	 * correctly when parentheses are put around the module/function name, also
	 * when there is no space in front of the left parenthesis.
	 */
	@Test
	public void testImportOrdinaryFromWithParentheses_noSpaceBefore() {
		String dataDir = "test/data/modules";
		String file = "nb_9.ipynb";
		PythonModule expectedParent = new PythonModule("ko", ImportType.FROM);
		PythonModule expectedModule = new PythonModule("mage", ImportType.ORDINARY, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that several Python module imports in one cell can be identified.
	 */
	@Test
	public void testSeveralImports() {
		String dataDir = "test/data/modules";
		String file = "nb_4.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		PythonModule module = new PythonModule("module1", ImportType.ORDINARY);
		module.functionUsages.put("fun1", 1);
		module.functionUsages.put("fun2", 1);
		module.functionUsages.put("fun3", 1);
		expectedModules.add(module);
		module = new PythonModule("module2", "mod2", ImportType.ALIAS);
		module.functionUsages.put("f", 2);
		expectedModules.add(module);
		module = new PythonModule("module3", "mod3", ImportType.ALIAS);
		expectedModules.add(module);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that Python module imports in different cells can be identified.
	 */
	@Test
	public void testSeveralImportCells() {
		String dataDir = "test/data/modules";
		String file = "nb_5.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(4);
		expectedModules.add(new PythonModule("module10", ImportType.ORDINARY));
		expectedModules.add(new PythonModule("module11", "mod", ImportType.ALIAS));
		expectedModules.add(new PythonModule("module12", ImportType.ORDINARY));
		expectedModules.add(new PythonModule("module13", "mod13", ImportType.ALIAS));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules in an import list can be identified.
	 */
	@Test
	public void testImportList() {
		String dataDir = "test/data/modules";
		String file = "nb_20.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		expectedModules.add(new PythonModule("moduleA", ImportType.ORDINARY));
		expectedModules.add(new PythonModule("moduleB", ImportType.ORDINARY));
		expectedModules.add(new PythonModule("moduleC", ImportType.ORDINARY));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules in an import list with aliases can be identified.
	 */
	@Test
	public void testImportListWithAliases() {
		String dataDir = "test/data/modules";
		String file = "nb_21.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		expectedModules.add(new PythonModule("moduleA", "a", ImportType.ALIAS));
		expectedModules.add(new PythonModule("moduleB", "b", ImportType.ALIAS));
		expectedModules.add(new PythonModule("moduleC", "c", ImportType.ALIAS));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules in an import list both with and without aliases can
	 * be identified.
	 */
	@Test
	public void testImportListWithOneAlias() {
		// import A, B as b, C
		String dataDir = "test/data/modules";
		String file = "nb_22.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		expectedModules.add(new PythonModule("moduleA", ImportType.ORDINARY));
		expectedModules.add(new PythonModule("moduleB", "b", ImportType.ALIAS));
		expectedModules.add(new PythonModule("moduleC", ImportType.ORDINARY));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules in an import list in a "from" import statement are
	 * identified correctly.
	 */
	@Test
	public void testFromImportListWithAlias() {
		String dataDir = "test/data/modules";
		String file = "nb_23.ipynb";
		PythonModule expectedParent = new PythonModule("Base", ImportType.FROM);
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		expectedModules.add(new PythonModule("moduleA", ImportType.ORDINARY, expectedParent));
		expectedModules.add(new PythonModule("moduleB", "b", ImportType.ALIAS, expectedParent));
		expectedModules.add(new PythonModule("moduleC", ImportType.ORDINARY, expectedParent));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules in a from import are imported when there is a
	 * trailing comma, if the submodules are within parentheses, but not
	 * otherwise.
	 */
	@Test
	public void testFromImportWithTrailingComma() {
		String dataDir = "test/data/modules";
		String file = "nb_24.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		PythonModule expectedParent = new PythonModule("Base", ImportType.FROM);
		expectedModules.add(new PythonModule("moduleA", ImportType.ORDINARY, expectedParent));
		expectedModules.add(new PythonModule("moduleB", "b", ImportType.ALIAS, expectedParent));
		expectedModules.add(new PythonModule("moduleC", ImportType.ORDINARY, expectedParent));
		verifyImports(getPath(dataDir, file), expectedModules);
	}

	/**
	 * Verify that a module in an import statement is identified correctly when
	 * it has parent module(s).
	 */
	@Test
	public void testImportWithSubmodules() {
		String dataDir = "test/data/modules";
		String file = "nb_30.ipynb";
		PythonModule expectedGrandParent = new PythonModule("A", ImportType.ORDINARY);
		PythonModule expectedParent = new PythonModule("B", ImportType.ORDINARY, expectedGrandParent);
		PythonModule expectedModule = new PythonModule("C", ImportType.ORDINARY, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a module in an import statement is identified correctly when
	 * it has parent module(s) and an alias.
	 */
	@Test
	public void testImportWithSubmodulesAndAlias() {
		String dataDir = "test/data/modules";
		String file = "nb_31.ipynb";
		PythonModule expectedGrandParent = new PythonModule("A", ImportType.ORDINARY);
		PythonModule expectedParent = new PythonModule("B", ImportType.ORDINARY, expectedGrandParent);
		PythonModule expectedModule = new PythonModule("C", "child", ImportType.ALIAS, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a list of modules in a "from" import statement can be
	 * identified correctly when they have aliases and/or additional parent
	 * modules.
	 */
	@Test
	public void testFromImportWithSubmodulesAndAlias() {
		String dataDir = "test/data/modules";
		String file = "nb_33.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(1);
		PythonModule expectedParent = new PythonModule("Base", ImportType.FROM);
		expectedModules.add(new PythonModule("A", "sub", ImportType.ALIAS, expectedParent));
		expectedModules.add(new PythonModule("B", ImportType.ORDINARY, expectedParent));
		expectedModules.add(new PythonModule("C", "sub2", ImportType.ALIAS, expectedParent));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that a module/function in a "from" import statement can be
	 * identified correctly when the parent has a submodule.
	 */
	@Test
	public void testFromImportWithParentSubmodule() {
		String dataDir = "test/data/modules";
		String file = "nb_34.ipynb";
		PythonModule expectedParent = new PythonModule("Base.Sub", ImportType.FROM);
		PythonModule expectedModule = new PythonModule("A", ImportType.ORDINARY, expectedParent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that import statements can be identified correctly when there are
	 * newlines in a code line.
	 */
	@Test
	public void testImportWithNewLinesInLine() {
		String dataDir = "test/data/modules";
		String file = "nb_39.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		PythonModule expectedModule = new PythonModule("A", ImportType.ORDINARY);
		expectedModules.add(expectedModule);
		PythonModule expectedParent = new PythonModule("B", ImportType.ORDINARY);
		expectedModule = new PythonModule("C", ImportType.ORDINARY, expectedParent);
		expectedModules.add(expectedModule);
		expectedParent = new PythonModule("D", ImportType.FROM);
		expectedModule = new PythonModule("E", "e", ImportType.ALIAS, expectedParent);
		expectedModules.add(expectedModule);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that import statements can be identified correctly when there are
	 * semicolons in a code line.
	 */
	@Test
	public void testImportWithSemicolonsInLine() {
		String dataDir = "test/data/modules";
		String file = "nb_40.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		PythonModule expectedModule = new PythonModule("A", ImportType.ORDINARY);
		expectedModules.add(expectedModule);
		PythonModule expectedParent = new PythonModule("B", ImportType.ORDINARY);
		expectedModule = new PythonModule("C", ImportType.ORDINARY, expectedParent);
		expectedModules.add(expectedModule);
		expectedParent = new PythonModule("D", ImportType.FROM);
		expectedModule = new PythonModule("E", "e", ImportType.ALIAS, expectedParent);
		expectedModules.add(expectedModule);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that import statements can be identified correctly when there are
	 * both newlines and semicolons in a code line.
	 */
	@Test
	public void testImportWithNewlinesAndSemicolonsInLine() {
		String dataDir = "test/data/modules";
		String file = "nb_41.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(3);
		PythonModule expectedModule = new PythonModule("A", ImportType.ORDINARY);
		expectedModules.add(expectedModule);
		PythonModule expectedParent = new PythonModule("B", ImportType.ORDINARY);
		expectedModule = new PythonModule("C", ImportType.ORDINARY, expectedParent);
		expectedModules.add(expectedModule);
		expectedParent = new PythonModule("D", ImportType.FROM);
		expectedModule = new PythonModule("E", "e", ImportType.ALIAS, expectedParent);
		expectedModules.add(expectedModule);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that lines started with "import" or "from" not followed by a space
	 * are smoothly ignored in import analysis.
	 */
	@Test
	public void testImportStartToBeIgnored() {
		String dataDir = "test/data/modules";
		String file = "nb_42.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that import statements can be identified correctly when followed
	 * by comments.
	 */
	@Test
	public void testImportWithComment() {
		String dataDir = "test/data/modules";
		String file = "nb_43.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(4);
		PythonModule parentModule = new PythonModule("A", ImportType.ORDINARY);
		PythonModule expectedModule = parentModule;
		expectedModules.add(expectedModule);
		expectedModule = new PythonModule("B", "b", ImportType.ALIAS, parentModule);
		expectedModules.add(expectedModule);
		parentModule = new PythonModule("A", ImportType.FROM);
		expectedModule = new PythonModule("C", ImportType.ORDINARY, parentModule);
		expectedModules.add(expectedModule);
		parentModule = new PythonModule("D", ImportType.FROM);
		expectedModule = new AllModules(parentModule);
		expectedModules.add(expectedModule);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that submodules of modules whose names consist of or start with
	 * dots can be imported with FROM imports.
	 */
	@Test
	public void testFromImportWithDots() {
		String dataDir = "test/data/modules";
		String file = "nb_51.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(4);
		PythonModule parentModule = new PythonModule(".", ImportType.FROM);
		expectedModules.add(new PythonModule("someModule", ImportType.ORDINARY, parentModule));
		parentModule = new PythonModule("...", ImportType.FROM);
		expectedModules.add(new PythonModule("someOtherModule", ImportType.ORDINARY, parentModule));
		parentModule = new PythonModule(".module", ImportType.FROM);
		expectedModules.add(new PythonModule("function", ImportType.ORDINARY, parentModule));
		parentModule = new PythonModule("...module2", ImportType.FROM);
		expectedModules.add(new PythonModule("function2", ImportType.ORDINARY, parentModule));
		parentModule = new PythonModule(".module3", ImportType.FROM);
		expectedModules.add(new PythonModule("function3a", ImportType.ORDINARY, parentModule));
		expectedModules.add(new PythonModule("function3b", ImportType.ORDINARY, parentModule));
		parentModule = new PythonModule(".module4", ImportType.FROM);
		expectedModules.add(new AllModules(parentModule));
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that lines that start with import or from not followed by white
	 * spaces are ignored, and that the same holds for from import that lack
	 * the keyword "import".
	 */
	@Test
	public void testImportNonImportStatements() {
		String dataDir = "test/data/modules";
		String file = "nb_52.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that "import" is only considered a key word if it has spaces
	 * around it.
	 */
	@Test
	public void testImportImportNonKeyword() {
		String dataDir = "test/data/modules";
		String file = "nb_53.ipynb";
		PythonModule parent = new PythonModule("importlib", ImportType.FROM);
		PythonModule expectedModule = new PythonModule("reload", ImportType.ORDINARY, parent);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases containing Swedish letters are handled
	 * correctly
	 */
	@Test
	public void testImportSwedish() {
		String dataDir = "test/data/modules";
		String file = "nb_60.ipynb";
		PythonModule expectedModule = new PythonModule("äpple", "ö", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases containing Chinese letters are handled
	 * correctly
	 */
	@Test
	public void testImportChinese() {
		String dataDir = "test/data/modules";
		String file = "nb_61.ipynb";
		PythonModule expectedModule = new PythonModule("模組", "my碼", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases containing Cyrillic letters are handled
	 * correctly
	 */
	@Test
	public void testImportCyrrilic() {
		String dataDir = "test/data/modules";
		String file = "nb_62.ipynb";
		PythonModule expectedModule = new PythonModule("модуль", "myкод", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases containing Greek letters are handled
	 * correctly
	 */
	@Test
	public void testImportGreek() {
		String dataDir = "test/data/modules";
		String file = "nb_63.ipynb";
		PythonModule expectedModule = new PythonModule("μονάδα_μέτρησης", "myκώδικας", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases containing combining marks are handled
	 * correctly
	 */
	@Test
	public void testImportCombining() {
		String dataDir = "test/data/modules";
		String file = "nb_64.ipynb";
		PythonModule expectedModule = new PythonModule("modulé", "àpa", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that modules and aliases whose name start with "_" are handled
	 * correctly.
	 */
	@Test
	public void testImport_() {
		String dataDir = "test/data/modules";
		String file = "nb_65.ipynb";
		PythonModule expectedModule = new PythonModule("__module__", "__alias__", ImportType.ALIAS);
		verifyImport(getPath(dataDir, file), expectedModule);
	}
	
	/**
	 * Verify that a "from" import is ignored if the space is after import is
	 * missing, and there are no parentheses.
	 */
	@Test
	public void testFromImportMissingSpace() {
		String dataDir = "test/data/modules";
		String file = "nb_54.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that modules are is imported when in an invalid import statement.
	 */
	@Test
	public void testInvalidImport() {
		String dataDir = "test/data/modules";
		String file = "nb_100.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that no module is imported when its name is invalid.
	 */
	@Test
	public void testImportInvalidModuleNames() {
		String dataDir = "test/data/modules";
		String file = "nb_101.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(getPath(dataDir, file), expectedModules);
	}
	
	/**
	 * Verify that one module is imported correctly from notebook
	 * @param file Path to notebook
	 * @param expectedModules Module that is expected to be imported
	 */
	private void verifyImport(String file, PythonModule expectedModule) {
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(1);
		expectedModules.add(expectedModule);
		verifyImports(file, expectedModules);
	}
	
	/**
	 * Verify that modules are imported correctly from notebook
	 * @param file Path to notebook
	 * @param expectedModules List of modules that are expected to be imported, in the specified order
	 */
	private void verifyImports(String file, List<PythonModule> expectedModules) {
		Notebook notebook = new Notebook(file);
		List<PythonModule> modules = notebook.modules();
		assertEquals("Incorrect list of modules returned!",
				expectedModules, modules);

		for (int i=0; i<expectedModules.size(); i++) {
			PythonModule expectedModule = expectedModules.get(i);
			PythonModule actualModule = modules.get(i);
			assertEquals("Wrong function usages stored for module " + i + "!",
					expectedModule.functionUsages, actualModule.functionUsages);
		}
	}
	
	/**
	 * Verify that the right function call lists are returned for modules
	 * imported ordinarily and with alias.
	 */
	@Test
	public void testFunctionCalls() {
		String dataDir = "test/data/modules";
		String file = "nb_4.ipynb";
		Notebook notebook = new Notebook(getPath(dataDir, file));
		
		// Import type doesn't matter here.
		PythonModule parent1 = new PythonModule("module1");
		PythonModule f1 = new PythonModule("fun1", ImportType.ORDINARY, parent1);
		PythonModule f2 = new PythonModule("fun2", ImportType.ORDINARY, parent1);
		PythonModule f3 = new PythonModule("fun3", ImportType.ORDINARY, parent1);
		PythonModule f4 = new PythonModule("fun4", ImportType.ORDINARY, parent1);
		PythonModule parent2 = new PythonModule("module2");
		PythonModule f = new PythonModule("f", ImportType.ORDINARY, parent2);
		PythonModule[] argumentFunctions = {f1, f2, f3, f4, f};
		
		Map<PythonModule, List<String>> expectedFunctionCalls = new HashMap<PythonModule, List<String>>(5);
		List<String> f1Calls = new ArrayList<String>(1);
		f1Calls.add("module1.fun1(3)");
		expectedFunctionCalls.put(f1, f1Calls);
		List<String> f2Calls = new ArrayList<String>(0);
		expectedFunctionCalls.put(f2, f2Calls);
		List<String> f3Calls = new ArrayList<String>(1);
		f3Calls.add("module1.fun3()");
		expectedFunctionCalls.put(f3, f3Calls);
		List<String> f4Calls = new ArrayList<String>(0);
		expectedFunctionCalls.put(f4, f4Calls);
		List<String> fCalls = new ArrayList<String>(2);
		fCalls.add("mod2.f(mod2.f(0))");
		fCalls.add("mod2.f(0)");
		expectedFunctionCalls.put(f, fCalls);
		
		Map<PythonModule, List<String>> functionCalls = notebook.functionCalls(argumentFunctions);
		assertEquals("Wrong function calls returned!", expectedFunctionCalls, functionCalls);
	}
	
	/**
	 * Verify that the right function call lists are returned for modules
	 * imported ordinarily and with a from import.
	 */
	@Test
	public void testFunctionCallsFromImport() {
		String dataDir = "test/data/modules";
		String file = "nb_36.ipynb";
		Notebook notebook = new Notebook(getPath(dataDir, file));
		
		// Import type doesn't matter here.
		PythonModule base = new PythonModule("Base", ImportType.ORDINARY);
		PythonModule a = new PythonModule("A", ImportType.ORDINARY, base);
		PythonModule f1 = new PythonModule("fun1", ImportType.ORDINARY, a);
		PythonModule c = new PythonModule("C");
		PythonModule d = new PythonModule("D", ImportType.ORDINARY, c);
		PythonModule f2 = new PythonModule("fun2", ImportType.ORDINARY, d);
		PythonModule[] argumentFunctions = {f1, f2};
		
		Map<PythonModule, List<String>> expectedFunctionCalls = new HashMap<PythonModule, List<String>>(2);
		List<String> f1Calls = new ArrayList<String>(3);
		f1Calls.add("A.fun1(15, a=0, b=3)");
		f1Calls.add("a.fun1(a.fun1(0))");
		f1Calls.add("a.fun1(0)");
		expectedFunctionCalls.put(f1, f1Calls);
		List<String> f2Calls = new ArrayList<String>(1);
		f2Calls.add("C.D.fun2()");
		expectedFunctionCalls.put(f2, f2Calls);
		
		Map<PythonModule, List<String>> functionCalls = notebook.functionCalls(argumentFunctions);
		assertEquals("Wrong function calls returned!", expectedFunctionCalls, functionCalls);
	}
	
	/**
	 * Verify that calls to function imported with from imports are returned
	 * correctly.
	 */
	@Test
	public void testFunctionCallsFromFunctionImport() {
		String dataDir = "test/data/modules";
		String file = "nb_46.ipynb";
		Notebook notebook = new Notebook(getPath(dataDir, file));
		
		// Import type doesn't matter here.
		PythonModule parent = new PythonModule("math");
		PythonModule sin = new PythonModule("sin", ImportType.ORDINARY, parent);
		PythonModule cos = new PythonModule("cos", ImportType.ORDINARY, parent);
		PythonModule tan = new PythonModule("tan", ImportType.ORDINARY, parent);
		PythonModule atan = new PythonModule("atan", ImportType.ORDINARY, parent);
		PythonModule[] argumentFunctions = {sin, cos, tan, atan};
		
		Map<PythonModule, List<String>> expectedFunctionCalls = new HashMap<PythonModule, List<String>>(4);
		List<String> sinCalls = new ArrayList<String>(2);
		sinCalls.add("sin(s)");
		sinCalls.add("sin(a)");
		expectedFunctionCalls.put(sin, sinCalls);
		List<String> tanCalls = new ArrayList<String>(1);
		tanCalls.add("tan(a)");
		expectedFunctionCalls.put(tan, tanCalls);
		List<String> empty = new ArrayList<String>(0);
		expectedFunctionCalls.put(cos, empty);
		expectedFunctionCalls.put(atan, empty);
		
		Map<PythonModule, List<String>> functionCalls = notebook.functionCalls(argumentFunctions);
		assertEquals("Wrong function calls returned when functions are explicitely imported!",
				expectedFunctionCalls, functionCalls);
	}
	
	/**
	 * Verify that calls to functions imported with * are returned correctly.
	 */
	@Test
	public void testFunctionCallsAllModules() {
		String dataDir = "test/data/modules";
		String file = "nb_44.ipynb";
		Notebook notebook = new Notebook(getPath(dataDir, file));
		
		// Import type doesn't matter here.
		PythonModule parent = new PythonModule("math");
		PythonModule sin = new PythonModule("sin", ImportType.ORDINARY, parent);
		PythonModule cos = new PythonModule("cos", ImportType.ORDINARY, parent);
		PythonModule tan = new PythonModule("tan", ImportType.ORDINARY, parent);
		PythonModule atan = new PythonModule("atan", ImportType.ORDINARY, parent);
		PythonModule[] argumentFunctions = {sin, cos, tan, atan};
		
		Map<PythonModule, List<String>> expectedFunctionCalls = new HashMap<PythonModule, List<String>>(4);
		List<String> sinCalls = new ArrayList<String>(2);
		sinCalls.add("sin(s)");
		sinCalls.add("sin(a)");
		expectedFunctionCalls.put(sin, sinCalls);
		List<String> tanCalls = new ArrayList<String>(1);
		tanCalls.add("tan(a)");
		expectedFunctionCalls.put(tan, tanCalls);
		List<String> empty = new ArrayList<String>(0);
		expectedFunctionCalls.put(cos, empty);
		expectedFunctionCalls.put(atan, empty);
		
		Map<PythonModule, List<String>> functionCalls = notebook.functionCalls(argumentFunctions);
		assertEquals("Wrong function calls returned when functions are imported using *!",
				expectedFunctionCalls, functionCalls);
	}

	/**
	 * Verify that the number of code cells doesn't increase every time we
	 * fetch them (and both keys "cells" and "worksheets" are present in the
	 * notebook).
	 */
	@Test
	public void testRefBugFix() {
		Notebook notebook = new Notebook("test/data/refBug/nb_1390458.ipynb");
		assertEquals("Wrong number of code cells reported!", 1, notebook.numCodeCells());
		assertEquals("Wrong LOC reported!", 1, notebook.LOC());
		SnippetCode[] hashes = notebook.snippetCodes();
		assertEquals("Wrong number of hashes reported!", 1, hashes.length);
	}
	
	/**
	 * Verify that the right string representation is returned for a notebook,
	 * with and without directory names in the path.
	 */
	@Test
	public void testToString_fullPath() {
		String name = "nb_76.ipynb";
		String path = "some/nonexistent/path/" + name;
		// Only name
		Notebook notebook = new Notebook(name);
		assertEquals("Wrong string representation of notebook", name, notebook.toString());
		// Full path
		notebook = new Notebook(path);
		assertEquals("Wrong string representation of notebook", name, notebook.toString());
	}
	
	/**
	 * @param dir Name of directory
	 * @param file Name of file
	 * @return The path to file, which lives in dir 
	 */
	private String getPath(String dir, String file) {
		return dir + File.separator + file;
	}
}
