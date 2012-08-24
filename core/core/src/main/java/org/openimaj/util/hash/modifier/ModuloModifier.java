package org.openimaj.util.hash.modifier;

import org.openimaj.util.hash.HashFunction;

/**
 * Modify the underlying hash function by applying the modulus to the value.
 * This has the effect of reducing the range of values the hash function can
 * take.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <O>
 */
public class ModuloModifier<O> extends HashModifier<O> {
	private int range;

	/**
	 * Construct with the given hash function and range. The output hash values
	 * will be between 0 and range.
	 * 
	 * @param hashFunction
	 *            the hash function
	 * @param range
	 *            the range
	 */
	public ModuloModifier(HashFunction<O> hashFunction, int range) {
		super(hashFunction);

		this.range = range;
	}

	@Override
	public int computeHashCode(O object) {
		final long innerHash = hashFunction.computeHashCode(object) & 0x00000000ffffffffL;

		return (int) (innerHash % range);
	}
}
