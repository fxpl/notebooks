package notebooks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class SccOutputAnalyzerTest {
	private SccOutputAnalyzer analyzer;
	private final static String notebookNamePattern = "nb_[0-9]+\\.ipynb";
	
	@Before
	public void setUp() {
		analyzer = new SccOutputAnalyzer();
	}
	
	/**
	 * Verify that the clone output files are created when paths are specified
	 * correctly.
	 * @throws IOException 
	 */
	@Test
	public void testArgumentParsing_correct() throws IOException {
		String args[] = {
				"--stats_file=test/data/scc/file_stats",
				"--pair_file=test/data/scc/clone_pairs",
				"--repro_file=test/data/hash/repros.csv"
		};
		analyzer.analyze(args);
		TestUtils.verifyExistenceOfAndRemoveCloneFiles();
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
		TestUtils.verifyExistenceOfAndRemoveCloneFiles(outputDir);
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyAbsenceOfCloneFiles();
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
		TestUtils.verifyExistenceOfAndRemoveCloneFiles();
	}
	
	/**
	 * Verify that connections are identified correctly at clone analysis based
	 * on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testConnectionsCsv() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = "file_stats";
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

		analyzer.initializeReproMap(reproMapPath);
		analyzer.initializeSnippetInfo(dataDir + "/" + statsFile);
		analyzer.clones(dataDir + "/" + cloneFile);
		
		TestUtils.checkCsv_anyOrder("connections", expectedLines);
		
		TestUtils.deleteCloneCsvs();
	}
	
	/**
	 * Verify that cloneFrequencies are computed correctly at clone analysis
	 * based on SourcererCC data.
	 * @throws IOException 
	 */
	@Test
	public void testCloneFreqCsv() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = "file_stats";
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
		analyzer.initializeSnippetInfo(dataDir + "/" + statsFile);
		analyzer.clones(dataDir + "/" + cloneFile);
		
		TestUtils.checkCsv_anyOrder("cloneFrequency", expectedLines);
		
		TestUtils.deleteCloneCsvs();
	}
	
	/**
	 * Verify that the right line count is reported when the number of clones
	 * is odd.
	 * @throws IOException 
	 */
	@Test
	public void testLocComputation_odd() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = "file_stats_loc_odd";
		String pairFile = "clone_pairs_loc_odd";
		String reproMap = "repro_map_loc.csv";
		
		String[] expectedLines = {
				hash2filesHeader(),
				"[0-9,a-f]+, 13, " + notebookNamePattern + ", [0-9]+, " + notebookNamePattern + ", [0-9]+, " + notebookNamePattern + ", [0-9]+"
		};
		
		analyzer.initializeSnippetInfo(dataDir + "/" + statsFile);
		analyzer.initializeReproMap(dataDir + "/" + reproMap);
		analyzer.clones(dataDir + "/" + pairFile);
		TestUtils.checkCsv_matches("hash2files", expectedLines);
		
		TestUtils.deleteCloneCsvs();
	}
	
	/**
	 * Verify that the right line count is reported when the number of clones
	 * is even.
	 * @throws IOException 
	 */
	@Test
	public void testLocComputation_even() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = "file_stats_loc_even";
		String pairFile = "clone_pairs_loc_even";
		String reproMap = "repro_map_loc.csv";
		
		String[] expectedLines = {
				hash2filesHeader(),
				"[0-9,a-f]+, 16, " + notebookNamePattern + ", [0-9]+, " + notebookNamePattern + ", [0-9]+, " + notebookNamePattern + ", [0-9]+, nb_[0-9]\\.ipynb, [0-9]"
		};
		
		analyzer.initializeSnippetInfo(dataDir + "/" + statsFile);
		analyzer.initializeReproMap(dataDir + "/" + reproMap);
		analyzer.clones(dataDir + "/" + pairFile);
		TestUtils.checkCsv_matches("hash2files", expectedLines);
		
		TestUtils.deleteCloneCsvs();
	}
	
	/**
	 * Verify that an AssertionError is thrown when the clone pairs file is on
	 * the wrong format.
	 * @throws IOException
	 */
	@Test (expected = AssertionError.class)
	public void testClones_corruptPairData() throws IOException {
		String dataDir = "test/data/scc";
		String statsFile = "file_stats";
		String cloneFile = "clone_pairs_corrupt";
		String reproMapPath = "test/data/hash/repros.csv";
		analyzer.initializeSnippetInfo(dataDir + "/" + statsFile);
		analyzer.initializeReproMap(reproMapPath);
		analyzer.clones(dataDir + "/" + cloneFile);
	}
	
	/* file2hashes and hash2files are not checked, since we don't know which
	   snippet will get which index. If cloneFrequency and connections files
	   are correct, it is very unlikely that the file-hash and hash-file maps
	   are incorrect! */
	
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
	 * @return Expected header of hash2files files
	 */
	private static String hash2filesHeader() {
		return "hash, LOC, file, index, ...";
	}
}