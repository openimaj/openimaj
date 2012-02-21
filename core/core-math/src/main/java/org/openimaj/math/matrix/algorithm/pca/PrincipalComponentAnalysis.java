package org.openimaj.math.matrix.algorithm.pca;

import java.util.Arrays;

import Jama.Matrix;

/**
 * Abstract base class for PCA implementations.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public abstract class PrincipalComponentAnalysis {
	protected Matrix basis;
	protected double [] mean;
	protected double [] eigenvalues;
	
	/**
	 * Learn the principal components of the given data array
	 * @param data the data
	 */
	public abstract void learnBasis(double [][] data);
	
	/**
	 * Learn the principal components of the given data matrix
	 * @param data the data
	 */
	public void learnBasis(Matrix data) {
		learnBasis(data.getArray());
	}
		
	/**
	 * Get the principal components. The principal components
	 * are the column vectors of the returned matrix.
	 * @return the principal components
	 */
	public Matrix getBasis() {
		return basis;
	}
	
	/**
	 * Get the principal components. The principal components
	 * are the column vectors of the returned matrix.
	 * 
	 * Syntactic suger for {@link #getBasis()}
	 * @return the principal components
	 */
	public Matrix getEigenVectors() {
		return basis;
	}
	
	/**
	 * @return the eigen values corresponding to the principal components
	 */
	public double [] getEigenValues() {
		return eigenvalues;
	}
	
	/**
	 * Get the eigen value corresponding to the ith principal component.
	 * @param i the index of the component
	 * @return the eigen value corresponding to the principal component
	 */
	public double getEigenValue(int i) {
		return eigenvalues[i];
	}
	
	/**
	 * Get the cumulative energy of the ith principal component
	 * @param i the index of the component
	 * @return the cumulative energy of the component
	 */
	public double getCumulativeEnergy(int i) {
		double energy = 0;
		
		for (int j=0; j<=i; j++)
			energy += eigenvalues[i];
		
		return energy;
	}
	
	/**
	 * Get the cumulative energies of each principal component
	 * @return the cumulative energies of the components
	 */
	public double [] getCumulativeEnergies() {
		double [] energy = new double[eigenvalues.length];
		
		energy[0] = eigenvalues[0];
		for (int j=1; j<energy.length; j++)
			energy[j] += energy[j-1] + eigenvalues[j];
		
		return energy;
	}
	
	/**
	 * @return The mean values
	 */
	public double[] getMean() {
		return mean;
	}
	
	/**
	 * Select a subset of the principal components. Calling
	 * this method throws away any extra basis vectors and
	 * eigenvalues. 
	 * @param n
	 */
	public void selectSubset(int n) {
		if (n >= eigenvalues.length)
			return;
		
		basis = basis.getMatrix(0, basis.getRowDimension()-1, 0, n-1);
		eigenvalues = Arrays.copyOf(eigenvalues, n);
	}
	
	/**
	 * Select a subset of the principal components such
	 * that all remaining components have a cumulative energy
	 * less than the given value. 
	 * 
	 * Calling this method throws away any extra basis vectors and
	 * eigenvalues. 
	 * 
	 * @param threshold threshold on the cumulative energy.
	 */
	public void selectSubsetEnergyThreshold(double threshold) {
		double [] energy = getCumulativeEnergies();
		
		for (int i=1; i<energy.length; i++) {
			if (energy[i] < threshold) {
				selectSubset(i-1);
				return;
			}
		}
	}
	
	/**
	 * Select a subset of the principal components such
	 * that all remaining components have a certain percentage cumulative energy
	 * of the total. The percentage is calculated
	 * relative to the total energy of the eigenvalues. Bear in mind
	 * that if not all the eigenvalues were calculated, or if
	 * some have previously been removed through {@link #selectSubset(int)},
	 * {@link #selectSubsetEnergyThreshold(double)} or {@link #selectSubsetPercentageEnergy(double)},
	 * then the percentage calculation only factors in the remaining eigenvalues
	 * that are available to it.
	 * 
	 * Calling this method throws away any extra basis vectors and
	 * eigenvalues. 
	 * 
	 * @param percentage percentage of the total cumulative energy to retain [0..1].
	 */
	public void selectSubsetPercentageEnergy(double percentage) {
		double [] energy = getCumulativeEnergies();
		double total = energy[energy.length - 1];
		
		for (int i=1; i<energy.length; i++) {
			if (energy[i]/total < percentage) {
				selectSubset(i-1);
				return;
			}
		}
	}

	/**
	 * Generate a new "observation" as a linear combination of
	 * the principal components (PC): mean + PC * scaling.
	 * 
	 * If the scaling vector is shorter than the number of
	 * components, it will be zero-padded. If it is longer,
	 * it will be truncated.
	 * 
	 * @param scalings the weighting for each PC
	 * @return generated observation
	 */
	public double [] generate(double[] scalings) {
		Matrix scale = new Matrix(this.eigenvalues.length, 1);
		
		for (int i=0; i<Math.min(eigenvalues.length, scalings.length); i++)
			scale.set(i, 0, scalings[i]);
		
		Matrix meanMatrix = new Matrix(new double[][]{mean}).transpose();
		
		return meanMatrix.plus(basis.times(scale)).getColumnPackedCopy();
	}
}
