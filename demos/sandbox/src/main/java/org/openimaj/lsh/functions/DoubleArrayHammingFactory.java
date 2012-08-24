package org.openimaj.lsh.functions;

import org.openimaj.feature.DoubleFVComparison;

import cern.jet.random.engine.MersenneTwister;

public class DoubleArrayHammingFactory extends DoubleArrayHashFunctionFactory {
	private class Function extends DoubleArrayHashFunction {
		private int ham;

		Function(DoubleArrayHammingFactory options, int ndims, MersenneTwister rng) {
			super(rng);

			if (options.bitsPerDim == 0)
				ham = (int) random.uniform(0, ndims);
			else
				ham = (int) random.uniform(0, ndims * options.bitsPerDim);
		}

		@Override
		public int computeHashCode(double[] point) {
			// which hash function
			if (bitsPerDim == 0) {
				return point[ham] == 0 ? 0 : 1;
			} else {
				// compact binary data
				final int m = ham % bitsPerDim;
				final int d = ham / bitsPerDim;
				return (int) (Double.doubleToRawLongBits(point[d]) >>> m & 1L);
			}
		}
	}

	int bitsPerDim;

	public DoubleArrayHammingFactory(int ndims, MersenneTwister rng, int bitsPerDim) {
		super(ndims, rng);

		this.bitsPerDim = bitsPerDim;
	}

	@Override
	public Function create() {
		return new Function(this, ndims, rng);
	}

	@Override
	public DoubleFVComparison fvDistanceFunction() {
		if (bitsPerDim == 0)
			return DoubleFVComparison.HAMMING;
		else
			return DoubleFVComparison.PACKED_HAMMING;
	}
}
