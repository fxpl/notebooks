package notebooks;

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

public abstract class AnalyzerTest {
	protected static String defaultOutputDirName;
	protected static File defaultOutputDir;
	protected static boolean deleteOutputDirOnTearDown = false;
	protected String[] cloneFilePrefixes;

	/**
	 * Create the default output directory if it doesn't exist. (If so, it will
	 * be deleted at tear down). If the directory does exist, remove all files
	 * in it, if any.
	 */
	public static void setUpOutputDirectory() {
		 defaultOutputDir = new File(defaultOutputDirName);
		 if (defaultOutputDir.exists()) {
			TestUtils.cleanDirectory(defaultOutputDir);
		 } else {
			defaultOutputDir.mkdir();
			deleteOutputDirOnTearDown = true;
		 }
	}

	protected static void tearDownClass() {
		if (deleteOutputDirOnTearDown) {
			defaultOutputDir.delete();
		}
		ThreadExecutor.tearDown();
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv in the default
	 * output directory has the right content.
	 * @param dir Directory to look for the file in
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, in order
	 */
	protected static void checkCsv(String prefix, String[] expectedLines) throws IOException {
		TestUtils.checkCsv(defaultOutputDirName, prefix, expectedLines);
	}
	
	/**
	 * Check that all expected patterns, and nothing else, can be found in the
	 * most recent file <dir>/<prefix><timestamp>.csv.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedPatterns Array of the regular expressions expected to match lines in the file, not necessarily in order
	 * @throws IOException
	 */
	protected static void checkCsv_anyOrder(String prefix, String[] expectedPatterns) throws IOException {
		TestUtils.checkCsv_anyOrder(defaultOutputDirName, prefix, expectedPatterns);
	}
	
	/**
	 * Check that all expected patterns can be found in the most recent file
	 * <dir>/<prefix><timestamp>.csv.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedPatterns Array of the regular expressions expected to match lines in the file, not necessarily in order
	 * @throws IOException 
	 */
	protected static void checkCsv_contains(String prefix, String[] expectedPatterns) throws IOException {
		TestUtils.checkCsv_contains(defaultOutputDirName, prefix, expectedPatterns);
	}
	
	/**
	 * Check that each line the most recent file <prefix><timestamp>.csv in the
	 * default output directory matches (in a regular expression sense) the
	 * corresponding expected lines.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedPatterns Array of the patterns expected to be found in the file, in order
	 */
	protected static void checkCsv_matches(String prefix, String[] expectedPatterns) throws IOException {
		TestUtils.checkCsv_matches(defaultOutputDirName, prefix, expectedPatterns);
	}
	
	/**
	 * Delete all CSV files created by the clone analysis in the specified
	 * directory.
	 * @param dir Directory to remove files from
	 */
	protected void deleteCloneCsvs(String dir) {
		for (String prefix: cloneFilePrefixes) {
			TestUtils.lastOutputFile(dir, prefix).delete();
			TestUtils.lastOutputFile(dir, prefix).delete();
		}
	}
	
	/**
	 * Delete all CSV files created by the clone analysis in the default output
	 * directory.
	 * @param dir Directory to remove files from
	 */
	protected void deleteCloneCsvs() {
		deleteCloneCsvs(defaultOutputDirName);
	}
	
	/**
	 * Find the output file <prefix><timestamp>.csv with the greatest (latest)
	 * time stamp in the default output directory.
	 * @param prefix First part of the output file
	 * @return Output file described above
	 */
	protected static File lastOutputFile(String prefix) {
		return TestUtils.lastOutputFile(defaultOutputDirName, prefix);
	}
	
	/**
	 * Verify that no output file from the clone analysis exist in the
	 * specified directory.
	 * @param dir Directory to look for files in
	 */
	protected void verifyAbsenceOfCloneFiles(String dir) {
		for (String prefix: cloneFilePrefixes) {
			verifyAbsenceOf(dir, prefix);
		}
	}
	
	/**
	 * Verify that no output file whose name starts with the specified prefix
	 * exists in the specified directory.
	 * @param dir Directory to look for files in
	 * @param prefix Prefix of unwanted file name
	 */
	protected void verifyAbsenceOf(String dir, String prefix) {
		File outputFile = TestUtils.lastOutputFile(dir, prefix);
		assertFalse("Unexpected output file: " + outputFile.getName(), outputFile.exists());
	}
	
	/**
	 * Verify that no output file from the clone analysis exist in the
	 * default output directory.
	 * @param dir Directory to look for files in
	 */
	protected void verifyAbsenceOfCloneFiles() {
		verifyAbsenceOfCloneFiles(defaultOutputDirName);
	}
	
	/**
	 * Verify that all files prefixed in expectedFiles exist in the default output
	 * directory, and remove them.
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * @throws IOException
	 */
	protected static void verifyExistenceOfAndRemove(String[] expectedFilePrefixes) throws IOException {
		TestUtils.verifyExistenceOfAndRemove(defaultOutputDirName, expectedFilePrefixes);
	}
	
	/**
	 * Verify that all clone analysis output files exist in the specified
	 * directory, and remove them.
	 * @param dir Name of directory to look in and remove from
	 * @throws IOException
	 */
	protected void verifyExistenceOfAndRemoveCloneFiles(String dir) throws IOException {
		TestUtils.verifyExistenceOfAndRemove(dir, cloneFilePrefixes);
	}
	
	/**
	 * Verify that all clone analysis output files exist in the default output
	 * directory, and remove them.
	 * @throws IOException 
	 */
	protected void verifyExistenceOfAndRemoveCloneFiles() throws IOException {
		verifyExistenceOfAndRemoveCloneFiles(defaultOutputDirName);
	}

	/**
	 * @return Expected header of cloneFrequency files
	 */
	protected static String cloneFrequencyHeader() {
		return "file, unique, clones, empty, clone frequency, non-empty clone frequency, "
				+ "intra clones, non-empty intra clones";
	}
}
