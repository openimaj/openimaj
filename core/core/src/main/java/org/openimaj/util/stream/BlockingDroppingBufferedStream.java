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
package org.openimaj.util.stream;

import org.openimaj.util.concurrent.BlockingDroppingQueue;

/**
 * Base for a {@link Stream} with an internal buffer based on a
 * {@link BlockingDroppingQueue}. The use of the {@link BlockingDroppingQueue}
 * allows the stream to potentially drop items that are not consumed at a fast
 * enough rate (although this depends on the actual
 * {@link BlockingDroppingQueue}).
 * <p>
 * This class is intended to be used to build {@link Stream} implementations
 * that are connected to external, live data-sources that can potentially
 * produce data at a rate which exceeds the rate at which the stream can be
 * processed or consumed.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of data item in the stream
 */
public class BlockingDroppingBufferedStream<T> extends AbstractStream<T> {
	BlockingDroppingQueue<T> buffer;
	private boolean isClosed = false;

	/**
	 * Construct with the given backing queue
	 * 
	 * @param buffer
	 *            the backing buffer
	 */
	public BlockingDroppingBufferedStream(BlockingDroppingQueue<T> buffer) {
		this.buffer = buffer;
	}

	protected void register(T obj) throws InterruptedException {
		buffer.offer(obj);
	}

	@Override
	public boolean hasNext() {
		return !isClosed;
	}

	/**
	 * Close the stream (make hasNext return false)
	 */
	public void close() {
		this.isClosed = true;
	}

	@Override
	public T next() {
		try {
			return buffer.take();
		} catch (final InterruptedException e) {
			return next();
		}
	}

	/**
	 * Get the underlying {@link BlockingDroppingQueue} that is used as the
	 * internal buffer.
	 * 
	 * @return the buffer
	 */
	public BlockingDroppingQueue<T> getBuffer() {
		return buffer;
	}
}
