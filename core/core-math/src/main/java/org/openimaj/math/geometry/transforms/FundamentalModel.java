package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class FundamentalModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	Matrix fundamental;
	private float tol;
	
	/**
	 * Create an {@link FundamentalModel} with a given tolerence for validation
	 * @param tolerance value specifying how close to 0 the equation x' * F * x = 0 should be to 
	 * consider two matching points as valid given a fundamental matrix F.
	 * 
	 */
	public FundamentalModel(float tolerance)
	{
		tol = tolerance;
		fundamental = new Matrix(3,3);
	}
	
	private FundamentalModel() {}

	@Override
	public Matrix getTransform() {
		return this.fundamental;
	}
	
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		fundamental = TransformUtilities.fundamentalMatrix(data);
	}

	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		if(fundamental == null) return false;
		
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
		
		// x' * F * x
		Matrix check = p2Mat.transpose().times(this.fundamental).times(p1Mat);
		double checkValue = Math.abs(check.get(0, 0)); 
		return checkValue < this.tol;
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
		FundamentalModel model = new FundamentalModel();
		model.fundamental = fundamental.copy();
		model.tol = tol;
		return model;
	}
	
	public static void main(String args[]){
	}
}
