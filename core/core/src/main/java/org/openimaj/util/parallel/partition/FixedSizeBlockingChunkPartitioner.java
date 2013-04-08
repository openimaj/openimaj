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
package org.openimaj.util.parallel.partition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * A {@link FixedSizeBlockingChunkPartitioner} dynamically partitions data into
 * chunks of a fixed length. The partitioner does not need to know about the
 * length of the data apriori. Beyond a {@link FixedSizeChunkPartitioner} this
 * implementation doesn't use an underlying iterator. It can only be
 * instantiated on queues and will always report to have more items available
 * while it waits for the queue to be filled.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object in the partition
 */
public class FixedSizeBlockingChunkPartitioner<T> implements Partitioner<T> {
	private Queue<T> queue;
	private int chunkSize = 20;

	/**
	 * Construct with data in the form of a {@link Queue}. The number of items
	 * per chunk is set at the default value (20).
	 * 
	 * @param objects
	 *            the {@link Queue} representing the data.
	 */
	public FixedSizeBlockingChunkPartitioner(Queue<T> objects) {
		this.queue = objects;
	}

	/**
	 * Construct with data in the form of an {@link Queue} and the given number
	 * of items per chunk.
	 * 
	 * @param objects
	 *            the {@link Queue} representing the data.
	 * @param chunkSize
	 *            number of items in each chunk.
	 */
	public FixedSizeBlockingChunkPartitioner(Queue<T> objects, int chunkSize) {
		this.queue = objects;
		this.chunkSize = chunkSize;
	}

	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Iterator<T> next() {

				final List<T> list = new ArrayList<T>(chunkSize);

				int i = 0;
				while (i < chunkSize) {
					T toAdd = null;
					synchronized (queue) {
						toAdd = queue.poll();
					}
					if (toAdd == null) {
						continue;
					}
					list.add(toAdd);
					i++;
				}

				return list.iterator();

			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}
}
