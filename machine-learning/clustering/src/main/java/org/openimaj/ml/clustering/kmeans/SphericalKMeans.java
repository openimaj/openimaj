package org.openimaj.ml.clustering.kmeans;

import java.util.Random;

import org.openimaj.data.DataSource;
import org.openimaj.data.DoubleArrayBackedDataSource;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.parallel.Parallel.IntRange;

/**
 * Multithreaded (optionally) damped spherical k-means with support for
 * bigger-than-memory data.
 * <p>
 * Spherical K-Means uses the inner product as the similarity metric, and is
 * constrained to finding centroids that lie on the surface of the unit
 * hypersphere (i.e. their length is 1). More formally, it solves:
 * <p>
 * min_{D,s}(sum_i(||Ds^(i) - x^(i)||_2^2))
 * <p>
 * s.t. ||s^(i)||_0 <= 1, for all i and ||D^(j)||_2 = 1, for all i
 * <p>
 * where D is a dictionary of centroids (with unit length) and s is an indicator
 * vector that is all zeros, except for a non-zero value in the position
 * corresponding top the assigned centroid.
 * <p>
 * The optional damping operation includes the previous centroid position in the
 * update computation, ensuring smoother convergence.
 * <p>
 * This implementation performs initialisation by randomly sampling centroids
 * from a Gaussian distribution, and then normalising to unit length. Any
 * centroids that become empty during the iterations are replaced by a new
 * random centroid generated in the same manner.
 * <p>
 * This implementation is able to deal with larger-than-memory datasets by
 * streaming the samples from disk using an appropriate {@link DataSource}. The
 * only requirement is that there is enough memory to hold all the centroids
 * plus working memory for the batches of samples being assigned.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class SphericalKMeans implements SpatialClusterer<SphericalKMeansResult, double[]> {
	protected final Random rng = new Random();
	protected final boolean damped;
	protected final int maxIters;
	protected final int k;
	protected final double terminationEps = 0.1;

	/**
	 * Construct with the given parameters. Uses damped updates and terminates
	 * if the change in fit between iterations is less than 0.1.
	 *
	 * @param k
	 *            number of clusters
	 * @param maxIters
	 *            maximum number of iterations
	 */
	public SphericalKMeans(int k, int maxIters) {
		this(k, maxIters, true);
	}

	/**
	 * Construct with the given parameters. Terminates if the change in fit
	 * between iterations is less than 0.1.
	 *
	 * @param k
	 *            number of clusters
	 * @param maxIters
	 *            maximum number of iterations
	 * @param damped
	 *            use damped updates
	 */
	public SphericalKMeans(int k, int maxIters, boolean damped) {
		this.k = k;
		this.maxIters = maxIters;
		this.damped = damped;
	}

	/**
	 * Construct with the given parameters. Uses damped updates and terminates
	 * if the change in fit between iterations is less than 0.1 or 10 iterations
	 * is reached.
	 *
	 * @param k
	 *            number of clusters
	 */
	public SphericalKMeans(int k) {
		this(k, 10);
	}

	private void makeRandomCentroid(double[] ds) {
		double sumsq = 0;
		for (int i = 0; i < ds.length; i++) {
			ds[i] = rng.nextGaussian();
			sumsq += (ds[i] * ds[i]);
		}

		sumsq = 1 / Math.sqrt(sumsq);
		for (int i = 0; i < ds.length; i++) {
			ds[i] *= sumsq;
		}
	}

	double performIteration(final DataSource<double[]> data, final SphericalKMeansResult result) {
		final int[] clusterSizes = new int[result.centroids.length];
		final double[][] newCentroids = new double[result.centroids.length][result.centroids[0].length];

		final double[] delta = { 0 };

		// perform the assignments
		Parallel.forRange(0, data.size(), 1, new Operation<Parallel.IntRange>() {
			@Override
			public void perform(IntRange range) {
				double _delta = 0;

				for (int i = range.start; i < range.stop; i++) {
					final double[] vector = data.getData(i);
					double assignmentWeight = Double.MIN_VALUE;
					for (int j = 0; j < result.centroids.length; j++) {
						final double[] centroid = result.centroids[j];

						double dp = 0;
						for (int k = 0; k < centroid.length; k++) {
							dp += centroid[k] * vector[k];
						}

						if (dp > assignmentWeight) {
							assignmentWeight = dp;
							result.assignments[i] = j;
						}
					}

					// aggregate the assignments to the relevant cluster
					clusterSizes[result.assignments[i]]++;
					_delta += assignmentWeight;
					for (int k = 0; k < newCentroids[0].length; k++) {
						newCentroids[result.assignments[i]][k] += vector[k];
					}
				}

				delta[0] += _delta;
			}
		});
		// update the centroids
		Parallel.forRange(0, result.centroids.length, 1, new Operation<Parallel.IntRange>() {
			@Override
			public void perform(IntRange range) {
				for (int j = range.start; j < range.stop; j++) {
					if (clusterSizes[j] == 0) {
						// reinit to random vector
						makeRandomCentroid(result.centroids[j]);
					} else {
						final double[] centroid = result.centroids[j];
						final double[] ncentroid = newCentroids[j];

						double norm = 0;
						if (damped) {
							for (int k = 0; k < centroid.length; k++) {
								centroid[k] += ncentroid[k];
								norm += centroid[k] * centroid[k];
							}
						} else {
							for (int k = 0; k < centroid.length; k++) {
								centroid[k] = ncentroid[k];
								norm += centroid[k] * centroid[k];
							}
						}
						norm = 1.0 / Math.sqrt(norm);

						for (int k = 0; k < ncentroid.length; k++) {
							centroid[k] *= norm;
						}
					}
				}
			}
		});

		return delta[0];
	}

	@Override
	public int[][] performClustering(double[][] data) {
		return new IndexClusters(cluster(data).assignments).clusters();
	}

	@Override
	public SphericalKMeansResult cluster(double[][] data) {
		return cluster(new DoubleArrayBackedDataSource(data));
	}

	@Override
	public SphericalKMeansResult cluster(DataSource<double[]> data) {
		final SphericalKMeansResult result = new SphericalKMeansResult();
		result.centroids = new double[k][data.numDimensions()];
		result.assignments = new int[data.size()];

		for (int j = 0; j < result.centroids.length; j++) {
			makeRandomCentroid(result.centroids[j]);
		}

		double last = 0;
		for (int i = 0; i < maxIters; i++) {
			final double d = performIteration(data, result);
			final double delta = d - last;
			System.out.println(d - last);
			if (delta < terminationEps)
				break;
			last = d;
		}

		return result;
	}
}
