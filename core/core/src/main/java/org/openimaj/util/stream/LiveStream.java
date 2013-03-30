package org.openimaj.util.stream;

public class LiveStream<T> extends AbstractStream<T> {
	public static abstract class LiveStreamConnection<T> {
		private LiveStream<T> stream;

		protected final void register(T obj) {
			if (stream != null)
				stream.register(obj);
		}

		private void setStream(LiveStream<T> stream) {
			this.stream = stream;
		}
	}

	StreamBuffer<T> buffer;

	public LiveStream(LiveStreamConnection<T> connection, StreamBuffer<T> buffer) {
		this.buffer = buffer;
		connection.setStream(this);
	}

	private void register(T obj) {
		try {
			buffer.offer(obj);
		} catch (final InterruptedException e) {
			register(obj);
		}
	}

	@Override
	public boolean hasNext() {
		return true;
	}

	@Override
	public T next() {
		try {
			return buffer.poll();
		} catch (final InterruptedException e) {
			return next();
		}
	}
}
