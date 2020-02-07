package notebooks;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class NotebookDumper implements Callable<Void> {
	String src, target;
	Dumper dumper;
	CountDownLatch counter;
		
	public NotebookDumper(String src, String target, Dumper caller, CountDownLatch counter) {
		this.src = src;
		this.target = target;
		this.dumper = caller;
		this.counter = counter;
	}

	public Void call() {
		Notebook srcNb = new Notebook(src);
		try {
			dumper.dump(srcNb, target);
		} catch (IOException e) {
			System.err.println("I/O error when dumping snippets in "
					+ srcNb.getName() + ": " + e + " Skipping notebook!");
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.err.println("Runtime error for notebook " + srcNb.getName()
					+ ": " + e + " Skipping notebook!");
			e.printStackTrace();
		}
		counter.countDown();
		return null;
	}
}
