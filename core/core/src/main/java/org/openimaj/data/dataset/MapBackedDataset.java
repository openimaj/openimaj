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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.util.iterator.ConcatenatedIterable;

/**
 * A {@link MapBackedDataset} is a concrete implementation of a
 * {@link GroupedDataset} backed by a {@link Map}. For efficiency, the
 * implementation also maintains a flat list of all data items.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <KEY>
 *            Type of dataset class key
 * @param <DATASET>
 *            Type of sub-datasets.
 * @param <INSTANCE>
 *            Type of objects in the dataset
 */
public class MapBackedDataset<KEY extends Object, DATASET extends Dataset<INSTANCE>, INSTANCE>
		extends AbstractMap<KEY, DATASET>
		implements GroupedDataset<KEY, DATASET, INSTANCE>
{
	protected Map<KEY, DATASET> map;

	/**
	 * Construct an empty {@link MapBackedDataset} backed by a {@link HashMap}.
	 */
	public MapBackedDataset() {
		this.map = new HashMap<KEY, DATASET>();
	}

	/**
	 * Construct with the given map.
	 * 
	 * @param map
	 *            the map
	 */
	public MapBackedDataset(Map<KEY, DATASET> map) {
		this.map = map;
	}

	/**
	 * Get the underlying map.
	 * 
	 * @return the underlying map
	 */
	public Map<KEY, DATASET> getMap() {
		return map;
	}

	@Override
	public DATASET getInstances(KEY key) {
		return map.get(key);
	}

	@Override
	public Set<KEY> getGroups() {
		return map.keySet();
	}

	@Override
	public INSTANCE getRandomInstances(KEY key) {
		return map.get(key).getRandomInstance();
	}

	@Override
	public INSTANCE getRandomInstance() {
		final int index = (int) (Math.random() * numInstances());
		int count = 0;

		for (final DATASET d : map.values()) {
			for (final INSTANCE i : d) {
				if (index == count)
					return i;

				count++;
			}
		}
		return null;
	}

	@Override
	public int numInstances() {
		int size = 0;

		for (final DATASET d : map.values()) {
			size += d.numInstances();
		}

		return size;
	}

	@Override
	public Iterator<INSTANCE> iterator() {
		return new ConcatenatedIterable<INSTANCE>(map.values()).iterator();
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public Set<Entry<KEY, DATASET>> entrySet() {
		return map.entrySet();
	}
}
