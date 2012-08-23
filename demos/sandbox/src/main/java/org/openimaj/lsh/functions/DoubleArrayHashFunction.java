package org.openimaj.lsh.functions;

import cern.jet.random.engine.MersenneTwister;

/**
 * Base {@link RandomisedHashFunction} for hashing double arrays.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class DoubleArrayHashFunction extends RandomisedHashFunction<double[]> {
	/**
	 * Default constructor
	 * 
	 * @param factory
	 *            factory to use
	 * @param rng
	 *            random generator
	 */
	DoubleArrayHashFunction(MersenneTwister rng) {
		super(rng);
	}
}
