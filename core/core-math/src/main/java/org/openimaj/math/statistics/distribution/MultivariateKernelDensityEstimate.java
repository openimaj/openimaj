/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

	@Override
	public double[] estimateLogProbability(double[][] x) {
		final double[] lps = new double[x.length];
		for (int i = 0; i < x.length; i++)
			lps[i] = estimateLogProbability(x[i]);
		return lps;
	}
}
