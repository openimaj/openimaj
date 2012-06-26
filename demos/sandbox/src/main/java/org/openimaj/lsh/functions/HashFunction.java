package org.openimaj.lsh.functions;

import cern.jet.random.engine.MersenneTwister;

/**
 * A (usually randomised) hash function
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O> Object being hashed
 * @param <T> The {@link HashFunction} itself
 */
public abstract class HashFunction<O, T extends HashFunction<O, T>> {
	protected MersenneTwister random;

	HashFunction(MersenneTwister rng) {
		this.random = rng;
	}

	/**
	 * Compute the hash code for the point, using the normalisation
	 * in normVal if the options allow it
	 * @param point the point
	 * @param normVal the normalisation value or 0 if not used
	 * @return the hash code
	 */
	public abstract int computeHashCode(O point, double normVal);
}
