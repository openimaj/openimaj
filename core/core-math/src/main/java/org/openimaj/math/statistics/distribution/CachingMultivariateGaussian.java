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

import org.openimaj.math.statistics.MeanAndCovariance;

import Jama.Matrix;

/**
 * A single multidimensional Gaussian. This implementation computes the inverse
 * and Cholesky decomposition of the covariance matrix and caches them for
 * efficient sampling and probability computation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class CachingMultivariateGaussian extends AbstractMultivariateGaussian implements MultivariateGaussian {
	protected Matrix covar;
	protected int N;

	private Matrix inv_covar;
	private double pdf_const_factor;
	private Matrix chol;

	protected CachingMultivariateGaussian() {
	}

	/**
	 * Construct the Gaussian with the provided center and covariance
	 * 
	 * @param mean
	 *            centre of the Gaussian
	 * @param covar
	 *            covariance of the Gaussian
	 */
	public CachingMultivariateGaussian(Matrix mean, Matrix covar) {
		N = mean.getColumnDimension();
		this.mean = mean;
		this.covar = covar;
		cacheValues();
	}

	/**
	 * Construct the Gaussian with the zero mean and unit variance
	 * 
	 * @param ndims
	 *            number of dimensions
	 */
	public CachingMultivariateGaussian(int ndims) {
		N = ndims;
		this.mean = new Matrix(1, N);
		this.covar = Matrix.identity(N, N);
		cacheValues();
	}

	protected void cacheValues() {
		inv_covar = covar.inverse();
		pdf_const_factor = 1.0 / Math.sqrt((Math.pow((2 * Math.PI), N) * covar.det()));

		chol = covar.chol().getL();
	}

	/**
	 * Estimate a multidimensional Gaussian from the data
	 * 
	 * @param samples
	 *            the data
	 * @return the Gaussian with the best fit to the data
	 */
	public static CachingMultivariateGaussian estimate(float[][] samples) {
		final int ndims = samples[0].length;

		final CachingMultivariateGaussian gauss = new CachingMultivariateGaussian();
		gauss.N = ndims;

		final MeanAndCovariance res = new MeanAndCovariance(samples);
		gauss.mean = res.mean;
		gauss.covar = res.covar;

		gauss.cacheValues();

		return gauss;
	}

	/**
	 * Estimate a multidimensional Gaussian from the data
	 * 
	 * @param samples
	 *            the data
	 * @return the Gaussian with the best fit to the data
	 */
	public static MultivariateGaussian estimate(Matrix samples) {
		return estimate(samples.getArray());
	}

	/**
	 * Estimate a multidimensional Gaussian from the data
	 * 
	 * @param samples
	 *            the data
	 * @return the Gaussian with the best fit to the data
	 */
	public static MultivariateGaussian estimate(double[][] samples) {
		final int ndims = samples[0].length;

		final CachingMultivariateGaussian gauss = new CachingMultivariateGaussian();
		gauss.N = ndims;

		final MeanAndCovariance res = new MeanAndCovariance(samples);
		gauss.mean = res.mean;
		gauss.covar = res.covar;

		gauss.cacheValues();

		return gauss;
	}

	/**
	 * Get the probability for a given point in space relative to the PDF
	 * represented by this Gaussian.
	 * 
	 * @param sample
	 *            the point
	 * @return the probability
	 */
	@Override
	public double estimateProbability(double[] sample) {
		final Matrix xm = new Matrix(1, N);
		for (int i = 0; i < N; i++)
			xm.set(0, i, sample[i] - mean.get(0, i));

		final Matrix xmt = xm.transpose();

		final double v = xm.times(inv_covar.times(xmt)).get(0, 0);

		return pdf_const_factor * Math.exp(-0.5 * v);
	}

	/**
	 * Get the probability for a given point in space relative to the PDF
	 * represented by this Gaussian.
	 * 
	 * @param sample
	 *            the point
	 * @return the probability
	 */
	public double estimateProbability(Float[] sample) {
		final Matrix xm = new Matrix(1, N);
		for (int i = 0; i < N; i++)
			xm.set(0, i, sample[i] - mean.get(0, i));

		final Matrix xmt = xm.transpose();

		final double v = xm.times(inv_covar.times(xmt)).get(0, 0);

		return pdf_const_factor * Math.exp(-0.5 * v);
	}

	@Override
	public double estimateLogProbability(double[] sample) {
		final Matrix xm = new Matrix(1, N);
		for (int i = 0; i < N; i++)
			xm.set(0, i, sample[i] - mean.get(0, i));

		final Matrix xmt = xm.transpose();

		final double v = xm.times(inv_covar.times(xmt)).get(0, 0);

		return Math.log(pdf_const_factor) + (-0.5 * v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.math.statistics.distribution.MultivariateGaussian#getCovariance
	 * ()
	 */
	@Override
	public Matrix getCovariance() {
		return covar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.math.statistics.distribution.MultivariateGaussian#numDims()
	 */
	@Override
	public int numDims() {
		return N;
	}

	@Override
	public double[] sample(Random rng) {
		final Matrix vec = new Matrix(N, 1);

		for (int i = 0; i < N; i++)
			vec.set(i, 0, rng.nextGaussian());

		final Matrix result = this.mean.plus(chol.times(vec).transpose());

		return result.getArray()[0];
	}

	@Override
	public double[][] sample(int count, Random rng) {
		final double[][] samples = new double[count][];

		for (int i = 0; i < count; i++)
			samples[i] = sample(rng);

		return samples;
	}

	@Override
	public double getCovariance(int row, int column) {
		return this.covar.get(row, column);
	}
}
