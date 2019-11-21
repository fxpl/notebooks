package notebooks;

import java.io.File;
import java.io.IOException;

public class PythonDumper {
	
	/**
	 * Dump all snippets in the Python notebook stored in srcDir to a separate
	 * file in targetDir. Every notebook in srcDir/somePath is stored in
	 * targetDir/somePath. targetDir/somePath is created if needed.
	 */
	public void dump(String src, String target) throws NotebookException, IOException {
		File srcFile = new File(src);
		if (!srcFile.isDirectory()) {
			// Create parent directory if needed
			File targetDir = new File(target);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			// Dump if Python file
			Notebook srcNb = new Notebook(src);
			if (Language.PYTHON.equals(srcNb.language())) {
				srcNb.dumpCode(target, "py");
			}
		} else {
			// This is a directory. Traverse.
			String[] subFiles = srcFile.list();
			String targetDirName = target + "/" + srcFile.getName();
			for (String subFile: subFiles) {
				dump(src + "/" + subFile, targetDirName);
			}
		}
	}
	
	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: PythonDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		try {
			new PythonDumper().dump(args[0], args[1]);
		} catch (NotebookException | IOException e) {
			System.err.println("Couldn't dump Python notebooks: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
