package org.openimaj.lsh.composition;

import org.openimaj.lsh.functions.HashFunction;

import cern.jet.random.engine.MersenneTwister;

public abstract class RandomProjectionHashComposition<O> extends HashComposition<O> {
	int[] projection;

	public RandomProjectionHashComposition(MersenneTwister rng, HashFunction<O>... functions) {
		super(functions);

		projection = new int[functions.length];
		for (int i = 0; i < functions.length; i++)
			projection[i] = rng.nextInt();
	}

}
