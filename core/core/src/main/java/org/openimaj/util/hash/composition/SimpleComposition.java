package org.openimaj.util.hash.composition;

import java.util.List;

import org.openimaj.util.hash.HashCodeUtil;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;

/**
 * Simple composition function. Computes the composite hash using the same
 * method as {@link HashCodeUtil} for int arrays.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 */
public class SimpleComposition<OBJECT> extends HashComposition<OBJECT> {
	/**
	 * Construct with the given functions.
	 * 
	 * @param functions
	 *            the underlying hash functions.
	 */
	public SimpleComposition(List<HashFunction<OBJECT>> functions) {
		super(functions);
	}

	/**
	 * Construct with the given functions.
	 * 
	 * @param first
	 *            the first function
	 * @param remainder
	 *            the remainder of the functions
	 */
	public SimpleComposition(HashFunction<OBJECT> first, HashFunction<OBJECT>... remainder) {
		super(first, remainder);
	}

	/**
	 * Construct with the factory which is used to produce the required number
	 * of functions.
	 * 
	 * @param factory
	 *            the factory to use to produce the underlying hash functions.
	 * @param nFuncs
	 *            the number of functions to create for the composition
	 */
	public SimpleComposition(HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
	}

	@Override
	public int computeHashCode(OBJECT object) {
		final int result = HashCodeUtil.SEED;

		for (int i = 0; i < hashFunctions.size(); i++)
			HashCodeUtil.hash(result, hashFunctions.get(i).computeHashCode(object));

		return result;
	}
}
