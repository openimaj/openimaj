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
package org.openimaj.math.statistics.distribution.metrics;

import java.util.Random;

import org.openimaj.math.statistics.distribution.MultivariateDistribution;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.comparator.DistanceComparator;

/**
 * By sampling a distribution and calculating the log liklihood 
 * of those samples against another distribution, construct a distance metric.
 * 
 * This function uses {@link MultivariateDistribution#estimateLogProbability(double[][])}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SampledMultivariateDistanceComparator implements DistanceComparator<MultivariateDistribution>{
	
	
	private static final int DEFAULT_SAMPLE = 1000;
	private int samples;
	private Random rng;

	/**
	 * 
	 */
	public SampledMultivariateDistanceComparator() {
		this.samples = DEFAULT_SAMPLE;
		this.rng = new Random();
	}
	
	/**
	 * @param nsamples
	 */
	public SampledMultivariateDistanceComparator(int nsamples) {
		this();
		this.samples = nsamples;
	}
	
	/**
	 * @param seed 
	 * @param nsamples
	 */
	public SampledMultivariateDistanceComparator(long seed, int nsamples) {
		this.rng = new Random(seed);
		this.samples = nsamples;
	}
	
	@Override
	public double compare(MultivariateDistribution o1, MultivariateDistribution o2) {
		double[][] X = o1.sample(samples, rng);
		double[] sampleP = o2.estimateLogProbability(X);
		return ArrayUtils.sumValues(sampleP);
	}

	@Override
	public boolean isDistance() {
		return false;
	}

}
