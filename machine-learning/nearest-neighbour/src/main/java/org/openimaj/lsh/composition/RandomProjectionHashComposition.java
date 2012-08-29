package org.openimaj.lsh.composition;

import java.util.List;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.composition.HashComposition;

import cern.jet.random.engine.MersenneTwister;

/**
 * Compose a set of hash functions by computing the dot product of the hashes
 * they produce with a random vector.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Object being hashed
 */
public class RandomProjectionHashComposition<OBJECT> extends HashComposition<OBJECT> {
	int[] projection;

	/**
	 * Construct with the given functions.
	 * 
	 * @param rng
	 *            the random number generator
	 * @param functions
	 *            the underlying hash functions.
	 */
	public RandomProjectionHashComposition(MersenneTwister rng, List<HashFunction<OBJECT>> functions) {
		super(functions);
		createProjection(rng);
	}

	/**
	 * Construct with the given functions.
	 * 
	 * @param rng
	 *            the random number generator
	 * @param first
	 *            the first function
	 * @param remainder
	 *            the remainder of the functions
	 */
	public RandomProjectionHashComposition(MersenneTwister rng, HashFunction<OBJECT> first,
			HashFunction<OBJECT>... remainder)
	{
		super(first, remainder);
		createProjection(rng);
	}

	/**
	 * Construct with the factory which is used to produce the required number
	 * of functions.
	 * 
	 * @param rng
	 *            the random number generator
	 * @param factory
	 *            the factory to use to produce the underlying hash functions.
	 * @param nFuncs
	 *            the number of functions to create for the composition
	 */
	public RandomProjectionHashComposition(MersenneTwister rng, HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
		createProjection(rng);
	}

	private void createProjection(MersenneTwister rng) {
		projection = new int[hashFunctions.size()];
		for (int i = 0; i < hashFunctions.size(); i++)
			projection[i] = rng.nextInt();
	}

	@Override
	public int computeHashCode(OBJECT object) {
		int hash = 0;

		for (int i = 0; i < projection.length; i++) {
			hash += projection[i] * hashFunctions.get(i).computeHashCode(object);
		}

		return hash;
	}
}
