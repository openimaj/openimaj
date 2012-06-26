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

/**
 * A {@link GrowingChunkPartitioner} dynamically partitions data into chunks. 
 * The partitioner does not need to know about the length of the data apriori.
 * The size of the chunks grows over time exponentially.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> Type of object in the partition
 */
public class GrowingChunkPartitioner<T> implements Partitioner<T> {
	private static final int NUM_HW_THREADS = Runtime.getRuntime().availableProcessors();
	private Iterator<T> objects;
	
	/**
	 * Construct with data in the form of an {@link Iterable}. 
	 * 
	 * @param objects the {@link Iterable} representing the data.
	 */
	public GrowingChunkPartitioner(Iterable<T> objects) {
		this.objects = objects.iterator();
	}

	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {
			private int currentIteration = 0;
			private int chunkSize = 2;
			
			@Override
			public boolean hasNext() {
				synchronized (objects) {
					return objects.hasNext();
				}
			}

			@Override
			public Iterator<T> next() {
				synchronized (objects) {
					if (!objects.hasNext())
						return null;
					
					if (currentIteration % NUM_HW_THREADS == 0) {
						chunkSize = (int)Math.pow(2, (currentIteration/NUM_HW_THREADS) + 1);
					}
					
					List<T> list = new ArrayList<T>(chunkSize);
					
					int i=0;
					while (objects.hasNext() && i<chunkSize) {
						list.add(objects.next());
						i++;
					}
					
					currentIteration++;
					return list.iterator();
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}
}
