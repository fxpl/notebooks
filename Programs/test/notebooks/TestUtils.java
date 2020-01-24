package notebooks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class TestUtils {
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv has the right
	 * content.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, in order
	 */
	static void checkCsv(String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedLines.length; i++) {
			String expectedLine = expectedLines[i];
			assertEquals("Wrong line number " + (i+1) + " for " + prefix + " csv!", expectedLine, outputReader.readLine());
		}
		outputReader.close();
	}
	
	/**
	 * Check that each line the most recent file <prefix><timestamp>.csv
	 * matches (in a regular expression sense) the corresponding expected
	 * lines.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedPatterns Array of the patterns expected to be found in the file, in order
	 */
	static void checkCsv_matches(String prefix, String[] expectedPatterns) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader outputReader = new BufferedReader(new FileReader(outputFile));
		for (int i=0; i<expectedPatterns.length; i++) {
			String expectedPattern = expectedPatterns[i];
			boolean match = outputReader.readLine().matches(expectedPattern);
			assertTrue("Wrong pattern of line number " + (i+1) + " for " + prefix + " csv!", match);
		}
		outputReader.close();
	}
	
	/**
	 * Check that the most recent file <prefix><timestamp>.csv contains all
	 * lines in expectedLines, and nothing more.
	 * @param prefix First part of name of file to be analyzed (see above)
	 * @param expectedLines Array of the lines expected to be found in the file, not necessarily in order
	 * @throws IOException 
	 */
	static void checkCsv_anyOrder(String prefix, String[] expectedLines) throws IOException {
		File outputFile = lastOutputFile(prefix);
		BufferedReader reader = new BufferedReader(new FileReader(outputFile));
		long linesInFile = reader.lines().count();
		reader.close();
		assertEquals("Wrong number of lines in " + outputFile.getName() +"!",
				expectedLines.length, linesInFile);
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
	 * Verify that no output file from the clone analysis exist.
	 */
	static void verifyAbsenceOfCloneFiles() {
		String[] prefixes = {
				"file2hashes",
				"hash2files",
				"cloneFrequency",
				"connections"
		};
		
		for (String prefix: prefixes) {
			File outputFile = lastOutputFile(prefix);
			assertFalse("Unexpected output file: " + outputFile.getName(), outputFile.exists());
		}
	}
	
	/**
	 * Verify that all files prefixed in expectedFiles exist in the current
	 * directory, and remove them.
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * throws IOException
	 */
	static void verifyExistenceAndRemove(String[] expectedFilePrefixes) throws IOException {
		verifyExistenceAndRemove(".", expectedFilePrefixes);
	}
	
	/**
	 * Verify that all files prefixed in expectedFiles exist in the specified
	 * directory, and remove them.
	 * @param dir Directory to look for files in
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * throws IOException
	 */
	static void verifyExistenceAndRemove(String dir, String[] expectedFilePrefixes) throws IOException {
		for (String prefix: expectedFilePrefixes) {
			File expectedFile = lastOutputFile(dir, prefix);
			assertTrue("Expected output file " + expectedFile.getName() + " is missing in " + dir + "!",
					expectedFile.exists());
			expectedFile.delete();
		}
	}
	
	/**
	 * Verify that all clone analysis output files exist in the current
	 * directory, and remove them.
	 * @throws IOException 
	 */
	static void verifyExistenceOfAndRemoveCloneFiles() throws IOException {
		verifyExistenceOfAndRemoveCloneFiles(".");
	}
	
	/**
	 * Verify that all clone analysis output files exist in the specified
	 * directory, and remove them.
	 * @param dir Name of directory to look in and remove from
	 * @throws IOException 
	 */
	static void verifyExistenceOfAndRemoveCloneFiles(String dir) throws IOException {
		String[] prefixes = {
				"file2hashes",
				"hash2files",
				"cloneFrequency",
				"connections"
		};
		verifyExistenceAndRemove(dir, prefixes);
	}
	
	/**
	 * Delete all CSV files created by the clone analysis. 
	 */
	static void deleteCloneCsvs() {
		lastOutputFile("file2hashes").delete();
		lastOutputFile("hash2files").delete();
		lastOutputFile("cloneFrequency").delete();
		lastOutputFile("connections").delete();
	}
	
	/**
	 * Find the output file <prefix><timestamp>.csv with the greatest (latest)
	 * time stamp in the current directory.
	 * @param prefix First part of the output file
	 * @return Output file described above
	 */
	static File lastOutputFile(String prefix) {
		return lastOutputFile(".", prefix);
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
		return new File(dir + "/" + outputFileName);
	}
}
