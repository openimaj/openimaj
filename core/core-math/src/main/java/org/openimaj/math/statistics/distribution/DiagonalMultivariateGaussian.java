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

import java.util.Arrays;
import java.util.Random;

import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;
import cern.jet.random.Normal;
import cern.jet.random.engine.MersenneTwister;

/**
 * Implementation of a {@link MultivariateGaussian} with a diagonal covariance
 * matrix.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class DiagonalMultivariateGaussian extends AbstractMultivariateGaussian {
	/**
	 * The diagonal of the covariance matrix
	 */
	public double[] variance;

	/**
	 * Construct the Gaussian with the provided center and covariance
	 * 
	 * @param mean
	 *            centre of the Gaussian
	 * @param variance
	 *            variance of the Gaussian
	 */
	public DiagonalMultivariateGaussian(Matrix mean, double[] variance) {
		this.mean = mean;
		this.variance = variance;
	}

	/**
	 * Construct the Gaussian with the zero mean and unit variance
	 * 
	 * @param ndims
	 *            number of dimensions
	 */
	public DiagonalMultivariateGaussian(int ndims) {
		this.mean = new Matrix(1, ndims);
		this.variance = new double[ndims];
		Arrays.fill(variance, 1);
	}

	@Override
	public Matrix getCovariance() {
		return MatrixUtils.diag(variance);
	}

	@Override
	public double getCovariance(int row, int col) {
		if (row < 0 || row >= variance.length || col < 0 || col > variance.length)
			throw new IndexOutOfBoundsException();

		if (row == col)
			return variance[row];
		return 0;
	}

	@Override
	public double estimateProbability(double[] sample) {
		final int N = this.variance.length;
		final double[] meanvector = mean.getArray()[0];

		double det = variance[0];
		for (int i = 1; i < N; i++)
			det *= variance[i];
		final double pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * det));

		double v = 0;
		for (int i = 0; i < N; i++) {
			final double diff = sample[i] - meanvector[i];
			v += diff * diff / variance[i];
		}

		return pdf_const_factor * Math.exp(-0.5 * v);
	}

	@Override
	public double estimateLogProbability(double[] sample) {
		final int N = this.variance.length;
		final double[] meanvector = mean.getArray()[0];

		double det = variance[0];
		for (int i = 1; i < N; i++)
			det *= variance[i];
		final double pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * det));

		double v = 0;
		for (int i = 0; i < N; i++) {
			final double diff = sample[i] - meanvector[i];
			v += diff * diff / variance[i];
		}

		return Math.log(pdf_const_factor) + (-0.5 * v);
	}

	@Override
	public double[] estimateLogProbability(double[][] samples) {
		final int N = this.variance.length;
		final double[] meanvector = mean.getArray()[0];

		double det = variance[0];
		for (int i = 1; i < N; i++)
			det *= variance[i];
		final double pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * det));

		final double[] lp = new double[samples.length];
		for (int j = 0; j < samples.length; j++) {
			double v = 0;
			for (int i = 0; i < N; i++) {
				final double diff = samples[j][i] - meanvector[i];
				v += diff * diff / variance[i];
			}
			lp[j] = Math.log(pdf_const_factor) + (-0.5 * v);
		}

		return lp;
	}

	@Override
	public double[][] sample(int nsamples, Random rng) {
		if (nsamples == 0)
			return new double[0][0];

		final Normal rng2 = new Normal(0, 1, new MersenneTwister());

		final int N = mean.getColumnDimension();
		final double[][] out = new double[nsamples][N];

		final double[] meanv = mean.getArray()[0];
		for (int i = 0; i < N; i++) {
			final double choli = Math.sqrt(this.variance[i]);

			for (int j = 0; j < nsamples; j++) {
				out[j][i] = choli * rng2.nextDouble() + meanv[i];
			}
		}

		return out;
	}
}
