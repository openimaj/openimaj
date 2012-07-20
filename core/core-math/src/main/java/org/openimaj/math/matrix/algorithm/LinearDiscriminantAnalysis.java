package org.openimaj.math.matrix.algorithm;

import java.util.List;

import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class LinearDiscriminantAnalysis {
	int numComponents;
	
	private IndependentPair<double[], double[][]> computeMeans(List<double[][]> data) {
		final int cols = data.get(0)[0].length;
		final int numClasses = data.size();
		int rows = 0;
		
		final double[] overallMean = new double[cols];
		final double[][] classMeans = new double[numClasses][];
		
		for (int i=0; i<numClasses; i++) {
			final double[][] classData = data.get(i);
			final int classSize = classData.length;
			
			classMeans[i] = computeSum(classData);
			rows++;
			
			for (int j=0; j<cols; j++) {
				overallMean[j] += classMeans[i][j];
				classMeans[i][j] /= classSize;
			}
		}
		
		for (int i=0; i<cols; i++) {
			overallMean[i] /= (double)rows;
		}
		
		return new IndependentPair<double[], double[][]>(overallMean, classMeans);
	}
	
	private double[] computeSum(double[][] data) {
		double[] sum = new double[data[0].length];
		
		for (int j=0; j<data.length; j++) {
			for (int i=0; i<sum.length; i++) {
				sum[i] += data[j][i];
			}
		}
		
		return sum;
	}
	
	public void learnBasis(List<double[][]> data) {
		int c = data.size();
		
		IndependentPair<double[], double[][]> meanData = computeMeans(data);
		final double[] overallMean = meanData.firstObject();
		final double[][] classMeans = meanData.secondObject();
		
		final Matrix Sw = new Matrix(overallMean.length, overallMean.length);
		final Matrix Sb = new Matrix(overallMean.length, overallMean.length);
		
		for (int i=0; i<c; i++) {
			final Matrix classData = new Matrix(data.get(i));
			final double[] classMean = classMeans[i];
			final int classSize = classData.getRowDimension();
			
			Matrix zeroCentred = MatrixUtils.minusRow(classData, classMean);
			MatrixUtils.plusEquals(Sw, zeroCentred.transpose().times(zeroCentred));
			
			ArrayUtils.subtract(classMean, overallMean);
			Matrix diff = new Matrix(new double[][]{ classMean });
			MatrixUtils.plusEquals(Sb, MatrixUtils.times(diff.transpose().times(diff), classSize));
		}
		
//		% solve general eigenvalue problem
//		[W, D] = eig(Sb, Sw);
//		% sort eigenvectors
//		[D, i] = sort(diag(D), ÕdescendÕ); 
//		W = W(:,i);
//		% keep at most (c-1) eigenvectors 
//		W = W(:,1:k);
	}
}
