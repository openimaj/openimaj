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
import java.util.Comparator;
import java.util.List;

import org.openimaj.util.function.Function;
import org.openimaj.util.stream.AbstractStream;
import org.openimaj.util.stream.Stream;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <T>
 *
 */
public abstract class SequentialStreamAggregator<T>
		implements
			Function<Stream<T>, Stream<T>>
{

	private Comparator<T> comp;

	/**
	 * @param comp the comparator for decided whether sequential items are the same
	 */
	public SequentialStreamAggregator(Comparator<T> comp) {
		this.comp = comp;
	}

	@Override
	public Stream<T> apply(final Stream<T> inner) {
		return new AbstractStream<T>() {

			List<T> currentList = new ArrayList<T>();
			@Override
			public boolean hasNext() {
				return inner.hasNext() || currentList.size() != 0;
			}
			@Override
			public T next() {
				while(inner.hasNext()){
					T next = inner.next();
					if(currentList.size() == 0 || comp.compare(currentList.get(0), next) == 0){
						currentList.add(next);
					}
					else{
						T toRet = combine(currentList);
						currentList.clear();
						currentList.add(next);
						return toRet;
					}
				}
				// The end of the stream is reached
				if(currentList.size()!=0){
					T toRet = combine(currentList);
					currentList.clear();
					return toRet;
				}
				else{
					throw new UnsupportedOperationException("The sequential combiner failed");
				}
			}
		};
	}

	/**
	 * Called when a window of identical items needs to be combined
	 * @param window
	 *
	 * @return the combination of the current and next values
	 */
	public abstract T combine(List<T> window) ;


}
