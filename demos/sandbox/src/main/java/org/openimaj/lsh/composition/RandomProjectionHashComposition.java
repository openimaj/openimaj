package org.openimaj.lsh.composition;

import java.util.List;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.composition.HashComposition;

import cern.jet.random.engine.MersenneTwister;

public abstract class RandomProjectionHashComposition<O> extends HashComposition<O> {
	int[] projection;

	public RandomProjectionHashComposition(MersenneTwister rng, List<HashFunction<O>> functions) {
		super(functions);

		projection = new int[functions.size()];
		for (int i = 0; i < functions.size(); i++)
			projection[i] = rng.nextInt();
	}

}
