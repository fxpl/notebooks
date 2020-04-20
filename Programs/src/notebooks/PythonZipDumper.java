package notebooks;

import java.io.IOException;

public class PythonZipDumper extends Dumper {
	
	protected void dump(Notebook src, String target) throws IOException {
		if (LangName.PYTHON.equals(src.language().getName())) {
			src.dumpCodeAsZip(target, "py");
		}
	}

	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: PythonZipDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		new PythonZipDumper().dumpAll(args[0], args[1]);
		ThreadExecutor.tearDown();
	}
}
