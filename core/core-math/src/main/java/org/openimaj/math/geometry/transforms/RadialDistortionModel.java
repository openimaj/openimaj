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
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Implementation of a Homogeneous Radial Distortion model - a transform that
 * corrects for the radial distortion (fish eye effect) caused by some camera
 * 
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
	
	Matrix radialDistortion;
	private Matrix matixK;
	
	/*
	 * SVD estimation of least-squares solution of radial distortion variables k0, k1 and k2
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#estimate(java.util.List)
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		if(data.size() < this.numItemsToEstimate()){
			return;
		}
		
		// The first and the last point are used to decide where the line should be
		Line2d modelLine = new Line2d(data.get(0).firstObject(),data.get(data.size()-1).firstObject());
		
		
		Matrix A, W=null;
		int i, j;
		A = new Matrix((data.size()-2)*2, 4);
		
		for ( i=1, j=0; i<data.size()-1; i++, j+=2 ) {
			Point2d p = data.get(i).firstObject();
			
			Line2d radialLine = new Line2d(p,new Point2dImpl(0,0));
			
			IntersectionResult intersect = modelLine.getIntersection(radialLine);
			
			Point2d pprime = intersect.intersectionPoint;
			double radius = radialLine.calculateLength();
			
			A.set(j, 0, p.getX());
			A.set(j, 1, p.getX() * radius);
			A.set(j, 2, p.getX() * radius * radius);
			A.set(j, 3, -pprime.getX());
			
			A.set(j, 0, p.getY());
			A.set(j, 1, p.getY() * radius);
			A.set(j, 2, p.getY() * radius * radius);
			A.set(j, 3, -pprime.getY());
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
		
		matixK = new Matrix(1,4);
		matixK.set(0,0, W.get(0,0)/W.get(3,0));
		matixK.set(0,1, W.get(1,0)/W.get(3,0));
		matixK.set(0,2, W.get(2,0)/W.get(3,0));
		matixK.set(1,0, W.get(3,0)/W.get(3,0));
		
	}
	

	@Override
	// FIXME: Implement this
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int numItemsToEstimate() {
		return 3;
	}
	
	@Override
	// FIXME: implement this
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> alldata)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	// FIXME: implement this
	public Point2d predict(Point2d data) {
		throw new UnsupportedOperationException();
	}
}
