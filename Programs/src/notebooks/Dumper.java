package notebooks;

import java.io.File;
import java.io.IOException;

public abstract class Dumper {

	/**
	 * Dump all snippets in the Python notebook(s) stored in src to a separate
	 * file in target. Every notebook in src/somePath is stored in
	 * target/somePath. target/somePath is created if needed.
	 */
	public void dumpAll(String src, String target) {
		File srcFile = new File(src);
		if (!srcFile.isDirectory()) {
			if (srcFile.getName().endsWith(".ipynb")) {
				createDirectoryIfMissing(target);
				dumpNotebook(src, target);
			}
		} else {
			// This is a directory. Traverse.
			String[] subFiles = srcFile.list();
			String targetDirName = target + File.separatorChar + srcFile.getName();
			for (String subFile: subFiles) {
				dumpAll(src + File.separatorChar + subFile, targetDirName);
			}
		}
	}

	private void dumpNotebook(String src, String target) {
		Notebook srcNb = new Notebook(src);
		try {
			dump(srcNb, target);
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
	 * Dump a single notebook.
	 * @param src Path to a notebook to dump
	 * @param target Path to directory where dumps will be stored
	 */
	protected abstract void dump(Notebook src, String target) throws NotebookException, IOException;

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

}