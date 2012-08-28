package org.openimaj.lsh.sketch;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.sketch.Sketcher;

/**
 * A {@link Sketcher} that produces bit-string sketches encoded as a
 * {@link BitSet}. Only the least-significant bit of each hash function will be
 * appended to the final sketch. The length of the output array will be computed
 * such that the bit from each hash function is contained.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being sketched
 */
public class BitSetLSHSketcher<OBJECT> implements Sketcher<OBJECT, BitSet> {
	List<HashFunction<OBJECT>> hashFunctions;

	/**
	 * Construct with the given functions.
	 * 
	 * @param functions
	 *            the underlying hash functions.
	 */
	public BitSetLSHSketcher(List<HashFunction<OBJECT>> functions) {
		this.hashFunctions = functions;
	}

	/**
	 * Construct with the given functions.
	 * 
	 * @param first
	 *            the first function
	 * @param remainder
	 *            the remainder of the functions
	 */
	public BitSetLSHSketcher(HashFunction<OBJECT> first, HashFunction<OBJECT>... remainder) {
		this.hashFunctions = new ArrayList<HashFunction<OBJECT>>();
		this.hashFunctions.add(first);

		for (final HashFunction<OBJECT> r : remainder)
			this.hashFunctions.add(r);
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
	public BitSetLSHSketcher(HashFunctionFactory<OBJECT> factory, int nFuncs) {
		this.hashFunctions = new ArrayList<HashFunction<OBJECT>>();

		for (int i = 0; i < nFuncs; i++)
			hashFunctions.add(factory.create());
	}

	@Override
	public BitSet createSketch(OBJECT input) {
		final int nbits = bitLength();
		final BitSet sketch = new BitSet(nbits);

		for (int k = 0; k < nbits; k++) {
			final int hash = hashFunctions.get(k).computeHashCode(input);

			sketch.set(k, ((hash & 1) == 1) ? true : false);
		}

		return sketch;
	}

	/**
	 * Get the length of the sketch in bits.
	 * 
	 * @return the number of bits in the sketch
	 */
	public int bitLength() {
		return hashFunctions.size();
	}
}
