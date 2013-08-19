package org.openimaj.math.statistics.distribution.kernel;

import java.util.Random;

/**
 * Standard univariate (1-d) kernel (window) implementations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public enum StandardUnivariateKernels implements UnivariateKernel {
	/**
	 * Univariate Gaussian kernel
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	Gaussian {
		@Override
		public double sample(Random rng) {
			return rng.nextGaussian();
		}

		@Override
		public double getCutOff() {
			// 99.7% of all the data lies within 3 s.d. of the mean
			return 3;
		}

		@Override
		public double evaluate(double value) {
			return Math.exp(-(value * value) / 2) / Math.sqrt(2 * Math.PI);
		}
	},
	/**
	 * Flat window
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	Flat {
		@Override
		public double sample(Random rng) {
			return rng.nextGaussian() - 0.5;
		}

		@Override
		public double getCutOff() {
			return 0.5;
		}

		@Override
		public double evaluate(double value) {
			if (value > 0.5)
				return 0;
			if (value < -0.5)
				return 0;
			return 1;
		}

	}
}
