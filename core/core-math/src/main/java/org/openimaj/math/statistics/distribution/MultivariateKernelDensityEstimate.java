package org.openimaj.math.statistics.distribution;

import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openimaj.math.statistics.distribution.kernel.UnivariateKernel;
import org.openimaj.util.pair.ObjectDoublePair;
import org.openimaj.util.tree.DoubleKDTree;

/**
 * A Parzen window kernel density estimate using a univariate kernel and
 * Euclidean distance. Uses a KD-Tree to for efficient neighbour search.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MultivariateKernelDensityEstimate extends AbstractMultivariateDistribution {
	double[][] data;
	UnivariateKernel kernel;
	private double bandwidth;
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
	public MultivariateKernelDensityEstimate(List<double[]> data, UnivariateKernel kernel, double bandwidth)
	{
		this.data = data.toArray(new double[data.size()][]);
		this.tree = new DoubleKDTree(this.data);
		this.kernel = kernel;
		this.bandwidth = bandwidth;
	}

	@Override
	public double[] sample(Random rng) {
		final double[] pt = data[rng.nextInt(data.length)].clone();

		for (int i = 0; i < pt.length; i++) {
			pt[i] = pt[i] + kernel.sample(rng) * this.getBandwidth();
		}

		return pt;
	}

	@Override
	public double estimateProbability(double[] sample) {
		final double[] prob = new double[1];
		final int[] count = new int[1];

		tree.coordinateRadiusSearch(sample, kernel.getCutOff() * getBandwidth(), new TObjectDoubleProcedure<double[]>() {
			@Override
			public boolean execute(double[] point, double distance) {
				prob[0] += kernel.evaluate(Math.sqrt(distance) / getBandwidth());
				count[0]++;

				return true;
			}
		});

		return prob[0] / (getBandwidth() * count[0]);
	}

	/**
	 * Get the underlying points that support the KDE within the window around
	 * the given point. Each point is returned together with its own density
	 * estimate.
	 * 
	 * @param sample
	 *            the point in the centre of the window
	 * @return the points in the window
	 */
	public List<ObjectDoublePair<double[]>> getSupport(double[] sample) {
		final List<ObjectDoublePair<double[]>> support = new ArrayList<ObjectDoublePair<double[]>>();

		tree.coordinateRadiusSearch(sample, kernel.getCutOff() * getBandwidth(), new TObjectDoubleProcedure<double[]>() {
			@Override
			public boolean execute(double[] a, double b) {
				support.add(ObjectDoublePair.pair(a, kernel.evaluate(Math.sqrt(b) / getBandwidth())));

				return true;
			}
		});

		return support;
	}

	/**
	 * Get the underlying data
	 * 
	 * @return the data
	 */
	public double[][] getData() {
		return data;
	}

	/**
	 * Get the bandwidth
	 * 
	 * @return the bandwidth
	 */
	public double getBandwidth() {
		return bandwidth;
	}

	/**
	 * Get the bandwidth scaled by the kernel support.
	 * 
	 * @see UnivariateKernel#getCutOff()
	 * 
	 * @return the scaled bandwidth
	 */
	public double getScaledBandwidth() {
		return bandwidth * this.kernel.getCutOff();
	}
}
