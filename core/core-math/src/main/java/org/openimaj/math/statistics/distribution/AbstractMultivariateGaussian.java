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
		return Math.log(estimateProbability(sample));
	}

	@Override
	public String toString() {
		return String.format("MultivariateGaussian[mean=%s,covar=%s]", MatrixUtils.toMatlabString(mean).trim(),
				MatrixUtils.toMatlabString(getCovariance()));
	}
}
