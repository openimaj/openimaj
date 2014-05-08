package org.openimaj.math.statistics.distribution.metrics;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.FullMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateDistribution;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.math.statistics.distribution.SphericalMultivariateGaussian;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.comparator.DistanceComparator;

import Jama.Matrix;

/**
 * By sampling a distribution and calculating the log liklihood 
 * of those samples against another distribution, construct a similarity metric.
 * 
 * This function uses {@link MultivariateDistribution#estimateLogProbability(double[][])}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestMultivariateDistanceComparator {
	
	@Test
	public void testSampledMultivariate() throws Exception {
		Matrix mean = new Matrix(1,1);  mean.set(0, 0, 0f);
		float covar = 0.01f;
		MultivariateGaussian gc = new SphericalMultivariateGaussian(mean ,covar);
		MultivariateGaussian gcloser = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 1),covar);
		MultivariateGaussian gfarther = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 2),covar);
		
		SampledMultivariateDistanceComparator comparator = new SampledMultivariateDistanceComparator();
		
		double meanSelfCompare = comparator.compare(gc, gc);
		double meanFartherCompare = comparator.compare(gc, gfarther);
		double meanCloserCompare = comparator.compare(gc, gcloser);
		assertTrue(meanSelfCompare > meanFartherCompare);
		assertTrue(meanSelfCompare > meanCloserCompare);
		assertTrue(meanCloserCompare > meanFartherCompare);
	}
	
	@Test
	public void testKLDivGaussian() throws Exception {
		Matrix mean = new Matrix(1,1);  mean.set(0, 0, 0f);
		float covar = 0.01f;
		MultivariateGaussian gc = new SphericalMultivariateGaussian(mean ,covar);
		MultivariateGaussian gcloser = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 1),covar);
		MultivariateGaussian gfarther = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 2),covar);
		
		DistanceComparator<MultivariateGaussian> comparator = new GaussianKLDivergence();
		
		double meanSelfCompare = comparator.compare(gc, gc);
		double meanFartherCompare = comparator.compare(gc, gfarther);
		double meanCloserCompare = comparator.compare(gc, gcloser);
		assertTrue(meanSelfCompare < meanFartherCompare);
		assertTrue(meanSelfCompare < meanCloserCompare);
		assertTrue(meanCloserCompare < meanFartherCompare);
	}
}

