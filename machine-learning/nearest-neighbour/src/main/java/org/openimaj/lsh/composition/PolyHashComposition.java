package org.openimaj.lsh.composition;

import java.util.List;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.composition.HashComposition;

/**
 * {@link HashComposition} that uses a polynomial function to combine the
 * individual hashes. Based on the composition in the <a
 * href="https://code.google.com/p/caltech-image-search/">Caltech Large Scale
 * Image Search Toolbox</a>.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Object being hashed
 */
public class PolyHashComposition<OBJECT> extends HashComposition<OBJECT> {
	private static final int HASH_POLY = 1368547;
	private static final int HASH_POLY_REM = 573440;
	private static final int HASH_POLY_A[] =
		{ 1342, 876454, 656565, 223, 337, 9847, 87676, 34234, 23445, 76543, 8676234, 3497, 9876, 87856, 2342858 };

	/**
	 * Construct with the given functions.
	 * 
	 * @param functions
	 *            the underlying hash functions.
	 */
	public PolyHashComposition(List<HashFunction<OBJECT>> functions) {
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
	public PolyHashComposition(HashFunction<OBJECT> first, HashFunction<OBJECT>... remainder) {
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
	public PolyHashComposition(HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
	}

	private final int addId(int id, int val, int pos) {
		return (val * HASH_POLY_A[pos % HASH_POLY_A.length] % HASH_POLY) + (id * HASH_POLY_REM % HASH_POLY);
	}

	@Override
	public int computeHashCode(OBJECT object) {
		if (hashFunctions == null || hashFunctions.size() == 0)
			return 0;

		int id = hashFunctions.get(0).computeHashCode(object);
		for (int i = 1, s = hashFunctions.size(); i < s; i++) {
			final int val = hashFunctions.get(i).computeHashCode(object);

			id = addId(id, val, i);
		}
		return id;
	}
}
