package org.openimaj.util.stream;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link StreamBuffer} that forgets items if they are not consumed fast
 * enough.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object
 * 
 */
public final class ForgetfulBuffer<T> implements StreamBuffer<T> {
	public class Statistics {
		private volatile long forgot;
		private volatile long seen;

		private void update() {
			if (value != null)
				forgot++;
			seen++;
		}

		public long forgot() {
			return forgot;
		}

		public long seen() {
			return seen;
		}

		@Override
		public String toString() {
			return String.format("Seen: %d, forgot: %d", seen, forgot);
		}
	}

	private ReentrantLock lock;
	private Condition notEmpty;
	private T value = null;
	private Statistics stats;

	/**
	 * Default constructor
	 */
	public ForgetfulBuffer() {
		lock = new ReentrantLock();
		notEmpty = lock.newCondition();
		stats = new Statistics();
	}

	@Override
	public void offer(T obj) throws InterruptedException {
		if (obj == null)
			throw new NullPointerException();
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			stats.update();

			value = obj;
			notEmpty.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public T poll() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			try {
				while (value == null)
					notEmpty.await();
			} catch (final InterruptedException ie) {
				notEmpty.signal(); // propagate to non-interrupted thread
				throw ie;
			}
			final T tmp = value;
			value = null;
			return tmp;
		} finally {
			lock.unlock();
		}
	}

	public Statistics getStatistics() {
		return stats;
	}
}
