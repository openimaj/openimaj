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
package org.openimaj.math.matrix.algorithm.pca;

import java.util.Arrays;
import java.util.List;

import Jama.Matrix;

/**
 * Abstract base class for PCA implementations.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public abstract class PrincipalComponentAnalysis {
	/**
	 * Interface for classes capable of selecting a subset of the PCA components
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public interface ComponentSelector {
		/**
		 * Select a subset of principal components, discarding the ones not
		 * selected
		 * 
		 * @param pca
		 */
		public void select(PrincipalComponentAnalysis pca);
	}

	/**
	 * {@link ComponentSelector} that selects the n-best components.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static class NumberComponentSelector implements ComponentSelector {
		int n;

		/**
		 * Construct with the number of components required
		 * 
		 * @param n
		 *            the number of components
		 */
		public NumberComponentSelector(int n) {
			this.n = n;
		}

		@Override
		public void select(PrincipalComponentAnalysis pca) {
			pca.selectSubset(n);
		}
	}

	/**
	 * {@link ComponentSelector} that selects a subset of the principal
	 * components such that all remaining components have a cumulative energy
	 * less than the given value.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class EnergyThresholdComponentSelector implements ComponentSelector {
		double threshold;

		/**
		 * Construct with the given threshold
		 * 
		 * @param threshold
		 *            the threshold
		 */
		public EnergyThresholdComponentSelector(double threshold) {
			this.threshold = threshold;
		}

		@Override
		public void select(PrincipalComponentAnalysis pca) {
			pca.selectSubsetEnergyThreshold(threshold);
		}
	}

	/**
	 * {@link ComponentSelector} that selects a subset of the principal
	 * components such that all remaining components have a certain percentage
	 * cumulative energy of the total. The percentage is calculated relative to
	 * the total energy of the eigenvalues.
	 * 
	 * Bear in mind that if not all the eigenvalues were calculated, or if some
	 * have previously been removed through {@link #selectSubset(int)},
	 * {@link #selectSubsetEnergyThreshold(double)} or
	 * {@link #selectSubsetPercentageEnergy(double)}, then the percentage
	 * calculation only factors in the remaining eigenvalues that are available
	 * to it.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class PercentageEnergyComponentSelector implements ComponentSelector {
		double percentage;

		/**
		 * Construct with the given percentage
		 * 
		 * @param percentage
		 *            percentage of the total cumulative energy to retain
		 *            [0..1].
		 */
		public PercentageEnergyComponentSelector(double percentage) {
			this.percentage = percentage;
		}

		@Override
		public void select(PrincipalComponentAnalysis pca) {
			pca.selectSubsetPercentageEnergy(percentage);
		}
	}

	protected Matrix basis;
	protected double[] mean;
	protected double[] eigenvalues;

	/**
	 * Learn the principal components of the given data list. Each item
	 * corresponds to an observation with the number of dimensions equal to the
	 * length of the array.
	 * 
	 * @param data
	 *            the data
	 */
	public void learnBasis(List<double[]> data) {
		learnBasis(data.toArray(new double[data.size()][]));
	}

	/**
	 * Learn the principal components of the given data array. Each row
	 * corresponds to an observation with the number of dimensions equal to the
	 * number of columns.
	 * 
	 * @param data
	 *            the data
	 */
	public void learnBasis(double[][] data) {
		learnBasis(new Matrix(data));
	}

	/**
	 * Learn the principal components of the given data matrix. Each row
	 * corresponds to an observation with the number of dimensions equal to the
	 * number of columns.
	 * 
	 * @param data
	 *            the data
	 */
	public void learnBasis(Matrix data) {
		final Matrix norm = this.buildNormalisedDataMatrix(data);
		learnBasisNorm(norm);
	}

	/**
	 * Learn the PCA basis from the centered data provided. Each row corresponds
	 * to an observation with the number of dimensions equal to the number of
	 * columns.
	 * 
	 * @param norm
	 */
	protected abstract void learnBasisNorm(Matrix norm);

	/**
	 * Zero-centre the data matrix and return a copy
	 * 
	 * @param data
	 *            the data matrix
	 * @return the normalised data
	 */
	protected Matrix buildNormalisedDataMatrix(Matrix m) {
		final double[][] data = m.getArray();

		mean = new double[data[0].length];

		for (int j = 0; j < data.length; j++)
			for (int i = 0; i < data[0].length; i++)
				mean[i] += data[j][i];

		for (int i = 0; i < data[0].length; i++)
			mean[i] /= data.length;

		final Matrix mat = new Matrix(data.length, data[0].length);
		final double[][] matdat = mat.getArray();

		for (int j = 0; j < data.length; j++)
			for (int i = 0; i < data[0].length; i++)
				matdat[j][i] = (data[j][i] - mean[i]);

		return mat;
	}

	/**
	 * Get the principal components. The principal components are the column
	 * vectors of the returned matrix.
	 * 
	 * @return the principal components
	 */
	public Matrix getBasis() {
		return basis;
	}

	/**
	 * Get a specific principle component vector as a double array. The returned
	 * array contains a copy of the data.
	 * 
	 * @param index
	 *            the index of the principle component
	 * 
	 * @return the principle component
	 */
	public double[] getPrincipalComponent(int index) {
		final double[] pc = new double[basis.getRowDimension()];
		final double[][] data = basis.getArray();

		for (int r = 0; r < pc.length; r++)
			pc[r] = data[r][index];

		return pc;
	}

	/**
	 * Get the principal components. The principal components are the column
	 * vectors of the returned matrix.
	 * 
	 * Syntactic sugar for {@link #getBasis()}
	 * 
	 * @return the principal components
	 */
	public Matrix getEigenVectors() {
		return basis;
	}

	/**
	 * @return the eigen values corresponding to the principal components
	 */
	public double[] getEigenValues() {
		return eigenvalues;
	}

	/**
	 * Get the eigen value corresponding to the ith principal component.
	 * 
	 * @param i
	 *            the index of the component
	 * @return the eigen value corresponding to the principal component
	 */
	public double getEigenValue(int i) {
		return eigenvalues[i];
	}

	/**
	 * Get the cumulative energy of the ith principal component
	 * 
	 * @param i
	 *            the index of the component
	 * @return the cumulative energy of the component
	 */
	public double getCumulativeEnergy(int i) {
		double energy = 0;

		for (int j = 0; j <= i; j++)
			energy += eigenvalues[i];

		return energy;
	}

	/**
	 * Get the cumulative energies of each principal component
	 * 
	 * @return the cumulative energies of the components
	 */
	public double[] getCumulativeEnergies() {
		final double[] energy = new double[eigenvalues.length];

		energy[0] = eigenvalues[0];
		for (int j = 1; j < energy.length; j++)
			energy[j] += energy[j - 1] + eigenvalues[j];

		return energy;
	}

	/**
	 * @return The mean values
	 */
	public double[] getMean() {
		return mean;
	}

	/**
	 * Select a subset of the principal components using a
	 * {@link ComponentSelector}. Calling this method throws away any extra
	 * basis vectors and eigenvalues.
	 * 
	 * @param selector
	 *            the {@link ComponentSelector} to apply
	 */
	public void selectSubset(ComponentSelector selector) {
		selector.select(this);
	}

	/**
	 * Select a subset of the principal components. Calling this method throws
	 * away any extra basis vectors and eigenvalues.
	 * 
	 * @param n
	 */
	public void selectSubset(int n) {
		if (n >= eigenvalues.length)
			return;

		basis = basis.getMatrix(0, basis.getRowDimension() - 1, 0, n - 1);
		eigenvalues = Arrays.copyOf(eigenvalues, n);
	}

	/**
	 * Select a subset of the principal components such that all remaining
	 * components have a cumulative energy less than the given value.
	 * 
	 * Calling this method throws away any extra basis vectors and eigenvalues.
	 * 
	 * @param threshold
	 *            threshold on the cumulative energy.
	 */
	public void selectSubsetEnergyThreshold(double threshold) {
		final double[] energy = getCumulativeEnergies();

		for (int i = 1; i < energy.length; i++) {
			if (energy[i] < threshold) {
				selectSubset(i - 1);
				return;
			}
		}
	}

	/**
	 * Select a subset of the principal components such that all remaining
	 * components have a certain percentage cumulative energy of the total. The
	 * percentage is calculated relative to the total energy of the eigenvalues.
	 * Bear in mind that if not all the eigenvalues were calculated, or if some
	 * have previously been removed through {@link #selectSubset(int)},
	 * {@link #selectSubsetEnergyThreshold(double)} or
	 * {@link #selectSubsetPercentageEnergy(double)}, then the percentage
	 * calculation only factors in the remaining eigenvalues that are available
	 * to it.
	 * 
	 * Calling this method throws away any extra basis vectors and eigenvalues.
	 * 
	 * @param percentage
	 *            percentage of the total cumulative energy to retain [0..1].
	 */
	public void selectSubsetPercentageEnergy(double percentage) {
		final double[] energy = getCumulativeEnergies();
		final double total = energy[energy.length - 1];

		for (int i = 1; i < energy.length; i++) {
			if (energy[i] / total > percentage) {
				selectSubset(i - 1);
				return;
			}
		}
	}

	/**
	 * Generate a new "observation" as a linear combination of the principal
	 * components (PC): mean + PC * scaling.
	 * 
	 * If the scaling vector is shorter than the number of components, it will
	 * be zero-padded. If it is longer, it will be truncated.
	 * 
	 * @param scalings
	 *            the weighting for each PC
	 * @return generated observation
	 */
	public double[] generate(double[] scalings) {
		final Matrix scale = new Matrix(this.eigenvalues.length, 1);

		for (int i = 0; i < Math.min(eigenvalues.length, scalings.length); i++)
			scale.set(i, 0, scalings[i]);

		final Matrix meanMatrix = new Matrix(new double[][] { mean }).transpose();

		return meanMatrix.plus(basis.times(scale)).getColumnPackedCopy();
	}

	/**
	 * Project a matrix of row vectors by the basis. The vectors are normalised
	 * by subtracting the mean and then multiplied by the basis. The returned
	 * matrix has a row for each vector.
	 * 
	 * @param m
	 *            the vector to project
	 * @return projected vectors
	 */
	public Matrix project(Matrix m) {
		final Matrix vec = m.copy();

		final int rows = vec.getRowDimension();
		final int cols = vec.getColumnDimension();
		final double[][] vecarr = vec.getArray();

		for (int r = 0; r < rows; r++)
			for (int c = 0; c < cols; c++)
				vecarr[r][c] -= mean[c];

		// T = (Vt.Dt)^T == Dt.Vt
		return vec.times(basis);
	}

	/**
	 * Project a vector by the basis. The vector is normalised by subtracting
	 * the mean and then multiplied by the basis.
	 * 
	 * @param vector
	 *            the vector to project
	 * @return projected vector
	 */
	public double[] project(double[] vector) {
		final Matrix vec = new Matrix(1, vector.length);
		final double[][] vecarr = vec.getArray();

		for (int i = 0; i < vector.length; i++)
			vecarr[0][i] = vector[i] - mean[i];

		return vec.times(basis).getColumnPackedCopy();
	}

	/**
	 * Get the standard deviations (sqrt of eigenvalues) of the principal
	 * components.
	 * 
	 * @return vector of standard deviations
	 */
	public double[] getStandardDeviations() {
		return getStandardDeviations(eigenvalues.length);
	}

	/**
	 * Get the standard deviations (sqrt of eigenvalues) of the n top principal
	 * components.
	 * 
	 * @param n
	 *            number of principal components
	 * @return vector of standard deviations
	 */
	public double[] getStandardDeviations(int n) {
		final double[] rngs = new double[n];

		for (int i = 0; i < rngs.length; i++) {
			rngs[i] = Math.sqrt(eigenvalues[i]);
		}

		return rngs;
	}

	@Override
	public String toString() {
		return String.format("PrincipalComponentAnalysis[dims=%d]", this.eigenvalues == null ? 0
				: this.eigenvalues.length);
	}
}
