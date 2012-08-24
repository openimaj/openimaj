package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;

import cern.jet.random.engine.MersenneTwister;

/**
 * A hash function for approximating Euclidean distance
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DoubleArrayPStableGaussianFactory extends DoubleArrayHashFunctionFactory {
	private class Function extends DoubleArrayHashFunction {
		double[] r;
		double shift;

		Function(int ndims, MersenneTwister rng) {
			super(rng);

			shift = (float) rng.uniform(0, w);

			// random direction
			r = new double[ndims];
			for (int i = 0; i < ndims; i++) {
				r[i] = rng.gaussian();
			}
		}

		@Override
		public int computeHashCode(double[] point) {
			double val = 0;
			for (int i = 0; i < point.length; i++) {
				val += point[i] * r[i];
			}

			val = (val + shift) / w;

			return (int) Math.floor(val);
		}
	}

	double w;

	public DoubleArrayPStableGaussianFactory(int ndims, MersenneTwister rng, double w) {
		super(ndims, rng);

		this.w = w;
	}

	@Override
	public Function create() {
		return new Function(ndims, rng);
	}

	@Override
	protected DoubleFVComparison fvDistanceFunction() {
		return DoubleFVComparison.EUCLIDEAN;
	}
}
