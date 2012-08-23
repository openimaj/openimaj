package org.openimaj.lsh.composition;

import org.openimaj.lsh.functions.HashFunction;

/**
 * {@link HashModifier} that extracts the Least Significant Bit of the
 * underlying hash.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <O>
 *            Type of object being hashed
 */
public class LSBModifier<O> extends ModuloModifier<O> {
	/**
	 * Construct with the given hash function
	 * 
	 * @param hashFunction
	 *            the hash function
	 */
	public LSBModifier(HashFunction<O> hashFunction) {
		super(hashFunction, 2);
	}
}
