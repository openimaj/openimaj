/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.stream.combine;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.parallel.GlobalExecutorPool;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * A stream combiner takes two streams and produces a new stream of synchronised
 * pairs of the stream values. The two streams are consumed in two threads which
 * the {@link #next()} method waits to complete before returning
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <A>
 *            Type of payload in first stream
 * @param <B>
 *            Type of payload in second stream
 */
public class StreamCombiner<A, B> extends AbstractStream<IndependentPair<A, B>> {

	private Stream<B> b;
	private Stream<A> a;
	private Starter<B> bstart;
	private Starter<A> astart;
	private ThreadPoolExecutor service;

	class Starter<T> implements Callable<T> {

		private Stream<T> stream;

		public Starter(Stream<T> a) {
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
	public StreamCombiner(Stream<A> a, Stream<B> b) {
		this.a = a;
		this.b = b;
		this.astart = new Starter<A>(this.a);
		this.bstart = new Starter<B>(this.b);
		this.service = GlobalExecutorPool.getPool();

	}

	@Override
	public boolean hasNext() {
		return a.hasNext() && b.hasNext();
	}

	@Override
	public IndependentPair<A, B> next() {
		final Future<A> futurea = this.service.submit(astart);
		final Future<B> futureb = this.service.submit(bstart);
		try {
			final A a = futurea.get();
			final B b = futureb.get();
			return IndependentPair.pair(a, b);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Create a new {@link StreamCombiner} from the given streams
	 * 
	 * @param a
	 *            first stream
	 * @param b
	 *            second stream
	 * @return the combined stream
	 */
	public static <A, B> StreamCombiner<A, B> combine(Stream<A> a, Stream<B> b) {
		return new StreamCombiner<A, B>(a, b);
	}

}
