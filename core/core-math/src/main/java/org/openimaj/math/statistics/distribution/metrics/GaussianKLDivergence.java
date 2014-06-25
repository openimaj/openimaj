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

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.util.comparator.DistanceComparator;

import Jama.Matrix;

/**
 * Calculate the KL divergence of two multivariate gaussians. Equation taken
 * from:
 * http://en.wikipedia.org/wiki/Multivariate_normal_distribution#Kullback.E2
 * .80.93Leibler_divergence
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class GaussianKLDivergence implements DistanceComparator<MultivariateGaussian> {

	@Override
	public boolean isDistance() {
		return true;
	}

	@Override
	public double compare(MultivariateGaussian o1, MultivariateGaussian o2) {
		final Matrix sig0 = o1.getCovariance();
		final Matrix sig1 = o2.getCovariance();
		final Matrix mu0 = o1.getMean();
		final Matrix mu1 = o2.getMean();
		final int K = o1.numDims();

		final Matrix sig1inv = sig1.inverse();
		final double sigtrace = MatrixUtils.trace(sig1inv.times(sig0));

		final Matrix mudiff = mu1.minus(mu0);
		final double xt_s_x = mudiff.transpose().times(sig1inv).times(mudiff).get(0, 0);
		final double ln_norm_sig = Math.log(sig0.norm1() / sig1.norm1());

		return 0.5 * (sigtrace + xt_s_x - K - ln_norm_sig);
	}

}
