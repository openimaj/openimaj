package org.openimaj.math.statistics.distribution;

import java.util.Random;

/**
 * A continuous multivariate distribution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public interface MultivariateDistribution {
	/**
	 * Sample the distribution.
	 * 
	 * @param count
	 *            the number of samples to draw
	 * @param rng
	 *            the random number generator
	 * @return a list of sample vectors from this distribution
	 */
	public double[][] sample(int count, Random rng);

	/**
	 * Sample the distribution.
	 * 
	 * the number of samples to draw
	 * 
	 * @param rng
	 *            the random number generator
	 * @return a list of sample vectors from this distribution
	 */
	public double[] sample(Random rng);

	/**
	 * Get the probability for a given point in space relative to the PDF
	 * represented by this distribution.
	 * 
	 * @param sample
	 *            the point
	 * @return the probability
	 */
	public double estimateProbability(double[] sample);

	/**
	 * Get the log probability for a given point in space relative to the PDF
	 * represented by this distribution.
	 * 
	 * @param sample
	 *            the point
	 * @return the log-probability
	 */
	public double estimateLogProbability(double[] sample);
}
