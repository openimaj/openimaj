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
package org.openimaj.math.statistics;

import Jama.Matrix;

/**
 * Class to compute the mean and covariance of some given data.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MeanAndCovariance {
	/**
	 * The mean vector (1xN)
	 */
	public final Matrix mean;

	/**
	 * The covariance matrix (NxN)
	 */
	public final Matrix covar;

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(float[][] samples)
	{
		final int nsamples = samples.length;
		final int ndims = samples[0].length;

		mean = new Matrix(1, ndims);
		covar = new Matrix(ndims, ndims);

		// mean
		for (int j = 0; j < nsamples; j++) {
			for (int i = 0; i < ndims; i++) {
				mean.set(0, i, mean.get(0, i) + samples[j][i]);
			}
		}
		for (int i = 0; i < ndims; i++) {
			mean.set(0, i, mean.get(0, i) / nsamples);
		}

		// covar
		for (int i = 0; i < ndims; i++) {
			for (int j = 0; j < ndims; j++) {
				double qij = 0;

				for (int k = 0; k < nsamples; k++) {
					qij += (samples[k][i] - mean.get(0, i)) * (samples[k][j] - mean.get(0, j));
				}

				covar.set(i, j, qij / (nsamples - 1));
			}
		}
	}

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(double[][] samples)
	{
		final int nsamples = samples.length;
		final int ndims = samples[0].length;

		mean = new Matrix(1, ndims);
		covar = new Matrix(ndims, ndims);

		// mean
		for (int j = 0; j < nsamples; j++) {
			for (int i = 0; i < ndims; i++) {
				mean.set(0, i, mean.get(0, i) + samples[j][i]);
			}
		}
		for (int i = 0; i < ndims; i++) {
			mean.set(0, i, mean.get(0, i) / nsamples);
		}

		// covar
		for (int i = 0; i < ndims; i++) {
			for (int j = 0; j < ndims; j++) {
				double qij = 0;

				for (int k = 0; k < nsamples; k++) {
					qij += (samples[k][i] - mean.get(0, i)) * (samples[k][j] - mean.get(0, j));
				}

				covar.set(i, j, qij / (nsamples - 1));
			}
		}
	}

	/**
	 * Construct a new {@link MeanAndCovariance} containing the mean vector and
	 * covariance matrix of the given data (each row is a data point)
	 * 
	 * @param samples
	 *            the data
	 */
	public MeanAndCovariance(Matrix samples) {
		this(samples.getArray());
	}

	/**
	 * Get the mean vector
	 * 
	 * @return the mean vector
	 */
	public Matrix getMean() {
		return mean;
	}

	/**
	 * Get the covariance matrix
	 * 
	 * @return the covariance matrix
	 */
	public Matrix getCovariance() {
		return covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(float[][] samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(float[][] samples) {
		return new MeanAndCovariance(samples).covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(double[][] samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(double[][] samples) {
		return new MeanAndCovariance(samples).covar;
	}

	/**
	 * Get the mean of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the mean
	 */
	public static Matrix computeMean(Matrix samples) {
		return new MeanAndCovariance(samples).mean;
	}

	/**
	 * Get the covariance of the data
	 * 
	 * @param samples
	 *            the data
	 * @return the covariance matrix
	 */
	public static Matrix computeCovariance(Matrix samples) {
		return new MeanAndCovariance(samples).covar;
	}
}
