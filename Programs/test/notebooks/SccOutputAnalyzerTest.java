package notebooks;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SccOutputAnalyzerTest extends AnalyzerTest {
	private SccOutputAnalyzer analyzer;
	
	@BeforeClass
	public static void setUpOutputDirectory() {
		defaultOutputDirName = "scc_output_analyzer_unit_test_output";
		AnalyzerTest.setUpOutputDirectory();
	}
	
	@Before
	public void setUp() {
		analyzer = new SccOutputAnalyzer();
		analyzer.outputDir = defaultOutputDirName;
	}
	
	@AfterClass
	public static void tearDown() {
		tearDownClass();
	}
	
	/**
	 * Verify that the clone output files are created when paths are specified
	 * correctly.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_pairFile() throws IOException {
		String args[] = {
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv"
		};
		analyzer.analyze(args);
		verifyExistenceOfAndRemoveCloneFiles();
	}
	
	/**
	 * Verify that the output directory is set correctly when give as an
	 * argument.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_outputDir() throws IOException {
		String outputDir = "test";
		String args[] = {
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv",
				"--output_dir=" + outputDir
		};
		analyzer.analyze(args);
		verifyExistenceOfAndRemoveCloneFiles(outputDir);
	}
	
	/**
	 * Verify that clone analysis is not run when statistics file is not
	 * specified.
	 */
	@Test
	public void testArgumentParsing_statsFileMissing() {
		String args[] = {
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv"
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when statistics file value is not
	 * specified.
	 */
	@Test
	public void testArgumentParsing_statsFileValueMissing() {
		String args[] = {
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv",
				"--stats_file"
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when clone pair file is not
	 * specified.
	 */
	@Test
	public void testArgumentParsing_pairFileMissing() {
		String[] args = {
				"--stats_file=test/data/scc/file_stats",
				"--repro_file=test/data/hash/repros.csv"
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when clone pair file value is not
	 * specified.
	 */
	@Test
	public void testArgumentParsing_pairFileValueMissing() {
		String[] args = {
				"--stats_file=test/data/scc/file_stats",
				"--repro_file=test/data/hash/repros.csv",
				"--pair_file",
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when clone pair file doesn't exist.
	 */
	@Test
	public void testArgumentParsing_nonExistentPairFile() {
		String args[] = {
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=nonexistent/path/file.csv",
				"--repro_file=test/data/hash/repros.csv"
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when repro file is not specified.
	 */
	@Test
	public void testArgumentParsing_reproFileMissing() {
		String[] args = {
				"--pair_file=test/data/scc/clone_pairs",
				"--stats_file=test/data/scc/file_stats",
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that clone analysis is not run when repro file value is not
	 * specified.
	 */
	@Test
	public void testArgumentParsing_reproFileValueMissing() {
		String[] args = {
				"--repro_file",
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=test/data/scc/clone_pairs"
		};
		analyzer.analyze(args);
		verifyAbsenceOfCloneFiles();
	}
	
	/**
	 * Verify that analyze runs smooth also when an unknown argument is given.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_unknownArg() throws IOException {
		String[] args = {
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv",
				"--unknown"};
		analyzer.analyze(args);
		verifyExistenceOfAndRemoveCloneFiles();
	}
	
	/**
	 * Verify that connections are identified correctly at clone analysis based
	 * on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testConnectionsCsv() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats";
		String pairFile = dataDir + "/clone_pairs";
		String reproFile = "test/data/hash/repros.csv";
		String[] expectedLines = {
				connectionsHeader(),
				"nb_4.ipynb, 0, 0.0000, 0, 0.0000",
				"nb_5.ipynb, 1, 1.0000, 0, 1.0000",
				"nb_1.ipynb, 6, 3.0000, 2, 4.0000",
				"nb_2.ipynb, 7, 2.3333, 3, 4.0000",
				"nb_3.ipynb, 1, 0.5000, 1, 0.0000",
				"nb_6.ipynb, 4, 2.0000, 4, 0.0000",
				"nb_7.ipynb, 5, 1.6667, 2, 1.5000",
				"nb_10.ipynb, 0, 0.0000, 0, 0.0000",
				"nb_8.ipynb, 1, 1.0000, 0, 1.0000",
				"nb_9.ipynb, 4, 2.0000, 2, 2.0000",
				"nb_11.ipynb, 1, 1.0000, 0, 1.0000",
				
		};

		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("connections", expectedLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that cloneFrequencies are computed correctly at clone analysis
	 * based on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testCloneFreqCsv() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats";
		String pairFile = dataDir + "/clone_pairs";
		String reproFile = "test/data/hash/repros.csv";
		
		String[] expectedLines = {
				cloneFrequencyHeader(),
				"nb_1.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
				"nb_2.ipynb, 0, 3, 0, 1.0000, 1.0000, 2, 2",
				"nb_3.ipynb, 1, 1, 0, 0.5000, 0.5000, 0, 0",
				"nb_4.ipynb, 2, 0, 2, 0.0000, 0, 0, 0",
				"nb_5.ipynb, 1, 1, 1, 0.5000, 1.0000, 0, 0",
				"nb_6.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
				"nb_7.ipynb, 0, 3, 0, 1.0000, 1.0000, 0, 0",
				"nb_8.ipynb, 0, 1, 0, 1.0000, 1.0000, 0, 0",
				"nb_9.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
				"nb_10.ipynb, 1, 0, 1, 0.0000, 0, 0, 0",
				"nb_11.ipynb, 0, 1, 0, 1.0000, 1.0000, 0, 0"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("cloneFrequency", expectedLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that SccOutputAnalyzer smoothly skips a clone pair with numbers
	 * that don't fit in an int.
	 * @throws IOException 
	 */
	@Test
	public void testClones_numberFormat() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats_small";
		String pairFile = dataDir + "/clone_pairs_overflow";
		String reproFile = "test/data/hash/repros.csv";
		
		String[] expectedCloneFrequecyLines = {
			cloneFrequencyHeader(),
			"nb_6.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
			"nb_9.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			"nb_6.ipynb, 2, 1.0000, 2, 0.0000",
			"nb_9.ipynb, 2, 1.0000, 2, 0.0000"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("cloneFrequency", expectedCloneFrequecyLines);
		checkCsv_anyOrder("connections", expectedConnectionsLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that snippet ID:s that don't exist in the file stats file are
	 * smoothly ignored in clone analysis.
	 * @throws IOException 
	 */
	@Test
	public void testClones_nonExistentID() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats_small";
		String pairFile = dataDir + "/clone_pairs_nonExistentID";
		String reproFile = "test/data/hash/repros.csv";
		
		String[] expectedCloneFrequecyLines = {
			cloneFrequencyHeader(),
			"nb_6.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
			"nb_9.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			"nb_6.ipynb, 2, 1.0000, 2, 0.0000",
			"nb_9.ipynb, 2, 1.0000, 2, 0.0000"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("cloneFrequency", expectedCloneFrequecyLines);
		checkCsv_anyOrder("connections", expectedConnectionsLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that connections are smoothly ignored if repro (or notebook) info
	 * is missing in first snippet in pair.
	 * @throws IOException
	 */
	@Test
	public void testClones_nullReproLeft() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats_xsmall";
		String pairFile = dataDir + "/clone_pairs_xsmall";
		String reproFile = dataDir + "/repro_file_xsmall";
		
		String[] expectedConnectionLines = {
			connectionsHeader(),
			"nb_8.ipynb, 0, 0.0000, 0, 0.0000",
			"nb_9.ipynb, 0, 0.0000, 0, 0.0000"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("connections", expectedConnectionLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that connections are smoothly ignored if repro (or notebook) info
	 * is missing in second snippet in pair.
	 * @throws IOException
	 */
	@Test
	public void testClones_nullReproRight() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats_xsmall";
		String pairFile = dataDir + "/clone_pairs_xsmall_rev";
		String reproFile = dataDir + "/repro_file_xsmall";
		
		String[] expectedConnectionLines = {
			connectionsHeader(),
			"nb_8.ipynb, 0, 0.0000, 0, 0.0000",
			"nb_9.ipynb, 0, 0.0000, 0, 0.0000"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("connections", expectedConnectionLines);
		deleteCloneCsvs();
	}
	
	/**
	 * Verify that lines not containing exact 4 comma-separated strings are
	 * smoothly ignored in clone analysis.
	 * @throws IOException 
	 */
	@Test
	public void testClones_corruptPairData() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = dataDir + "/file_stats_small";
		String pairFile = dataDir + "/clone_pairs_corrupt";
		String reproFile = "test/data/hash/repros.csv";
		
		String[] expectedCloneFrequecyLines = {
			cloneFrequencyHeader(),
			"nb_6.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2",
			"nb_9.ipynb, 0, 2, 0, 1.0000, 1.0000, 2, 2"
		};
		String[] expectedConnectionsLines = {
			connectionsHeader(),
			"nb_6.ipynb, 2, 1.0000, 2, 0.0000",
			"nb_9.ipynb, 2, 1.0000, 2, 0.0000"
		};
		
		analyzer.clones(statsFile, reproFile, pairFile);
		checkCsv_anyOrder("cloneFrequency", expectedCloneFrequecyLines);
		checkCsv_anyOrder("connections", expectedConnectionsLines);
		deleteCloneCsvs();
	}
	
	/**
	 * @return Expected header of connections files
	 */
	protected static String connectionsHeader() {
		return "file, non-empty connections, non-empty connections normalized, "
				+ "non-empty intra repro connections, mean non-empty inter repro connections";
	}
	
	@Override
	protected void deleteCloneCsvs(String dir) {
		TestUtils.lastOutputFile(dir, "cloneFrequency").delete();
		TestUtils.lastOutputFile(dir, "connections").delete();
	}
	
	@Override
	protected void verifyAbsenceOfCloneFiles(String dir) {
		String[] prefixes = {
				//"snippetsA",
				"cloneFrequency",
				"connections"
		};
		
		for (String prefix: prefixes) {
			File outputFile = TestUtils.lastOutputFile(dir, prefix);
			assertFalse("Unexpected output file: " + outputFile.getName(), outputFile.exists());
		}
	}
	
	@Override
	protected void verifyExistenceOfAndRemoveCloneFiles(String dir) throws IOException {
		String[] prefixes = {
				"cloneFrequency",
				"connections"
		};
		TestUtils.verifyExistenceOfAndRemove(dir, prefixes);
	}
}
