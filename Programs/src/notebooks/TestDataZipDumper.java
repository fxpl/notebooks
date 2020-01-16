package notebooks;

import java.io.IOException;

public class TestDataZipDumper extends Dumper {
	
	protected void dump(Notebook src, String target) throws NotebookException, IOException {
		src.dumpCodeAsZip(target, "py");
	}

	public static void main(String[] args) {
		if (2 != args.length) {
			System.out.println("Usage: TestDataZipDumper <path to input file or directory> <path to output directory>");
			System.exit(1);
		}
		new TestDataZipDumper().dumpAll(args[0], args[1]);
	}
}
