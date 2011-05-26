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

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.line.Line2d.IntersectionResult;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Implementation of a Homogeneous Radial Distortion model - a transform that
 * corrects for the radial distortion (fish eye effect) caused by some camera
 * 
 * The independent variable is the point as measured from the camera. The dependent variable
 * is the point as it should be on the line it belongs to. Helper functions are provided
 * to find the dependent variable given a line and an independent variable
 * @author Jonathon Hare
 *
 */
public class RadialDistortionModel implements Model<Point2d, Point2d>{
	protected float tol;
	
	/**
	 * Create an RadialDistortionModel with a given tolerence for validation
	 * @param tolerance summed distance of points from the line
	 */
	public RadialDistortionModel(float tolerance)
	{
		tol = tolerance;
	}
	
	@Override
	public RadialDistortionModel clone() {
		RadialDistortionModel hm = new RadialDistortionModel(tol);
		return hm;
	}
	
	public Matrix matrixK;
	private Point2d middle = new Point2dImpl(0,0);
	
	
	public static IndependentPair<Point2d, Point2d> getRadialIndependantPair(Line2d modelLine, Point2d independantPoint){
		return getRadialIndependantPair(modelLine,independantPoint,new Point2dImpl(0,0));
	}
	public static IndependentPair<Point2d, Point2d> getRadialIndependantPair(Line2d modelLine, Point2d independantPoint, Point2d middle){
		Line2d radialLine = new Line2d(independantPoint,middle);
		IntersectionResult intersect = modelLine.getIntersection(radialLine);
		
		return new IndependentPair<Point2d,Point2d>(independantPoint,intersect.intersectionPoint);
	}
	
	
	/*
	 * SVD estimation of least-squares solution of radial distortion variables k0, k1 and k2
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#estimate(java.util.List)
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		if(data.size() < this.numItemsToEstimate()){
			return;
		}
		
		
		
		
		Matrix A, W=null;
		int i, j;
		A = new Matrix(data.size()*2, 4);
		
		for ( i=0, j=0; i<data.size(); i++, j+=2 ) {
			Point2d independant = data.get(i).firstObject();
			Point2d dependant = data.get(i).secondObject();
			
			Line2d radialLine = new Line2d(independant,middle);
			double radius = radialLine.calculateLength();
			
			A.set(j, 0, independant.getX());
			A.set(j, 1, independant.getX() * radius);
			A.set(j, 2, independant.getX() * radius * radius);
			A.set(j, 3, -dependant.getX());
			
			A.set(j+1, 0, independant.getY());
			A.set(j+1, 1, independant.getY() * radius);
			A.set(j+1, 2, independant.getY() * radius * radius);
			A.set(j+1, 3, -dependant.getY());
		}
		
		/*
		 * The JAMA SVD method seems to be broken in some cases (m<n),
		 * like when we are trying to do a four-point estimate...*/
//		SingularValueDecomposition svd = A.svd();
//		W = svd.getV().getMatrix(0, 8, 8, 8);
		/* */
		
		//This is a hack to use MJT instead
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(A.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);
			
			W = new Matrix(svd.getVt().numRows(), 1);
			
			for (i=0; i<svd.getVt().numRows(); i++) {
				W.set(i, 0, svd.getVt().get(3, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return;
		}
		//End hack
		
		matrixK = new Matrix(1,4);
		matrixK.set(0,0, W.get(0,0)/W.get(3,0));
		matrixK.set(0,1, W.get(1,0)/W.get(3,0));
		matrixK.set(0,2, W.get(2,0)/W.get(3,0));
		matrixK.set(0,3, W.get(3,0)/W.get(3,0));
		
	}
	

	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		Point2d predicted = this.predict(data.firstObject());
		return new Line2d(data.secondObject(),predicted).calculateLength() < tol;
	}
	
	@Override
	public int numItemsToEstimate() {
		return 3;
	}
	
	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> alldata)
	{
		double sumError = 0;
		for(IndependentPair<Point2d,Point2d> pair : alldata){
			Point2d predicted = this.predict(pair.firstObject());
			sumError += new Line2d(pair.secondObject(),predicted).calculateLength() ;
		}
		return sumError;
	}

	@Override
	public Point2d predict(Point2d p) {
		p = p.clone();
		p.setX(p.getX() - middle.getX());
		p.setY(p.getY() - middle.getY());
		Line2d line = new Line2d(new Point2dImpl(0,0) ,p);
		float r = (float) line.calculateLength();
		float k0 = (float) this.matrixK.get(0, 0);
		float k1 = (float) this.matrixK.get(0, 1);
		float k2 = (float) this.matrixK.get(0, 2);
		float px = p.getX();
		float py = p.getY();
		Point2d ret = new Point2dImpl(
			px * k0 + px * k1 * r + px * k2 * r * r,
			py * k0 + py * k1 * r + py * k2 * r * r
		);
		ret.setX(ret.getX() + middle.getX());
		ret.setY(ret.getY() + middle.getY());
		return ret;
	}

	public void setMiddle(Point2dImpl middle) {
		this.middle = middle;
	}

	public void setKMatrix(Matrix kMatrix) {
		this.matrixK = kMatrix;
	}

	public Matrix getKMatrix() {
		// TODO Auto-generated method stub
		return this.matrixK;
	}
}
