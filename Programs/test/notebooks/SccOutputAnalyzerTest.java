package notebooks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class SccOutputAnalyzerTest {
	private SccOutputAnalyzer analyzer;
	
	
	@Before
	public void setUp() {
		analyzer = new SccOutputAnalyzer();
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
		analyzer.analyze(args);
		TestUtils.checkExistenceAndRemove(expectedFilePrefixes);
	}
	
	// TODO: testArgumentParsing: output_dir, nb_path (med/utan värde), repro_file utan värde. okänt argument
	
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

		analyzer.initializeReproMap(reproMapPath);
		analyzer.clones(dataDir + "/" + cloneFile, dataDir + "/" + statFile);
		
		TestUtils.checkCsv_anyOrder("connections", expectedLines);
		
		TestUtils.deleteCloneCsvs();
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
		
		TestUtils.checkCsv_anyOrder("cloneFrequency", expectedLines);
		
		TestUtils.deleteCloneCsvs();
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
}