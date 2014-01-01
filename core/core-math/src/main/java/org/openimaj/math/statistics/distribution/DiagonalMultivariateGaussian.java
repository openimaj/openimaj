package org.openimaj.math.statistics.distribution;

import java.util.Arrays;

import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * Implementation of a {@link MultivariateGaussian} with a diagonal covariance
 * matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DiagonalMultivariateGaussian extends AbstractMultivariateGaussian {
	/**
	 * The diagonal of the covariance matrix
	 */
	public double[] variance;

	/**
	 * Construct the Gaussian with the provided center and covariance
	 * 
	 * @param mean
	 *            centre of the Gaussian
	 * @param variance
	 *            variance of the Gaussian
	 */
	public DiagonalMultivariateGaussian(Matrix mean, double[] variance) {
		this.mean = mean;
		this.variance = variance;
	}

	/**
	 * Construct the Gaussian with the zero mean and unit variance
	 * 
	 * @param ndims
	 *            number of dimensions
	 */
	public DiagonalMultivariateGaussian(int ndims) {
		this.mean = new Matrix(1, ndims);
		this.variance = new double[ndims];
		Arrays.fill(variance, 1);
	}

	@Override
	public Matrix getCovariance() {
		return MatrixUtils.diag(variance);
	}
}
