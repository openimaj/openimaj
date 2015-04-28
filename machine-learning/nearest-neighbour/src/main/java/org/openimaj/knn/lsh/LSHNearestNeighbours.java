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
package org.openimaj.knn.lsh;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openimaj.knn.IncrementalNearestNeighbours;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.pair.IntFloatPair;
import org.openimaj.util.queue.BoundedPriorityQueue;

/**
 * Nearest-neighbours based on Locality Sensitive Hashing (LSH). A number of
 * internal hash-tables are created with an associated hash-code (which is
 * usually a composite of multiple locality sensitive hashes). For a given
 * query, the hash-code of the query object computed for each hash function and
 * a lookup is made in each table. The set of matching items drawn from the
 * tables is then combined and sorted by distance (and trimmed if necessary)
 * before being returned.
 * <p>
 * Note: This object is not thread-safe. Multiple insertions or mixed insertions
 * and searches should not be performed concurrently without external locking.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Type of object being stored.
 */
public class LSHNearestNeighbours<OBJECT>
		implements
		IncrementalNearestNeighbours<OBJECT, float[], IntFloatPair>
{
	/**
	 * Encapsulates a hash table with an associated hash function and pointers
	 * to the data.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @param <OBJECT>
	 *            Type of object being hashed
	 */
	private static class Table<OBJECT> {
		private final TIntObjectHashMap<TIntArrayList> table;
		HashFunction<OBJECT> function;

		public Table(HashFunction<OBJECT> function) {
			this.function = function;
			table = new TIntObjectHashMap<TIntArrayList>();
		}

		/**
		 * Insert a single point
		 *
		 * @param point
		 *            the point
		 * @param pid
		 *            the id of the point in the data
		 */
		protected void insertPoint(OBJECT point, int pid) {
			final int hash = function.computeHashCode(point);

			TIntArrayList bucket = table.get(hash);
			if (bucket == null) {
				table.put(hash, bucket = new TIntArrayList());
			}

			bucket.add(pid);
		}

		/**
		 * Search for a point in the table
		 *
		 * @param point
		 *            query point
		 * @param norm
		 *            normalisation factor
		 * @return ids of matched points
		 */
		protected TIntArrayList searchPoint(OBJECT point) {
			final int hash = function.computeHashCode(point);

			return table.get(hash);
		}
	}

	protected DistanceComparator<OBJECT> distanceFcn;
	protected List<Table<OBJECT>> tables;
	protected List<OBJECT> data = new ArrayList<OBJECT>();

	/**
	 * Construct with the given hash functions and distance function. One table
	 * will be created per hash function.
	 *
	 * @param tableHashes
	 *            The hash functions
	 * @param distanceFcn
	 *            The distance function
	 */
	public LSHNearestNeighbours(List<HashFunction<OBJECT>> tableHashes, DistanceComparator<OBJECT> distanceFcn) {
		final int numTables = tableHashes.size();
		this.distanceFcn = distanceFcn;
		this.tables = new ArrayList<Table<OBJECT>>(numTables);

		for (int i = 0; i < numTables; i++) {
			tables.add(new Table<OBJECT>(tableHashes.get(i)));
		}
	}

	/**
	 * Construct with the given hash function factory which will be used to
	 * initialize the requested number of hash tables.
	 *
	 * @param factory
	 *            The hash function factory.
	 * @param numTables
	 *            The number of requested tables.
	 * @param distanceFcn
	 *            The distance function.
	 */
	public LSHNearestNeighbours(HashFunctionFactory<OBJECT> factory, int numTables, DistanceComparator<OBJECT> distanceFcn)
	{
		this.distanceFcn = distanceFcn;
		this.tables = new ArrayList<Table<OBJECT>>(numTables);

		for (int i = 0; i < numTables; i++) {
			tables.add(new Table<OBJECT>(factory.create()));
		}
	}

	/**
	 * Get the number of hash tables
	 *
	 * @return The number of hash tables
	 */
	public int numTables() {
		return tables.size();
	}

	/**
	 * Insert data into the tables
	 *
	 * @param d
	 *            the data
	 */
	public void addAll(Collection<OBJECT> d) {
		int i = this.data.size();

		for (final OBJECT point : d) {
			this.data.add(point);

			for (final Table<OBJECT> table : tables) {
				table.insertPoint(point, i);
			}

			i++;
		}
	}

	/**
	 * Insert data into the tables
	 *
	 * @param d
	 *            the data
	 */
	public void addAll(OBJECT[] d) {
		int i = this.data.size();

		for (final OBJECT point : d) {
			this.data.add(point);

			for (final Table<OBJECT> table : tables) {
				table.insertPoint(point, i);
			}

			i++;
		}
	}

	@Override
	public int add(OBJECT o) {
		final int index = this.data.size();
		this.data.add(o);

		for (final Table<OBJECT> table : tables) {
			table.insertPoint(o, index);
		}

		return index;
	}

	/**
	 * Search for similar data in the underlying tables and return all matches
	 *
	 * @param data
	 *            the points
	 * @return matched ids
	 */
	public TIntHashSet[] search(OBJECT[] data) {
		final TIntHashSet[] pls = new TIntHashSet[data.length];

		for (int i = 0; i < data.length; i++) {
			pls[i] = search(data[i]);
		}

		return pls;
	}

	/**
	 * Search for a similar data item in the underlying tables and return all
	 * matches
	 *
	 * @param data
	 *            the point
	 * @return matched ids
	 */
	public TIntHashSet search(OBJECT data) {
		final TIntHashSet pl = new TIntHashSet();

		for (final Table<OBJECT> table : tables) {
			final TIntArrayList result = table.searchPoint(data);

			if (result != null)
				pl.addAll(result);
		}

		return pl;
	}

	/**
	 * Compute identifiers of the buckets in which the given points belong for
	 * all the tables.
	 *
	 * @param data
	 *            the points
	 * @return the bucket identifiers
	 */
	public int[][] getBucketId(OBJECT[] data) {
		final int[][] ids = new int[data.length][];

		for (int i = 0; i < data.length; i++) {
			ids[i] = getBucketId(data[i]);
		}

		return ids;
	}

	/**
	 * Compute identifiers of the buckets in which the given point belongs for
	 * all the tables.
	 *
	 * @param point
	 *            the point
	 * @return the bucket identifiers
	 */
	public int[] getBucketId(OBJECT point) {
		final int[] ids = new int[tables.size()];

		for (int j = 0; j < tables.size(); j++) {
			ids[j] = tables.get(j).function.computeHashCode(point);
		}

		return ids;
	}

	@Override
	public void searchNN(OBJECT[] qus, int[] argmins, float[] mins) {
		final int[][] argminsWrapper = { argmins };
		final float[][] minsWrapper = { mins };

		searchKNN(qus, 1, argminsWrapper, minsWrapper);
	}

	@Override
	public void searchKNN(OBJECT[] qus, int K, int[][] argmins, float[][] mins) {
		// loop on the search data
		for (int i = 0; i < qus.length; i++) {
			final TIntHashSet pl = search(qus[i]);

			// now sort the selected points by distance
			final int[] ids = pl.toArray();
			final List<OBJECT> vectors = new ArrayList<OBJECT>(ids.length);
			for (int j = 0; j < ids.length; j++) {
				vectors.add(data.get(ids[j]));
			}

			exactNN(vectors, ids, qus[i], K, argmins[i], mins[i]);
		}
	}

	@Override
	public void searchNN(List<OBJECT> qus, int[] argmins, float[] mins) {
		final int[][] argminsWrapper = { argmins };
		final float[][] minsWrapper = { mins };

		searchKNN(qus, 1, argminsWrapper, minsWrapper);
	}

	@Override
	public void searchKNN(List<OBJECT> qus, int K, int[][] argmins, float[][] mins) {
		final int size = qus.size();
		// loop on the search data
		for (int i = 0; i < size; i++) {
			final TIntHashSet pl = search(qus.get(i));

			// now sort the selected points by distance
			final int[] ids = pl.toArray();
			final List<OBJECT> vectors = new ArrayList<OBJECT>(ids.length);
			for (int j = 0; j < ids.length; j++) {
				vectors.add(data.get(ids[j]));
			}

			exactNN(vectors, ids, qus.get(i), K, argmins[i], mins[i]);
		}
	}

	/*
	 * Exact NN on a subset
	 */
	private void exactNN(List<OBJECT> subset, int[] ids, OBJECT query, int K, int[] argmins, float[] mins) {
		final int size = subset.size();

		// Fix for when the user asks for too many points.
		final int actualK = Math.min(K, size);

		for (int k = actualK; k < K; k++) {
			argmins[k] = -1;
			mins[k] = Float.MAX_VALUE;
		}

		if (actualK == 0)
			return;

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(actualK, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(actualK + 1);
		for (int i = 0; i < actualK + 1; i++) {
			list.add(new IntFloatPair());
		}

		final List<IntFloatPair> result = search(subset, query, queue, list);

		for (int k = 0; k < actualK; ++k) {
			final IntFloatPair p = result.get(k);
			argmins[k] = ids[p.first];
			mins[k] = p.second;
		}
	}

	private List<IntFloatPair> search(List<OBJECT> subset, OBJECT query, BoundedPriorityQueue<IntFloatPair> queue,
			List<IntFloatPair> results)
	{
		final int size = subset.size();

		IntFloatPair wp = null;
		// reset all values in the queue to MAX, -1
		for (final IntFloatPair p : results) {
			p.second = Float.MAX_VALUE;
			p.first = -1;
			wp = queue.offerItem(p);
		}

		// perform the search
		for (int i = 0; i < size; i++) {
			wp.second = (float) distanceFcn.compare(query, subset.get(i));
			wp.first = i;
			wp = queue.offerItem(wp);
		}

		return queue.toOrderedListDestructive();
	}

	@Override
	public int size() {
		return data.size();
	}

	/**
	 * Get a read-only view of the underlying data.
	 *
	 * @return a read-only view of the underlying data.
	 */
	public List<OBJECT> getData() {
		return new AbstractList<OBJECT>() {

			@Override
			public OBJECT get(int index) {
				return data.get(index);
			}

			@Override
			public int size() {
				return data.size();
			}
		};
	}

	/**
	 * Get the data item at the given index.
	 *
	 * @param i
	 *            The index
	 * @return the retrieved object
	 */
	public OBJECT get(int i) {
		return data.get(i);
	}

	@Override
	public int[] addAll(List<OBJECT> d) {
		final int[] indexes = new int[d.size()];

		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = add(d.get(i));
		}

		return indexes;
	}

	@Override
	public List<IntFloatPair> searchKNN(OBJECT query, int K) {
		final ArrayList<OBJECT> qus = new ArrayList<OBJECT>(1);
		qus.add(query);

		final int[][] idx = new int[1][K];
		final float[][] dst = new float[1][K];

		this.searchKNN(qus, K, idx, dst);

		final List<IntFloatPair> res = new ArrayList<IntFloatPair>();
		for (int k = 0; k < K; k++) {
			if (idx[0][k] != -1)
				res.add(new IntFloatPair(idx[0][k], dst[0][k]));
		}

		return res;
	}

	@Override
	public IntFloatPair searchNN(OBJECT query) {
		final ArrayList<OBJECT> qus = new ArrayList<OBJECT>(1);
		qus.add(query);

		final int[] idx = new int[1];
		final float[] dst = new float[1];

		this.searchNN(qus, idx, dst);

		if (idx[0] == -1)
			return null;

		return new IntFloatPair(idx[0], dst[0]);
	}
}
