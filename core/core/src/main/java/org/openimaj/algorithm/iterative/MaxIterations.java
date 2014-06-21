package org.openimaj.algorithm.iterative;

import org.openimaj.util.function.Predicate;

/**
 * Predicate for stopping iteration after a set maximum number of iterations.
 * This predicate tests whether iteration should <b>stop</b>, so only returns
 * <b>true</b> after the maximum number of iterations has been reached.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MaxIterations implements Predicate<IterationState> {
	private int maxIters;

	/**
	 * Construct with the given maximum number of iterations
	 * 
	 * @param maxIters
	 *            maximum number of iterations
	 */
	public MaxIterations(int maxIters) {
		this.maxIters = maxIters;
	}

	@Override
	public boolean test(IterationState is) {
		return is.iteration >= maxIters;
	}
}
