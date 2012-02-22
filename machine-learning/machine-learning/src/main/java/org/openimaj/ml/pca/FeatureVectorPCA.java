package org.openimaj.ml.pca;

import java.util.Collection;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVector;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;

import Jama.Matrix;

public class FeatureVectorPCA extends PrincipalComponentAnalysis {
	PrincipalComponentAnalysis inner; 
	
	public FeatureVectorPCA(PrincipalComponentAnalysis inner) {
		this.inner = inner;
	}
	
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
	
//	public DoubleFV project(FeatureVector vector) {
//		return new DoubleFV(project(vector.asDoubleVector()));
//	}

	@Override
	public void learnBasis(double[][] data) {
		inner.learnBasis(data);
		this.basis = inner.getBasis();
		this.eigenvalues = inner.getEigenValues();
		this.mean = inner.getMean();
	}

	@Override
	protected void learnBasisNorm(Matrix norm) {
		inner.learnBasis(norm);
	}
}
