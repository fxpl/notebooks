package notebooks;

import java.io.IOException;

public class PythonDumper extends Dumper {

	protected void dump(Notebook src, String target) throws IOException {
		if (LangName.PYTHON.equals(src.language().getName())) {
			src.dumpCode(target, "py");
		}
	}

	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: PythonDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		new PythonDumper().dumpAll(args[0], args[1]);
		ThreadExecutor.tearDown();
	}
}
