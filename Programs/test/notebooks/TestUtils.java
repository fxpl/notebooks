package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestUtils {
	
	/**
	 * Check that the most recent file <dir>/<prefix><timestamp>.csv has the
	 * right content.
	 * @param dir Directory to look for the file in
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, in order
	 */
	static void checkCsv(String dir, String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(dir, prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			assertEquals("Wrong line number " + (i+1) + " for " + prefix + " csv!", expectedLine, outputReader.readLine());
		}
		outputReader.close();
	}
	
	/**
	 * Check that each line the most recent file <dir>/<prefix><timestamp>.csv
	 * matches (in a regular expression sense) the corresponding expected
	 * lines.
	 * @param dir Directory to look for the file in
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedPatterns Array of the patterns expected to be found in the file, in order
	 */
	static void checkCsv_matches(String dir, String prefix, String[] expectedPatterns) throws IOException {
		File outputFile = lastOutputFile(dir, prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedPatterns.length; i++) {
			String expectedPattern = expectedPatterns[i];
			boolean match = outputReader.readLine().matches(expectedPattern);
			assertTrue("Wrong pattern of line number " + (i+1) + " for " + prefix + " csv!", match);
		}
		assertNull("Too many lines in csv!", outputReader.readLine());
		outputReader.close();
	}
	
	/**
	 * Check that the most recent file <dir>/<prefix><timestamp>.csv contains
	 * all lines in expectedLines, and nothing more.
	 * @param dir Directory to look for the file in
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, not necessarily in order
	 * @throws IOException 
	 */
	static void checkCsv_anyOrder(String dir, String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(dir, prefix);
		BufferedReader reader = new BufferedReader(new FileReader(outputFile));
		long linesInFile = reader.lines().count();
		reader.close();
		assertEquals("Wrong number of lines in " + outputFile.getName() +"!",
				expectedLines.length, linesInFile);
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			boolean exists = false;
			BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
			String nextLine = null;
			do {
				nextLine = outputReader.readLine();
				if (expectedLine.equals(nextLine)) {
					exists = true;
				}
			} while (null != nextLine && false == exists);
			outputReader.close();
			assertTrue("The line " + expectedLine + " cannot be found in " + prefix + " csv!", exists);
		}
	}
	
	/**
	 * Remove all files from the specified directory
	 * @param directory Directory to remove files from
	 */
	static void cleanDirectory(File directory) {
		String[] filesToRemove = directory.list();
		if (0 < filesToRemove.length) {
			System.err.print("The directory '" + directory.getName() + "' contains files. ");
			System.err.println("These will be removed!");
			for (String fileName: filesToRemove) {
				new File(directory, fileName).delete();
			}
		}
	}
	
	/**
	 * Verify that all files prefixed in expectedFiles exist in the specified
	 * directory, and remove them.
	 * @param dir Directory to look for files in
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * @throws IOException
	 */
	static void verifyExistenceOfAndRemove(String dir, String[] expectedFilePrefixes) throws IOException {
		for (String prefix: expectedFilePrefixes) {
			File expectedFile = lastOutputFile(dir, prefix);
			assertTrue("Expected output file " + expectedFile.getName() + " is missing in " + dir + "!",
					expectedFile.exists());
			expectedFile.delete();
		}
	}
	
	/**
	 * Find the output file <prefix><timestamp>.csv with the greatest (latest)
	 * time stamp in the directory given as argument.
	 * @param prefix First part of the output file
	 * @param dir Directory to look for file in.
	 * @return Output file described above 
	 */
	static File lastOutputFile(String dir, String prefix) {
		File directory = new File(dir);
		String outputFileName = prefix + ".csv";
		for (String currentFileName: directory.list()) {
			if (currentFileName.matches(prefix + "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+\\.csv")
					&& currentFileName.compareTo(outputFileName) > 0) {
				outputFileName = currentFileName;
			}
		}
		return new File(dir, outputFileName);
	}
}
