package notebooks;

import java.io.File;
import java.io.IOException;

public class PythonDumper {
	
	/**
	 * Dump all snippets in the Python notebook(s) stored in src to a separate
	 * file in target. Every notebook in src/somePath is stored in
	 * target/somePath. target/somePath is created if needed.
	 */
	public void dump(String src, String target) {
		File srcFile = new File(src);
		if (!srcFile.isDirectory()) {
			if (srcFile.getName().endsWith(".ipynb")) {
				createDirectoryIfMissing(target);
				dumpIfPythonNotebook(src, target);
			}
		} else {
			// This is a directory. Traverse.
			String[] subFiles = srcFile.list();
			String targetDirName = target + File.separatorChar + srcFile.getName();
			for (String subFile: subFiles) {
				dump(src + File.separatorChar + subFile, targetDirName);
			}
		}
	}

	/**
	 * @param src Path to a notebook to dump
	 * @param target Path to directory where dumps will be stored
	 */
	private void dumpIfPythonNotebook(String src, String target) {
		Notebook srcNb = new Notebook(src);
		try {
			if (Language.PYTHON.equals(srcNb.language())) {
				srcNb.dumpCode(target, "py");
			}
		} catch (NotebookException e) {
			System.err.println("Couldn't dump notebook " + srcNb.getName() + ": " + e.getMessage() + " Skipping!");
		} catch (IOException e) {
			System.err.println("I/O error when dumping python snippets: " + e.getMessage());
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.err.println("Runtime error for notebook " + srcNb.getName() + ": " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Create a directory if it doesn't already exist. Also create its parent
	 * directories if needed.
	 * @param path Path to the directory to be created
	 */
	private void createDirectoryIfMissing(String path) {
		File targetDir = new File(path);
		if (!targetDir.exists()) {
			targetDir.mkdirs();
		}
	}
	
	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: PythonDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		new PythonDumper().dump(args[0], args[1]);
	}
}
