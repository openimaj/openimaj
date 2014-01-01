package org.openimaj.math.statistics.distribution;

import Jama.Matrix;

/**
 * Implementation of a spherical {@link MultivariateGaussian} (diagonal
 * covariance matrix with equal values).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SphericalMultivariateGaussian extends AbstractMultivariateGaussian {
	/**
	 * The variance
	 */
	public double variance = 1;

	/**
	 * Construct the Gaussian with the provided center and covariance
	 * 
	 * @param mean
	 *            centre of the Gaussian
	 * @param variance
	 *            variance of the Gaussian
	 */
	public SphericalMultivariateGaussian(Matrix mean, double variance) {
		this.mean = mean;
		this.variance = variance;
	}

	/**
	 * Construct the Gaussian with the zero mean and unit variance
	 * 
	 * @param ndims
	 *            number of dimensions
	 */
	public SphericalMultivariateGaussian(int ndims) {
		this.mean = new Matrix(1, ndims);
	}

	@Override
	public Matrix getCovariance() {
		final int d = mean.getColumnDimension();
		return Matrix.identity(d, d).timesEquals(variance);
	}
}
