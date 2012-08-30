package org.openimaj.ml.clustering.kmeans.fast;

public class KMeansConfiguration {
	/**
	 * The default number of samples per parallel assignment instance in the
	 * training phase.
	 */
	public static final int DEFAULT_BLOCK_SIZE = 50000;

	/**
	 * The default number of checks performed during search when in exact mode.
	 */
	public static final int DEFAULT_NCHECKS = 768;

	/**
	 * The default number of kdtrees when not in exact mode.
	 */
	public static final int DEFAULT_NTREES = 8;

	/**
	 * The default number of iterations during the training phase.
	 */
	public static final int DEFAULT_NITERS = 30;

	protected int M;
	protected int K;
	protected int ntrees;
	protected int nchecks;
	protected int block_size;
	protected int niters;
	protected boolean exact;
	protected int nThreads;

	/**
	 * Using data with M elements create K clusters. Whether the
	 * searching/clustering strategy should be exact is specified as are ntrees
	 * and nchecks (which are ignored if exact is true). The number of
	 * simultaneous threads during the training phase can also be specified.
	 * 
	 * @param M
	 *            number of elements in the data points. Default iterations and
	 *            block size is used.
	 * @param K
	 *            number of clusters to be found
	 * @param exact
	 *            exact mode
	 * @param ntrees
	 *            number of trees (ignored in exact mode)
	 * @param nchecks
	 *            number of checks per tree (ignored in exact mode)
	 * @param nThreads
	 *            number of parallel threads
	 */
	public KMeansConfiguration(int M, int K, boolean exact, int ntrees, int nchecks, int nThreads) {
		this(M, K, exact, ntrees, nchecks, DEFAULT_BLOCK_SIZE, DEFAULT_NITERS, nThreads);
	}

	/**
	 * Using data with M elements create K clusters. Whether the
	 * searching/clustering strategy should be exact is specified. Defaults are
	 * used for all other parameters.
	 * 
	 * @param M
	 *            number of elements in the data points
	 * @param K
	 *            number of clusters to be found
	 * @param exact
	 *            exact mode
	 */
	public KMeansConfiguration(int M, int K, boolean exact) {
		this(M, K, exact, DEFAULT_NTREES, DEFAULT_NCHECKS, DEFAULT_BLOCK_SIZE, DEFAULT_NITERS, Runtime.getRuntime()
				.availableProcessors());
	}

	/**
	 * Using data with M elements create K clusters. Whether the
	 * searching/clustering strategy should be exact is specified. The number of
	 * iterations during training can also be specified. All other parameters
	 * are default.
	 * 
	 * @param M
	 *            number of elements in the data points. Default iterations and
	 *            block size is used.
	 * @param K
	 *            number of clusters to be found
	 * @param exact
	 *            exact mode
	 * @param niters
	 *            number of iterations
	 */
	public KMeansConfiguration(int M, int K, boolean exact, int niters) {
		this(M, K,
				exact,
				DEFAULT_NTREES,
				DEFAULT_NCHECKS,
				DEFAULT_BLOCK_SIZE,
				niters,
				Runtime.getRuntime().availableProcessors());
	}

	/**
	 * Using data with M elements create K clusters. Whether the
	 * searching/clustering strategy should be exact is specified as are ntrees
	 * and nchecks (which are ignored if exact is true). The number of
	 * simultaneous threads during the training phase can also be specified.
	 * 
	 * @param M
	 *            number of elements in the data points. Default iterations and
	 *            block size is used.
	 * @param K
	 *            number of clusters to be found
	 * @param exact
	 *            exact mode
	 * @param ntrees
	 *            number of trees (ignored in exact mode)
	 * @param nchecks
	 *            number of checks per tree (ignored in exact mode)
	 * @param nThreads
	 *            number of parallel threads
	 * @param block_size
	 *            number of samples per parallel thread
	 * @param niters
	 *            number of training iterations
	 */
	public KMeansConfiguration(int M, int K, boolean exact, int ntrees, int nchecks, int block_size, int niters,
			int nThreads)
	{
		this.ntrees = ntrees;
		this.nchecks = nchecks;
		this.block_size = block_size;
		this.niters = niters;
		this.exact = exact;
		this.nThreads = nThreads;
		this.M = M;
		this.K = K;
	}

	/**
	 * A completely default Fast#T#KMeans used primarily as a convenience
	 * function for reading.
	 */
	public KMeansConfiguration() {
		this(0, 0, false, DEFAULT_NTREES, DEFAULT_NCHECKS, DEFAULT_BLOCK_SIZE, DEFAULT_NITERS, Runtime.getRuntime()
				.availableProcessors());
	}
}
