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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openimaj.data.identity.Identifiable;
import org.openimaj.util.iterator.ConcatenatedIterable;

/**
 * A {@link MapBackedDataset} is a concrete implementation of a
 * {@link GroupedDataset} backed by a {@link Map}.
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
	public INSTANCE getRandomInstance(KEY key) {
		return map.get(key).getRandomInstance();
	}

	@Override
	public INSTANCE getRandomInstance() {
		final int index = (int) (Math.random() * numInstances());
		int count = 0;

		for (final DATASET d : map.values()) {
			if (index >= count + d.numInstances()) {
				count += d.numInstances();
			} else {
				if (d instanceof ListDataset) {
					return ((ListDataset<INSTANCE>) d).get(index - count);
				} else {
					for (final INSTANCE i : d) {
						if (index == count)
							return i;

						count++;
					}
				}
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public DATASET put(KEY key, DATASET value) {
		return map.put(key, value);
	}

	/**
	 * Convenience method for populating a dataset by chaining method calls:
	 *
	 * <pre>
	 * final MapBackedDataset&lt;String, ListDataset&lt;String&gt;, String&gt; ds = new MapBackedDataset&lt;String, ListDataset&lt;String&gt;, String&gt;()
	 * 		.add(&quot;A&quot;, new ListBackedDataset&lt;String&gt;())
	 * 		.add(&quot;B&quot;, new ListBackedDataset&lt;String&gt;());
	 * </pre>
	 *
	 * @param key
	 *            the key to insert
	 * @param dataset
	 *            the value to insert
	 * @return this dataset
	 */
	public MapBackedDataset<KEY, DATASET, INSTANCE> add(KEY key, DATASET dataset) {
		this.put(key, dataset);
		return this;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param datasets
	 *            the datasets representing the groups
	 * @return the newly constructed grouped dataset.
	 */
	@SafeVarargs
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET... datasets)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();

		for (final DATASET d : datasets) {
			ds.put(d.getID(), d);
		}

		return ds;
	}

	/**
	 * A builder for creating {@link MapBackedDataset} instances from
	 * {@link Identifiable} sub-datasets. Example:
	 *
	 * <pre>
	 * final MapBackedDataset<String, VFSListDataset<String>, String> ds = new MapBackedDataset.IdentifiableBuilder<VFSListDataset<String>, String>()
	 * 					.add(new VFSListDataset<String>(...))
	 * 					.add(new VFSListDataset<String>(...))
	 * 					.build();
	 * </pre>
	 *
	 * For small {@link MapBackedDataset}s, the <tt>MapBackedDataset.of()</tt>
	 * methods are even more convenient.
	 * <p>
	 * Builder instances can be reused - it is safe to call {@link #build()}
	 * multiple times to build multiple maps in series. Each map is a superset
	 * of the maps created before it.
	 *
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @param <DATASET>
	 *            Type of sub-datasets.
	 * @param <INSTANCE>
	 *            Type of objects in the dataset
	 */
	public static class IdentifiableBuilder<DATASET extends Dataset<INSTANCE> & Identifiable, INSTANCE> {
		MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();

		/**
		 * Add the sub-dataset such that it becomes a group in the
		 * {@link MapBackedDataset} returned by {@link #build()} where the key
		 * is the identifier returned by {@link Identifiable#getID()}.
		 * <p>
		 * If duplicate keys (i.e. sub-datasets with duplicate identifiers) are
		 * added, only the last one will appear in the resultant dataset
		 * produced by {@link #build()}.
		 *
		 * @param dataset
		 *            the sub-dataset to add
		 * @return the builder
		 */
		public IdentifiableBuilder<DATASET, INSTANCE> add(DATASET dataset) {
			ds.put(dataset.getID(), dataset);

			return this;
		}

		/**
		 * Returns a newly-created {@link MapBackedDataset}.
		 *
		 * @return a newly-created {@link MapBackedDataset}.
		 */
		public MapBackedDataset<String, DATASET, INSTANCE> build() {
			return new MapBackedDataset<String, DATASET, INSTANCE>(ds);
		}
	}

	/**
	 * Returns a new builder. The generated builder is equivalent to the builder
	 * created by the {@link IdentifiableBuilder#IdentifiableBuilder()}
	 * constructor.
	 *
	 * @return a new builder.
	 */
	public static <DATASET extends Dataset<INSTANCE> & Identifiable, INSTANCE>
	IdentifiableBuilder<DATASET, INSTANCE> builder()
	{
		return new IdentifiableBuilder<DATASET, INSTANCE>();
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param d1
	 *            first dataset
	 *
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET d1)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();
		ds.put(d1.getID(), d1);
		return ds;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param d1
	 *            first dataset
	 * @param d2
	 *            second dataset
	 *
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET d1, DATASET d2)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();
		ds.put(d1.getID(), d1);
		ds.put(d2.getID(), d2);
		return ds;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param d1
	 *            first dataset
	 * @param d2
	 *            second dataset
	 * @param d3
	 *            third dataset
	 *
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET d1, DATASET d2, DATASET d3)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();
		ds.put(d1.getID(), d1);
		ds.put(d2.getID(), d2);
		ds.put(d3.getID(), d3);
		return ds;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param d1
	 *            first dataset
	 * @param d2
	 *            second dataset
	 * @param d3
	 *            third dataset
	 * @param d4
	 *            forth dataset
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET d1, DATASET d2, DATASET d3, DATASET d4)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();
		ds.put(d1.getID(), d1);
		ds.put(d2.getID(), d2);
		ds.put(d3.getID(), d3);
		ds.put(d4.getID(), d4);
		return ds;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param d1
	 *            first dataset
	 * @param d2
	 *            second dataset
	 * @param d3
	 *            third dataset
	 * @param d4
	 *            forth dataset
	 * @param d5
	 *            fifth dataset
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(DATASET d1, DATASET d2, DATASET d3, DATASET d4, DATASET d5)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();
		ds.put(d1.getID(), d1);
		ds.put(d2.getID(), d2);
		ds.put(d3.getID(), d3);
		ds.put(d4.getID(), d4);
		ds.put(d5.getID(), d5);
		return ds;
	}

	/**
	 * Convenience method to construct a {@link MapBackedDataset} from a number
	 * of {@link Identifiable} sub-datasets. Each sub-dataset becomes a group,
	 * and the key is the identifier returned by {@link Identifiable#getID()}.
	 *
	 * @param datasets
	 *            the datasets representing the groups
	 * @return the newly constructed grouped dataset.
	 */
	public static <INSTANCE, DATASET extends Dataset<INSTANCE> & Identifiable>
	MapBackedDataset<String, DATASET, INSTANCE> of(Collection<DATASET> datasets)
	{
		final MapBackedDataset<String, DATASET, INSTANCE> ds = new MapBackedDataset<String, DATASET, INSTANCE>();

		for (final DATASET d : datasets) {
			ds.put(d.getID(), d);
		}

		return ds;
	}
}
