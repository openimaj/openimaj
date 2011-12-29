package org.openimaj.ml.pca;

import java.util.Collection;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;

import Jama.Matrix;

public abstract class PrincipalComponentAnalysis {
	protected Matrix basis;
	protected double [] mean;
	
	public abstract void learnBasis(double [][] data);
	
	public void learnBasis(FeatureVector[] data) {
		double [][] d = new double[data.length][];
		
		for (int i=0; i<data.length; i++) {
			d[i] = data[i].asDoubleVector();
		}
		
		learnBasis(d);
	}
	
	public void learnBasis(Collection<FeatureVector> data) {
		double [][] d = new double[data.size()][];
		
		int i=0;
		for (FeatureVector fv : data) {
			d[i++] = fv.asDoubleVector();
		}
		
		learnBasis(d);
	}
	
	public Matrix getBasis() {
		return basis;
	}
	
	public double[] getMean() {
		return mean;
	}
	
	public double[] project(double [] vector) {
		Matrix vec = new Matrix(vector.length, 1);
		final double[][] vecarr = vec.getArray();
		
		for (int i=0; i<vector.length; i++)
			vecarr[i][0] = vector[i] - mean[i];
		
		return basis.times(vec).getColumnPackedCopy();
	}
	
	public DoubleFV project(FeatureVector vector) {
		return new DoubleFV(project(vector.asDoubleVector()));
	}
}
