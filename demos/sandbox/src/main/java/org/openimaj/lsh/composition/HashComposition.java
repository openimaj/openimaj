package org.openimaj.lsh.composition;

import org.openimaj.lsh.functions.HashFunction;

public abstract class HashComposition<O> implements HashFunction<O> {
	private HashFunction<O>[] hashFunctions;

	protected HashComposition(HashFunction<O>... functions) {
		this.hashFunctions = functions;
	}
}
