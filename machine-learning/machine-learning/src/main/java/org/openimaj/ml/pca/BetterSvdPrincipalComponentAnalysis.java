package org.openimaj.ml.pca;

import org.openimaj.math.matrix.ThinSingularValueDecomposition;

import Jama.Matrix;
import cern.colt.Arrays;


public class BetterSvdPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	public BetterSvdPrincipalComponentAnalysis(int ndims) {
		this.ndims = ndims;
	}
	
	protected Matrix buildNormalisedDataMatrix(double[][] data) {
		mean = new double[data[0].length];
		
		for (int j=0; j<data.length; j++)
			for (int i=0; i<data[0].length; i++)
				mean[i] += data[j][i];
		
		for (int i=0; i<data[0].length; i++)
			mean[i] /= data.length;
		
		final Matrix mat = new Matrix(data.length, data[0].length);
		final double[][] matdat = mat.getArray();
		
		final double sf = 1.0 / Math.sqrt(data.length - 1);
		
		for (int j=0; j<data.length; j++)
			for (int i=0; i<data[0].length; i++)
				matdat[j][i] = (data[j][i] - mean[i]) * sf;
		
		return mat;
	}
	
	@Override
	public void learnBasis(double[][] data) {
		Matrix dataMatrix = this.buildNormalisedDataMatrix(data);
		
		ThinSingularValueDecomposition svd = new ThinSingularValueDecomposition(dataMatrix, ndims);
		basis = svd.Vt;
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
		
		PrincipalComponentAnalysis pca = new BetterSvdPrincipalComponentAnalysis(2);
		pca.learnBasis(data);
		
		pca.basis.print(5, 5);
		
		for (double [] d : data) {
			System.out.println(Arrays.toString(pca.project(d)));
		}
	}
}
