package notebooks;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Wrapper class for a fixed thread pool containing 2*cores threads, where
 * cores is the number of available CPU cores. The class is written as a
 * singleton since we only want one thread pool per run.
 */
public class ThreadExecutor {
	private static ThreadExecutor instance;
	private ExecutorService threadPool;
	
	private ThreadExecutor() {
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Setting up a thread pool with " + (2*cores) + " threads.");
		threadPool = Executors.newFixedThreadPool(2*cores);
	}
	
	/**
	 * @return The thread executor
	 */
	public static ThreadExecutor getInstance() {
		if (null == instance) {
			instance = new ThreadExecutor();
		}
		return instance;
	}
	
	/**
	 * Submit a task for execution.
	 */
	public<T> Future<T> submit(Callable<T> task) {
		return threadPool.submit(task);
	}
	
	/**
	 * Close the thread executor down and nullify it. (If getInstance is called
	 * after a call to this method, a new thread executor will be set up.)
	 */
	public static void tearDown() {
		if (null != instance) {
			instance.shutDown();
			instance = null;
		}
	}
	
	private void shutDown() {
		threadPool.shutdown();
	}
}
