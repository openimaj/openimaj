package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;

import cern.jet.random.engine.MersenneTwister;

/**
 * A hash function for approximating L1 distance in closed spaces using random
 * hyperplanes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DoubleArrayHyperplaneL1Factory extends DoubleArrayHashFunctionFactory {
	private class Function extends DoubleArrayHashFunction {
		int dimension;
		double shift;
		double range;

		Function(int ndims, MersenneTwister rng) {
			super(rng);

			// choose a random dimension
			dimension = (int) rng.uniform(0, ndims);

			// random shift
			shift = (float) rng.uniform(min, max);

			range = (max - min);
		}

		@Override
		public int computeHashCode(double[] point) {
			return (int) Math.floor(((float) point[dimension] - shift) / range);
		}
	}

	double min = 0;
	double max = 1;

	public DoubleArrayHyperplaneL1Factory(int ndims, MersenneTwister rng, int min, int max) {
		super(ndims, rng);

		this.min = min;
		this.max = max;
	}

	@Override
	public Function create() {
		return new Function(ndims, rng);
	}

	@Override
	protected DoubleFVComparison fvDistanceFunction() {
		return DoubleFVComparison.CITY_BLOCK;
	}
}
