package org.openimaj.math.matrix.algorithm.ica;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public abstract class IndependentComponentAnalysis {
	public abstract Matrix getSignalToInterferenceMatrix();

	public abstract Matrix getDemixingMatrix();

	public abstract Matrix getIndependentComponentMatrix();

	/**
	 * Estimate the independent components of the given data array. Each row
	 * corresponds to an observation with the number of dimensions equal to the
	 * number of columns.
	 * 
	 * @param data
	 *            the data
	 */
	public void estimateComponents(double[][] data) {
		estimateComponents(new Matrix(data));
	}

	/**
	 * Estimate the independent components of the given data array. Each row
	 * corresponds to an observation with the number of dimensions equal to the
	 * number of columns.
	 * 
	 * @param data
	 *            the data
	 */
	public void estimateComponents(Matrix data) {
		final IndependentPair<Matrix, double[]> p = computeMeanCentre(data);
		final Matrix meanCentredX = p.firstObject();
		final double[] mean = p.getSecondObject();

		final IndependentPair<Matrix, Matrix> p2 = decorrelate(meanCentredX);
		final Matrix Z = p2.firstObject();
		final Matrix CC = p2.secondObject();
		estimateComponentsWhitened(Z, mean, meanCentredX, CC);
	}

	private IndependentPair<Matrix, Matrix> decorrelate(Matrix meanCentredX) {
		final Matrix C = MatrixUtils.covariance(meanCentredX.transpose());
		final Matrix CC = MatrixUtils.invSqrtSym(C);
		return IndependentPair.pair(CC.times(meanCentredX), CC);
	}

	private IndependentPair<Matrix, double[]> computeMeanCentre(Matrix m) {
		final double[][] data = m.getArray();

		final double[] mean = new double[data.length];

		for (int j = 0; j < data.length; j++)
			for (int i = 0; i < data[0].length; i++)
				mean[j] += data[j][i];

		for (int i = 0; i < data.length; i++)
			mean[i] /= data[0].length;

		final Matrix mat = new Matrix(data.length, data[0].length);
		final double[][] matdat = mat.getArray();

		for (int j = 0; j < data.length; j++)
			for (int i = 0; i < data[0].length; i++)
				matdat[j][i] = (data[j][i] - mean[j]);

		return IndependentPair.pair(mat, mean);
	}

	/**
	 * Estimate the IC's from the given decorrelated (mean-centred and whitened)
	 * data matrix, Z.
	 * 
	 * @param Z
	 *            the whitened data; one observation per row
	 * @param mean
	 *            the mean of each dimension
	 * @param X
	 *            the mean-centered data; one observation per row
	 */
	protected abstract void estimateComponentsWhitened(Matrix Z, double[] mean, Matrix X, Matrix CC);
}
