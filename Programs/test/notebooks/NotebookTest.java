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
			Notebook nb = new Notebook(dataDir + File.separator + nbFile);
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
			Notebook nb = new Notebook(dataDir + File.separator + nbFile + ".ipynb");
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
			Notebook nb = new Notebook(dataDir + File.separator + nbFile);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
			Notebook notebook = new Notebook(dataDir + File.separator + files[i]);
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
		Notebook notebook = new Notebook(dataDir + File.separator + file);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
	}
	
	/**
	 * Verify that a single Python module import with alias can be identified.
	 */
	@Test
	public void testSingleImportWithAlias() {
		String dataDir = "test/data/modules";
		String file = "nb_2.ipynb";
		PythonModule expectedModule = new PythonModule("kalv", "naut", ImportType.ALIAS);
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
	}
	
	/**
	 * Verify that a module in a "from" import statement can be identified
	 * correctly when it is a sub module of other module(s).
	 */
	@Test
	public void testFromImportWithSubmodule() {
		String dataDir = "test/data/modules";
		String file = "nb_32.ipynb";
		PythonModule expectedGrandParent = new PythonModule("Base", ImportType.FROM);
		PythonModule expectedParent = new PythonModule("A", ImportType.ORDINARY, expectedGrandParent);
		PythonModule expectedModule = new PythonModule("B", ImportType.ORDINARY, expectedParent);
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		PythonModule expectedGrandParent = new PythonModule("Base", ImportType.FROM);
		PythonModule expectedParent1 = new PythonModule("A", ImportType.ORDINARY, expectedGrandParent);
		PythonModule expectedParent2 = new PythonModule("C", ImportType.ORDINARY, expectedGrandParent);
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(1);
		expectedModules.add(new PythonModule("B", "sub", ImportType.ALIAS, expectedParent1));
		expectedModules.add(new PythonModule("D", ImportType.ORDINARY, expectedParent2));
		expectedModules.add(new PythonModule("E", "sub2", ImportType.ALIAS, expectedGrandParent));
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImport(dataDir + File.separator + file, expectedModule);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
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
		verifyImports(dataDir + File.separator + file, expectedModules);
	}
	
	/**
	 * Verify that modules are is imported when in an invalid import statement.
	 */
	@Test
	public void testInvalidImport() {
		String dataDir = "test/data/modules";
		String file = "nb_100.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(dataDir + File.separator + file, expectedModules);
	}
	
	/**
	 * Verify that no module is imported when its name is invalid.
	 */
	@Test
	public void testImportInvalidModuleNames() {
		String dataDir = "test/data/modules";
		String file = "nb_101.ipynb";
		List<PythonModule> expectedModules = new ArrayList<PythonModule>(0);
		verifyImports(dataDir + File.separator + file, expectedModules);
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
}
