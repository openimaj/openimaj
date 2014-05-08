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
