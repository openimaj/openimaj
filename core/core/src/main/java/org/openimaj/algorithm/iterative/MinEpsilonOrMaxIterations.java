package org.openimaj.algorithm.iterative;

import org.openimaj.util.function.predicates.Or;

/**
 * Convenience class that or's together the {@link MinEpsilon} and
 * {@link MaxIterations} to produce a predicate that stops (returns
 * true) as soon as either the minimum error is reached or the maximum number of
 * iterations is exceeded.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MinEpsilonOrMaxIterations extends Or<IterationState> {
	/**
	 * Construct with the given minimum epsilon and maximum number of
	 * iterations. Iterations will stop at whichever is reached first.
	 * 
	 * @param epsilon
	 *            the epsilon value below which iteration should stop
	 * @param maxIter
	 *            maximum number of iterations
	 */
	public MinEpsilonOrMaxIterations(double epsilon, int maxIter) {
		super(new MinEpsilon(epsilon), new MaxIterations(maxIter));
	}
}
