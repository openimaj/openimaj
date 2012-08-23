package org.openimaj.lsh.composition;

import org.openimaj.lsh.functions.HashFunction;

public abstract class HashModifier<O> implements HashFunction<O> {
	protected HashFunction<O> hashFunction;

	protected HashModifier(HashFunction<O> hashFunction) {
		this.hashFunction = hashFunction;
	}
}
