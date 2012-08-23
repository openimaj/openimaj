package org.openimaj.lsh.functions;

import org.openimaj.util.comparator.DistanceComparator;

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
public abstract class RandomisedHashFunctionFactory<T extends RandomisedHashFunction<Q>, Q> {
	/**
	 * Construct a new {@link RandomisedHashFunction} using the given random generator.
	 * 
	 * @param ndims
	 *            number of dimensions of data
	 * @param rng
	 *            the random number generator
	 * @return the new {@link RandomisedHashFunction}
	 */
	public abstract T create(int ndims, MersenneTwister rng);

	/**
	 * @return a distance comparator that produces distances of the type
	 *         approximated by this hashing scheme
	 */
	public abstract DistanceComparator<Q> distanceFunction();
}
