package org.openimaj.util.stream;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class QueuedBuffer<T> implements StreamBuffer<T> {
	private BlockingQueue<T> queue;

	public QueuedBuffer() {
		queue = new LinkedBlockingQueue<T>();
	}

	public QueuedBuffer(int capacity) {
		queue = new ArrayBlockingQueue<T>(capacity);
	}

	@Override
	public void offer(T obj) {
		queue.offer(obj);
	}

	@Override
	public T poll() throws InterruptedException {
		return queue.take();
	}

}
