package org.openimaj.math.statistics.distribution.metrics;

import java.util.Random;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MultivariateDistribution;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.comparator.DistanceComparator;

import Jama.Matrix;

/**
 * Calculate the KL divergence of two multivariate gaussians.
 * Equation taken from:
 * http://en.wikipedia.org/wiki/Multivariate_normal_distribution#Kullback.E2.80.93Leibler_divergence
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class GaussianKLDivergence implements DistanceComparator<MultivariateGaussian>{
	
	
	

	@Override
	public boolean isDistance() {
		return true;
	}

	@Override
	public double compare(MultivariateGaussian o1, MultivariateGaussian o2) {
		Matrix sig0 = o1.getCovariance();
		Matrix sig1 = o2.getCovariance();
		Matrix mu0 = o1.getMean();
		Matrix mu1 = o2.getMean();
		int K = o1.numDims();
		
		Matrix sig1inv = sig1.inverse();
		double sigtrace = MatrixUtils.trace(sig1inv.times(sig0));
		
		Matrix mudiff = mu1.minus(mu0);
		double xt_s_x = mudiff.transpose().times(sig1inv).times(mudiff).get(0, 0);
		double ln_norm_sig = Math.log(sig0.norm1()/sig1.norm1());
		
		return 0.5 * (sigtrace + xt_s_x - K - ln_norm_sig);
	}

}
