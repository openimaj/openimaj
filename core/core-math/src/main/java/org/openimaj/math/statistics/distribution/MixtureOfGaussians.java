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
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.CholeskyDecomposition;
import Jama.Matrix;

/**
 * Implementation of a mixture of gaussians (gaussian mixture model). This class
 * only models the distribution itself, and does not contain the necessary code
 * to learn a mixture (for that see the <code>GaussianMixtureModelEM</code>
 * class for example).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MixtureOfGaussians extends AbstractMultivariateDistribution {
	/**
	 * Amount to add to the diagonal of the covariance matrix if it's
	 * ill-conditioned.
	 */
	public static final double MIN_COVAR_RECONDITION = 1.0e-7;

	/**
	 * The individual gaussians
	 */
	public MultivariateGaussian[] gaussians;

	/**
	 * The weight of each gaussian
	 */
	public double[] weights;

	/**
	 * Construct the mixture with the given gaussians and weights
	 * 
	 * @param gaussians
	 *            the gaussians
	 * @param weights
	 *            the weights
	 */
	public MixtureOfGaussians(MultivariateGaussian[] gaussians, double[] weights) {
		this.gaussians = gaussians;
		this.weights = weights;
	}

	@Override
	public double[] sample(Random rng) {
		return sample(1, rng)[0];
	}

	@Override
	public double[][] sample(int n_samples, Random rng) {
		final double[] weight_cdf = ArrayUtils.cumulativeSum(this.weights);
		final double[][] X = new double[n_samples][this.gaussians[0].getMean().getColumnDimension()];

		// decide which component to use for each sample
		final int[] comps = new int[n_samples];
		for (int i = 0; i < n_samples; i++) {
			comps[i] = Arrays.binarySearch(weight_cdf, rng.nextDouble());
			if (comps[i] < 0)
				comps[i] = 0;
			if (comps[i] >= gaussians.length)
				comps[i] = gaussians.length - 1;
		}

		// generate samples for each component... doing it this way is more
		// efficient as it minimises the number of times you'd need to do the
		// cholesky decomposition
		for (int i = 0; i < gaussians.length; i++) {
			final int[] idxs = ArrayUtils.search(comps, i);

			if (idxs.length == 0)
				continue;

			// generate the sample
			final double[][] samples = gaussians[i].sample(idxs.length, rng);

			for (int j = 0; j < samples.length; j++) {
				X[idxs[j]] = samples[j];
			}
		}

		return X;
	}

	@Override
	public double estimateLogProbability(double[] sample) {
		return estimateLogProbability(new double[][] { sample })[0];
	}

	/**
	 * Get the probability for a given points in space relative to the PDF
	 * represented by the gaussian mixture.
	 * 
	 * @param samples
	 *            the points
	 * @return the probability
	 */
	@Override
	public double[] estimateLogProbability(double[][] samples) {
		if (samples[0].length != this.gaussians[0].getMean().getColumnDimension()) {
			throw new IllegalArgumentException(
					"The number of dimensions of the given data is not compatible with the model");
		}

		final double[][] lpr = computeWeightedLogProb(samples);

		final double[] logprob = new double[samples.length];
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < lpr[0].length; j++) {
				logprob[i] += Math.exp(lpr[i][j]);
			}
			logprob[i] = Math.log(logprob[i]);
		}

		return logprob;
	}

	/**
	 * Compute the log probability of the given data points belonging to each of
	 * the given gaussians
	 * 
	 * @param x
	 *            the points
	 * @param gaussians
	 *            the gaussians
	 * @return the log probability of each point belonging to each gaussian
	 *         distribution
	 */
	public static double[][] logProbability(double[][] x, MultivariateGaussian[] gaussians) {
		final int ndims = x[0].length;
		final int nmix = gaussians.length;
		final int nsamples = x.length;
		final Matrix X = new Matrix(x);

		final double[][] log_prob = new double[nsamples][nmix];
		for (int i = 0; i < nmix; i++) {
			final Matrix mu = gaussians[i].getMean();
			final Matrix cv = gaussians[i].getCovariance();

			final CholeskyDecomposition chol = cv.chol();
			Matrix cv_chol;
			if (chol.isSPD()) {
				cv_chol = chol.getL();
			} else {
				// covar probably doesn't have enough samples, so
				// recondition it
				final Matrix m = cv.plus(Matrix.identity(ndims, ndims).timesEquals(MIN_COVAR_RECONDITION));
				cv_chol = m.chol().getL();
			}

			double cv_log_det = 0;
			final double[][] cv_chol_d = cv_chol.getArray();
			for (int j = 0; j < ndims; j++) {
				cv_log_det += Math.log(cv_chol_d[j][j]);
			}
			cv_log_det *= 2;

			final Matrix cv_sol = cv_chol.solve(
					MatrixUtils.minusRow(X, mu.getArray()[0]).transpose()).transpose();
			for (int k = 0; k < nsamples; k++) {
				double sum = 0;
				for (int j = 0; j < ndims; j++) {
					sum += cv_sol.get(k, j) * cv_sol.get(k, j);
				}

				log_prob[k][i] = -0.5 * (sum + cv_log_det + ndims * Math.log(2 * Math.PI));
			}

		}

		return log_prob;
	}

	protected double[][] computeWeightedLogProb(double[][] samples) {
		final double[][] lpr = logProbability(samples);

		for (int j = 0; j < lpr[0].length; j++) {
			final double logw = Math.log(this.weights[j]);

			for (int i = 0; i < lpr.length; i++) {
				lpr[i][j] += logw;
			}
		}

		return lpr;
	}

	/**
	 * Compute the log probability of the given data points belonging to each of
	 * the gaussians
	 * 
	 * @param x
	 *            the points
	 * @return the log probability of each point belonging to each gaussian
	 *         distribution
	 */
	public double[][] logProbability(double[][] x) {
		final int nmix = gaussians.length;
		final int nsamples = x.length;

		final double[][] log_prob = new double[nsamples][nmix];
		for (int i = 0; i < nmix; i++) {
			final double[] lp = gaussians[i].estimateLogProbability(x);

			for (int j = 0; j < nsamples; j++) {
				log_prob[j][i] = lp[j];
			}
		}

		return log_prob;
		// return logProbability(x, gaussians);
	}

	/**
	 * Predict the log-posterior for the given sample; this is the
	 * log-probability of the sample point belonging to each of the gaussians in
	 * the mixture.
	 * 
	 * @param sample
	 *            the sample
	 * @return the log-probability for each gaussian
	 */
	public double[] predictLogPosterior(double[] sample) {
		return predictLogPosterior(new double[][] { sample })[0];
	}

	/**
	 * Predict the log-posterior for the given samples; this is the
	 * log-probability of each sample point belonging to each of the gaussians
	 * in the mixture.
	 * 
	 * @param samples
	 *            the samples
	 * @return the log-probability for each gaussian
	 */
	public double[][] predictLogPosterior(double[][] samples) {
		if (samples[0].length != this.gaussians[0].getMean().getColumnDimension()) {
			throw new IllegalArgumentException(
					"The number of dimensions of the given data is not compatible with the model");
		}

		final double[][] lpr = computeWeightedLogProb(samples);
		final double[] logprob = logsumexp(lpr);

		final double[][] responsibilities = new double[samples.length][gaussians.length];
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < gaussians.length; j++) {
				responsibilities[i][j] = lpr[i][j] - logprob[i]; // note no exp
																	// as want
																	// log prob
			}
		}

		return responsibilities;
	}

	/**
	 * Compute the posterior distribution of the samples, and the overall log
	 * probability of each sample as belonging to the model.
	 * 
	 * @param samples
	 *            the samples
	 * @return a pair of (log probabilities, log posterior probabilities)
	 */
	public IndependentPair<double[], double[][]> scoreSamples(double[][] samples) {
		if (samples[0].length != this.gaussians[0].getMean().getColumnDimension()) {
			throw new IllegalArgumentException(
					"The number of dimensions of the given data is not compatible with the model");
		}

		final double[][] lpr = computeWeightedLogProb(samples);

		final double[] logprob = logsumexp(lpr);

		final double[][] responsibilities = new double[samples.length][gaussians.length];
		for (int i = 0; i < samples.length; i++) {
			for (int j = 0; j < gaussians.length; j++) {
				responsibilities[i][j] = Math.exp(lpr[i][j] - logprob[i]);
			}
		}

		return IndependentPair.pair(logprob, responsibilities);
	}

	private double[] logsumexp(double[][] data) {
		final double[] lse = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			final double max = ArrayUtils.maxValue(data[i]);

			for (int j = 0; j < data[0].length; j++) {
				lse[i] += Math.exp(data[i][j] - max);
			}
			lse[i] = max + Math.log(lse[i]);
		}
		return lse;
	}

	/**
	 * Get the gaussians that make up the mixture
	 * 
	 * @return the gaussians
	 */
	public MultivariateGaussian[] getGaussians() {
		return gaussians;
	}

	/**
	 * Get the mixture weights for each gaussian.
	 * 
	 * @return the weights
	 */
	public double[] getWeights() {
		return weights;
	}

	@Override
	public double estimateProbability(double[] sample) {
		return Math.exp(estimateLogProbability(sample));
	}

	/**
	 * Predict the class (the index of the most-probable gaussian) to which the
	 * given data point belongs.
	 * 
	 * @param data
	 *            the data point
	 * @return the class index
	 */
	public int predict(double[] data) {
		final double[] posterior = predictLogPosterior(data);
		return ArrayUtils.maxIndex(posterior);
	}
}
