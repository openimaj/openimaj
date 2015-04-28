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
package org.openimaj.ml.clustering.kmeans;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.openimaj.data.ArrayBackedDataSource;
import org.openimaj.data.DataSource;
import org.openimaj.feature.FeatureVector;
import org.openimaj.knn.NearestNeighboursFactory;
import org.openimaj.knn.ObjectNearestNeighbours;
import org.openimaj.knn.ObjectNearestNeighboursExact;
import org.openimaj.knn.ObjectNearestNeighboursProvider;
import org.openimaj.ml.clustering.FeatureVectorCentroidsResult;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.ExactFeatureVectorAssigner;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.pair.IntFloatPair;

import com.rits.cloning.Cloner;

/**
 * Fast, parallel implementation of the K-Means algorithm with support for
 * bigger-than-memory data. Various flavors of K-Means are supported through the
 * selection of different subclasses of {@link ObjectNearestNeighbours}; for
 * example, exact K-Means can be achieved using an
 * {@link ObjectNearestNeighboursExact}. The specific choice of
 * nearest-neighbour object is controlled through the
 * {@link NearestNeighboursFactory} provided to the {@link KMeansConfiguration}
 * used to construct instances of this class. The choice of
 * {@link ObjectNearestNeighbours} affects the speed of clustering; using
 * approximate nearest-neighbours algorithms for the K-Means can produces
 * comparable results to the exact KMeans algorithm in much shorter time. The
 * choice and configuration of {@link ObjectNearestNeighbours} can also control
 * the type of distance function being used in the clustering.
 * <p>
 * The algorithm is implemented as follows: Clustering is initiated using a
 * {@link ByteKMeansInit} and is iterative. In each round, batches of samples
 * are assigned to centroids in parallel. The centroid assignment is performed
 * using the pre-configured {@link ObjectNearestNeighbours} instances created
 * from the {@link KMeansConfiguration}. Once all samples are assigned new
 * centroids are calculated and the next round started. Data point pushing is
 * performed using the same techniques as center point assignment.
 * <p>
 * This implementation is able to deal with larger-than-memory datasets by
 * streaming the samples from disk using an appropriate {@link DataSource}. The
 * only requirement is that there is enough memory to hold all the centroids
 * plus working memory for the batches of samples being assigned.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of object being clustered
 */
public class FeatureVectorKMeans<T extends FeatureVector> implements SpatialClusterer<FeatureVectorCentroidsResult<T>, T>
{
	private static class CentroidAssignmentJob<T extends FeatureVector> implements Callable<Boolean> {
		private final DataSource<T> ds;
		private final int startRow;
		private final int stopRow;
		private final ObjectNearestNeighbours<T> nno;
		private final double[][] centroids_accum;
		private final int[] counts;

		public CentroidAssignmentJob(DataSource<T> ds, int startRow, int stopRow, ObjectNearestNeighbours<T> nno,
				double[][] centroids_accum, int[] counts)
		{
			this.ds = ds;
			this.startRow = startRow;
			this.stopRow = stopRow;
			this.nno = nno;
			this.centroids_accum = centroids_accum;
			this.counts = counts;
		}

		@Override
		public Boolean call() {
			try {
				final int D = ds.getData(0).length();

				final T[] points = ds.createTemporaryArray(stopRow - startRow);
				ds.getData(startRow, stopRow, points);

				final int[] argmins = new int[points.length];
				final float[] mins = new float[points.length];

				nno.searchNN(points, argmins, mins);

				synchronized (centroids_accum) {
					for (int i = 0; i < points.length; ++i) {
						final int k = argmins[i];
						final double[] vector = points[i].asDoubleVector();
						for (int d = 0; d < D; ++d) {
							centroids_accum[k][d] += vector[d];
						}
						counts[k] += 1;
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	}

	private static class Result<T extends FeatureVector> extends FeatureVectorCentroidsResult<T>
			implements
	ObjectNearestNeighboursProvider<T>
	{
		protected ObjectNearestNeighbours<T> nn;

		@Override
		public ObjectNearestNeighbours<T> getNearestNeighbours() {
			return nn;
		}

		@Override
		public HardAssigner<T, float[], IntFloatPair> defaultHardAssigner() {
			return new ExactFeatureVectorAssigner<T>(this, nn.distanceComparator());
		}
	}

	private FeatureVectorKMeansInit<T> init = new FeatureVectorKMeansInit.RANDOM<T>();
	private KMeansConfiguration<ObjectNearestNeighbours<T>, T> conf;
	private Random rng = new Random();

	/**
	 * Construct the clusterer with the the given configuration.
	 *
	 * @param conf
	 *            The configuration.
	 */
	public FeatureVectorKMeans(KMeansConfiguration<ObjectNearestNeighbours<T>, T> conf) {
		this.conf = conf;
	}

	/**
	 * A completely default {@link ByteKMeans} used primarily as a convenience
	 * function for reading.
	 */
	protected FeatureVectorKMeans() {
		this(new KMeansConfiguration<ObjectNearestNeighbours<T>, T>());
	}

	/**
	 * Get the current initialisation algorithm
	 *
	 * @return the init algorithm being used
	 */
	public FeatureVectorKMeansInit<T> getInit() {
		return init;
	}

	/**
	 * Set the current initialisation algorithm
	 *
	 * @param init
	 *            the init algorithm to be used
	 */
	public void setInit(FeatureVectorKMeansInit<T> init) {
		this.init = init;
	}

	/**
	 * Set the seed for the internal random number generator.
	 *
	 * @param seed
	 *            the random seed for init random sample selection, no seed if
	 *            seed < -1
	 */
	public void seed(long seed) {
		if (seed < 0)
			this.rng = new Random();
		else
			this.rng = new Random(seed);
	}

	/**
	 * Perform clustering on the given data.
	 *
	 * @param data
	 *            the data.
	 *
	 * @return the generated clusters.
	 */
	public FeatureVectorCentroidsResult<T> cluster(List<T> data) {
		@SuppressWarnings("unchecked")
		T[] d = (T[]) Array.newInstance(data.get(0).getClass(), data.size());
		d = data.toArray(d);
		return cluster(d);
	}

	@Override
	public FeatureVectorCentroidsResult<T> cluster(T[] data) {
		final ArrayBackedDataSource<T> ds = new ArrayBackedDataSource<T>(data, rng) {
			@Override
			public int numDimensions() {
				return data[0].length();
			}
		};

		try {
			final Result<T> result = cluster(ds, conf.K);
			result.nn = conf.factory.create(result.centroids);

			return result;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int[][] performClustering(T[] data) {
		final FeatureVectorCentroidsResult<T> clusters = this.cluster(data);
		return new IndexClusters(clusters.defaultHardAssigner().assign(data)).clusters();
	}

	/**
	 * Perform clustering on the given data.
	 *
	 * @param data
	 *            the data.
	 *
	 * @return the generated clusters.
	 */
	public int[][] performClustering(List<T> data) {
		@SuppressWarnings("unchecked")
		T[] d = (T[]) Array.newInstance(data.get(0).getClass(), data.size());
		d = data.toArray(d);

		final FeatureVectorCentroidsResult<T> clusters = this.cluster(d);
		return new IndexClusters(clusters.defaultHardAssigner().assign(d)).clusters();
	}

	/**
	 * Initiate clustering with the given data and number of clusters.
	 * Internally this method constructs the array to hold the centroids and
	 * calls {@link #cluster(DataSource, Object)}.
	 *
	 * @param data
	 *            data source to cluster with
	 * @param K
	 *            number of clusters to find
	 * @return cluster centroids
	 */
	protected Result<T> cluster(DataSource<T> data, int K) throws Exception {
		final Result<T> result = new Result<T>();
		result.centroids = data.createTemporaryArray(K);

		init.initKMeans(data, result.centroids);

		cluster(data, result);

		return result;
	}

	/**
	 * Main clustering algorithm. A number of threads as specified are started
	 * each containing an assignment job and a reference to the same set of
	 * ObjectNearestNeighbours object (i.e. Exact or KDTree). Each thread is
	 * added to a job pool and started in parallel. A single accumulator is
	 * shared between all threads and locked on update.
	 *
	 * @param data
	 *            the data to be clustered
	 * @param centroids
	 *            the centroids to be found
	 */
	protected void cluster(DataSource<T> data, Result<T> result) throws Exception {
		final T[] centroids = result.centroids;
		final int K = centroids.length;
		final int D = centroids[0].length();
		final int N = data.size();
		final double[][] centroids_accum = new double[K][D];
		final int[] new_counts = new int[K];

		final ExecutorService service = conf.threadpool;

		for (int i = 0; i < conf.niters; i++) {
			System.err.println("Iteration " + i);
			for (int j = 0; j < K; j++)
				Arrays.fill(centroids_accum[j], 0);
			Arrays.fill(new_counts, 0);

			final ObjectNearestNeighbours<T> nno = conf.factory.create(centroids);

			final List<CentroidAssignmentJob<T>> jobs = new ArrayList<CentroidAssignmentJob<T>>();
			for (int bl = 0; bl < N; bl += conf.blockSize) {
				final int br = Math.min(bl + conf.blockSize, N);
				jobs.add(new CentroidAssignmentJob<T>(data, bl, br, nno, centroids_accum, new_counts));
			}

			service.invokeAll(jobs);

			for (int k = 0; k < K; ++k) {
				if (new_counts[k] == 0) {
					// If there's an empty cluster we replace it with a random
					// point.
					new_counts[k] = 1;

					final T[] rnd = data.createTemporaryArray(1);
					data.getRandomRows(rnd);

					final Cloner cloner = new Cloner();
					centroids[k] = cloner.deepClone(rnd[0]);
				} else {
					for (int d = 0; d < D; ++d) {
						centroids[k].setFromDouble(d, centroids_accum[k][d] / new_counts[k]);
					}
				}
			}
		}
	}

	protected float roundFloat(double value) {
		return (float) value;
	}

	protected double roundDouble(double value) {
		return value;
	}

	protected long roundLong(double value) {
		return Math.round(value);
	}

	protected int roundInt(double value) {
		return (int) Math.round(value);
	}

	@Override
	public FeatureVectorCentroidsResult<T> cluster(DataSource<T> ds) {
		try {
			final Result<T> result = cluster(ds, conf.K);
			result.nn = conf.factory.create(result.centroids);

			return result;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the configuration
	 *
	 * @return the configuration
	 */
	public KMeansConfiguration<ObjectNearestNeighbours<T>, T> getConfiguration() {
		return conf;
	}

	/**
	 * Set the configuration
	 *
	 * @param conf
	 *            the configuration to set
	 */
	public void setConfiguration(KMeansConfiguration<ObjectNearestNeighbours<T>, T> conf) {
		this.conf = conf;
	}

	/**
	 * Convenience method to quickly create an exact {@link ByteKMeans}. All
	 * parameters other than the number of clusters are set at their defaults,
	 * but can be manipulated through the configuration returned by
	 * {@link #getConfiguration()}.
	 * <p>
	 * Euclidean distance is used to measure the distance between points.
	 *
	 * @param K
	 *            the number of clusters
	 * @param distance
	 *            the distance measure
	 * @return a {@link ByteKMeans} instance configured for exact k-means
	 */
	public static <T extends FeatureVector> FeatureVectorKMeans<T> createExact(int K,
			DistanceComparator<? super T> distance)
	{
		final KMeansConfiguration<ObjectNearestNeighbours<T>, T> conf =
				new KMeansConfiguration<ObjectNearestNeighbours<T>, T>(K, new ObjectNearestNeighboursExact.Factory<T>(
						distance));

		return new FeatureVectorKMeans<T>(conf);
	}

	/**
	 * Convenience method to quickly create an exact {@link ByteKMeans}. All
	 * parameters other than the number of clusters and number of iterations are
	 * set at their defaults, but can be manipulated through the configuration
	 * returned by {@link #getConfiguration()}.
	 * <p>
	 * Euclidean distance is used to measure the distance between points.
	 *
	 * @param K
	 *            the number of clusters
	 * @param distance
	 *            the distance measure
	 * @param niters
	 *            maximum number of iterations
	 * @return a {@link ByteKMeans} instance configured for exact k-means
	 */
	public static <T extends FeatureVector> FeatureVectorKMeans<T> createExact(int K,
			DistanceComparator<? super T> distance, int niters)
	{
		final KMeansConfiguration<ObjectNearestNeighbours<T>, T> conf =
				new KMeansConfiguration<ObjectNearestNeighbours<T>, T>(K,
						new ObjectNearestNeighboursExact.Factory<T>(distance),
						niters);

		return new FeatureVectorKMeans<T>(conf);
	}

	@Override
	public String toString() {
		return String.format("%s: {K=%d, NN=%s}", this.getClass().getSimpleName(), this.conf.K, this.conf
				.getNearestNeighbourFactory().getClass().getSimpleName());
	}
}
