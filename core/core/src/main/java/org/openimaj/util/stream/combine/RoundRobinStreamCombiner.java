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
