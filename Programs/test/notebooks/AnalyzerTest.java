package notebooks;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.*;
import notebooks.Analyzer;
import notebooks.LangSpec;
import notebooks.Language;
import notebooks.Snippet;

public class AnalyzerTest {
	
	private Analyzer analyzer;
	
	@Before
	public void setUp() {
		analyzer = new Analyzer();
	}
	
	@After
	public void tearDown() {
		analyzer.shutDown();
	}
	
	@Test
	public void testAllAnalyzes() throws IOException { 
		String testDir = "test/data/all";
		String fileName = "single_import_diff_langs.ipynb";
		String snippetHash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedCodeCellsLines = {codeCellsHeader(),
				fileName + ", 1"};
		String[] expectedLOCLines = {LOCHeader(),
				fileName + ", 2, 1, 1"};
		String[] expectedLangLines = {languagesHeader(),
					fileName + ", " + Language.JULIA + ", " + LangSpec.METADATA_LANGUAGEINFO_NAME
				};
		String[] expectedAllLangLines = {allLanguagesHeader(),
					fileName + ", " + Language.SCALA + ", " + Language.JULIA + ", "
					+ Language.R + ", " + Language.OTHER + ", " + Language.PYTHON
				};
		String[] expectedFile2hashesLines = {file2hashesHeader(),
				fileName + ", " + snippetHash};
		String[] expectedHash2filesLines = {hash2filesHeader(),
				snippetHash + ", 1, " + fileName + ", 0"};
		String[] expectedCloneFreqLines = {cloneFrequencyHeader(),
				fileName + ", 0, 1, 0.0000"};
		String[] expectedConnectionsLines = {connectionsHeader(),
				fileName + ", 0, 0.0000, 0, 0.0000"};
		
		analyzer.initializeNotebooksFrom(testDir + "/" + fileName);
		analyzer.allAnalyzes();
		
		checkCsv("code_cells", expectedCodeCellsLines);
		checkCsv("loc", expectedLOCLines);
		checkCsv("languages", expectedLangLines);
		checkCsv("all_languages", expectedAllLangLines);
		checkCsv("file2hashes", expectedFile2hashesLines);
		checkCsv("hash2files", expectedHash2filesLines);
		checkCsv("cloneFrequency", expectedCloneFreqLines);
		checkCsv("connections", expectedConnectionsLines);
		
		lastOutputFile("code_cells").delete();
		lastOutputFile("loc").delete();
		lastOutputFile("languages").delete();
		lastOutputFile("all_languages").delete();
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output file allLanguageValues<current-date-time>.csv has
	 * the right content after analysis of all language value fields.
	 * @throws IOException 
	 */
	@Test
	public void testAllLanguageValues() throws IOException {
		String dataDir = "test/data/langFields";
		String[] files = {"all_lang_specs.ipynb", "empty.ipynb", "no_kernelspec.ipynb"};
		String[] expectedLines = {
				allLanguagesHeader(),
				files[0] + ", " + Language.JULIA + ", " + Language.PYTHON + ", "	+ Language.R + ", " + Language.OTHER + ", " + Language.SCALA, 
				files[1] + ", " + Language.UNKNOWN + ", " + Language.UNKNOWN + ", "	+ Language.UNKNOWN + ", " + Language.UNKNOWN + ", " + Language.UNKNOWN,
				files[2] + ", " + Language.R + ", " + Language.JULIA + ", "	+ Language.UNKNOWN + ", " + Language.UNKNOWN + ", " + Language.SCALA
		};
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.allLanguageValues();
		checkCsv("all_languages", expectedLines);
		lastOutputFile("all_languages").delete();
		
	}
	
	/**
	 * Verify that all language values are set to UNKNOWN when
	 * allLanguageValues is called for a notebook with a non parseable file. 
	 * @throws IOException
	 */
	@Test
	public void testAllLanguageValues_nonParseable() throws IOException {
		String dataDir = "test/data/langFields";
		String file = "non_parseable.ipynb";
		String[] expectedLInes = {
				"file, " + LangSpec.METADATA_LANGUAGE + ", " + LangSpec.METADATA_LANGUAGEINFO_NAME
				+ ", " + LangSpec.METADATA_KERNELSPEC_LANGUAGE + ", " + LangSpec.METADATA_KERNELSPEC_NAME
				+ ", " + LangSpec.CODE_CELLS,	// header
				file + ", " + Language.UNKNOWN + ", " + Language.UNKNOWN + ", "	+ Language.UNKNOWN + ", " + Language.UNKNOWN + ", " + Language.UNKNOWN
		};
		analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		analyzer.allLanguageValues();
		checkCsv("all_languages", expectedLInes);
		lastOutputFile("all_languages").delete();
	}

	/**
	 * Verify that snippets are stored correctly in the clone hash map.
	 * @throws IOException 
	 */
	@Test
	public void testClones() throws IOException {
		String dataDir = "test/data/hash";
		String[] files = {"empty_code_string.ipynb", "empty_code_strings.ipynb",
				"missing_cells.ipynb", "single_import.ipynb", "two_import_cells.ipynb",
				"intra_clones.ipynb", "intra_clones_and_unique.ipynb"
		};
		// Expected values
		Map<SnippetCode, List<Snippet>> expectedClones = new HashMap<SnippetCode, List<Snippet>>();
		List<Snippet> emptySnippets = new ArrayList<Snippet>(2);
		emptySnippets.add(new Snippet("empty_code_string.ipynb", 0));
		emptySnippets.add(new Snippet("empty_code_strings.ipynb", 0));
		expectedClones.put(new SnippetCode(0, "D41D8CD98F00B204E9800998ECF8427E"), emptySnippets);	// Empty
		List<Snippet> numpy = new ArrayList<Snippet>(2);
		numpy.add(new Snippet("single_import.ipynb", 0));
		numpy.add(new Snippet("two_import_cells.ipynb", 0));
		expectedClones.put(new SnippetCode(1, "33BE8D72467938FBB23EF42CF8C9E85F"), numpy); // import numpy
		List<Snippet> pandas = new ArrayList<Snippet>(1);
		pandas.add(new Snippet("two_import_cells.ipynb", 1));
		expectedClones.put(new SnippetCode(1, "6CABFDBC20F69189D4B8894A06C78F49"), pandas); // import pandas
		List<Snippet> kossa = new ArrayList<Snippet>(4);
		kossa.add(new Snippet("intra_clones.ipynb", 0));
		kossa.add(new Snippet("intra_clones.ipynb", 1));
		kossa.add(new Snippet("intra_clones_and_unique.ipynb", 0));
		kossa.add(new Snippet("intra_clones_and_unique.ipynb", 2));
		expectedClones.put(new SnippetCode(1, "0120F99AA7C49E1CD5F4EE4A6BB1CC4A"), kossa);
		List<Snippet> unique = new ArrayList<Snippet>(1);
		unique.add(new Snippet("intra_clones_and_unique.ipynb", 1));
		expectedClones.put(new SnippetCode(1, "A2D53E3DA394A52271CF00632C961D2A"), unique);
		
		// Actual values
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		Map<SnippetCode, List<Snippet>> clones = analyzer.clones();
		
		// Check values
		assertTrue(expectedClones.keySet().equals(clones.keySet()));
		for (SnippetCode snippetCode: expectedClones.keySet()) {
			List<Snippet> expectedSnippets = expectedClones.get(snippetCode);
			List<Snippet> actualSnippets = clones.get(snippetCode);
			assertEquals("Wrong number of snippets stored for " + snippetCode + ":", expectedSnippets.size(), actualSnippets.size());
			assertTrue("Wrong snippets stored for " + snippetCode, actualSnippets.containsAll(expectedSnippets));
		}
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv,
	 * hash2files<current-date-time>.csv cloneFrequency<current-date-time>.csv
	 * and connections<current-date-time>.csv have the right content after
	 * clone analysis of an empty notebook.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_emptyNotebook() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "missing_cells.ipynb";
		String[] expectedSnippetLines = {
				file2hashesHeader()
		};
		String[] expectedClonesLines = {
				hash2filesHeader()
		};
		String[] expectedFrequencyLines = {
				cloneFrequencyHeader(),
				fileName + ", 0, 0, 0"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				fileName + ", 0, 0.0000, 0, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedSnippetLines);
		checkCsv("hash2files", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv,
	 * hash2files<current-date-time>.csv, cloneFrequency<current-date-time>.csv
	 * and connections<current-date-time>.csv have the right content after
	 * clone analysis of a notebook with a single snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_singleSnippet() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "single_import.ipynb";
		String hash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedSnippetLines = {
				file2hashesHeader(),
				fileName + ", " + hash
		};
		String[] expectedClonesLines = {
				hash2filesHeader(),
				hash + ", 1, " + fileName + ", 0"
		};
		String[] expectedFrequencyLiens = {
				cloneFrequencyHeader(),
				fileName + ", 0, 1, 0.0000"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				fileName + ", 0, 0.0000, 0, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedSnippetLines);
		checkCsv("hash2files", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLiens);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv, 
	 * hash2files<current-date-time>.csv, cloneFrequency<current-date-time>.csv
	 * and connections<current-date-time>.csv have the right content after
	 * clone analysis of a notebooks with a clone.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_intraClone() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "intra_clones.ipynb";
		String hash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String[] expectedFile2HashesLines = {
				file2hashesHeader(),
				fileName + ", " + hash + ", " + hash
		};
		String[] expectedHash2FileLines = {
				hash2filesHeader(),
				hash + ", 1, " + fileName + ", 0, " + fileName + ", 1"
		};
		String[] expectedFrequencyLines = {
				cloneFrequencyHeader(),
				fileName + ", 2, 0, 1.0000"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				fileName + ", 2, 1.0000, 2, 1.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedFile2HashesLines);
		checkCsv("hash2files", expectedHash2FileLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashes<current-date-time>.csv,
	 * cloneFrequency<current-date-time>.csv and
	 * connections<current-date-time>.csv have the right content after clone
	 * analysis of a notebooks with both clones and a unique snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_mixed() throws IOException {
		String dataDir = "test/data/hash";
		String fileName = "intra_clones_and_unique.ipynb";
		String kossaHash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String uniqueHash = "A2D53E3DA394A52271CF00632C961D2A";
		String[] expectedFile2HashesLines = {
			file2hashesHeader(),
			fileName + ", " + kossaHash + ", " + uniqueHash + ", " + kossaHash
		};
		// hash2Files is hard to test since we don't know in which order the hashes are stored
		String[] expectedFrequencyLines = {
			cloneFrequencyHeader(),
			fileName + ", 2, 1, 0.6667"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			fileName + ", 2, 0.6667, 2, 0.6667"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + fileName);
		analyzer.clones();
		
		checkCsv("file2hashes", expectedFile2HashesLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that connections are identified correctly at analysis of the whole
	 * clone test files directory.
	 * @throws IOException 
	 */
	@Test
	public void testConnectionsCsv_all() throws IOException {
		String dataDir = "test/data/hash";
		String[] expectedLines = {
				connectionsHeader(),
				"empty_code_string.ipynb, 1, 1.0000, 0, 0.0000",
				"empty_code_strings.ipynb, 1, 1.0000, 0, 0.0000",
				"intra_clones.ipynb, 6, 3.0000, 6, 3.0000",
				"intra_clones_and_unique.ipynb, 6, 2.0000, 6, 2.0000",
				"missing_cells.ipynb, 0, 0.0000, 0, 0.0000",
				"single_import.ipynb, 1, 1.0000, 1, 1.0000",
				"two_import_cells.ipynb, 1, 0.5000, 1, 0.5000",
		};
		
		analyzer.initializeNotebooksFrom(dataDir);
		analyzer.clones();
		
		checkCsv_anyOrder("connections", expectedLines);
		
		deleteCloneCsvs();
	}
	
	
	/**
	 * Verify that the right languages are found in the notebooks.
	 * @throws IOException
	 */
	@Test
	public void testLanguage_values() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/lang");
		Map<Language, Integer> expected = new HashMap<Language, Integer>();
		expected.put(Language.PYTHON, 6);
		expected.put(Language.JULIA, 3);
		expected.put(Language.R, 2);
		expected.put(Language.SCALA, 3);
		expected.put(Language.OTHER, 1);
		expected.put(Language.UNKNOWN, 9);
		Map<Language, Integer> actual = analyzer.languages();
		assertEquals("Error in language extraction:", expected, actual);
		lastOutputFile("languages").delete();
	}
	
	/**
	 * Verify that the output file languages<current-date-time>.csv is created
	 * and filled correctly when the languages are extracted.
	 * @throws IOException
	 */
	@Test
	public void testLanguage_csv() throws IOException {
		String dataDir = "test/data/lang";
		String[] files = {"k_l_cpp.ipynb", "k_l_R.ipynb", "li_n_python.ipynb"};
		Language[] languages = {Language.OTHER, Language.R, Language.PYTHON};
		LangSpec[] langSpecs = {LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_LANGUAGEINFO_NAME};
		String[] expectedLines = new String[languages.length+1];
		expectedLines[0] = languagesHeader();
		for (int i=0; i<languages.length; i++) {
			expectedLines[i+1] = files[i] + ", " + languages[i] + ", " + langSpecs[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.languages();
		
		checkCsv("languages", expectedLines);
		
		lastOutputFile("languages").delete();
	}
	
	/**
	 * Verify that the right number of code lines are found in the notebooks
	 * under a directory.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/loc");
		assertEquals("Wrong LOC!", 55, analyzer.LOC());
		lastOutputFile("loc").delete();
	}
	
	/**
	 * Verify that the output file loc<current-date-time>.csv is created and
	 * filled correctly when the number of LOC are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testLOC_csv() throws IOException {
		String dataDir = "test/data/loc";
		String[] files = {"code_and_md_3loc.ipynb", "markdownCells.ipynb",
				"two_codeCells_13loc.ipynb"};
		int[] LOC = {3, 0, 13};
		int[] emptyLOC = {0, 0, 2};
		String[] expectedLines = new String[LOC.length+1];
		expectedLines[0] = LOCHeader();
		for (int i=0; i<LOC.length; i++) {
			expectedLines[i+1] = files[i] + ", " + LOC[i] + ", " + (LOC[i]-emptyLOC[i]) + ", " + emptyLOC[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.LOC();
		
		checkCsv("loc", expectedLines);	

		lastOutputFile("loc").delete();
	}
	
	/**
	 * Verify that the right number of cells are found in the notebooks under a
	 * directory.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_total() throws IOException {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of cells found in notebooks!", 15, analyzer.numCodeCells());
		lastOutputFile("code_cells").delete();
	}
	
	/**
	 * Verify that the output file code_cells<current-date-time>.csv is
	 * created and filled correctly when the number of cells are counted.
	 * @throws IOException on errors when handling output file
	 */
	@Test
	public void testNumCodeCells_csv() throws IOException {
		String dataDir = "test/data/count";
		String[] files = {"zero.ipynb", "one.ipynb", "three_with_md.ipynb"};
		int[] numCodeCells = {0, 1, 3};
		String[] expectedLines = new String[numCodeCells.length+1];
		expectedLines[0] = codeCellsHeader();
		for (int i=0; i<numCodeCells.length; i++) {
			expectedLines[i+1] = files[i] + ", " + numCodeCells[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.numCodeCells();
		
		checkCsv("code_cells", expectedLines);
		
		lastOutputFile("code_cells").delete();
	}
	
	/**
	 * @return Expected header of snippet files
	 */
	private static String codeCellsHeader() {
		return "file, code cells";
	}

	/**
	 * Verify that the right number of notebooks are found under a directory.
	 */
	@Test
	public void testNumNotebooks() {
		analyzer.initializeNotebooksFrom("test/data/count");
		assertEquals("Wrong number of notebooks found:", 12, analyzer.numNotebooks());
	}
	
	/**
	 * @return Expected header of all_languages files
	 */
	private static String allLanguagesHeader() {
		return "file, " + LangSpec.METADATA_LANGUAGE + ", " + LangSpec.METADATA_LANGUAGEINFO_NAME
				+ ", " + LangSpec.METADATA_KERNELSPEC_LANGUAGE + ", " + LangSpec.METADATA_KERNELSPEC_NAME
				+ ", " + LangSpec.CODE_CELLS;
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv has the right
	 * content.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, in order
	 */
	private void checkCsv(String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			assertEquals("Wrong line number " + (i+1) + " for " + prefix + " csv!", expectedLine, outputReader.readLine());
		}
		outputReader.close();
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv contains all
	 * lines in expectedLines.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, not necessarily in order
	 */
	private void checkCsv_anyOrder(String prefix, String[] expectedLines) throws FileNotFoundException {
		File outputFile = lastOutputFile(prefix);
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			boolean exists = false;
			Scanner outputReader = new Scanner(outputFile);
			while (outputReader.hasNext() && false == exists) {
				if (outputReader.nextLine().equals(expectedLine)) {
					exists = true;
				}
			}
			outputReader.close();
			assertTrue("The line " + expectedLine + " cannot be found in " + prefix + " csv!", exists);
		}
	}
	
	/**
	 * @return Expected header of cloneFrequency files
	 */
	private static String cloneFrequencyHeader() {
		return "file, clones, unique, clone frequency";
	}
	
	/**
	 * @return Expected header of connections files
	 */
	private static String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized";
	}
	
	/**
	 * Delete all CSV files created by the clone analysis. 
	 */
	private void deleteCloneCsvs() {
		lastOutputFile("file2hashes").delete();
		lastOutputFile("hash2files").delete();
		lastOutputFile("cloneFrequency").delete();
		lastOutputFile("connections").delete();
	}
	
	/**
	 * @return Expected header of file2hash files
	 */
	private static String file2hashesHeader() {
		return "file, snippets";
	}
	
	/**
	 * @return Expected header of hash2files files
	 */
	private static String hash2filesHeader() {
		return "hash, LOC, file, index, ...";
	}
	
	/**
	 * @return Expected header of languages files
	 */
	private static String languagesHeader() {
		return "file, language, language spec in";
	}
	
	/**
	 * Find the output file <prefix><timestamp>.csv with the greatest (latest)
	 * time stamp.
	 * @param prefix First part of the output file
	 * @return Output file described above 
	 */
	private File lastOutputFile(String prefix) {
		File directory = new File(".");
		String outputFileName = prefix + ".csv";
		for (String currentFileName: directory.list()) {
			if (currentFileName.matches(prefix + "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\.csv")
					&& currentFileName.compareTo(outputFileName) > 0) {
				outputFileName = currentFileName;
			}
		}
		return new File(outputFileName);
	}
	
	/**
	 * @return Expected header of LOC file
	 */
	private static String LOCHeader() {
		return "file, total LOC, non-blank LOC, blank LOC";
	}

}
