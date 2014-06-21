package org.openimaj.algorithm.iterative;

import org.openimaj.util.function.Predicate;

/**
 * Predicate for stopping iteration after a given threshold on epsilon is
 * reached. This predicate tests whether iteration should <b>stop</b>, so only
 * returns <b>true</b> after the maximum number of iterations has been reached.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MinEpsilon implements Predicate<IterationState> {
	private double minEps;

	/**
	 * Construct with the given minimum epsilon value is reached
	 * 
	 * @param minEps
	 *            the epsilon value below which iteration should stop
	 */
	public MinEpsilon(double minEps) {
		this.minEps = minEps;
	}

	@Override
	public boolean test(IterationState is) {
		return is.epsilon < minEps;
	}
}
