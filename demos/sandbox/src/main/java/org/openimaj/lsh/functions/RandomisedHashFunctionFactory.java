package org.openimaj.lsh.functions;

import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.hash.HashFunctionFactory;

import cern.jet.random.engine.MersenneTwister;

/**
 * A factory for producing {@link RandomisedHashFunction}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            type of {@link RandomisedHashFunction} produced.
 * @param <Q>
 *            type of object being hashed
 */
public abstract class RandomisedHashFunctionFactory<T extends RandomisedHashFunction<Q>, Q>
		implements
			HashFunctionFactory<T, Q>
{
	protected MersenneTwister rng;
	protected int ndims;

	/**
	 * * @param ndims number of dimensions of data
	 * 
	 * @param rng
	 *            the random number generator
	 */
	protected RandomisedHashFunctionFactory(int ndims, MersenneTwister rng) {
		this.rng = rng;
		this.ndims = ndims;
	}

	/**
	 * Construct a new {@link RandomisedHashFunction} using the given random
	 * generator.
	 * 
	 * @return the new {@link RandomisedHashFunction}
	 */
	@Override
	public abstract T create();

	/**
	 * @return a distance comparator that produces distances of the type
	 *         approximated by this hashing scheme
	 */
	public abstract DistanceComparator<Q> distanceFunction();
}
