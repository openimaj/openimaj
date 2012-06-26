package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.util.array.ArrayUtils;

import cern.jet.random.engine.MersenneTwister;

/**
 * A hash function for approximating city-block distance
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DoubleCityBlock extends HashFunctionFactory<DoubleHashFunction> {
	private class Function extends DoubleHashFunction {
		int dimension;
		double shift;
		double range;

		Function(int ndims, MersenneTwister rng) {
			super(rng);

			// choose a random dimension
			dimension = (int) rng.uniform(0, ndims);
			
			//random shift
			shift = (float) rng.uniform(min, max);
			
			range = (max - min);
		}

		@Override
		public int computeHashCode(double[] point, double normVal) {
			if (norm)
				return (int) Math.floor(((double)point[dimension] / normVal - shift + 1.));
			else
				return (int) Math.floor(((float)point[dimension] - shift) / range);
		}
	}

	double min = 0;
	double max = 1;

	@Override
	public Function create(int ndims, MersenneTwister rng) {
		return new Function(ndims, rng);
	}
	
	@Override
	public double computeNorm(double [] vector) {
		return ArrayUtils.sumValues(vector);
	}

	@Override
	public DoubleFVComparison defaultDistanceFunction() {
		return DoubleFVComparison.CITY_BLOCK;
	}
}
