package notebooks;

import static org.junit.Assert.assertEquals;
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
	 * Verify that all files prefixed in expectedFiles exist, and remove them.
	 * @param expectedFilePrefixes Prefixes for all expected files
	 * throws IOException
	 */
	static void checkExistenceAndRemove(String[] expectedFilePrefixes) throws IOException {
		for (String prefix: expectedFilePrefixes) {
			File expectedFile = lastOutputFile(prefix);
			assertTrue("Expected output file " + expectedFile.getName() + " is missing!",
					expectedFile.exists());
			expectedFile.delete();
		}
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
	 * time stamp.
	 * @param prefix First part of the output file
	 * @return Output file described above 
	 */
	static File lastOutputFile(String prefix) {
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
}
