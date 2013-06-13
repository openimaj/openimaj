package org.openimaj.util.stream.combine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.data.Context;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * A stream combiner takes two streams of {@link Context} objects and produces a
 * new stream of {@link Context}s which contain the stream values from both
 * input streams. To combine contexts, prefix strings are added to all the keys
 * to ensure that there is no overlap. The key prefixes can be empty if it is
 * known that the context keys will not collide. The two streams are consumed in
 * two threads which the {@link #next()} method waits to complete before
 * returning.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ContextStreamCombiner extends AbstractStream<Context> {
	class Starter implements Callable<Context> {
		private Stream<Context> stream;

		public Starter(Stream<Context> a) {
			stream = a;
		}

		@Override
		public Context call() throws Exception {
			return stream.next();
		}
	}

	private ThreadPoolExecutor service;
	private Stream<Context> b;
	private Stream<Context> a;
	private String aprefix;
	private String bprefix;

	/**
	 * Construct the combiner to consume the two given streams. The keys from
	 * the first {@link Stream} will be prefixed "a" and the second "b".
	 * 
	 * @param a
	 *            the first stream
	 * @param b
	 *            the second stream
	 */
	public ContextStreamCombiner(Stream<Context> a, Stream<Context> b) {
		this.a = a;
		this.b = b;
		this.aprefix = "a";
		this.bprefix = "b";
		this.service = GlobalExecutorPool.getPool();

	}

	/**
	 * Construct the combiner to consume the two given streams, using the given
	 * prefixes to modify the keys from the respective streams.
	 * 
	 * @param a
	 *            the first stream
	 * @param b
	 *            the second stream
	 * @param aprefix
	 *            the first stream key prefix
	 * @param bprefix
	 *            the second stream key prefix
	 */
	public ContextStreamCombiner(Stream<Context> a, Stream<Context> b, String aprefix, String bprefix) {
		this.a = a;
		this.b = b;
		this.aprefix = aprefix;
		this.bprefix = bprefix;
		this.service = GlobalExecutorPool.getPool();

	}

	@Override
	public boolean hasNext() {
		return a.hasNext() && b.hasNext();
	}

	@Override
	public Context next() {
		final Future<Context> futurea = this.service.submit(new Starter(a));
		final Future<Context> futureb = this.service.submit(new Starter(b));
		try {
			final Context a = futurea.get();
			final Context b = futureb.get();
			return a.combine(b, aprefix, bprefix);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Utility method to combine two streams. The keys from the first
	 * {@link Stream} will be prefixed "a" and the second "b".
	 * 
	 * @param a
	 *            the first stream
	 * @param b
	 *            the second stream
	 * @return the combined stream
	 */
	public static Stream<Context> combine(Stream<Context> a, Stream<Context> b) {
		return new ContextStreamCombiner(a, b);
	}
}
