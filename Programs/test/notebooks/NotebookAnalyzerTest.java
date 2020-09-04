package notebooks;

import static org.junit.Assert.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.*;
import notebooks.NotebookAnalyzer;
import notebooks.LangSpec;
import notebooks.LangName;
import notebooks.Snippet;

public class NotebookAnalyzerTest extends AnalyzerTest {
	private NotebookAnalyzer analyzer;
	
	@BeforeClass
	public static void setUpOutputDirectory() {
		defaultOutputDirName = "notebook_analyzer_unit_test_output";
		AnalyzerTest.setUpOutputDirectory();
	}
	
	@Before
	public void setUp() {
		analyzer = new NotebookAnalyzer();
		analyzer.outputDir = defaultOutputDirName;
		cloneFilePrefixes = new String[4];
		cloneFilePrefixes[0] = "file2hashesA";
		cloneFilePrefixes[1] = "hash2filesA";
		cloneFilePrefixes[2] = "cloneFrequency";
		cloneFilePrefixes[3] = "connections";
	}
	
	@AfterClass
	public static void tearDown() {
		tearDownClass();
	}
	
	@Test
	public void testAllAnalyzes() throws IOException { 
		String testDir = "test/data/all";
		String notebookFile = "nb_1.ipynb";
		String reproFile = "repros.csv";
		String snippetHash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedCodeCellsLines = {codeCellsHeader(),
				notebookFile + ", 1"};
		String[] expectedLOCLines = {LOCHeader(),
				notebookFile + ", 2, 1, 1"};
		String[] expectedLangLines = {languagesHeader(),
					notebookFile + ", " + LangName.JULIA + ", " + LangSpec.METADATA_LANGUAGEINFO_NAME
				};
		String[] expectedAllLangLines = {allLanguagesHeader(),
					notebookFile + ", " + LangName.SCALA + ", " + LangName.JULIA + ", "
					+ LangName.R + ", " + LangName.OTHER + ", " + LangName.PYTHON
				};
		String[] expectedFile2hashesLines = {file2hashesHeader(),
				notebookFile + ", " + snippetHash};
		String[] expectedHash2filesLines = {hash2filesHeader(),
				snippetHash + ", 1, " + notebookFile + ", 0"};
		String[] expectedCloneFreqLines = {cloneFrequencyHeader(),
				notebookFile + ", 1, 0, 0, 0.0000, 0.0000, 0, 0"};
		String[] expectedConnectionsLines = {connectionsHeader(),
				notebookFile + ", 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000"};
		
		analyzer.initializeNotebooksFrom(testDir + File.separator + notebookFile);
		analyzer.initializeReproInfo(testDir + File.separator + reproFile);
		analyzer.allAnalyzes();
		
		checkCsv("code_cells", expectedCodeCellsLines);
		checkCsv("loc", expectedLOCLines);
		checkCsv("languages", expectedLangLines);
		checkCsv("all_languages", expectedAllLangLines);
		checkCsv("file2hashesA", expectedFile2hashesLines);
		checkCsv("hash2filesA", expectedHash2filesLines);
		checkCsv("cloneFrequency", expectedCloneFreqLines);
		checkCsv("connections", expectedConnectionsLines);
		
		lastOutputFile("code_cells").delete();
		lastOutputFile("loc").delete();
		lastOutputFile("languages").delete();
		lastOutputFile("all_languages").delete();
		deleteCloneCsvs();
	}
	
	@Test
	public void testAllAnalyzes_error() throws IOException {
		String testDir = "test/data/all";
		String notebookFile = "empty.ipynb";
		String[] expectedCodeCellsLines = {codeCellsHeader(),
				notebookFile + ", 0"};
		String[] expectedLOCLines = {LOCHeader(),
				notebookFile + ", 0, 0, 0"};
		String[] expectedLangLines = {languagesHeader(),
					notebookFile + ", " + LangName.UNKNOWN + ", " + LangSpec.NONE
				};
		String[] expectedAllLangLines = {allLanguagesHeader(),
					notebookFile + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", "
					+ LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN
				};
		String[] expectedFile2hashesLines = {file2hashesHeader(),
				notebookFile};
		String[] expectedHash2filesLines = {hash2filesHeader()};
		String[] expectedCloneFreqLines = {cloneFrequencyHeader(),
				notebookFile + ", 0, 0, 0, 0, 0, 0, 0"};
		String[] expectedConnectionsLines = {connectionsHeader(),
				notebookFile + ", 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000"};
		
		analyzer.initializeNotebooksFrom(testDir + File.separator + notebookFile);
		analyzer.allAnalyzes();
		
		checkCsv("code_cells", expectedCodeCellsLines);
		checkCsv("loc", expectedLOCLines);
		checkCsv("languages", expectedLangLines);
		checkCsv("all_languages", expectedAllLangLines);
		checkCsv("file2hashesA", expectedFile2hashesLines);
		checkCsv("hash2filesA", expectedHash2filesLines);
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
				files[0] + ", " + LangName.JULIA + ", " + LangName.PYTHON + ", "	+ LangName.R + ", " + LangName.OTHER + ", " + LangName.SCALA, 
				files[1] + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", "	+ LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN,
				files[2] + ", " + LangName.R + ", " + LangName.JULIA + ", "	+ LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", " + LangName.SCALA
		};
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
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
				file + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", "	+ LangName.UNKNOWN + ", " + LangName.UNKNOWN + ", " + LangName.UNKNOWN
		};
		analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		analyzer.allLanguageValues();
		checkCsv("all_languages", expectedLInes);
		lastOutputFile("all_languages").delete();
	}
	
	/**
	 * Verify that the output files for number of code cells, loc, languages,
	 * all language values, and clone output files are created when the
	 * argument "-all" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_allAnalyses() throws IOException {
		String[] arg = {"--all"};
		String[] expectedFilePrefixes = {
				"code_cells",
				"loc",
				"languages",
				"all_languages",
				"file2hashesA",
				"hash2filesA",
				"cloneFrequency",
				"connections"
		};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefixes);
	}
	
	/**
	 * Verify that the clone output files are created when the argument
	 * "-clones" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_clones() throws IOException {
		String[] arg = {"--clones"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemoveCloneFiles();
	}
	
	/**
	 * Verify that output file with number of code cells are created when the
	 * argument "-count" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_count() throws IOException {
		String[] arg = {"--count"};
		String[] expectedFilePrefix = {"code_cells"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefix);
	}
	
	/**
	 * Verify that the language output file is created when the argument
	 * "-lang" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_lang() throws IOException {
		String[] arg = {"--lang"};
		String[] expectedFilePrefix = {"languages"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefix);
	}
	
	/**
	 * Verify that the all language values output file is created when the
	 * argument "-lang_all" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_lang_all() throws IOException {
		String[] arg = {"--lang_all"};
		String[] expectedFilePrefix = {"all_languages"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefix);
	}
	
	/**
	 * Verify that the loc output file is created when the argument "-loc" is
	 * given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_loc() throws IOException {
		String[] arg = {"--loc"};
		String[] expectedFilePrefix = {"loc"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefix);
	}
	
	/**
	 * Verify that the modules output file is created when the argument
	 * "--modules" is given.
	 * @throws IOException
	 */
	@Test
	public void testArgumentParsing_modules() throws IOException {
		String[] arg = {"--modules"};
		String[] expectedFilePrefix = {"modules"};
		analyzer.analyze(arg);
		verifyExistenceOfAndRemove(expectedFilePrefix);
	}
	
	/**
	 * Verify that all relevant files are created when several arguments are
	 * given.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_severalArgs() throws IOException {
		String[] args = {"--lang_all", "--lang", "--count", "--loc"};
		String[] expectedFilePrefixes = {
				"all_languages",
				"languages",
				"code_cells",
				"loc"
		};
		analyzer.analyze(args);
		verifyExistenceOfAndRemove(expectedFilePrefixes);
	}
	
	/**
	 * Verify that analyze runs smooth when an unknown argument is given.
	 */
	@Test
	public void testArguentParsing_unknownArg() {
		String[] args = {"--unknown"};
		analyzer.analyze(args);
	}
	
	/**
	 * Verify that the right notebook(s) are analyzed when the nb_path argument
	 * is given.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_nbPath() throws IOException {
		String[] args = {
				"--count",
				"--nb_path=test/data/count/two.ipynb"
		};
		String[] expectedLines = {
				codeCellsHeader(),
				"two.ipynb, 2"
		};
		analyzer.analyze(args);
		checkCsv("code_cells", expectedLines);
		lastOutputFile("code_cells").delete();
	}
	
	/**
	 *  Verify that analyze runs smoothly when notebook path is not specified.
	 */
	@Test
	public void testArgumentParsing_nbPathValueMissing() {
		String[] args = {
				"--nb_path"
		};
		analyzer.analyze(args);
	}
	
	/**
	 * Verify that the output directory is set correctly when given as an
	 * argument.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_outputDir() throws IOException {
		String outputDir = "test";
		String[] args = {
				"--count",
				"--output_dir=" + outputDir
		};
		String[] expectedOutputPrefix = {"code_cells"};
		analyzer.analyze(args);
		TestUtils.verifyExistenceOfAndRemove(outputDir, expectedOutputPrefix);
	}
	
	/**
	 * Verify that repro information is initialized correctly (=> connections
	 * file is correct) when repro file path is specified.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_reproFile() throws IOException {
		String[] args = {
				"--nb_path=test/data/arg",
				"--repro_file=test/data/arg/repros.csv",
				"--clones"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				"nb_1.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				"nb_2.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000"
		};
		analyzer.analyze(args);
		checkCsv_anyOrder("connections", expectedConnectionsLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that clone analysis runs smoothly also when repro information is
	 * missing.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_clonesWithoutReproFile() throws IOException {
		String[] args = {
				"--clones",
				"--nb_path=test/data/arg"
		};
		String[] expectedConnectionLines = {
				connectionsHeader(),
				"nb_1.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000",
				"nb_2.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000"
		};
		analyzer.analyze(args);
		checkCsv_anyOrder("connections", expectedConnectionLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that clone analysis runs smoothly also when repro information is
	 * missing. 
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_clonesWithoutReproFileValue() throws IOException {
		String[] args = {
				"--clones",
				"--nb_path=test/data/arg",
				"--repro_file"
		};
		String[] expectedConnectionLines = {
				connectionsHeader(),
				"nb_1.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000",
				"nb_2.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000"
		};
		analyzer.analyze(args);
		checkCsv_anyOrder("connections", expectedConnectionLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that clone analysis runs smoothly also when repro file doesn't
	 * exist.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_nonExistentReproFile() throws IOException {
		String[] args = {
				"--clones",
				"--nb_path=test/data/arg",
				"--repro_file=nonexistent_file"
		};
		String[] expectedConnectionLines = {
				connectionsHeader(),
				"nb_1.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000",
				"nb_2.ipynb, 1, 1.0000, 1, 1.0000, 1, 1, 0.0000, 0.0000"
		};
		analyzer.analyze(args);
		checkCsv_anyOrder("connections", expectedConnectionLines);
		deleteCloneCsvs();
	}

	/**
	 * Verify that snippets are stored correctly in the clone hash map.
	 * @throws IOException 
	 */
	@Test
	public void testClones() throws IOException {
		String dataDir = "test/data/hash";
		String[] files = {"nb_4.ipynb", "nb_5.ipynb",
				"nb_100.ipynb", "nb_6.ipynb", "nb_7.ipynb",
				"nb_1.ipynb", "nb_2.ipynb"
		};
		String reproFile = "repros.csv";
		// Expected values
		Map<SnippetCode, List<Snippet>> expectedClones = new HashMap<SnippetCode, List<Snippet>>();
		List<Snippet> emptySnippets = new ArrayList<Snippet>(3);
		emptySnippets.add(new Snippet("nb_4.ipynb", 0));
		emptySnippets.add(new Snippet("nb_4.ipynb", 1));
		emptySnippets.add(new Snippet("nb_5.ipynb", 0));
		expectedClones.put(new SnippetCode(0, "D41D8CD98F00B204E9800998ECF8427E"), emptySnippets);	// Empty
		List<Snippet> numpy = new ArrayList<Snippet>(2);
		numpy.add(new Snippet("nb_6.ipynb", 0));
		numpy.add(new Snippet("nb_6.ipynb", 1));
		numpy.add(new Snippet("nb_7.ipynb", 0));
		expectedClones.put(new SnippetCode(1, "33BE8D72467938FBB23EF42CF8C9E85F"), numpy); // import numpy
		List<Snippet> pandas = new ArrayList<Snippet>(1);
		pandas.add(new Snippet("nb_7.ipynb", 1));
		expectedClones.put(new SnippetCode(1, "6CABFDBC20F69189D4B8894A06C78F49"), pandas); // import pandas
		List<Snippet> kossa = new ArrayList<Snippet>(4);
		kossa.add(new Snippet("nb_1.ipynb", 0));
		kossa.add(new Snippet("nb_1.ipynb", 1));
		kossa.add(new Snippet("nb_2.ipynb", 0));
		kossa.add(new Snippet("nb_2.ipynb", 2));
		expectedClones.put(new SnippetCode(1, "0120F99AA7C49E1CD5F4EE4A6BB1CC4A"), kossa);
		List<Snippet> nonUnique = new ArrayList<Snippet>(1);
		nonUnique.add(new Snippet("nb_2.ipynb", 1));
		expectedClones.put(new SnippetCode(1, "8D91DA91141E24A95233199750861876"), nonUnique);
		List<Snippet> somePackage = new ArrayList<Snippet>(2);
		somePackage.add(new Snippet("nb_7.ipynb", 2));
		expectedClones.put(new SnippetCode(1, "5CA918CC7C216AF51875415D3FE5C21F"), somePackage);
		List<Snippet> f = new ArrayList<Snippet>(2);
		f.add(new Snippet("nb_5.ipynb", 1));
		expectedClones.put(new SnippetCode(1, "ECE926D8C0356205276A45266D361161"), f);
		
		// Actual values
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
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
	 * Verify that the clone output files have the right content after clone
	 * analysis of an empty notebook.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_emptyNotebook() throws IOException {
		String dataDir = "test/data/hash";
		String notebookFile = "nb_100.ipynb";
		String reproMapName = "repros.csv";
		String[] expectedSnippetLines = {
				file2hashesHeader(),
				notebookFile
		};
		String[] expectedClonesLines = {
				hash2filesHeader()
		};
		String[] expectedFrequencyLines = {
				cloneFrequencyHeader(),
				notebookFile + ", 0, 0, 0, 0, 0, 0, 0"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + File.separator + notebookFile);
		analyzer.initializeReproInfo(dataDir + File.separator + reproMapName);
		analyzer.clones();
		
		checkCsv("file2hashesA", expectedSnippetLines);
		checkCsv("hash2filesA", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the clone output files have the right content after clone
	 * analysis of a notebook with a single snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_singleSnippet() throws IOException {
		String dataDir = "test/data/hash";
		String notebookFile = "nb_6.ipynb";
		String reproFile = "repros.csv";
		String hash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedSnippetLines = {
				file2hashesHeader(),
				notebookFile + ", " + hash + ", " + hash
		};
		String[] expectedClonesLines = {
				hash2filesHeader(),
				hash + ", 1, " + notebookFile + ", 0, " + notebookFile + ", 1"
		};
		String[] expectedFrequencyLiens = {
				cloneFrequencyHeader(),
				notebookFile + ", 0, 2, 0, 1.0000, 1.0000, 2, 2"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 2, 1.0000, 2, 1.0000, 2, 2, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + File.separator + notebookFile);
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
		analyzer.clones();
		
		checkCsv("file2hashesA", expectedSnippetLines);
		checkCsv("hash2filesA", expectedClonesLines);
		checkCsv("cloneFrequency", expectedFrequencyLiens);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the clone output files have the right content after clone
	 * analysis of a notebooks with a clone.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_intraClone() throws IOException {
		String dataDir = "test/data/hash";
		String notebookFile = "nb_1.ipynb";
		String reproFile = "repros.csv";
		String hash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String[] expectedFile2HashesLines = {
				file2hashesHeader(),
				notebookFile + ", " + hash + ", " + hash
		};
		String[] expectedHash2FileLines = {
				hash2filesHeader(),
				hash + ", 1, " + notebookFile + ", 0, " + notebookFile + ", 1"
		};
		String[] expectedFrequencyLines = {
				cloneFrequencyHeader(),
				notebookFile + ", 0, 2, 0, 1.0000, 1.0000, 2, 2"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 2, 1.0000, 2, 1.0000, 2, 2, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + File.separator + notebookFile);
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
		analyzer.clones();
		
		checkCsv("file2hashesA", expectedFile2HashesLines);
		checkCsv("hash2filesA", expectedHash2FileLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashesA<current-date-time>.csv,
	 * cloneFrequency<current-date-time>.csv and
	 * connections<current-date-time>.csv have the right content after clone
	 * analysis of a notebook with both clones and a unique snippet.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_mixed() throws IOException {
		String dataDir = "test/data/hash";
		String notebookFile = "nb_2.ipynb";
		String reproFile = "repros.csv";
		String kossaHash = "0120F99AA7C49E1CD5F4EE4A6BB1CC4A";
		String nonUniqueHash = "8D91DA91141E24A95233199750861876";
		String[] expectedFile2HashesLines = {
			file2hashesHeader(),
			notebookFile + ", " + kossaHash + ", " + nonUniqueHash + ", " + kossaHash
		};
		// hash2Files is hard to test since we don't know in which order the hashes are stored
		String[] expectedFrequencyLines = {
			cloneFrequencyHeader(),
			notebookFile + ", 1, 2, 0, 0.6667, 0.6667, 2, 2"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			notebookFile + ", 2, 0.6667, 2, 0.6667, 2, 2, 0.0000, 0.0000"
		};
		
		analyzer.initializeNotebooksFrom(dataDir + File.separator + notebookFile);
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
		analyzer.clones();
		
		checkCsv("file2hashesA", expectedFile2HashesLines);
		checkCsv("cloneFrequency", expectedFrequencyLines);
		checkCsv("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that the output files file2hashesA<current-date-time>.csv,
	 * cloneFrequency<current-date-time>.csv and
	 * connections<current-date-time>.csv have the right content after clone
	 * analysis of a notebook with an empty clone.
	 * @throws IOException
	 */
	@Test
	public void testClones_csv_emptyClone() throws IOException {
		String dataDir = "test/data/hash_maintenance";
		String[] notebookFiles = {"nb_1.ipynb", "nb_2.ipynb", "nb_4.ipynb"};
		String reproFile = "test/data/hash/repros.csv";
		String emptyHash = "D41D8CD98F00B204E9800998ECF8427E";
		String codeHash = "82B3B93D07679915D2486E460668A907";
		String otherCodeHash = "76846A33FEF92030A17FFDC1C4966235";
		String uniqueCodeHash = "8944C013B85704C3D9125C531AC33417";
		
		String[] expectedFile2HashesLines = {
			file2hashesHeader(),
			notebookFiles[0] + ", " + emptyHash + ", " + codeHash + ", " + codeHash + ", " + emptyHash + ", " + uniqueCodeHash + ", " + otherCodeHash,
			notebookFiles[1] + ", " + codeHash + ", " + emptyHash,
			notebookFiles[2] + ", " + "DB51AD8A14D6C61A115D05D6810463CB" + ", " + otherCodeHash
		};
		String[] expectedFrequencyLines = {
			cloneFrequencyHeader(),
			notebookFiles[0] + ", 1, 5, 2, 0.8333, 0.7500, 4, 2",
			notebookFiles[1] + ", 0, 2, 1, 1.0000, 1.0000, 0, 0",
			notebookFiles[2] + ", 1, 1, 0, 0.5000, 0.5000, 0, 0"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			notebookFiles[0] + ", 9, 1.5000, 5, 1.2500, 4, 2, 2.5000, 1.5000",
			notebookFiles[1] + ", 4, 2.0000, 2, 2.0000, 0, 0, 4.0000, 2.0000",
			notebookFiles[2] + ", 1, 0.5000, 1, 0.5000, 0, 0, 1.0000, 1.0000"
		};
		
		for (String notebookFile: notebookFiles) {
			System.out.println("Initializing from " + notebookFile);
			analyzer.initializeNotebooksFrom(dataDir + File.separator + notebookFile);
		}
		analyzer.initializeReproInfo(reproFile);
		analyzer.clones();
		
		checkCsv_anyOrder("file2hashesA", expectedFile2HashesLines);
		checkCsv_anyOrder("cloneFrequency", expectedFrequencyLines);
		checkCsv_anyOrder("connections", expectedConnectionsLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * When there is an odd number of clones with different LOC, verify that
	 * only one line is stored for this clone in hash2files, and that the line
	 * count is the median of the line counts of the clones.
	 */
	@Test
	public void testHash2files_csv_clonesWithDifferentLOC_odd() throws IOException {
		String dataDir = "test/data/hash";
		String[] files = {"nb_12.ipynb", "nb_13.ipynb", "nb_14.ipynb"};
		String reproFile = "repros.csv";
		String[] expectedLines = {
			hash2filesHeader(),
			"159651F111F7381E9043CA7D2671E3E2\\, 2\\, nb_[0-9]+\\.ipynb\\, [0-9]+.*"
		};
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
		analyzer.clones();

		checkCsv_matches("hash2filesA", expectedLines);
		deleteCloneCsvs();
	}
	
	/**
	 * When there is an even number of clones with different LOC, verify that
	 * only one line is stored for this clone in hash2files, and that the line
	 * count is the median of the line counts of the clones.
	 */
	@Test
	public void testHash2files_csv_clonesWithDifferentLOC_even() throws IOException {
		String dataDir = "test/data/hash";
		String[] files = {"nb_12.ipynb", "nb_13.ipynb"};
		String reproFile = "repros.csv";
		String[] expectedLines = {
			hash2filesHeader(),
			"159651F111F7381E9043CA7D2671E3E2\\, 1\\, nb_[0-9]+\\.ipynb\\, [0-9]+.*"
		};
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
		analyzer.clones();
		
		checkCsv_matches("hash2filesA", expectedLines);
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
		String reproFile = "repros.csv";
		String[] expectedLines = {
				connectionsHeader(),
				"nb_4.ipynb, 6, 3.0000, 0, 0.0000, 4, 0, 2.0000, 0.0000",
				"nb_5.ipynb, 4, 2.0000, 1, 1.0000, 2, 0, 1.0000, 1.0000",
				"nb_1.ipynb, 6, 3.0000, 6, 3.0000, 2, 2, 4.0000, 4.0000",
				"nb_2.ipynb, 7, 2.3333, 7, 2.3333, 3, 3, 4.0000, 4.0000",
				"nb_3.ipynb, 1, 0.5000, 1, 0.5000, 1, 1, 0.0000, 0.0000",
				"nb_100.ipynb, 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000",
				"nb_6.ipynb, 4, 2.0000, 4, 2.0000, 4, 4, 0.0000, 0.0000",
				"nb_7.ipynb, 5, 1.6667, 5, 1.6667, 2, 2, 1.5000, 1.5000",
				"nb_10.ipynb, 3, 3.0000, 0, 0.0000, 0, 0, 3.0000, 0.0000",
				"nb_8.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				"nb_9.ipynb, 4, 2.0000, 4, 2.0000, 2, 2, 2.0000, 2.0000",
				"nb_11.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				"nb_12.ipynb, 2, 2.0000, 2, 2.0000, 0, 0, 1.0000, 1.0000",
				"nb_13.ipynb, 2, 2.0000, 2, 2.0000, 0, 0, 1.0000, 1.0000",
				"nb_14.ipynb, 2, 2.0000, 2, 2.0000, 0, 0, 1.0000, 1.0000"
		};
		
		analyzer.initializeNotebooksFrom(dataDir);
		analyzer.initializeReproInfo(dataDir + File.separator + reproFile);
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
		Map<LangName, Integer> expected = new HashMap<LangName, Integer>();
		expected.put(LangName.PYTHON, 6);
		expected.put(LangName.JULIA, 3);
		expected.put(LangName.R, 2);
		expected.put(LangName.SCALA, 3);
		expected.put(LangName.OTHER, 1);
		expected.put(LangName.UNKNOWN, 9);
		Map<LangName, Integer> actual = analyzer.languages();
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
		LangName[] languages = {LangName.OTHER, LangName.R, LangName.PYTHON};
		LangSpec[] langSpecs = {LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_KERNELSPEC_LANGUAGE, LangSpec.METADATA_LANGUAGEINFO_NAME};
		String[] expectedLines = new String[languages.length+1];
		expectedLines[0] = languagesHeader();
		for (int i=0; i<languages.length; i++) {
			expectedLines[i+1] = files[i] + ", " + languages[i] + ", " + langSpecs[i];
		}
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
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
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.LOC();
		
		checkCsv("loc", expectedLines);	

		lastOutputFile("loc").delete();
	}
	
	/**
	 * Verify that the correct modules are found in notebooks.
	 */
	@Test
	public void testModules() throws IOException {
		String dataDir = "test/data/modules";
		String[] files = {"nb_1.ipynb", "nb_2.ipynb", "nb_R.ipynb",
				"nb_3.ipynb", "nb_4.ipynb", "nb_5.ipynb"};
		String[] pythonFiles = {"nb_1.ipynb", "nb_2.ipynb", "nb_3.ipynb",
				"nb_4.ipynb", "nb_5.ipynb"};
		List<List<PythonModule>> expectedModules = new ArrayList<List<PythonModule>>(files.length);
		for (int i=0; i<files.length; i++) {
			expectedModules.add(i, new ArrayList<PythonModule>());
		}
		expectedModules.get(0).add(0, new PythonModule("kossan_mu", ImportType.ORDINARY));
		expectedModules.get(1).add(0, new PythonModule("kalv", "naut", ImportType.ALIAS));
		expectedModules.get(2).add(0, new AllModules(new PythonModule("ko", ImportType.FROM)));
		expectedModules.get(3).add(0, new PythonModule("module1", ImportType.ORDINARY));
		expectedModules.get(3).add(1, new PythonModule("module2", "mod2", ImportType.ALIAS));
		expectedModules.get(3).add(2, new PythonModule("module3", "mod3", ImportType.ALIAS));
		expectedModules.get(4).add(0, new PythonModule("module10", ImportType.ORDINARY));
		expectedModules.get(4).add(1, new PythonModule("module11", "mod", ImportType.ALIAS));
		expectedModules.get(4).add(2, new PythonModule("module12", ImportType.ORDINARY));
		expectedModules.get(4).add(3, new PythonModule("module13", "mod13", ImportType.ALIAS));
		
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		List<List<PythonModule>> modules = analyzer.modules();
		for (int i=0; i<pythonFiles.length; i++) {
			assertEquals("Wrong modules returned for " + pythonFiles[i] + "!",
					expectedModules.get(i), modules.get(i));
			for (int j=0; j<expectedModules.get(i).size(); j++) {
				assertEquals("Incorrect import type stored for notebook " + i + ", " + j + "!",
						expectedModules.get(i).get(j).importedWith(),
						modules.get(i).get(j).importedWith());
			}
		}
		lastOutputFile("modules").delete();
	}
	
	/**
	 * Verify that the output file modules<current-date-time>.csv is created
	 * and filled correctly when the modules are identified.
	 * @throws IOException on errors when handling input/output file(s) 
	 */
	@Test
	public void testModules_csv() throws IOException {
		String dataDir = "test/data/modules";
		String[] files = {"nb_1.ipynb", "nb_2.ipynb", "nb_R.ipynb",
				"nb_3.ipynb", "nb_4.ipynb", "nb_5.ipynb"};
		String[] expectedLines = {
			modulesHeader(),
			"nb_1.ipynb, kossan_mu",
			"nb_2.ipynb, kalv(naut)",
			"nb_3.ipynb, ko.*",
			"nb_4.ipynb, module1, module2(mod2), module3(mod3)",
			"nb_5.ipynb, module10, module11(mod), module12, module13(mod13)"
		};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.modules();
		
		checkCsv("modules", expectedLines);
		lastOutputFile("modules").delete();
	}
	
	/**
	 * Verify the behavior of modules for an invalid JSON file.
	 * @throws IOException on errors handling the input file
	 */
	@Test
	public void testModules_invalidFile() throws IOException {
		String dataDir = "test/data/modules";
		String file = "empty.ipynb";
		List<List<PythonModule>> expectedModules = new ArrayList<>(0);
		String[] expectedLines = {
			modulesHeader()
		};
		analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		List<List<PythonModule>> modules = analyzer.modules();
		
		assertEquals("Wrong modules returned for invalid file!",
				expectedModules, modules);
		checkCsv("modules", expectedLines);
		lastOutputFile("modules").delete();
	}
	
	/**
	 * Verify that the correct modules top list is returned by sortedModules.
	 * @throws IOException on errors handling the input files
	 */
	@Test
	public void testSortedModules() throws IOException {
		String dataDir = "test/data/modules";
		String[] files = {"nb_10.ipynb", "nb_R.ipynb", "nb_11.ipynb",
				"nb_12.ipynb", "nb_13.ipynb"};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		
		List<Quantity> expectedModulesSorted = new ArrayList<Quantity>();
		expectedModulesSorted.add(0, new Quantity("moduleZ", 4));
		expectedModulesSorted.add(1, new Quantity("moduleY", 3));
		expectedModulesSorted.add(2, new Quantity("moduleW", 2));
		expectedModulesSorted.add(3, new Quantity("parentModule.moduleX", 1));
		
		List<List<PythonModule>> modules = analyzer.modules();
		List<Quantity> modulesSorted = NotebookAnalyzer.sortedModules(modules);
		
		assertEquals("Incorrect list of most commonly imported modules.",
				expectedModulesSorted, modulesSorted);
		lastOutputFile("modules").delete();
	}
	
	/**
	 * Verify that pedigree is taken into consideration when modules top list
	 * is created.
	 */
	@Test
	public void testSortedModules_diffParents() throws IOException {
		String dataDir = "test/data/modules";
		String[] files = {"nb_30.ipynb", "nb_31.ipynb", "nb_35.ipynb"};
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		
		List<Quantity> expectedModulesSorted = new ArrayList<Quantity>(2);
		expectedModulesSorted.add(0, new Quantity("A.B.C", 2));
		expectedModulesSorted.add(1, new Quantity("B.C", 1));
		List<List<PythonModule>> modules = analyzer.modules();
		List<Quantity> modulesSorted = NotebookAnalyzer.sortedModules(modules);
		assertEquals("Wrong module quantities stored when name is same but ancestors differ.",
				expectedModulesSorted, modulesSorted);
	}
	
	/**
	 * Verify that the string representation of the most commonly imported
	 * modules is created correctly.
	 * @throws IOException on errors handling the input files
	 */
	@Test
	public void testMostCommonModulesAsString() throws IOException {
		List<Quantity> modulesSorted = new ArrayList<Quantity>();
		modulesSorted.add(0, new Quantity("moduleZ", 4));
		modulesSorted.add(1, new Quantity("moduleY", 3));
		modulesSorted.add(2, new Quantity("moduleW", 2));
		modulesSorted.add(3, new Quantity("parentModule.moduleX", 1));
		
		// All modules
		String expectedModulesString = "1. moduleZ: 4\n"
				+ "2. moduleY: 3\n"
				+ "3. moduleW: 2\n"
				+ "4. parentModule.moduleX: 1\n";
		String modulesString = NotebookAnalyzer.mostCommonModulesAsString(modulesSorted, Integer.MAX_VALUE);
		assertEquals("Wrong top modules reported!", expectedModulesString, modulesString);
		// Limited number of modules
		expectedModulesString = "1. moduleZ: 4\n";
		modulesString = NotebookAnalyzer.mostCommonModulesAsString(modulesSorted, 1);
		assertEquals("Wrong top modules reported!", expectedModulesString, modulesString);
		lastOutputFile("modules").delete();
	}
	
	/**
	 * Verify that a correct summary of the import type frequencies is created
	 * by importTypeSummary.
	 * @throws IOException on errors handling the input files
	 */
	@Test
	public void testImportTypeSummary() throws IOException {
		String dataDir = "test/data/modules";
		String[] files = {"nb_1.ipynb", "nb_2.ipynb", "nb_R.ipynb",
				"nb_3.ipynb", "nb_4.ipynb", "nb_5.ipynb"};
		String expectedImportTypeString = "ORDINARY: 4 (40.00%)\n"
				+ "ALIAS: 5 (50.00%)\n"
				+ "FROM: 1 (10.00%)\n";
		for (String file: files) {
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		List<List<PythonModule>> modules = analyzer.modules();
		String importTypeString = NotebookAnalyzer.importTypeSummary(modules);
		assertEquals("Wrong import types reported!",
				expectedImportTypeString, importTypeString);
		lastOutputFile("modules").delete();
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
			analyzer.initializeNotebooksFrom(dataDir + File.separator + file);
		}
		analyzer.numCodeCells();
		
		checkCsv("code_cells", expectedLines);
		
		lastOutputFile("code_cells").delete();
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
	 * @return Expected header of code cells files
	 */
	private static String codeCellsHeader() {
		return "file, code cells";
	}
	
	/**
	 * @return Expected header of connections files
	 */
	private static String connectionsHeader() {
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized, "
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections";
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
	 * @return Expected header of LOC file
	 */
	private static String LOCHeader() {
		return "file, total LOC, non-blank LOC, blank LOC";
	}
	
	/**
	 * @return Expected header of modules file
	 */
	private String modulesHeader() {
		return "notebook, modules ...";
	}
}
