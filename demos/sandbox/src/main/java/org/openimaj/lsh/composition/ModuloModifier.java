package org.openimaj.lsh.composition;

import org.openimaj.lsh.functions.HashFunction;

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
