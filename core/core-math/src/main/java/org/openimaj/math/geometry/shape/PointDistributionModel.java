package org.openimaj.math.geometry.shape;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.algorithm.GeneralisedProcrustesAnalysis;
import org.openimaj.math.matrix.algorithm.pca.CovarPrincipalComponentAnalysis;
import org.openimaj.math.matrix.algorithm.pca.PrincipalComponentAnalysis;

import Jama.Matrix;

public class PointDistributionModel {
	protected PrincipalComponentAnalysis pc;
	protected PointList mean; 

	public PointDistributionModel(List<PointList> data) {
		//align
		mean = GeneralisedProcrustesAnalysis.alignPoints(data, true, 5, 10);
		
		//build data matrix
		Matrix m = buildDataMatrix(data);
		
		//perform pca
		this.pc = new CovarPrincipalComponentAnalysis();
		pc.learnBasis(m);
	}
	
	private Matrix buildDataMatrix(List<PointList> data) {
		final int nData = data.size();
		final int nPoints = data.get(0).size();
		
		Matrix m = new Matrix(nData, nPoints * 2);
		double[][] mData = m.getArray();
		
		for (int i=0; i<nData; i++) {
			PointList pts = data.get(i);
			for (int j=0, k=0; k<nPoints; j+=2, k++) {
				Point2d pt = pts.points.get(k);
				
				mData[i][j] = pt.getX();
				mData[i][j+1] = pt.getY();
			}
		}
		
		return m;
	}

	public PointList getMean() {
		return mean;
	}
	
	public void setNumComponents(int n) {
		pc.selectSubset(n);
	}
	
	public PointList generateNewShape(double [] scaling) {
		PointList newShape = new PointList();
		
		double[] pts = pc.generate(scaling);
		
		for (int i=0; i<pts.length; i+=2) {
			float x = (float) pts[i];
			float y = (float) pts[i+1];
			
			newShape.points.add(new Point2dImpl(x, y));
		}
		
		return newShape;
	}
}
