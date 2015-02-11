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
package org.openimaj.data.dataset;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Set;

import org.openimaj.io.InputStreamObjectReader;
import org.openimaj.io.ObjectReader;
import org.openimaj.util.iterator.ConcatenatedIterable;

/**
 * Base class for {@link GroupedDataset}s in which each instance is read with an
 * {@link InputStreamObjectReader}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <KEY>
 *            Type of dataset class key
 * @param <DATASET>
 *            Type of sub-datasets.
 * @param <INSTANCE>
 *            Type of instances in the dataset
 * @param <SOURCE>
 *            the type of object that provides the data to create the instance
 */
public abstract class ReadableGroupDataset<KEY, DATASET extends Dataset<INSTANCE>, INSTANCE, SOURCE>
extends AbstractMap<KEY, DATASET>
implements GroupedDataset<KEY, DATASET, INSTANCE>
{
	protected ObjectReader<INSTANCE, SOURCE> reader;

	/**
	 * Construct with the given {@link InputStreamObjectReader}.
	 *
	 * @param reader
	 *            the {@link InputStreamObjectReader}.
	 */
	public ReadableGroupDataset(ObjectReader<INSTANCE, SOURCE> reader) {
		this.reader = reader;
	}

	@Override
	public INSTANCE getRandomInstance() {
		final int index = (int) (Math.random() * numInstances());
		int count = 0;

		for (final KEY key : this.getGroups()) {
			final DATASET group = getInstances(key);

			if (count + group.numInstances() > index) {
				if (group instanceof ListDataset) {
					return ((ListDataset<INSTANCE>) group).get(index - count);
				} else {
					for (final INSTANCE i : group) {
						if (index == count)
							return i;

						count++;
					}
				}
			} else {
				count += group.numInstances();
			}
		}
		return null;
	}

	@Override
	public int numInstances() {
		int size = 0;

		for (final KEY key : this.getGroups()) {
			size += this.getInstances(key).numInstances();
		}

		return size;
	}

	@Override
	public Iterator<INSTANCE> iterator() {
		return new ConcatenatedIterable<INSTANCE>(this.values()).iterator();
	}

	@Override
	public DATASET getInstances(KEY key) {
		return this.get(key);
	}

	@Override
	public Set<KEY> getGroups() {
		return this.keySet();
	}

	@Override
	public INSTANCE getRandomInstance(KEY key) {
		return get(key).getRandomInstance();
	}
}
