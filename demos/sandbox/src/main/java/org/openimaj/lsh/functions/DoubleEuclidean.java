package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.util.array.ArrayUtils;

import cern.jet.random.engine.MersenneTwister;

/**
 * A hash function for approximating Euclidean distance
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class DoubleEuclidean extends HashFunctionFactory<DoubleHashFunction> {
	private class Function extends DoubleHashFunction {
		double [] r;
		double shift;

		Function(int ndims, MersenneTwister rng) {
			super(rng);

			shift = (float) rng.uniform(0, w);
			r = new double[ndims];
			
			//random direction
			double norm = 0;
			for (int i=0; i<ndims; i++) {
				r[i] = rng.gaussian();
				norm += r[i] * r[i];
			}

			//normalize
			norm = Math.sqrt(norm);
			for (int i=0; i<ndims; i++) {
				r[i] /= norm;
			}
		}

		@Override
		public int computeHashCode(double[] point, double normVal) {
			double val = 0;
			for (int i=0; i<point.length; i++) {
				val += point[i] * r[i];
			}
			
			if (norm) {
				val = (val / normVal) + 1.0;
			}

			val = (val + shift) / w;
			
			return (int) val;
		}
	}

	double w = .25;
	
	@Override
	public Function create(int ndims, MersenneTwister rng) {
		return new Function(ndims, rng);
	}

	@Override
	public double computeNorm(double [] vector) {
		return Math.sqrt(ArrayUtils.sumValuesSquared(vector));
	}

	@Override
	public DoubleFVComparison defaultDistanceFunction() {
		return DoubleFVComparison.EUCLIDEAN;
	}
}
