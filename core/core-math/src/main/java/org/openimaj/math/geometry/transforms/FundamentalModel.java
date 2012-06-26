/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Implementation of a Fundamental matrix model that estimates the
 * epipolar geometry.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FundamentalModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	/**
	 * Interface for classes able to test whether a point pair
	 * satisifies the epipolar geometry.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static interface ValidationCondition {
		/**
		 * Test if a point pair satisifies the epipolar geometry.
		 * @param data The point pair
		 * @param fundamental The fundamental matrix
		 * @return true if satisfied; false otherwise.
		 */
		public boolean validate(IndependentPair<Point2d, Point2d> data, Matrix fundamental);
	}
	
	/**
	 * {@link ValidationCondition} that calculates the distance of the
	 * two points from the closest epipolar line.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public static class EpipolarDistanceCondition implements ValidationCondition {
		float tol;
		
		/**
		 * Construct with tolerance distance. Distance from epipolar line
		 * of both points in the validate method must be less than
		 * the tolerance for the validation to succeed.
		 * @param tol
		 */
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
	
	/**
	 * {@link ValidationCondition} based on Sampson's geometric error.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public static class SampsonGeometricErrorCondition implements ValidationCondition {
		double tol;
		
		/**
		 * @param tol
		 */
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
	
	protected Matrix normFundamental;
	protected Matrix fundamental;
	protected ValidationCondition condition;
	protected Pair<Matrix> norms;
	
	/**
	 * Create an {@link FundamentalModel} with a given validation condition
	 * @param condition Condition to determine whether a point is an inlier
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
