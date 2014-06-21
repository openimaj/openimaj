package org.openimaj.algorithm.iterative;

/**
 * The current state of an iterative algorithm.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class IterationState {
	/**
	 * The current iteration number
	 */
	public int iteration = -1;

	/**
	 * The current error or change in error
	 */
	public double epsilon = Double.MAX_VALUE;
}
