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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openimaj.util.function.Operation;

/**
 * A {@link RangePartitioner} partitions data of a known size into a predefined
 * number of equally sized partitions. If the time taken for processing each
 * partition with {@link Operation}s varies, then this partitioner is not
 * load-balancing (so threads may end up waiting whilst others are still
 * working).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of object in the partition
 */
public class RangePartitioner<T> implements Partitioner<T> {
	private final List<T> data;
	private final int numPartitions;
	private final int partitionSize;
	private int currentPartition = 0;
	private int remainder;

	/**
	 * Construct with a {@link List} of data and the given number of partitions.
	 * 
	 * @param list
	 *            the data
	 * @param numPartitions
	 *            the number of partitions
	 */
	public RangePartitioner(List<T> list, int numPartitions) {
		this.data = list;

		final int ops = data.size();
		final double div = ops / (double) numPartitions;

		if (div >= 1) {
			partitionSize = (int) div;
			remainder = (int) ((div - partitionSize) * numPartitions);
			this.numPartitions = numPartitions;
		} else {
			partitionSize = 1;
			remainder = 0;
			this.numPartitions = ops;
		}
	}

	/**
	 * Construct with a {@link Collection} of data and the given number of
	 * partitions.
	 * 
	 * @param c
	 *            the data
	 * @param numPartitions
	 *            the number of partitions
	 */
	public RangePartitioner(Collection<T> c, int numPartitions) {
		this(new ArrayList<T>(c), numPartitions);
	}

	/**
	 * Construct with an array of data and the given number of partitions.
	 * 
	 * @param array
	 *            the data
	 * @param numPartitions
	 *            the number of partitions
	 */
	public RangePartitioner(T[] array, int numPartitions) {
		this(Arrays.asList(array), numPartitions);
	}

	/**
	 * Construct with a {@link List} of data and the number of partitions equal
	 * to the number of hardware threads.
	 *
	 * @param list
	 *            the data
	 */
	public RangePartitioner(List<T> list) {
		this(list, Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Construct with a {@link Collection} of data and the number of partitions
	 * equal to the number of hardware threads.
	 *
	 * @param c
	 *            the data
	 */
	public RangePartitioner(Collection<T> c) {
		this(new ArrayList<T>(c), Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Construct with an array of data and the number of partitions equal to the
	 * number of hardware threads.
	 *
	 * @param array
	 *            the data
	 */
	public RangePartitioner(T[] array) {
		this(Arrays.asList(array), Runtime.getRuntime().availableProcessors());
	}

	@Override
	public Iterator<Iterator<T>> getPartitions() {
		return new Iterator<Iterator<T>>() {
			int offset = 0;

			@Override
			public boolean hasNext() {
				return currentPartition < numPartitions;
			}

			@Override
			public Iterator<T> next() {
				final int start = offset + currentPartition * partitionSize;
				int stop = Math.min(offset + (++currentPartition) * partitionSize, data.size());

				if (remainder > 0) {
					stop++;

					remainder--;
					offset++;
				}

				return data.subList(start, stop).iterator();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}

}
