package org.openimaj.util.stream;

import org.openimaj.util.concurrent.BlockingDroppingQueue;

/**
 * Abstract base for a {@link Stream} with an internal buffer based on a
 * {@link BlockingDroppingQueue}. The use of the {@link BlockingDroppingQueue}
 * allows the stream to potentially drop items that are not consumed at a fast
 * enough rate (although this depends on the actual
 * {@link BlockingDroppingQueue}).
 * <p>
 * This class is intended to be used to build {@link Stream} implementations
 * that are connected to external, live data-sources that can potentially
 * produce data at a rate which exceeds the rate at which the stream can be
 * processed or consumed.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of data item in the stream
 */
public abstract class BlockingDroppingBufferedStream<T> extends AbstractStream<T> {
	BlockingDroppingQueue<T> buffer;
	private boolean isClosed = false;

	protected BlockingDroppingBufferedStream(BlockingDroppingQueue<T> buffer) {
		this.buffer = buffer;
	}

	protected void register(T obj) throws InterruptedException {
		buffer.offer(obj);
	}

	@Override
	public boolean hasNext() {
		return !isClosed;
	}

	/**
	 * Close the stream (make hasNext return false)
	 */
	public void close() {
		this.isClosed = true;
	}

	@Override
	public T next() {
		try {
			return buffer.take();
		} catch (final InterruptedException e) {
			return next();
		}
	}
}
