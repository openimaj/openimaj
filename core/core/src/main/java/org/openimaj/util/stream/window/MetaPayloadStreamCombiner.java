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
public class MetaPayloadStreamCombiner<AP,AM,BP,BM> extends AbstractStream<MetaPayload<IndependentPair<AP, BP>,IndependentPair<AM,BM>>>{

	private Stream<? extends MetaPayload<BP,BM>> b;
	private Stream<? extends MetaPayload<AP,AM>> a;
	private Starter<MetaPayload<AP, AM>> astart;
	private Starter<MetaPayload<BP, BM>> bstart;
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
	public <A extends MetaPayload<AP,AM>> MetaPayloadStreamCombiner( Stream<A> a, Stream<? extends MetaPayload<BP,BM>> b) {

		this.a = a;
		this.b = b;
		this.astart = new Starter<MetaPayload<AP, AM>>(this.a);
		this.bstart = new Starter<MetaPayload<BP, BM>>(this.b);
		this.service = GlobalExecutorPool.getPool();

	}
	@Override
	public boolean hasNext() {
		return a.hasNext() && b.hasNext();
	}

	@Override
	public MetaPayload<IndependentPair<AP, BP>,IndependentPair<AM,BM>> next() {
		Future<MetaPayload<AP, AM>> futurea = this.service.submit(astart);
		Future<MetaPayload<BP, BM>> futureb = this.service.submit(bstart);
		try {
			MetaPayload<AP, AM> ai = futurea.get();
			MetaPayload<BP, BM> bi = futureb.get();
			IndependentPair<AP, BP> payloads = IndependentPair.pair(ai.getPayload(), bi.getPayload());
			IndependentPair<AM, BM> metas = IndependentPair.pair(ai.getMeta(), bi.getMeta());
			return new MetaPayload<IndependentPair<AP,BP>, IndependentPair<AM,BM>>(payloads, metas);
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
	public static <AP,AM,BP,BM> MetaPayloadStreamCombiner<AP,AM, BP,BM> combine(Stream<? extends MetaPayload<AP,AM>> a, Stream<? extends MetaPayload<BP,BM>> b) {
		return new MetaPayloadStreamCombiner<AP,AM,BP,BM>(a, b);
	}

}
