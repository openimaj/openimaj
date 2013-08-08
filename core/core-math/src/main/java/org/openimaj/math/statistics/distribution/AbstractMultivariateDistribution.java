package org.openimaj.math.statistics.distribution;

import java.util.Random;

/**
 * A continuous multivariate distribution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public abstract class AbstractMultivariateDistribution implements MultivariateDistribution {
	@Override
	public double[][] sample(int count, Random rng) {
		final double[][] samples = new double[count][];

		for (int i = 0; i < count; i++)
			samples[i] = sample(rng);

		return samples;
	}

	@Override
	public double estimateLogProbability(double[] sample) {
		return Math.log(estimateProbability(sample));
	}
}
