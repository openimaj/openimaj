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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.distribution.MultivariateDistribution;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.math.statistics.distribution.SphericalMultivariateGaussian;
import org.openimaj.util.comparator.DistanceComparator;

import Jama.Matrix;

/**
 * By sampling a distribution and calculating the log liklihood of those samples
 * against another distribution, construct a similarity metric.
 *
 * This function uses
 * {@link MultivariateDistribution#estimateLogProbability(double[][])}
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestMultivariateDistanceComparator {

	/**
	 * Test sampling multivariate gaussian
	 *
	 * @throws Exception
	 */
	@Test
	public void testSampledMultivariate() throws Exception {
		final Matrix mean = new Matrix(1, 1);
		mean.set(0, 0, 0f);
		final float covar = 0.01f;
		final MultivariateGaussian gc = new SphericalMultivariateGaussian(mean, covar);
		final MultivariateGaussian gcloser = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 1), covar);
		final MultivariateGaussian gfarther = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 2), covar);

		final SampledMultivariateDistanceComparator comparator = new SampledMultivariateDistanceComparator();

		final double meanSelfCompare = comparator.compare(gc, gc);
		final double meanFartherCompare = comparator.compare(gc, gfarther);
		final double meanCloserCompare = comparator.compare(gc, gcloser);
		assertTrue(meanSelfCompare > meanFartherCompare);
		assertTrue(meanSelfCompare > meanCloserCompare);
		assertTrue(meanCloserCompare > meanFartherCompare);
	}

	/**
	 * Test KLDivergenece
	 *
	 * @throws Exception
	 */
	@Test
	public void testKLDivGaussian() throws Exception {
		final Matrix mean = new Matrix(1, 1);
		mean.set(0, 0, 0f);
		final float covar = 0.01f;
		final MultivariateGaussian gc = new SphericalMultivariateGaussian(mean, covar);
		final MultivariateGaussian gcloser = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 1), covar);
		final MultivariateGaussian gfarther = new SphericalMultivariateGaussian(MatrixUtils.plus(mean.copy(), 2), covar);

		final DistanceComparator<MultivariateGaussian> comparator = new GaussianKLDivergence();

		final double meanSelfCompare = comparator.compare(gc, gc);
		final double meanFartherCompare = comparator.compare(gc, gfarther);
		final double meanCloserCompare = comparator.compare(gc, gcloser);
		assertTrue(meanSelfCompare < meanFartherCompare);
		assertTrue(meanSelfCompare < meanCloserCompare);
		assertTrue(meanCloserCompare < meanFartherCompare);
	}
}
