package org.openimaj.ml.pca;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import cern.colt.Arrays;


public class CovarPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	public CovarPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}
	
	protected Matrix buildNormalisedDataMatrix(double[][] data) {
		mean = new double[data[0].length];
		
		for (int j=0; j<data.length; j++)
			for (int i=0; i<data[0].length; i++)
				mean[i] += data[j][i];
		
		for (int i=0; i<data[0].length; i++)
			mean[i] /= data.length;
		
		final Matrix mat = new Matrix(data[0].length, data.length);
		final double[][] matdat = mat.getArray();
		
		for (int j=0; j<data.length; j++)
			for (int i=0; i<data[0].length; i++)
				matdat[i][j] = (data[j][i] - mean[i]) / (data.length - 1);
		
		return mat;
	}
	
	@Override
	public void learnBasis(double[][] data) {
		Matrix dataMatrix = this.buildNormalisedDataMatrix(data);
		
		Matrix covar = dataMatrix.times(dataMatrix.transpose());
		
		EigenvalueDecomposition eig = covar.eig();
		Matrix all_eigenvectors = eig.getV();
		
		//note eigenvalues are in increasing order, so last vec is first pc
		basis = all_eigenvectors.getMatrix(0, all_eigenvectors.getRowDimension()-1, Math.max(0, all_eigenvectors.getColumnDimension() - ndims), all_eigenvectors.getColumnDimension()-1);
		basis = basis.transpose();
		
		//re-order
		double[][] basisData = basis.getArray();
		for (int i=0; i<basisData.length/2; i++) {
			double[] tmp = basisData[i];
			basisData[i] = basisData[basisData.length - i - 1];
			basisData[basisData.length - i - 1] = tmp;
		}
	}

	public static void main(String[] args) {
		double [][] data = {
				{0, 0},
				{2, 2},
				{4, 4},
				{0, 2},
				{1, 1},
				{2, 0},
		};
		
		PrincipalComponentAnalysis pca = new CovarPrincipalComponentAnalysis(2);
		pca.learnBasis(data);
		
		pca.basis.print(5, 5);
		
		for (double [] d : data) {
			System.out.println(Arrays.toString(pca.project(d)));
		}
	}
}
