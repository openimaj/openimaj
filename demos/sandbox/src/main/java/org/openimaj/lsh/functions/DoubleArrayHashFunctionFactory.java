package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.util.comparator.DistanceComparator;

import cern.jet.random.engine.MersenneTwister;

/**
 * Base {@link RandomisedHashFunction} for hashing double arrays.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class DoubleArrayHashFunctionFactory
		extends
			RandomisedHashFunctionFactory<DoubleArrayHashFunction, double[]>
{
	protected DoubleArrayHashFunctionFactory(int ndims, MersenneTwister rng) {
		super(ndims, rng);
	}

	protected abstract DoubleFVComparison fvDistanceFunction();

	@Override
	public final DistanceComparator<double[]> distanceFunction() {
		final DoubleFVComparison dst = fvDistanceFunction();

		return new DistanceComparator<double[]>() {
			@Override
			public double compare(double[] o1, double[] o2) {
				return dst.compare(o1, o2);
			}

			@Override
			public boolean isDistance() {
				return dst.isDistance();
			}
		};
	}
}
