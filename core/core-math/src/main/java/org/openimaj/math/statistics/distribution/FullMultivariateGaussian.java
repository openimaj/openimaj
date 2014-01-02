package org.openimaj.math.statistics.distribution;

import Jama.Matrix;

/**
 * Implementation of a basic {@link MultivariateGaussian} with a full covariance
 * matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FullMultivariateGaussian extends AbstractMultivariateGaussian {
	/**
	 * The covariance matrix
	 */
	public Matrix covar;

	/**
	 * Construct the Gaussian with the provided center and covariance
	 * 
	 * @param mean
	 *            centre of the Gaussian
	 * @param covar
	 *            covariance of the Gaussian
	 */
	public FullMultivariateGaussian(Matrix mean, Matrix covar) {
		this.mean = mean;
		this.covar = covar;
	}

	/**
	 * Construct the Gaussian with the zero mean and unit variance
	 * 
	 * @param ndims
	 *            number of dimensions
	 */
	public FullMultivariateGaussian(int ndims) {
		this.mean = new Matrix(1, ndims);
		this.covar = Matrix.identity(ndims, ndims);
	}

	@Override
	public Matrix getCovariance() {
		return covar;
	}

	@Override
	public double getCovariance(int row, int column) {
		return this.covar.get(row, column);
	}
}
