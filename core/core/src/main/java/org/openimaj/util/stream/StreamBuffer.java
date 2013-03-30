package org.openimaj.util.stream;

public interface StreamBuffer<T> {
	void offer(T obj) throws InterruptedException;

	T poll() throws InterruptedException;
}
