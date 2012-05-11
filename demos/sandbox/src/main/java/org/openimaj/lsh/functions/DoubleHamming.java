package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;

import cern.jet.random.engine.MersenneTwister;

public class DoubleHamming extends HashFunctionFactory<DoubleHashFunction> {
	private class Function extends DoubleHashFunction {
		private int ham;

		Function(DoubleHamming options, int ndims, MersenneTwister rng) {
			super(rng);

			if (options.bitsPerDim == 0)
				ham = (int) random.uniform(0, ndims);
			else
				ham = (int) random.uniform(0, ndims * options.bitsPerDim);
		}

		@Override
		public int computeHashCode(double[] point, double norm) {
			//which hash function
			if (bitsPerDim == 0) {
				return point[ham]==0 ? 0 : 1;
			} else {
				// compact binary data
				int m = ham % bitsPerDim;
				int d = ham / bitsPerDim;         
				return (int) ( Double.doubleToRawLongBits(point[d]) >>> m & 1L );
			}
		}
	}
	
	int bitsPerDim;
	
	@Override
	public Function create(int ndims, MersenneTwister rng) {
		return new Function(this, ndims, rng);
	}

	@Override
	public DoubleFVComparison defaultDistanceFunction() {
		if (bitsPerDim == 0)
			return DoubleFVComparison.HAMMING;
		return 
			DoubleFVComparison.PACKED_HAMMING;
	}
}
