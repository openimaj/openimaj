package org.openimaj.math.statistics.distribution.kernel;

import java.util.Random;

/**
 * A Univariate kernel (i.e. a 1-d window function with an underlying
 * distribution)
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public interface UnivariateKernel {

	/**
	 * Draw a sample from the kernel's underlying distribution
	 * 
	 * @param rng
	 *            the random generator
	 * 
	 * @return a sample
	 */
	double sample(Random rng);

	/**
	 * Get the absolute value at which the kernel's response is essentially
	 * zero.
	 * 
	 * @return the value for zero response
	 */
	double getCutOff();

	/**
	 * Evaluate the kernel at the given value
	 * 
	 * @param value
	 *            the value
	 * @return the kernel response
	 */
	double evaluate(double value);
}
