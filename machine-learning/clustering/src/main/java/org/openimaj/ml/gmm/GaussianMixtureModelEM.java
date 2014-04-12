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
package org.openimaj.ml.gmm;

import gnu.trove.list.array.TDoubleArrayList;

import java.util.Arrays;
import java.util.EnumSet;

import org.apache.commons.math.util.MathUtils;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.math.statistics.MeanAndCovariance;
import org.openimaj.math.statistics.distribution.AbstractMultivariateGaussian;
import org.openimaj.math.statistics.distribution.DiagonalMultivariateGaussian;
import org.openimaj.math.statistics.distribution.FullMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MixtureOfGaussians;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.math.statistics.distribution.SphericalMultivariateGaussian;
import org.openimaj.ml.clustering.DoubleCentroidsResult;
import org.openimaj.ml.clustering.kmeans.DoubleKMeans;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Gaussian mixture model learning using the EM algorithm. Supports a range of
 * different shapes Gaussian through different covariance matrix forms. An
 * initialisation step is used to learn the initial means using K-Means,
 * although this can be disabled in the constructor.
 * <p>
 * Implementation was originally inspired by the SciPy's "gmm.py".
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GaussianMixtureModelEM {
	/**
	 * Different forms of covariance matrix supported by the
	 * {@link GaussianMixtureModelEM}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static enum CovarianceType {
		/**
		 * Spherical Gaussians: variance is the same along all axes and zero
		 * across-axes.
		 */
		Spherical {
			@Override
			protected void setCovariances(MultivariateGaussian[] gaussians, Matrix cv)
			{
				double mean = 0;

				for (int i = 0; i < cv.getRowDimension(); i++)
					for (int j = 0; j < cv.getColumnDimension(); j++)
						mean += cv.get(i, j);
				mean /= (cv.getColumnDimension() * cv.getRowDimension());

				for (final MultivariateGaussian mg : gaussians) {
					((SphericalMultivariateGaussian) mg).variance = mean;
				}
			}

			@Override
			protected MultivariateGaussian[] createGaussians(int ngauss, int ndims) {
				final MultivariateGaussian[] arr = new MultivariateGaussian[ngauss];
				for (int i = 0; i < ngauss; i++) {
					arr[i] = new SphericalMultivariateGaussian(ndims);
				}

				return arr;
			}

			@Override
			protected void mstep(EMGMM gmm, GaussianMixtureModelEM learner, Matrix X, Matrix responsibilities,
					Matrix weightedXsum,
					double[] norm)
			{
				final Matrix avgX2uw = responsibilities.transpose().times(X.arrayTimes(X));

				for (int i = 0; i < gmm.gaussians.length; i++) {
					final Matrix weightedXsumi = new Matrix(new double[][] { weightedXsum.getArray()[i] });
					final Matrix avgX2uwi = new Matrix(new double[][] { avgX2uw.getArray()[i] });

					final Matrix avgX2 = avgX2uwi.times(norm[i]);
					final Matrix mu = ((AbstractMultivariateGaussian) gmm.gaussians[i]).mean;
					final Matrix avgMeans2 = MatrixUtils.pow(mu, 2);
					final Matrix avgXmeans = mu.arrayTimes(weightedXsumi).times(norm[i]);
					final Matrix covar = MatrixUtils.plus(avgX2.minus(avgXmeans.times(2)).plus(avgMeans2),
							learner.minCovar);

					((SphericalMultivariateGaussian) gmm.gaussians[i]).variance =
							MatrixUtils.sum(covar) / X.getColumnDimension();
				}
			}
		},
		/**
		 * Gaussians with diagonal covariance matrices.
		 */
		Diagonal {
			@Override
			protected void setCovariances(MultivariateGaussian[] gaussians, Matrix cv)
			{
				for (final MultivariateGaussian mg : gaussians) {
					((DiagonalMultivariateGaussian) mg).variance = MatrixUtils.diagVector(cv);
				}
			}

			@Override
			protected MultivariateGaussian[] createGaussians(int ngauss, int ndims) {
				final MultivariateGaussian[] arr = new MultivariateGaussian[ngauss];
				for (int i = 0; i < ngauss; i++) {
					arr[i] = new DiagonalMultivariateGaussian(ndims);
				}

				return arr;
			}

			@Override
			protected void mstep(EMGMM gmm, GaussianMixtureModelEM learner, Matrix X, Matrix responsibilities,
					Matrix weightedXsum,
					double[] norm)
			{
				final Matrix avgX2uw = responsibilities.transpose().times(X.arrayTimes(X));

				for (int i = 0; i < gmm.gaussians.length; i++) {
					final Matrix weightedXsumi = new Matrix(new double[][] { weightedXsum.getArray()[i] });
					final Matrix avgX2uwi = new Matrix(new double[][] { avgX2uw.getArray()[i] });

					final Matrix avgX2 = avgX2uwi.times(norm[i]);
					final Matrix mu = ((AbstractMultivariateGaussian) gmm.gaussians[i]).mean;
					final Matrix avgMeans2 = MatrixUtils.pow(mu, 2);
					final Matrix avgXmeans = mu.arrayTimes(weightedXsumi).times(norm[i]);

					final Matrix covar = MatrixUtils.plus(avgX2.minus(avgXmeans.times(2)).plus(avgMeans2),
							learner.minCovar);

					((DiagonalMultivariateGaussian) gmm.gaussians[i]).variance = covar.getArray()[0];
				}
			}
		},
		/**
		 * Gaussians with full covariance
		 */
		Full {
			@Override
			protected MultivariateGaussian[] createGaussians(int ngauss, int ndims) {
				final MultivariateGaussian[] arr = new MultivariateGaussian[ngauss];
				for (int i = 0; i < ngauss; i++) {
					arr[i] = new FullMultivariateGaussian(ndims);
				}

				return arr;
			}

			@Override
			protected void setCovariances(MultivariateGaussian[] gaussians, Matrix cv) {
				for (final MultivariateGaussian mg : gaussians) {
					((FullMultivariateGaussian) mg).covar = cv.copy();
				}
			}

			@Override
			protected void mstep(EMGMM gmm, GaussianMixtureModelEM learner, Matrix X, Matrix responsibilities,
					Matrix weightedXsum,
					double[] norm)
			{
				// Eq. 12 from K. Murphy,
				// "Fitting a Conditional Linear Gaussian Distribution"
				final int nfeatures = X.getColumnDimension();
				for (int c = 0; c < learner.nComponents; c++) {
					final Matrix post = responsibilities.getMatrix(0, X.getRowDimension() - 1, c, c).transpose();

					final double factor = 1.0 / (ArrayUtils.sumValues(post.getArray()) + 10 * MathUtils.EPSILON);

					final Matrix pXt = X.transpose();
					for (int i = 0; i < pXt.getRowDimension(); i++)
						for (int j = 0; j < pXt.getColumnDimension(); j++)
							pXt.set(i, j, pXt.get(i, j) * post.get(0, j));

					final Matrix argcv = pXt.times(X).times(factor);
					final Matrix mu = ((FullMultivariateGaussian) gmm.gaussians[c]).mean;

					((FullMultivariateGaussian) gmm.gaussians[c]).covar = argcv.minusEquals(mu.transpose().times(mu))
							.plusEquals(Matrix.identity(nfeatures, nfeatures).times(learner.minCovar));
				}
			}
		},
		/**
		 * Gaussians with a tied covariance matrix; the same covariance matrix
		 * is shared by all the gaussians.
		 */
		Tied {
			// @Override
			// protected double[][] logProbability(double[][] x,
			// MultivariateGaussian[] gaussians)
			// {
			// final int ndim = x[0].length;
			// final int nmix = gaussians.length;
			// final int nsamples = x.length;
			// final Matrix X = new Matrix(x);
			//
			// final double[][] logProb = new double[nsamples][nmix];
			// final Matrix cv = ((FullMultivariateGaussian)
			// gaussians[0]).covar;
			//
			// final CholeskyDecomposition chol = cv.chol();
			// Matrix cvChol;
			// if (chol.isSPD()) {
			// cvChol = chol.getL();
			// } else {
			// // covar probably doesn't have enough samples, so
			// // recondition it
			// final Matrix m = cv.plus(Matrix.identity(ndim, ndim).timesEquals(
			// MixtureOfGaussians.MIN_COVAR_RECONDITION));
			// cvChol = m.chol().getL();
			// }
			//
			// double cvLogDet = 0;
			// final double[][] cvCholD = cvChol.getArray();
			// for (int j = 0; j < ndim; j++) {
			// cvLogDet += Math.log(cvCholD[j][j]);
			// }
			// cvLogDet *= 2;
			//
			// for (int i = 0; i < nmix; i++) {
			// final Matrix mu = ((FullMultivariateGaussian) gaussians[i]).mean;
			// final Matrix cvSol = cvChol.solve(MatrixUtils.minusRow(X,
			// mu.getArray()[0]).transpose())
			// .transpose();
			// for (int k = 0; k < nsamples; k++) {
			// double sum = 0;
			// for (int j = 0; j < ndim; j++) {
			// sum += cvSol.get(k, j) * cvSol.get(k, j);
			// }
			//
			// logProb[k][i] = -0.5 * (sum + cvLogDet + ndim * Math.log(2 *
			// Math.PI));
			// }
			// }
			//
			// return logProb;
			// }

			@Override
			protected void setCovariances(MultivariateGaussian[] gaussians,
					Matrix cv)
			{
				for (final MultivariateGaussian mg : gaussians) {
					((FullMultivariateGaussian) mg).covar = cv;
				}
			}

			@Override
			protected MultivariateGaussian[] createGaussians(int ngauss, int ndims) {
				final MultivariateGaussian[] arr = new MultivariateGaussian[ngauss];
				final Matrix covar = new Matrix(ndims, ndims);

				for (int i = 0; i < ngauss; i++) {
					arr[i] = new FullMultivariateGaussian(new Matrix(1, ndims), covar);
				}

				return arr;
			}

			@Override
			protected void mstep(EMGMM gmm, GaussianMixtureModelEM learner, Matrix X, Matrix responsibilities,
					Matrix weightedXsum, double[] norm)
			{
				// Eq. 15 from K. Murphy, "Fitting a Conditional Linear Gaussian
				final int nfeatures = X.getColumnDimension();

				final Matrix avgX2 = X.transpose().times(X);
				final double[][] mudata = new double[gmm.gaussians.length][];
				for (int i = 0; i < mudata.length; i++)
					mudata[i] = ((FullMultivariateGaussian) gmm.gaussians[i]).mean.getArray()[0];
				final Matrix mu = new Matrix(mudata);

				final Matrix avgMeans2 = mu.transpose().times(weightedXsum);
				final Matrix covar = avgX2.minus(avgMeans2)
						.plus(Matrix.identity(nfeatures, nfeatures).times(learner.minCovar))
						.times(1.0 / X.getRowDimension());

				for (int i = 0; i < learner.nComponents; i++)
					((FullMultivariateGaussian) gmm.gaussians[i]).covar = covar;
			}
		};

		protected abstract MultivariateGaussian[] createGaussians(int ngauss, int ndims);

		protected abstract void setCovariances(MultivariateGaussian[] gaussians, Matrix cv);

		/**
		 * Mode specific maximisation-step. Implementors should use the state to
		 * update the covariance of each of the
		 * {@link GaussianMixtureModelEM#gaussians}.
		 * 
		 * @param gmm
		 *            the mixture model being learned
		 * @param X
		 *            the data
		 * @param responsibilities
		 *            matrix with the same number of rows as X where each col is
		 *            the amount that the data point belongs to each gaussian
		 * @param weightedXsum
		 *            responsibilities.T * X
		 * @param inverseWeights
		 *            1/weights
		 */
		protected abstract void mstep(EMGMM gmm, GaussianMixtureModelEM learner, Matrix X,
				Matrix responsibilities, Matrix weightedXsum, double[] inverseWeights);
	}

	/**
	 * Options for controlling what gets updated during the initialisation
	 * and/or iterations.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static enum UpdateOptions {
		/**
		 * Update the means
		 */
		Means,
		/**
		 * Update the weights
		 */
		Weights,
		/**
		 * Update the covariances
		 */
		Covariances
	}

	private static class EMGMM extends MixtureOfGaussians {
		EMGMM(int nComponents) {
			super(null, null);

			this.weights = new double[nComponents];
			Arrays.fill(this.weights, 1.0 / nComponents);
		}
	}

	private static final double DEFAULT_THRESH = 1e-2;
	private static final double DEFAULT_MIN_COVAR = 1e-3;
	private static final int DEFAULT_NITERS = 100;
	private static final int DEFAULT_NINIT = 1;

	CovarianceType ctype;
	int nComponents;
	private double thresh;
	private double minCovar;
	private int nIters;
	private int nInit;

	private boolean converged = false;
	private EnumSet<UpdateOptions> initOpts;
	private EnumSet<UpdateOptions> iterOpts;

	/**
	 * Construct with the given arguments.
	 * 
	 * @param nComponents
	 *            the number of gaussian components
	 * @param ctype
	 *            the form of the covariance matrices
	 * @param thresh
	 *            the threshold at which to stop iterating
	 * @param minCovar
	 *            the minimum value allowed in the diagonal of the estimated
	 *            covariance matrices to prevent overfitting
	 * @param nIters
	 *            the maximum number of iterations
	 * @param nInit
	 *            the number of runs of the algorithm to perform; the best
	 *            result will be kept.
	 * @param iterOpts
	 *            options controlling what is updated during iteration
	 * @param initOpts
	 *            options controlling what is updated during initialisation.
	 *            Enabling the {@link UpdateOptions#Means} option will cause
	 *            K-Means to be used to generate initial starting points for the
	 *            means.
	 */
	public GaussianMixtureModelEM(int nComponents, CovarianceType ctype, double thresh, double minCovar,
			int nIters, int nInit, EnumSet<UpdateOptions> iterOpts, EnumSet<UpdateOptions> initOpts)
	{
		this.ctype = ctype;
		this.nComponents = nComponents;
		this.thresh = thresh;
		this.minCovar = minCovar;
		this.nIters = nIters;
		this.nInit = nInit;
		this.iterOpts = iterOpts;
		this.initOpts = initOpts;

		if (nInit < 1) {
			throw new IllegalArgumentException("GMM estimation requires at least one run");
		}
		this.converged = false;
	}

	/**
	 * Construct with the given arguments.
	 * 
	 * @param nComponents
	 *            the number of gaussian components
	 * @param ctype
	 *            the form of the covariance matrices
	 */
	public GaussianMixtureModelEM(int nComponents, CovarianceType ctype)
	{
		this(nComponents, ctype, DEFAULT_THRESH, DEFAULT_MIN_COVAR, DEFAULT_NITERS, DEFAULT_NINIT, EnumSet
				.allOf(UpdateOptions.class), EnumSet.allOf(UpdateOptions.class));
	}

	/**
	 * Get's the convergence state of the algorithm. Will return false if
	 * {@link #estimate(double[][])} has not been called, or if the last call to
	 * {@link #estimate(double[][])} failed to reach convergence before running
	 * out of iterations.
	 * 
	 * @return true if the last call to {@link #estimate(double[][])} reached
	 *         convergence; false otherwise
	 */
	public boolean hasConverged() {
		return converged;
	}

	/**
	 * Estimate a new {@link MixtureOfGaussians} from the given data. Use
	 * {@link #hasConverged()} to check whether the EM algorithm reached
	 * convergence in the estimation of the returned model.
	 * 
	 * @param X
	 *            the data matrix.
	 * @return the generated GMM.
	 */
	public MixtureOfGaussians estimate(Matrix X) {
		return estimate(X.getArray());
	}

	/**
	 * Estimate a new {@link MixtureOfGaussians} from the given data. Use
	 * {@link #hasConverged()} to check whether the EM algorithm reached
	 * convergence in the estimation of the returned model.
	 * 
	 * @param X
	 *            the data array.
	 * @return the generated GMM.
	 */
	public MixtureOfGaussians estimate(double[][] X) {
		final EMGMM gmm = new EMGMM(nComponents);

		if (X.length < nComponents)
			throw new IllegalArgumentException(String.format(
					"GMM estimation with %d components, but got only %d samples", nComponents, X.length));

		double max_log_prob = Double.NEGATIVE_INFINITY;

		for (int j = 0; j < nInit; j++) {
			gmm.gaussians = ctype.createGaussians(nComponents, X[0].length);

			if (initOpts.contains(UpdateOptions.Means)) {
				// initialise using k-means
				final DoubleKMeans km = DoubleKMeans.createExact(nComponents);
				final DoubleCentroidsResult means = km.cluster(X);

				for (int i = 0; i < nComponents; i++) {
					((AbstractMultivariateGaussian) gmm.gaussians[i]).mean.getArray()[0] = means.centroids[i];
				}
			}

			if (initOpts.contains(UpdateOptions.Weights)) {
				gmm.weights = new double[nComponents];
				Arrays.fill(gmm.weights, 1.0 / nComponents);
			}

			if (initOpts.contains(UpdateOptions.Covariances)) {
				// cv = np.cov(X.T) + self.min_covar * np.eye(X.shape[1])
				final Matrix cv = MeanAndCovariance.computeCovariance(X);

				ctype.setCovariances(gmm.gaussians, cv);
			}

			// EM algorithm
			final TDoubleArrayList log_likelihood = new TDoubleArrayList();

			// reset converged to false
			converged = false;
			double[] bestWeights = null;
			MultivariateGaussian[] bestMixture = null;
			for (int i = 0; i < nIters; i++) {
				// Expectation step
				final IndependentPair<double[], double[][]> score = gmm.scoreSamples(X);
				final double[] curr_log_likelihood = score.firstObject();
				final double[][] responsibilities = score.secondObject();
				log_likelihood.add(ArrayUtils.sumValues(curr_log_likelihood));

				// Check for convergence.
				if (i > 0 && Math.abs(log_likelihood.get(i) - log_likelihood.get(i - 1)) < thresh) {
					converged = true;
					break;
				}

				// Perform the maximisation step
				mstep(gmm, X, responsibilities);

				// if the results are better, keep it
				if (nIters > 0) {
					if (log_likelihood.getQuick(i) > max_log_prob) {
						max_log_prob = log_likelihood.getQuick(i);
						bestWeights = gmm.weights;
						bestMixture = gmm.gaussians;
					}
				}

				// check the existence of an init param that was not subject to
				// likelihood computation issue.
				if (Double.isInfinite(max_log_prob) && nIters > 0) {
					throw new RuntimeException(
							"EM algorithm was never able to compute a valid likelihood given initial " +
									"parameters. Try different init parameters (or increasing n_init) or " +
									"check for degenerate data.");
				}

				if (nIters > 0) {
					gmm.gaussians = bestMixture;
					gmm.weights = bestWeights;
				}
			}
		}

		return gmm;
	}

	private void mstep(EMGMM gmm, double[][] X, double[][] responsibilities) {
		final double[] weights = ArrayUtils.colSum(responsibilities);
		final Matrix resMat = new Matrix(responsibilities);
		final Matrix Xmat = new Matrix(X);

		final Matrix weighted_X_sum = resMat.transpose().times(Xmat);
		final double[] inverse_weights = new double[weights.length];
		for (int i = 0; i < inverse_weights.length; i++)
			inverse_weights[i] = 1.0 / (weights[i] + 10 * MathUtils.EPSILON);

		if (iterOpts.contains(UpdateOptions.Weights)) {
			final double sum = ArrayUtils.sumValues(weights);
			for (int i = 0; i < weights.length; i++) {
				gmm.weights[i] = (weights[i] / (sum + 10 * MathUtils.EPSILON) + MathUtils.EPSILON);
			}
		}

		if (iterOpts.contains(UpdateOptions.Means)) {
			// self.means_ = weighted_X_sum * inverse_weights
			final double[][] wx = weighted_X_sum.getArray();

			for (int i = 0; i < nComponents; i++) {
				final double[][] m = ((AbstractMultivariateGaussian) gmm.gaussians[i]).mean.getArray();

				for (int j = 0; j < m[0].length; j++) {
					m[0][j] = wx[i][j] * inverse_weights[i];
				}
			}
		}

		if (iterOpts.contains(UpdateOptions.Covariances)) {
			ctype.mstep(gmm, this, Xmat, resMat, weighted_X_sum, inverse_weights);
		}
	}

	@Override
	public GaussianMixtureModelEM clone() {
		try {
			return (GaussianMixtureModelEM) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
