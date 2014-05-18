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

import java.util.Random;

import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

/**
 * Abstract base class for {@link MultivariateGaussian} implementations
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public abstract class AbstractMultivariateGaussian implements MultivariateGaussian {
	/**
	 * The mean vector
	 */
	public Matrix mean;

	@Override
	public Matrix getMean() {
		return mean;
	}

	@Override
	public double[] sample(Random rng) {
		final int N = mean.getColumnDimension();
		final Matrix chol = getCovariance().chol().getL();
		final Matrix vec = new Matrix(N, 1);

		for (int i = 0; i < N; i++)
			vec.set(i, 0, rng.nextGaussian());

		final Matrix result = this.mean.plus(chol.times(vec).transpose());

		return result.getArray()[0];
	}

	@Override
	public double[][] sample(int nsamples, Random rng) {
		if (nsamples == 0)
			return new double[0][0];

		final int N = mean.getColumnDimension();
		final Matrix chol = getCovariance().chol().getL();
		final Matrix vec = new Matrix(N, nsamples);

		for (int i = 0; i < N; i++)
			for (int j = 0; j < nsamples; j++)
				vec.set(i, j, rng.nextGaussian());

		final Matrix result = chol.times(vec).transpose();
		for (int i = 0; i < result.getRowDimension(); i++)
			for (int j = 0; j < result.getColumnDimension(); j++)
				result.set(i, j, result.get(i, j) + mean.get(0, j));

		return result.getArray();
	}

	@Override
	public int numDims() {
		return mean.getColumnDimension();
	}

	@Override
	public double estimateProbability(double[] sample) {
		final int N = mean.getColumnDimension();
		final Matrix inv_covar = getCovariance().inverse();
		final double pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * getCovariance().det()));

		final Matrix xm = new Matrix(1, N);
		for (int i = 0; i < N; i++)
			xm.set(0, i, sample[i] - mean.get(0, i));

		final Matrix xmt = xm.transpose();
		final double v = xm.times(inv_covar.times(xmt)).get(0, 0);

		return pdf_const_factor * Math.exp(-0.5 * v);
	}

	@Override
	public double estimateLogProbability(double[] sample) {
		final int N = mean.getColumnDimension();
		final Matrix inv_covar = getCovariance().inverse();
		final double cov_det = getCovariance().det();
		final double pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * cov_det));

		final Matrix xm = new Matrix(1, N);
		for (int i = 0; i < N; i++)
			xm.set(0, i, sample[i] - mean.get(0, i));

		final Matrix xmt = xm.transpose();
		final double v = xm.times(inv_covar.times(xmt)).get(0, 0);

		return Math.log(pdf_const_factor) + (-0.5 * v);
	}

	@Override
	public String toString() {
		// only pretty print with low dimensionality
		if (this.numDims() < 5)
			return String.format("MultivariateGaussian[mean=%s,covar=%s]", MatrixUtils.toMatlabString(mean).trim(),
					MatrixUtils.toMatlabString(getCovariance()));
		return super.toString();
	}

	@Override
	public double[] estimateLogProbability(double[][] x) {
		final double[] lps = new double[x.length];
		for (int i = 0; i < x.length; i++)
			lps[i] = estimateLogProbability(x[i]);
		return lps;
	}
}
