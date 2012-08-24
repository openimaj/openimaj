package org.openimaj.lsh.functions;

import org.openimaj.util.hash.HashFunction;

import cern.jet.random.engine.MersenneTwister;

/**
 * A randomised hash function
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <O>
 *            Object being hashed
 */
public abstract class RandomisedHashFunction<O> implements HashFunction<O> {
	protected MersenneTwister random;

	RandomisedHashFunction(MersenneTwister rng) {
		this.random = rng;
	}
}
