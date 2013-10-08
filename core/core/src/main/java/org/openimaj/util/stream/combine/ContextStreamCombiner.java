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
