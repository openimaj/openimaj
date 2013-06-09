package org.openimaj.util.stream.combine;

import java.util.List;

import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * Given a {@link List} of streams of a given type, present a stream of that
 * type which asks each stream in the {@link List} for an item in turn. If any
 * stream in the {@link List} says it has no item, this {@link Stream} will
 * report it has no item and likely stop processing all streams. The specifics
 * of this behaviour are that if when it comes to a stream's turn to provide an
 * item it fails to do so, all the streams will stop being asked simultaneously.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of items in the streams being combined
 */
public class RoundRobinStreamCombiner<T> extends AbstractStream<T> {
	private List<Stream<T>> streams;
	private int currentStream;

	/**
	 * Construct with the given streams. Items from each stream will be consumed
	 * in a round-robin fashion, starting with the first stream given.
	 * 
	 * @param streams
	 *            the streams to consume
	 */
	public RoundRobinStreamCombiner(List<Stream<T>> streams) {
		this.streams = streams;
		this.currentStream = 0;
	}

	@Override
	public boolean hasNext() {
		return streams.get(currentStream).hasNext();
	}

	@Override
	public T next() {
		final T toRet = this.streams.get(currentStream).next();

		nextStream();

		return toRet;
	}

	private void nextStream() {
		this.currentStream++;

		if (this.currentStream >= streams.size())
			this.currentStream = 0;
	}
}
