package org.openimaj.math.matrix.algorithm.pca;

import no.uib.cipr.matrix.NotConvergedException;
import Jama.Matrix;
import cern.colt.Arrays;


public class SvdPrincipalComponentAnalysis extends PrincipalComponentAnalysis {
	int ndims;
	
	public SvdPrincipalComponentAnalysis(int ndims) {
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
		
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(dataMatrix.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
			
			no.uib.cipr.matrix.DenseMatrix output = svd.getVt();
			
			this.basis = new Matrix(ndims, output.numRows());
			for (int j=0; j<output.numRows(); j++)
				for (int i=0; i<ndims; i++)
					basis.getArray()[i][j] = output.get(j, i);
		} catch (NotConvergedException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
//		double [][] data = {
//				{0, 0},
//				{2, 2},
//				{4, 4},
//				{0, 2},
//				{1, 1},
//				{2, 0},
//		};
//		
//		PrincipalComponentAnalysis pca = new SvdPrincipalComponentAnalysis(2);
//		pca.learnBasis(data);
//		
//		pca.basis.print(5, 5);
//		
//		for (double [] d : data) {
//			System.out.println(Arrays.toString(pca.project(d)));
//		}
	}
}
