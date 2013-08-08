package org.openimaj.math.statistics.distribution;

import java.util.List;
import java.util.Random;

import org.openimaj.math.statistics.distribution.kernel.UnivariateKernel;
import org.openimaj.util.tree.DoubleKDTree;

/**
 * A parzen window kernel density estimate using a univariate kernel and
 * euclidean distance. Uses a KD-Tree to for efficient neighbour search.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MultivariateKernelDensityEstimate extends AbstractMultivariateDistribution {
	double[][] data;
	UnivariateKernel kernel;
	double bandwidth;
	DoubleKDTree tree;

	/**
	 * Construct with the given data, kernel and bandwidth
	 * 
	 * @param data
	 *            the data
	 * @param kernel
	 *            the kernel
	 * @param bandwidth
	 *            the bandwidth
	 */
	public MultivariateKernelDensityEstimate(double[][] data, UnivariateKernel kernel, double bandwidth) {
		this.data = data;
		this.tree = new DoubleKDTree(data);
		this.kernel = kernel;
		this.bandwidth = bandwidth;
	}

	@Override
	public double[] sample(Random rng) {
		final double[] pt = data[rng.nextInt(data.length)].clone();

		for (int i = 0; i < pt.length; i++) {
			pt[i] = pt[i] + kernel.sample(rng) * this.bandwidth;
		}

		return pt;
	}

	@Override
	public double estimateProbability(double[] sample) {
		final List<double[]> neighbours = tree.radiusSearch(sample, kernel.getCutOff());
		double dist = 0;

		double prob = 0;
		for (int i = 0; i < neighbours.size(); i++) {
			final double[] ni = neighbours.get(i);

			for (int j = 0; j < sample.length; j++) {
				final double val = (sample[j] - ni[j]);
				dist += val * val;
			}

			prob += kernel.evaluate(Math.sqrt(dist) / bandwidth);
		}

		return prob / (bandwidth * data.length);
	}
}
