package org.openimaj.util.stream.combine;

import java.util.List;

import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;


/**
 * Given a {@link List} of streams of a given type, present a stream of that type which
 * asks each stream in the {@link List} for an item in turn. If any stream in the {@link List}
 * says it has no item, this {@link Stream} will report it has no item and likely stop processing
 * all streams. The specifics of this behaviour are that if when it comes to a stream's turn to provide an item
 * it fails to do so, all the streams will stop being asked simultaniously.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <A>
 */
public class RoundRobinStreamCombiner<A> extends AbstractStream<A>{

	private List<Stream<A>> streams;
	private int currentStream;

	/**
	 * @param streams the streams to consume
	 */
	public RoundRobinStreamCombiner(List<Stream<A>> streams) {
		this.streams = streams;
		this.currentStream = 0;
	}
	@Override
	public boolean hasNext() {
		return streams.get(currentStream).hasNext();
	}

	@Override
	public A next() {
		A toRet = this.streams.get(currentStream).next();
		nextStream();
		return toRet;

	}
	private void nextStream() {
		this.currentStream++;
		if(this.currentStream >= streams.size()) this.currentStream = 0;
	}


}
