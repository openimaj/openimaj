package org.openimaj.math.statistics;

import Jama.Matrix;

/**
 * Class to compute the mean and covariance of some given data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MeanAndCovariance {
	/**
	 * The mean vector (1xN)
	 */
	public final Matrix mean;

	/**
	 * The covariance matrix (NxN)
	 */
	public final Matrix covar;

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(float[][] samples)
	{
		final int nsamples = samples.length;
		final int ndims = samples[0].length;

		mean = new Matrix(1, ndims);
		covar = new Matrix(ndims, ndims);

		// mean
		for (int j = 0; j < nsamples; j++) {
			for (int i = 0; i < ndims; i++) {
				mean.set(0, i, mean.get(0, i) + samples[j][i]);
			}
		}
		for (int i = 0; i < ndims; i++) {
			mean.set(0, i, mean.get(0, i) / nsamples);
		}

		// covar
		for (int i = 0; i < ndims; i++) {
			for (int j = 0; j < ndims; j++) {
				double qij = 0;

				for (int k = 0; k < nsamples; k++) {
					qij += (samples[k][i] - mean.get(0, i)) * (samples[k][j] - mean.get(0, j));
				}

				covar.set(i, j, qij / (nsamples - 1));
			}
		}
	}

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(double[][] samples)
	{
		final int nsamples = samples.length;
		final int ndims = samples[0].length;

		mean = new Matrix(1, ndims);
		covar = new Matrix(ndims, ndims);

		// mean
		for (int j = 0; j < nsamples; j++) {
			for (int i = 0; i < ndims; i++) {
				mean.set(0, i, mean.get(0, i) + samples[j][i]);
			}
		}
		for (int i = 0; i < ndims; i++) {
			mean.set(0, i, mean.get(0, i) / nsamples);
		}

		// covar
		for (int i = 0; i < ndims; i++) {
			for (int j = 0; j < ndims; j++) {
				double qij = 0;

				for (int k = 0; k < nsamples; k++) {
					qij += (samples[k][i] - mean.get(0, i)) * (samples[k][j] - mean.get(0, j));
				}

				covar.set(i, j, qij / (nsamples - 1));
			}
		}
	}

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(Matrix samples) {
		this(samples.getArray());
	}

	/**
	 * Get the mean vector
	 * 
	 * @return the mean vector
	 */
	public Matrix getMean() {
		return mean;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return the covariance matrix
	 */
	public Matrix getCovariance() {
		return covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(float[][] samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(float[][] samples) {
		return new MeanAndCovariance(samples).covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(double[][] samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(double[][] samples) {
		return new MeanAndCovariance(samples).covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(Matrix samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(Matrix samples) {
		return new MeanAndCovariance(samples).covar;
	}
}
