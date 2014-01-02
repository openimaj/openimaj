package org.openimaj.math.statistics.distribution;

import Jama.Matrix;

/**
 * Interface describing a multivariate gaussian distribution
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public interface MultivariateGaussian extends MultivariateDistribution {

	/**
	 * Get the covariance
	 * 
	 * @return the covariance
	 */
	public abstract Matrix getCovariance();

	/**
	 * Get the mean
	 * 
	 * @return the mean
	 */
	public abstract Matrix getMean();

	/**
	 * Get the dimensionality
	 * 
	 * @return number of dimensions
	 */
	public abstract int numDims();

	/**
	 * Get a covariance value from the covariance matrix.
	 * <p>
	 * This method is provided for efficiency as not all implementations will
	 * store the full matrix, and it would be wasteful to create it each time a
	 * value is needed.
	 * 
	 * @param row
	 *            the row of the matrix value to get
	 * @param column
	 *            the column of the matrix value to get
	 * @return the covariance at the given row and column
	 */
	public abstract double getCovariance(int row, int column);
}
