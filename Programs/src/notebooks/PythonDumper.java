package notebooks;

import java.io.IOException;

public class PythonDumper extends Dumper {

	protected void dumpNotebook(String src, String target) {
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

	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: PythonDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		new PythonDumper().dumpAll(args[0], args[1]);
	}
}
