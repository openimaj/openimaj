package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

public class FundamentalModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	public static interface ValidationCondition {
		public boolean validate(IndependentPair<Point2d, Point2d> data, Matrix fundamental);
	}
	
	public static class EpipolarDistanceCondition implements ValidationCondition {
		float tol;
		
		public EpipolarDistanceCondition(float tol) {
			this.tol = tol;
		}
		
		@Override
		public boolean validate(IndependentPair<Point2d, Point2d> data, Matrix fundamental) {
			Matrix p1Mat = new Matrix(3,1);
			Matrix p2Mat = new Matrix(3,1);
			
			// x
			p1Mat.set(0, 0, data.firstObject().getX());
			p1Mat.set(1, 0, data.firstObject().getY());
			p1Mat.set(2, 0, 1);
			
			// x'
			p2Mat.set(0, 0, data.secondObject().getX());
			p2Mat.set(1, 0, data.secondObject().getY());
			p2Mat.set(2, 0, 1);
			
			Matrix l1 = fundamental.times(p1Mat);
			double n1 = Math.sqrt(l1.get(0, 0) * l1.get(0, 0) + l1.get(1, 0) * l1.get(1, 0));
			double d1 = Math.abs((l1.get(0, 0)*p2Mat.get(0, 0) + l1.get(1, 0)*p2Mat.get(1, 0) + l1.get(2, 0)*p2Mat.get(2, 0)) / n1); 
			
			Matrix l2 = fundamental.transpose().times(p2Mat);
			double n2 = Math.sqrt(l2.get(0, 0) * l2.get(0, 0) + l2.get(1, 0) * l2.get(1, 0));
			double d2 = Math.abs((l2.get(0, 0)*p1Mat.get(0, 0) + l2.get(1, 0)*p1Mat.get(1, 0) + l2.get(2, 0)*p1Mat.get(2, 0)) / n2);
			
			return d1 < tol && d2 < tol;
		}
	}
	
	public static class SampsonGeometricErrorCondition implements ValidationCondition {
		double tol;
		
		public SampsonGeometricErrorCondition(double tol) {
			this.tol = tol;
		}
		
		@Override
		public boolean validate(IndependentPair<Point2d, Point2d> data, Matrix fundamental) {
			Matrix p1 = new Matrix(3,1);
			Matrix p2 = new Matrix(3,1);
			
			// x
			p1.set(0, 0, data.firstObject().getX());
			p1.set(1, 0, data.firstObject().getY());
			p1.set(2, 0, 1);
			
			// x'
			p2.set(0, 0, data.secondObject().getX());
			p2.set(1, 0, data.secondObject().getY());
			p2.set(2, 0, 1);
			
			double p2tFp1 = p2.transpose().times(fundamental).times(p1).get(0, 0);
			Matrix Fp1 = fundamental.times(p1);
			Matrix Ftp2 = fundamental.transpose().times(p2);     
			
			double dist =  (p2tFp1*p2tFp1) / (Fp1.get(0, 0)*Fp1.get(0, 0) + Fp1.get(1,0)*Fp1.get(1,0) + Ftp2.get(0,0)*Ftp2.get(0,0) + Ftp2.get(1,0)*Ftp2.get(1,0));
			
			return Math.abs(dist) < tol;
		}
	}
	
	Matrix normFundamental;
	Matrix fundamental;
	ValidationCondition condition;
	Pair<Matrix> norms;
	
	/**
	 * Create an {@link FundamentalModel} with a given tolerence in pixels for validation
	 * @param tolerance value specifying how far in pixels points are allowed to deviate from the epipolar lines.
	 */
	public FundamentalModel(ValidationCondition condition)
	{
		this.condition = condition;
		normFundamental = new Matrix(3,3);
	}
	
	@Override
	public Matrix getTransform() {
		return this.fundamental;
	}
	
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		this.norms = TransformUtilities.getNormalisations(data);
		List<? extends IndependentPair<Point2d, Point2d>> normData = TransformUtilities.normalise(data, norms);
		
		this.normFundamental = TransformUtilities.fundamentalMatrix8Pt(normData);
		this.fundamental = norms.secondObject().transpose().times(normFundamental).times(norms.firstObject());
	}

	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		if(normFundamental == null) return false;
		
		IndependentPair<Point2d, Point2d> normData = TransformUtilities.normalise(data, norms);
		
		return condition.validate(normData, normFundamental);
	}

	@Override
	public Point2d predict(Point2d data) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numItemsToEstimate() {
		return 8;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> data) {
		double totalCheck = data.size();
		double correct = 0;
		for (IndependentPair<Point2d, Point2d> independentPair : data) {
			if(this.validate(independentPair)) correct += 1;
		}
		return correct / totalCheck;
	}
	
	/**
	 * Clone the model
	 * @return a cloned copy
	 */
	@Override
	public FundamentalModel clone(){
		FundamentalModel model = new FundamentalModel(condition);
		if (model.normFundamental != null) model.normFundamental = normFundamental.copy();
		if (model.fundamental != null) model.fundamental = fundamental.copy();
		if (model.norms != null) model.norms = new Pair<Matrix>(norms.firstObject().copy(), norms.secondObject().copy());
		return model;
	}
}
