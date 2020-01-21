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
		String notebookFile = "nb_1.ipynb";
		String reproFile = "repros.csv";
		String snippetHash = "33BE8D72467938FBB23EF42CF8C9E85F";
		String[] expectedCodeCellsLines = {codeCellsHeader(),
				notebookFile + ", 1"};
		String[] expectedLOCLines = {LOCHeader(),
				notebookFile + ", 2, 1, 1"};
		String[] expectedLangLines = {languagesHeader(),
					notebookFile + ", " + Language.JULIA + ", " + LangSpec.METADATA_LANGUAGEINFO_NAME
				};
		String[] expectedAllLangLines = {allLanguagesHeader(),
					notebookFile + ", " + Language.SCALA + ", " + Language.JULIA + ", "
					+ Language.R + ", " + Language.OTHER + ", " + Language.PYTHON
				};
		String[] expectedFile2hashesLines = {file2hashesHeader(),
				notebookFile + ", " + snippetHash};
		String[] expectedHash2filesLines = {hash2filesHeader(),
				snippetHash + ", 1, " + notebookFile + ", 0"};
		String[] expectedCloneFreqLines = {cloneFrequencyHeader(),
				notebookFile + ", 0, 1, 0.0000"};
		String[] expectedConnectionsLines = {connectionsHeader(),
				notebookFile + ", 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000"};
		
		analyzer.initializeNotebooksFrom(testDir + "/" + notebookFile);
		analyzer.initializeReproMap(testDir + "/" + reproFile);
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
	 * Verify that the output files for number of code cells, loc, languages,
	 * all language values, and clone output files are created when the
	 * argument "-all" is given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_allAnalyses() throws IOException {
		String[] arg = {"-all"};
		String[] expectedFilePrefixes = {
				"code_cells",
				"loc",
				"languages",
				"all_languages",
				"file2hashes",
				"hash2files",
				"cloneFrequency",
				"connections"
		};
		testArgumentParsing(arg, expectedFilePrefixes);
	}
	
	/**
	 * Verify that the clone output files are created when the argument
	 * "-clones" is given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_clones() throws IOException {
		String[] arg = {"-clones"};	// Repro file not needed since nb_path is missing
		String[] expectedFilePrefixes = {
				"file2hashes",
				"hash2files",
				"cloneFrequency",
				"connections"
		};
		testArgumentParsing(arg, expectedFilePrefixes);
	}
	
	/**
	 * Verify that the clone output files are created when the argument
	 * "-clones_scc" is given and paths are specified correctly.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_clones_scc() throws IOException {
		String args[] = {
				"-clones_scc",
				"-scc_stats_file",
				"test/data/scc/file_stats",
				"-scc_clones_file",
				"test/data/scc/clone_pairs",
				"-repro_file",
				"test/data/hash/repros.csv"
		};
		String[] expectedFilePrefixes = {
				"file2hashes",
				"hash2files",
				"cloneFrequency",
				"connections"
		};
		testArgumentParsing(args, expectedFilePrefixes);
	}
	
	/**
	 * Verify that output file with number of code cells are created when the
	 * argument "-count" is given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_count() throws IOException {
		String[] arg = {"-count"};
		String[] expectedFilePrefix = {"code_cells"};
		testArgumentParsing(arg, expectedFilePrefix);
	}
	
	/**
	 * Verify that the language output file is created when the argument
	 * "-lang" is given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_lang() throws IOException {
		String[] arg = {"-lang"};
		String[] expectedFilePrefix = {"languages"};
		testArgumentParsing(arg, expectedFilePrefix);
	}
	
	/**
	 * Verify that the all language values output file is created when the
	 * argument "-lang_all" is given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_lang_all() throws IOException {
		String[] arg = {"-lang_all"};
		String[] expectedFilePrefix = {"all_languages"};
		testArgumentParsing(arg, expectedFilePrefix);
	}
	
	/**
	 * Verify that the loc output file is created when the argument "-loc" is
	 * given.
	 * throws IOException
	 */
	@Test
	public void testArgumentParsing_loc() throws IOException {
		String[] arg = {"-loc"};
		String[] expectedFilePrefix = {"loc"};
		testArgumentParsing(arg, expectedFilePrefix);
	}
	
	/**
	 * Verify that all relevant files are created when several arguments are
	 * given.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_severalArgs() throws IOException {
		String[] args = {"-lang_all", "-lang", "-count", "-loc"};
		String[] expectedFilePrefixes = {
				"all_languages",
				"languages",
				"code_cells",
				"loc"
		};
		testArgumentParsing(args, expectedFilePrefixes);
	}
	
	// TODO: testArgumentParsing: output_dir, nb_path (med/utan värde), repro_file utan värde. okänt argument

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
		List<Snippet> emptySnippets = new ArrayList<Snippet>(2);
		emptySnippets.add(new Snippet("nb_4.ipynb", 0));
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
			analyzer.initializeNotebooksFrom(dataDir + "/" + file);
		}
		analyzer.initializeReproMap(dataDir + "/" + reproFile);
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
		String notebookFile = "nb_100.ipynb";
		String reproMapName = "repros.csv";
		String[] expectedSnippetLines = {
				file2hashesHeader()
		};
		String[] expectedClonesLines = {
				hash2filesHeader()
		};
		String[] expectedFrequencyLines = {
				cloneFrequencyHeader(),
				notebookFile + ", 0, 0, 0"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + notebookFile);
		analyzer.initializeReproMap(dataDir + "/" + reproMapName);
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
				notebookFile + ", 2, 0, 1.0000"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 2, 1.0000, 2, 1.0000, 2, 2, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + notebookFile);
		analyzer.initializeReproMap(dataDir + "/" + reproFile);
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
				notebookFile + ", 2, 0, 1.0000"
		};
		String[] expectedConnectionsLines = {
				connectionsHeader(),
				notebookFile + ", 2, 1.0000, 2, 1.0000, 2, 2, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + notebookFile);
		analyzer.initializeReproMap(dataDir + "/" + reproFile);
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
			notebookFile + ", 2, 1, 0.6667"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			notebookFile + ", 2, 0.6667, 2, 0.6667, 2, 2, 0.0000, 0.0000"
		};
		
		// Actual values
		analyzer.initializeNotebooksFrom(dataDir + "/" + notebookFile);
		analyzer.initializeReproMap(dataDir + "/" + reproFile);
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
		String reproFile = "repros.csv";
		String[] expectedLines = {
				connectionsHeader(),
				"nb_4.ipynb, 2, 2.0000, 0, 0.0000, 1, 0, 1.0000, 0.0000",
				"nb_5.ipynb, 3, 1.5000, 1, 1.0000, 1, 0, 1.0000, 1.0000",
				"nb_1.ipynb, 6, 3.0000, 6, 3.0000, 2, 2, 4.0000, 4.0000",
				"nb_2.ipynb, 7, 2.3333, 7, 2.3333, 3, 3, 4.0000, 4.0000",
				"nb_3.ipynb, 1, 0.5000, 1, 0.5000, 1, 1, 0.0000, 0.0000",
				"nb_100.ipynb, 0, 0.0000, 0, 0.0000, 0, 0, 0.0000, 0.0000",
				"nb_6.ipynb, 4, 2.0000, 4, 2.0000, 4, 4, 0.0000, 0.0000",
				"nb_7.ipynb, 5, 1.6667, 5, 1.6667, 2, 2, 1.5000, 1.5000",
				"nb_10.ipynb, 2, 2.0000, 0, 0.0000, 0, 0, 2.0000, 0.0000",
				"nb_8.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				"nb_9.ipynb, 4, 2.0000, 4, 2.0000, 2, 2, 2.0000, 2.0000",
				"nb_11.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000"
		};
		
		analyzer.initializeNotebooksFrom(dataDir);
		analyzer.initializeReproMap(dataDir + "/" + reproFile);
		analyzer.clones();
		
		checkCsv_anyOrder("connections", expectedLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that connections are identified correctly at clone analysis based
	 * on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testConnectionsCsv_sccData() throws IOException {
		String dataDir = "test/data/scc";
		String statFile = "file_stats";
		String cloneFile = "clone_pairs";
		String reproMapPath = "test/data/hash/repros.csv";
		
		String[] expectedLines = {
				connectionsHeader(),
				"nb_4.ipynb, 2, 2.0000, 0, 0.0000, 1, 0, 1.0000, 0.0000",
				"nb_5.ipynb, 3, 1.5000, 1, 1.0000, 1, 0, 1.0000, 1.0000",
				"nb_1.ipynb, 6, 3.0000, 6, 3.0000, 2, 2, 4.0000, 4.0000",
				"nb_2.ipynb, 7, 2.3333, 7, 2.3333, 3, 3, 4.0000, 4.0000",
				"nb_3.ipynb, 1, 0.5000, 1, 0.5000, 1, 1, 0.0000, 0.0000",
				"nb_6.ipynb, 4, 2.0000, 4, 2.0000, 4, 4, 0.0000, 0.0000",
				"nb_7.ipynb, 5, 1.6667, 5, 1.6667, 2, 2, 1.5000, 1.5000",
				"nb_10.ipynb, 2, 2.0000, 0, 0.0000, 0, 0, 2.0000, 0.0000",
				"nb_8.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				"nb_9.ipynb, 4, 2.0000, 4, 2.0000, 2, 2, 2.0000, 2.0000",
				"nb_11.ipynb, 1, 1.0000, 1, 1.0000, 0, 0, 1.0000, 1.0000",
				
		};

		analyzer.initializeReproMap(reproMapPath);	// TODO: Den här kommer inte att fungera!
		analyzer.clones(dataDir + "/" + cloneFile, dataDir + "/" + statFile);
		
		checkCsv_anyOrder("connections", expectedLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that cloneFrequencies are computed correctly at clone analysis
	 * based on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testCloneFreqCsv_sccData() throws IOException {
		String dataDir = "test/data/scc";
		String statFile = "file_stats";
		String cloneFile = "clone_pairs";
		String reproMapPath = "test/data/hash/repros.csv";
		
		String[] expectedLines = {
				cloneFrequencyHeader(),
				"nb_1.ipynb, 2, 0, 1.0000",
				"nb_2.ipynb, 3, 0, 1.0000",
				"nb_3.ipynb, 1, 1, 0.5000",
				"nb_4.ipynb, 1, 0, 1.0000",
				"nb_5.ipynb, 2, 0, 1.0000",
				"nb_6.ipynb, 2, 0, 1.0000",
				"nb_7.ipynb, 3, 0, 1.0000",
				"nb_8.ipynb, 1, 0, 1.0000",
				"nb_9.ipynb, 2, 0, 1.0000",
				"nb_10.ipynb, 1, 0, 1.0000",
				"nb_11.ipynb, 1, 0, 1.0000"
		};
		
		analyzer.initializeReproMap(reproMapPath);
		analyzer.clones(dataDir + "/" + cloneFile, dataDir + "/" + statFile);
		
		checkCsv_anyOrder("cloneFrequency", expectedLines);
		
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that an AssertionError is thrown when the clone pairs file is on
	 * the wrong format.
	 * @throws IOException
	 */
	/* TODO: Om jag ska kunna ha med det här testet måste jag enabla assertions!
	@Test (expected = AssertionError.class)
	public void testClones_corruptSccData() throws IOException {
		String dataDir = "test/data/scc";
		String statFile = "file_stats";
		String cloneFile = "clone_pairs_corrupt";
		String reproMapPath = "test/data/hash/repros.csv";
		analyzer.initializeReproMap(reproMapPath);
		analyzer.clones(dataDir + "/" + cloneFile, dataDir + "/" + statFile);
	}*/
	
	// TODO: Borde man kolla file2hashes och hash2files också? Hur?!
	
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
	
	/** TODO: Kolla även antalet rader!
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
			while (outputReader.hasNextLine() && false == exists) {
				String nextLine = outputReader.nextLine();
				if (nextLine.equals(expectedLine)) {
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
		return "file, connections, connections normalized, non-empty connections, non-empty connections normalized, "
				+ "intra repro connections, non-empty intra repro connections, mean inter repro connections, mean non-empty inter repro connections";
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
			if (currentFileName.matches(prefix + "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+\\.csv")
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
	
	/**
	 * Verify that all files prefixed in expectedFiles exist after a call to
	 * analyzer.analyze with args as argument. Also remove all expected files.
	 * @param args Argument vector
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * throws IOException
	 */
	private void testArgumentParsing(String[] args, String[] expectedFilePrefixes) throws IOException {
		analyzer.analyze(args);
		for (String prefix: expectedFilePrefixes) {
			File expectedFile = lastOutputFile(prefix);
			assertTrue("Expected output file " + expectedFile.getName() + " is missing!",
					expectedFile.exists());
			expectedFile.delete();
		}
	}

}
