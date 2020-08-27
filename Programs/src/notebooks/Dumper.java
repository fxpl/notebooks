package notebooks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public abstract class Dumper {
	private List<String> sources = new ArrayList<String>();
	private List<String> targets = new ArrayList<String>();

	/**
	 * Dump all snippets in the Python notebook(s) stored in src to a separate
	 * file in target. Every notebook in src/somePath is stored in
	 * target/somePath. target/somePath is created if needed.
	 */
	public void dumpAll(String src, String target) {
		identifyNotebooks(src, target);
		dumpNotebooks();
	}
	
	/**
	 * Dump a single notebook.
	 * @param src Path to a notebook to dump
	 * @param target Path to directory where dumps will be stored
	 */
	protected abstract void dump(Notebook src, String target) throws IOException;
	
	private void identifyNotebooks(String src, String target) {
		File srcFile = new File(src);
		if (!srcFile.isDirectory()) {
			if (srcFile.getName().endsWith(".ipynb")) {
				sources.add(src);
				targets.add(target);
			}
		} else {
			Utils.heartBeat("Traversing " + srcFile.getPath());
			// This is a directory. Traverse.
			String[] subFiles = srcFile.list();
			String targetDirName = target + File.separatorChar + srcFile.getName();
			for (String subFile: subFiles) {
				identifyNotebooks(src + File.separatorChar + subFile, targetDirName);
			}
		}
	}
	
	private void dumpNotebooks() {
		int numNotebooks = sources.size();
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>(numNotebooks);
		CountDownLatch counter = new CountDownLatch(numNotebooks);
		for (int i=0; i<numNotebooks; i++) {
			String source = sources.get(i);
			String target = targets.get(i);
			createDirectoryIfMissing(target);
			tasks.add(new NotebookDumper(source, target, this, counter));
		}
		ThreadExecutor.getInstance().invokeAll(tasks);
		
		try {
			counter.await();
		} catch (InterruptedException e) {
			System.err.println("Thread was interrupted while dumping a notebook: " + e);
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

}