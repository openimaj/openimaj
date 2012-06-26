package org.openimaj.math.hash;
/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */

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
	private int salt;

	HashFunction(MersenneTwister rng) {
		this.random = rng;
		this.salt = random.nextInt();
	}
	
	int seed(){
		return salt;
	}

	/**
	 * Compute the hash code for the point, using the normalisation
	 * in normVal if the options allow it
	 * @param data the item to hash
	 * @return the hash code
	 */
	public abstract int computeHashCode(O data);
}

