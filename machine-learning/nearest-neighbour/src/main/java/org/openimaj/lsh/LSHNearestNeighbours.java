package org.openimaj.lsh;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.data.DataSource;
import org.openimaj.knn.NearestNeighbours;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.pair.DoubleIntPair;
import org.openimaj.util.pair.FloatIntPair;

public class LSHNearestNeighbours<OBJECT>
		implements
			NearestNeighbours<OBJECT, float[]>
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
	protected DataSource<OBJECT> data;

	public LSHNearestNeighbours(List<HashFunction<OBJECT>> tableHashes, DistanceComparator<OBJECT> distanceFcn) {
		final int numTables = tableHashes.size();
		this.distanceFcn = distanceFcn;
		this.tables = new ArrayList<Table<OBJECT>>(numTables);

		for (int i = 0; i < numTables; i++) {
			tables.add(new Table<OBJECT>(tableHashes.get(i)));
		}
	}

	public LSHNearestNeighbours(HashFunctionFactory<OBJECT> factory, int numTables, DistanceComparator<OBJECT> distanceFcn)
	{
		this.distanceFcn = distanceFcn;
		this.tables = new ArrayList<Table<OBJECT>>(numTables);

		for (int i = 0; i < numTables; i++) {
			tables.add(new Table<OBJECT>(factory.create()));
		}
	}

	/**
	 * @return the number of hash tables
	 */
	public int numTables() {
		return tables.size();
	}

	/**
	 * Insert points into the tables
	 * 
	 * @param data
	 *            points
	 */
	private void insertPoints(DataSource<OBJECT> data) {
		int i = 0;

		for (final OBJECT point : data) {
			for (final Table<OBJECT> table : tables) {
				table.insertPoint(point, i);
			}

			i++;
		}
	}

	/**
	 * Search for points in the underlying tables and return all matches
	 * 
	 * @param data
	 *            the points
	 * @return matched ids
	 */
	public TIntHashSet[] searchPoints(OBJECT[] data) {
		final TIntHashSet[] pls = new TIntHashSet[data.length];

		for (int i = 0; i < data.length; i++) {
			pls[i] = searchPoint(data[i]);
		}

		return pls;
	}

	/**
	 * Search for a point in the underlying tables and return all matches
	 * 
	 * @param data
	 *            the point
	 * @return matched ids
	 */
	public TIntHashSet searchPoint(OBJECT data) {
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
			final TIntHashSet pl = searchPoint(qus[i]);

			// now sort the selected points by distance
			final int[] ids = pl.toArray();
			final List<OBJECT> vectors = new ArrayList<OBJECT>(ids.length);
			for (int j = 0; j < ids.length; j++) {
				vectors.add(data.getData(ids[j]));
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
			final TIntHashSet pl = searchPoint(qus.get(i));

			// now sort the selected points by distance
			final int[] ids = pl.toArray();
			final List<OBJECT> vectors = new ArrayList<OBJECT>(ids.length);
			for (int j = 0; j < ids.length; j++) {
				vectors.add(data.getData(ids[j]));
			}

			exactNN(vectors, ids, qus.get(i), K, argmins[i], mins[i]);
		}
	}

	/*
	 * Exact NN on a subset
	 */
	private void exactNN(List<OBJECT> data, int[] ids, OBJECT query, int K, int[] argmins, float[] mins) {
		final int size = data.size();

		// Fix for when the user asks for too many points.
		K = Math.min(K, size);

		final FloatIntPair[] knn_prs = new FloatIntPair[size];
		for (int i = 0; i < size; i++) {
			knn_prs[i] = new FloatIntPair((float) distanceFcn.compare(query, data.get(i)), ids[i]);
		}

		Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
			@Override
			public boolean apply(Object arg0, Object arg1) {
				final DoubleIntPair p1 = (DoubleIntPair) arg0;
				final DoubleIntPair p2 = (DoubleIntPair) arg1;

				if (p1.first < p2.first)
					return true;
				if (p2.first < p1.first)
					return false;
				return (p1.second < p2.second);
			}
		});

		for (int k = 0; k < K; ++k) {
			argmins[k] = knn_prs[k].second;
			mins[k] = knn_prs[k].first;
		}
	}

	@Override
	public int numDimensions() {
		return data.numDimensions();
	}

	@Override
	public int size() {
		return data.numRows();
	}
}
