package org.openimaj.util.parallel;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A globally available (static) fixed-size {@link ThreadPoolExecutor}. The number of
 * threads is equal to the number of available hardware threads as reported by
 * {@link Runtime#availableProcessors()}. 
 * 
 * To avoid the need to shutdown the threadpool, the threads are all daemons.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class GlobalExecutorPool {
	/**
	 * A {@link ThreadFactory} that produces daemon threads.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class DaemonThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable paramRunnable) {
			Thread t = new Thread(paramRunnable);
			t.setDaemon(true);
			return t;
		}
	}
	
	private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new DaemonThreadFactory());
	
	/**
	 * Get the pool.
	 * @return the pool.
	 */
	public static ThreadPoolExecutor getPool() {
		return pool;
	}
}
