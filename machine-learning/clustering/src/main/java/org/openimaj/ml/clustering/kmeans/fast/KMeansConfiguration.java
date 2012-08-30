package org.openimaj.ml.clustering.kmeans.fast;

/**
 * Configuration for the KMeans algorithm implementations.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KMeansConfiguration implements Cloneable {
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
	 *            number of elements in the data points.
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
	 *            number of elements in the data points.
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
	 *            number of elements in the data points.
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public KMeansConfiguration clone() {
		try {
			return (KMeansConfiguration) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the dimensionality.
	 * 
	 * @return the number of elements in the data points
	 */
	public int numDimensions() {
		return M;
	}

	/**
	 * Set the dimensionality.
	 * 
	 * @param m
	 *            the number of elements in the data points
	 */
	public void setNumDimensions(int m) {
		M = m;
	}

	/**
	 * Get the number of clusters
	 * 
	 * @return the number of clusters
	 */
	public int getK() {
		return K;
	}

	/**
	 * Set the number of clusters
	 * 
	 * @param k
	 *            the number of clusters
	 */
	public void setK(int k) {
		K = k;
	}

	/**
	 * Get the number of clusters
	 * 
	 * @return the number of clusters
	 */
	public int numClusters() {
		return K;
	}

	/**
	 * Set the number of clusters
	 * 
	 * @param k
	 *            the number of clusters
	 */
	public void setNumClusters(int k) {
		K = k;
	}

	/**
	 * Get the number of trees in the KD-tree ensemble (ignored in exact mode).
	 * 
	 * @return the number of trees in the KD-tree ensemble (ignored in exact
	 *         mode).
	 */
	public int getNtrees() {
		return ntrees;
	}

	/**
	 * Set the number of trees in the KD-tree ensemble (ignored in exact mode).
	 * 
	 * @param ntrees
	 *            the number of trees in the KD-tree ensemble (ignored in exact
	 *            mode).
	 */
	public void setNtrees(int ntrees) {
		this.ntrees = ntrees;
	}

	/**
	 * Get the number of checks per tree (ignored in exact mode)
	 * 
	 * @return the number of checks per tree (ignored in exact mode)
	 */
	public int getNchecks() {
		return nchecks;
	}

	/**
	 * Set the number of checks per tree (ignored in exact mode)
	 * 
	 * @param nchecks
	 *            the number of checks per tree (ignored in exact mode)
	 */
	public void setNchecks(int nchecks) {
		this.nchecks = nchecks;
	}

	/**
	 * Get the number of samples processed in a batch by a thread. This needs to
	 * be small enough that that the memory isn't exhausted, but big enough for
	 * the thread to have enough data to work for a while.
	 * 
	 * @return the the number of samples processed in a batch by a thread
	 */
	public int getBlockSize() {
		return block_size;
	}

	/**
	 * Set the number of samples processed in a batch by a thread. This needs to
	 * be small enough that that the memory isn't exhausted, but big enough for
	 * the thread to have enough data to work for a while.
	 * 
	 * @param block_size
	 *            the number of samples processed in a batch by a thread
	 */
	public void setBlockSize(int block_size) {
		this.block_size = block_size;
	}

	/**
	 * Get the maximum allowed number of iterations.
	 * 
	 * @return the maximum allowed number of iterations.
	 */
	public int getMaxIterations() {
		return niters;
	}

	/**
	 * Set the maximum allowed number of iterations.
	 * 
	 * @param niters
	 *            the maximum allowed number of iterations.
	 */
	public void setMaxIterations(int niters) {
		this.niters = niters;
	}

	/**
	 * Should the KMeans algorithm work in exact mode (true; brute-force
	 * distance nearest-neighbours) or (false) approximate mode using an
	 * ensemble of KD-Trees for neighbour estimation.
	 * 
	 * @return true if exact; false if approximate.
	 */
	public boolean isExact() {
		return exact;
	}

	/**
	 * Set whether the KMeans algorithm works in exact mode (true; brute-force
	 * distance nearest-neighbours) or (false) approximate mode using an
	 * ensemble of KD-Trees for neighbour estimation.
	 * 
	 * @param exact
	 *            true to enable exact mode; false to enable approximate.
	 */
	public void setExact(boolean exact) {
		this.exact = exact;
	}

	/**
	 * @return the nThreads
	 */
	public int getnThreads() {
		return nThreads;
	}

	/**
	 * @param nThreads
	 *            the nThreads to set
	 */
	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}
}
