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

import java.util.ArrayList;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * Given a period of time to wait in milliseconds, this function consumes
 * a stream for that period of time and produces a new stream of lists representing windows of that time period
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class ContextRealTimeWindowFunction<IN> implements Function<Stream<IN>,Stream<Context>>{

	private long waitTime;

	private long currentWindowStartTime;
	/**
	 * @param waitTime
	 */
	public ContextRealTimeWindowFunction(long waitTime) {
		this.waitTime = waitTime;
		this.currentWindowStartTime = -1;
	}

	@Override
	public Stream<Context> apply(final Stream<IN> inner) {

		return new AbstractStream<Context>() {

			@Override
			public boolean hasNext() {
				return inner.hasNext();
			}
			@Override
			public Context next() {
				currentWindowStartTime = System.currentTimeMillis();
				final ArrayList<IN> currentWindow = new ArrayList<IN>();
				while(inner.hasNext()){
					if(System.currentTimeMillis() - currentWindowStartTime >= ContextRealTimeWindowFunction.this.waitTime ){
						break;
					}
					IN next = inner.next();
					currentWindow.add(next);
				}

//				RealTimeWindowFunction.this.currentWindowStartTime = System.currentTimeMillis();
				Context context = new Context();
				context.put("windowstart", currentWindowStartTime);
				context.put("item",currentWindow);

				return context;
			}
		};
	}

}
