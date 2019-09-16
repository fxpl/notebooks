import java.io.*;
import java.util.ArrayList;

/**
 * Analyzer for Jupyter notebooks.
 */
public class Analyzer {
	private ArrayList<Notebook> notebooks;
	
	public Analyzer() {
		this.notebooks = new ArrayList<Notebook>();
	}
	
	/**
	 * Find each notebook file (i.e. file whose name ends with ".ipynb") in the
	 * file give as an argument to the method, or its sub directories
	 * (recursively). Create a Notebook from each file and store it in the
	 * field notebooks. (The argument may itself be a python notebook file. In
	 * that case, a Notebooks is created from it.
	 * 
	 * @param file Directory or file to look in for notebooks
	 */
	private void readNotebooksFrom(File file) {
		if (!file.isDirectory()) {
			// Regular file
			if (file.getName().endsWith(".ipynb")) {
				// Python notebook file
				this.notebooks.add(new Notebook(file));
			}
		} else {
			// The file is a directory. Traverse it.
			String[] subFiles = file.list();
			for (String subFileName: subFiles) {
				readNotebooksFrom(new File(file.getPath() + "/" + subFileName));
			}
		}
	}

	public static void main(String[] args) {
		File topDirectory = new File(args[0]);
		Analyzer analyzer = new Analyzer();
		analyzer.readNotebooksFrom(topDirectory);
	}
}
