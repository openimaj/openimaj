package org.openimaj.lsh;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import org.openimaj.data.DataSource;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.lsh.functions.DoubleHashFunction;
import org.openimaj.lsh.functions.HashFunctionFactory;
import org.openimaj.util.pair.DoubleIntPair;

import cern.jet.random.engine.MersenneTwister;

public class DoubleNearestNeighboursLSH<F extends HashFunctionFactory<DoubleHashFunction>> extends DoubleNearestNeighbours {
	private static class Table<F extends HashFunctionFactory<DoubleHashFunction>> {
		private static final int HASH_POLY = 1368547; 
		private static final int HASH_POLY_REM = 573440;
		private static final int HASH_POLY_A[] = {1342, 876454, 656565, 223, 337, 9847, 87676, 34234, 23445, 76543, 8676234, 3497, 9876, 87856, 2342858};

		private TIntObjectHashMap<TIntArrayList> table;
		DoubleHashFunction[] functions;

		public Table(F factory, MersenneTwister rng, int ndims, int nFunctions) {
			functions = new DoubleHashFunction[nFunctions];

			for (int i=0; i<nFunctions; i++)
				functions[i] = factory.create(ndims, rng);

			table = new TIntObjectHashMap<TIntArrayList>();
		}

		/**
		 * Insert a single point
		 * @param point the point
		 * @param pid the id of the point in the data
		 * @param norm whether or not to apply normalisation
		 */
		protected void insertPoint(double[] point, int pid, double normVal) {
			int hash = computeHashCode(point, normVal);

			TIntArrayList bucket = table.get(hash);
			if (bucket == null) {
				table.put(hash, bucket = new TIntArrayList());
			}

			bucket.add(pid);
		}

		/**
		 * Search for a point in the table
		 * @param point query point
		 * @param norm normalisation factor
		 * @return ids of matched points
		 */
		protected TIntArrayList searchPoint(double [] point, double norm) {
			int hash = computeHashCode(point, norm);

			return table.get(hash);
		}

		/**
		 * Compute the hash code for the point
		 * @param point the hash code
		 * @param normVal the normalisation value
		 * @return the code
		 */
		protected int computeHashCode(double[] point, double normVal) {
			if (functions == null || functions.length == 0) return 0;

//			int id = functions[0].computeHashCode(point, normVal);
//			for (int i=1, s=functions.length; i<s; i++) {
//				int val = functions[i].computeHashCode(point, normVal);
//				
//				id = addId(id, val, i);
//			}
			
			String h = "";
			for (int i=0, s=functions.length; i<s; i++) {
				h += functions[i].computeHashCode(point, normVal);
			}
			System.out.println(h);
			return h.hashCode();
			//return id;
		}

		private int addId(int id, int val, int pos) {
			return (val * HASH_POLY_A[pos % HASH_POLY_A.length] % HASH_POLY) +  (id * HASH_POLY_REM % HASH_POLY);
			//return (id << 1) | val;
		}
	}
	
	protected DoubleFVComparison distanceFcn = DoubleFVComparison.EUCLIDEAN;
	protected Table<F>[] tables;
	protected DataSource<double[]> data;
	protected F factory;

	@SuppressWarnings("unchecked")
	public
	DoubleNearestNeighboursLSH(F factory, int seed, int ntables, int nFunctions, DataSource<double[]> data) {
		this.factory = factory;
		this.distanceFcn = factory.defaultDistanceFunction();
		this.tables = new Table[ntables];
		this.data = data;

		MersenneTwister rng = new MersenneTwister(seed);
		for (int i=0; i<ntables; i++) {
			tables[i] = new Table<F>(factory, rng, data.numDimensions(), nFunctions);
		}

		insertPoints(data);
	}

	/**
	 * @return the number of hash tables
	 */
	public int numTables() {
		return tables.length;
	}

	/**
	 * @return the number of hash functions per table
	 */
	public int numFunctions() {
		return tables[0].functions.length;
	}

	/**
	 * Insert points into the tables
	 * @param data points
	 */
	private void insertPoints(DataSource<double[]> data) {
		int i=0;

		for (double[] point : data) {
			double norm = factory.norm ? factory.computeNorm(point) : 0;
			
			for (Table<F> table : tables) {
				table.insertPoint(point, i, norm);
			}
			
			i++;
		}
	}

	/**
	 * Search for points in the underlying tables and return all matches
	 * @param data the points
	 * @return matched ids
	 */
	public TIntHashSet[] searchPoints(double[][] data) {
		TIntHashSet[] pls = new TIntHashSet[data.length];

		for (int i=0; i<data.length; i++) {
			pls[i] = searchPoint(data[i]);
		}

		return pls;
	}

	/**
	 * Search for a point in the underlying tables and return all matches
	 * @param data the point
	 * @return matched ids
	 */
	public TIntHashSet searchPoint(double[] data) {
		TIntHashSet pl = new TIntHashSet();

		double norm = factory.norm ? factory.computeNorm(data) : 0;
		for (Table<F> table : tables) {
			TIntArrayList result = table.searchPoint(data, norm);
			
			if (result != null) pl.addAll(result);
		}

		return pl;
	}

	/**
	 * Compute identifiers of the buckets in which the given
	 * points belong for all the tables.
	 * @param data the points
	 * @return the bucket identifiers
	 */
	public int [][] getBucketId(double[][] data) {
		int [][] ids = new int[data.length][];

		for (int i=0; i<data.length; i++) {
			ids[i] = getBucketId(data[i]);
		}

		return ids;
	}

	/**
	 * Compute identifiers of the buckets in which the given
	 * point belongs for all the tables.
	 * @param point the point
	 * @return the bucket identifiers
	 */
	public int [] getBucketId(double[] point) {
		int [] ids = new int[tables.length];

		double norm = factory.norm ? factory.computeNorm(point) : 0;

		for (int j=0; j<tables.length; j++) {
			ids[j] = tables[j].computeHashCode(point, norm);
		}

		return ids;
	}

	/**
	 * Get the hash function values for every hash function in
	 * every table for the given point
	 * @param point the point
	 * @return an array of hash function values per table (rows) and function (columns) 
	 */
	int [][] getHashFunctionValues(double[] point) {
		int nFunctions = tables[0].functions.length;

		int[][] values = new int[tables.length][nFunctions];

		double norm = factory.norm ? factory.computeNorm(point) : 0;
		
		for (int t=0; t<tables.length; t++) {
			for (int f=0; f<nFunctions; ++f) {
				values[t][f] = tables[t].functions[f].computeHashCode(point, norm);
			}
		}

		return values;
	}

	/**
	 * Get the hash function values for every hash function in
	 * every table for the given points
	 * @param data the points
	 * @return an array of arrays of hash function values per table (rows) and function (columns) 
	 */
	public int [][][] getHashFunctionValues(double[][] data) {
		int[][][] values = new int[data.length][][];

		for (int i=0; i<data.length; i++) {
			values[i] = getHashFunctionValues(data[i]);
		}

		return values;
	}

	@Override
	public void searchNN(double[][] qus, int[] argmins, double[] mins) {
		int [][] argminsWrapper = { argmins };
		double [][] minsWrapper = { mins };
		
		searchKNN(qus, 1, argminsWrapper, minsWrapper);
	}

	@Override
	public void searchKNN(double[][] qus, int K, int[][] argmins, double[][] mins) {
		//loop on the search data
		for (int i=0; i<qus.length; i++) {
			TIntHashSet pl = searchPoint(qus[i]);

			//now sort the selected points by distance 
			int [] ids = pl.toArray();
			double[][] vectors = new double[ids.length][];
			for (int j=0; j<ids.length; j++) {
				vectors[j] = data.getData(ids[j]);
			}

			System.out.println("Performing exact on " + pl.size() + " samples");
			exactNN(vectors, ids, qus[i], K, argmins[i], mins[i]);
		}  
	}

	/*
	 * Exact NN on a subset
	 */
	private void exactNN(double[][] data, int [] ids, double[] query, int K, int[] argmins, double[] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, data.length);

		DoubleIntPair[] knn_prs = new DoubleIntPair[data.length];
		for (int i=0; i < data.length; i++) {
			knn_prs[i] = new DoubleIntPair(distanceFcn.compare(query, data[i]), ids[i]);
		}

		Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
			@Override
			public boolean apply(Object arg0, Object arg1) {
				DoubleIntPair p1 = (DoubleIntPair) arg0;
				DoubleIntPair p2 = (DoubleIntPair) arg1;

				if (p1.first < p2.first) return true;
				if (p2.first < p1.first) return false;
				return (p1.second < p2.second);
			}});

		for (int k=0; k < K; ++k) {
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
