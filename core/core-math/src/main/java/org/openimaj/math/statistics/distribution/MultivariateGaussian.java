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
}
