package org.openimaj.util.stream.combine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.data.Context;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;


/**
 * A stream combiner takes two streams and produces a new stream of syncrhonised
 * pairs of the stream values. The two streams are consumed in two threads which
 * the {@link #next()} method waits to complete before returning
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ContextStreamCombiner extends AbstractStream<Context>{

	private Stream<Context> b;
	private Stream<Context> a;
	private ThreadPoolExecutor service;
	private String aprefix;
	private String bprefix;

	class Starter implements Callable<Context>{

		private Stream<Context> stream;

		public Starter(Stream<Context> a) {
			stream = a;
		}

		@Override
		public Context call() throws Exception {
			return stream.next();
		}

	}
	/**
	 * @param a
	 * @param b
	 */
	public ContextStreamCombiner(Stream<Context> a, Stream<Context> b) {
		this.a = a;
		this.b = b;
		this.aprefix = "a";
		this.bprefix = "b";
		this.service = GlobalExecutorPool.getPool();

	}

	/**
	 * @param a
	 * @param b
	 * @param aprefix
	 * @param bprefix
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
		Future<Context> futurea = this.service.submit(new Starter(a));
		Future<Context> futureb = this.service.submit(new Starter(b));
		try {
			Context a = futurea.get();
			Context b = futureb.get();
			return a.combine(b,aprefix,bprefix);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Calls the constructor with a and b
	 * @param a
	 * @param b
	 * @return
	 */
	public static Stream<Context> combine(Stream<Context> a, Stream<Context> b){
		return new ContextStreamCombiner(a, b);
	}

}
