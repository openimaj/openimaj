package org.openimaj.math.matrix.similarity.processor;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.util.pair.IndependentPair;

public class MultidimensionalScaling implements SimilarityMatrixProcessor {
	private int numIterations = 1000;
	private double rate = 0.01;
	
	protected List<IndependentPair<String, Point2d>> points;
	
	public MultidimensionalScaling() {
		
	}

	public MultidimensionalScaling(int numIterations, double rate) {
		this.numIterations = numIterations;
		this.rate = rate;
	}

	@Override
	public void process(SimilarityMatrix matrix) {
		final int sz = matrix.getRowDimension();
		
		final double[][] realDists = matrix.process(new NormaliseData(true)).getArray();
		
		//initialise points randomly
		points = new ArrayList<IndependentPair<String, Point2d>>(sz);
		for (int i=0; i<sz; i++) {
			points.add(new IndependentPair<String, Point2d>(matrix.getIndexValue(i), Point2dImpl.createRandomPoint()));
		}
		
		Point2dImpl[] grad = new Point2dImpl[sz];
		for (int i=0; i<sz; i++)
			grad[i] = new Point2dImpl();
		
		double lastError = Double.MAX_VALUE;
		double[][] fakeDists = new double[sz][sz];
		for (int m=0; m<numIterations; m++) {
			for (int r=0; r<sz; r++) {
				for (int c=r+1; c<sz; c++) {
					double dist = Line2d.distance(points.get(r).secondObject(), points.get(c).secondObject());
					fakeDists[r][c] = dist;
					fakeDists[c][r] = dist;
				}
			}
			
			for (int i=0; i<sz; i++) {
				grad[i].x = 0; grad[i].y = 0;
			}
			
			double totalError = 0;
			for (int k=0; k<sz; k++) {
				for (int j=0; j<sz; j++) {
					if (k==j) continue;
					
					double errorterm = (fakeDists[j][k] - realDists[j][k]) / realDists[j][k];
					
					grad[k].x += ((((Point2dImpl)points.get(k).secondObject()).x - points.get(j).secondObject().getX()) / fakeDists[j][k]) * errorterm;
					grad[k].y += ((((Point2dImpl)points.get(k).secondObject()).y - points.get(j).secondObject().getY()) / fakeDists[j][k]) * errorterm;
					
					totalError = Math.abs(errorterm);
				}
			}
			
			if (lastError < totalError)
				break;
			lastError = totalError;
			
			for (int k=0; k<sz; k++) {
				((Point2dImpl)points.get(k).secondObject()).x += rate * grad[k].x;
				((Point2dImpl)points.get(k).secondObject()).y += rate * grad[k].y;
			}
		}
	}

	public List<IndependentPair<String, Point2d>> getPoints() {
		return points;
	}
	
	@Override
	public String toString() {
		if (points == null) return super.toString();
		
		StringBuilder sb = new StringBuilder();
		
		for (IndependentPair<String, Point2d> pair : points) {
			sb.append(String.format("%s\t%4.3f\t%4.3f\n", pair.firstObject(), pair.secondObject().getX(), pair.secondObject().getY()));
		}
		
		return sb.toString();
	}
}
