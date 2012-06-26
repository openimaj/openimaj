package org.openimaj.lsh.functions;

import cern.jet.random.engine.MersenneTwister;

/**
 * Base {@link HashFunction} for hashing double arrays.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class DoubleHashFunction extends HashFunction<double[], DoubleHashFunction>
{
	/**
	 * Default constructor
	 * @param factory factory to use 
	 * @param rng random generator
	 */
	DoubleHashFunction(MersenneTwister rng) {
		super(rng);
	}
}
