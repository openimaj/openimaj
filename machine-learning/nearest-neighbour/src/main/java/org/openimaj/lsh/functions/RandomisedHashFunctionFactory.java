package org.openimaj.lsh.functions;

import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.hash.HashFunctionFactory;

import cern.jet.random.engine.MersenneTwister;

/**
 * A factory for producing {@link RandomisedHashFunction}s.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            type of object being hashed
 */
public abstract class RandomisedHashFunctionFactory<OBJECT>
		implements
			HashFunctionFactory<OBJECT>
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
	 * @return a distance comparator that produces distances of the type
	 *         approximated by this hashing scheme
	 */
	public abstract DistanceComparator<OBJECT> distanceFunction();
}
