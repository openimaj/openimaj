package org.openimaj.util.stream.window;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;


/**
 * A stream combiner takes two streams and produces a new stream of syncrhonised
 * pairs of the stream values. The two streams are consumed in two threads which
 * the {@link #next()} method waits to complete before returning
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <AP>
 * @param <AM>
 * @param <BP>
 * @param <BM>
 *
 */
public class AggregationStreamCombiner<AP,AM,BP,BM> extends AbstractStream<Aggregation<IndependentPair<AP, BP>,IndependentPair<AM,BM>>>{

	private Stream<? extends Aggregation<BP,BM>> b;
	private Stream<? extends Aggregation<AP,AM>> a;
	private Starter<Aggregation<AP, AM>> astart;
	private Starter<Aggregation<BP, BM>> bstart;
	private ThreadPoolExecutor service;

	class Starter<T> implements Callable<T>{

		private Stream<? extends T> stream;

		public Starter(Stream<? extends T> a) {
			stream = a;
		}

		@Override
		public T call() throws Exception {
			return stream.next();
		}

	}
	/**
	 * @param a
	 * @param b
	 */
	public <A extends Aggregation<AP,AM>> AggregationStreamCombiner( Stream<A> a, Stream<? extends Aggregation<BP,BM>> b) {

		this.a = a;
		this.b = b;
		this.astart = new Starter<Aggregation<AP, AM>>(this.a);
		this.bstart = new Starter<Aggregation<BP, BM>>(this.b);
		this.service = GlobalExecutorPool.getPool();

	}
	@Override
	public boolean hasNext() {
		return a.hasNext() && b.hasNext();
	}

	@Override
	public Aggregation<IndependentPair<AP, BP>,IndependentPair<AM,BM>> next() {
		Future<Aggregation<AP, AM>> futurea = this.service.submit(astart);
		Future<Aggregation<BP, BM>> futureb = this.service.submit(bstart);
		try {
			Aggregation<AP, AM> ai = futurea.get();
			Aggregation<BP, BM> bi = futureb.get();
			IndependentPair<AP, BP> payloads = IndependentPair.pair(ai.getPayload(), bi.getPayload());
			IndependentPair<AM, BM> metas = IndependentPair.pair(ai.getMeta(), bi.getMeta());
			return new Aggregation<IndependentPair<AP,BP>, IndependentPair<AM,BM>>(payloads, metas);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	/**
	 * @param a
	 * @param b
	 * @return an aggregation of the two streams
	 */
	public static <AP,AM,BP,BM> AggregationStreamCombiner<AP,AM, BP,BM> combine(Stream<? extends Aggregation<AP,AM>> a, Stream<? extends Aggregation<BP,BM>> b) {
		return new AggregationStreamCombiner<AP,AM,BP,BM>(a, b);
	}

}
