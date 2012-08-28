package org.openimaj.lsh;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.List;

import org.openimaj.data.DataSource;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.lsh.functions.DoubleHashFunctionFactory;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.pair.DoubleIntPair;

public class DoubleNearestNeighboursLSH<F extends DoubleHashFunctionFactory>
		extends
			DoubleNearestNeighbours
{
	private static class Table {
		private final TIntObjectHashMap<TIntArrayList> table;
		HashFunction<double[]> function;

		public Table(HashFunction<double[]> function) {
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
		protected void insertPoint(double[] point, int pid) {
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
		protected TIntArrayList searchPoint(double[] point) {
			final int hash = function.computeHashCode(point);

			return table.get(hash);
		}
	}

	protected DistanceComparator<double[]> distanceFcn;
	protected Table[] tables;
	protected DataSource<double[]> data;

	public DoubleNearestNeighboursLSH(List<HashFunction<double[]>> tableHashes, DistanceComparator<double[]> distanceFcn)
	{
		final int numTables = tableHashes.size();
		this.distanceFcn = distanceFcn;
		this.tables = new Table[numTables];

		for (int i = 0; i < numTables; i++) {
			tables[i] = new Table(tableHashes.get(i));
		}
	}

	public DoubleNearestNeighboursLSH(DoubleHashFunctionFactory factory, int numTables) {
		this.distanceFcn = factory.distanceFunction();
		this.tables = new Table[numTables];

		for (int i = 0; i < numTables; i++) {
			tables[i] = new Table(factory.create());
		}
	}

	public DoubleNearestNeighboursLSH(HashFunctionFactory<double[]> factory, int numTables,
			DistanceComparator<double[]> distanceFcn)
	{
		this.distanceFcn = distanceFcn;
		this.tables = new Table[numTables];

		for (int i = 0; i < numTables; i++) {
			tables[i] = new Table(factory.create());
		}
	}

	/**
	 * @return the number of hash tables
	 */
	public int numTables() {
		return tables.length;
	}

	/**
	 * Insert points into the tables
	 * 
	 * @param data
	 *            points
	 */
	private void insertPoints(DataSource<double[]> data) {
		int i = 0;

		for (final double[] point : data) {
			for (final Table table : tables) {
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
	public TIntHashSet[] searchPoints(double[][] data) {
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
	public TIntHashSet searchPoint(double[] data) {
		final TIntHashSet pl = new TIntHashSet();

		for (final Table table : tables) {
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
	public int[][] getBucketId(double[][] data) {
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
	public int[] getBucketId(double[] point) {
		final int[] ids = new int[tables.length];

		for (int j = 0; j < tables.length; j++) {
			ids[j] = tables[j].function.computeHashCode(point);
		}

		return ids;
	}

	@Override
	public void searchNN(double[][] qus, int[] argmins, double[] mins) {
		final int[][] argminsWrapper = { argmins };
		final double[][] minsWrapper = { mins };

		searchKNN(qus, 1, argminsWrapper, minsWrapper);
	}

	@Override
	public void searchKNN(double[][] qus, int K, int[][] argmins, double[][] mins) {
		// loop on the search data
		for (int i = 0; i < qus.length; i++) {
			final TIntHashSet pl = searchPoint(qus[i]);

			// now sort the selected points by distance
			final int[] ids = pl.toArray();
			final double[][] vectors = new double[ids.length][];
			for (int j = 0; j < ids.length; j++) {
				vectors[j] = data.getData(ids[j]);
			}

			System.out.println("Performing exact on " + pl.size() + " samples");
			exactNN(vectors, ids, qus[i], K, argmins[i], mins[i]);
		}
	}

	/*
	 * Exact NN on a subset
	 */
	private void exactNN(double[][] data, int[] ids, double[] query, int K, int[] argmins, double[] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, data.length);

		final DoubleIntPair[] knn_prs = new DoubleIntPair[data.length];
		for (int i = 0; i < data.length; i++) {
			knn_prs[i] = new DoubleIntPair(distanceFcn.compare(query, data[i]), ids[i]);
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
