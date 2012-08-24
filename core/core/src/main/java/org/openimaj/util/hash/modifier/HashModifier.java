package org.openimaj.util.hash.modifier;

import org.openimaj.util.hash.HashFunction;

/**
 * A hash function that modifies the hash code produced by another hash
 * function. A common use case would be to bound the range of the function to a
 * smaller range.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Object being hashed
 */
public abstract class HashModifier<OBJECT> implements HashFunction<OBJECT> {
	protected HashFunction<OBJECT> hashFunction;

	protected HashModifier(HashFunction<OBJECT> hashFunction) {
		this.hashFunction = hashFunction;
	}
}
