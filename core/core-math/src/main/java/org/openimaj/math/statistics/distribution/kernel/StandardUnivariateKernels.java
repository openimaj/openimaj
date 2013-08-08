package org.openimaj.math.statistics.distribution.kernel;

import java.util.Random;

/**
 * Standard kernel implementations
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
			return 13;
		}

		@Override
		public double evaluate(double value) {
			return Math.exp(-(value * value) / 2) / Math.sqrt(2 * Math.PI);
		}
	}
}
