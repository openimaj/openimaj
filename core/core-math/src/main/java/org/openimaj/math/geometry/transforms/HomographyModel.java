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
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * Implementation of a Homogeneous Homography model - a transform that
 * models the relationship between planes under projective constraints (9 D.o.F)
 * 
 * @author Jonathon Hare
 *
 */
public class HomographyModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	protected Matrix homography;
	protected float tol;
	
	/**
	 * Create an HomographyModel with a given tolerence for validation
	 * @param tolerance value specifying how close (euclidean distance) a point
	 * must be from its predicted position to sucessfully validate.
	 */
	public HomographyModel(float tolerance)
	{
		tol = tolerance;
		homography = new Matrix(3,3);
	}
	
	@Override
	public HomographyModel clone() {
		HomographyModel hm = new HomographyModel(tol);
		hm.homography = homography.copy();
		return hm;
	}
	
	@Override
	public Matrix getTransform() {
		return homography;
	}
	
	/*
	 * SVD estimation of least-squares solution of 3D homogeneous homography
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#estimate(java.util.List)
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		Matrix A, W=null;
		int i, j;
		
		A = new Matrix(data.size()*2, 9);
		
		for ( i=0, j=0; i<data.size(); i++, j+=2 ) {
			float x1 = data.get(i).firstObject().getX();
			float y1 = data.get(i).firstObject().getY();
			float x2 = data.get(i).secondObject().getX();
			float y2 = data.get(i).secondObject().getY();
			
			A.set(j, 0, x1);			//x
			A.set(j, 1, y1);			//y
			A.set(j, 2, 1);				//1
			A.set(j, 3, 0);				//0
			A.set(j, 4, 0);				//0
			A.set(j, 5, 0);				//0
			A.set(j, 6, -(x2*x1));		//-x'*x
			A.set(j, 7, -(x2*y1));		//-x'*y
			A.set(j, 8, -(x2));			//-x'
			
			A.set(j+1, 0, 0);			//0
			A.set(j+1, 1, 0);			//0
			A.set(j+1, 2, 0);			//0
			A.set(j+1, 3, x1);			//x
			A.set(j+1, 4, y1);			//y
			A.set(j+1, 5, 1);			//1
			A.set(j+1, 6, -(y2*x1));	//-y'*x
			A.set(j+1, 7, -(y2*y1));	//-y'*y
			A.set(j+1, 8, -(y2));		//-y'
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
				W.set(i, 0, svd.getVt().get(8, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return;
		}
		//End hack
		
		homography.set(0,0, W.get(0,0));
		homography.set(0,1, W.get(1,0));
		homography.set(0,2, W.get(2,0));
		homography.set(1,0, W.get(3,0));
		homography.set(1,1, W.get(4,0));
		homography.set(1,2, W.get(5,0));
		homography.set(2,0, W.get(6,0));
		homography.set(2,1, W.get(7,0));
		homography.set(2,2, W.get(8,0));
		
		//it probably makes sense to rescale the matrix here by 1 / tf[2][2], unless tf[2][2] == 0
		if (Math.abs(homography.get(2,2)) > 0.001) {
			double tmp = homography.get(2,2);
			
			for (i=0; i<3; i++) {
				for (j=0; j<3; j++) {
					if (Math.abs(homography.get(i, j)) < 10e-10) {
						homography.set(i, j, 0.0);
					} else {
						homography.set(i, j, homography.get(i, j) / tmp);
					}
				}
			}
		}
	}
	
	/*
	 * Validation based on euclidean distance between actual and predicted points. 
	 * Success if distance is less than threshold
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#validate(uk.ac.soton.ecs.iam.jsh2.util.IPair)
	 */
	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		Matrix p1 = new Matrix(3,1);
		p1.set(0,0, data.firstObject().getX());
		p1.set(1,0, data.firstObject().getY());
		p1.set(2,0, 1);
		
		Matrix p2_est = homography.times(p1);
		
		float dx = data.secondObject().getX() - (float)p2_est.get(0,0);
		float dy = data.secondObject().getY() - (float)p2_est.get(1,0);
		
		float dist = (dx*dx + dy*dy);
		
		if (dist <= tol*tol) return true;
		
		return false;
	}
	
	@Override
	public int numItemsToEstimate() {
		return 4;
	}
	
	/*
	 * Relative error is sum of squared euclidean distance between actual and predicted positions
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#calculateError(java.util.List)
	 */
	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> alldata)
	{
		double error=0;
		
		for (IndependentPair<Point2d, Point2d> data : alldata) {
			Matrix p1 = new Matrix(3,1);
			p1.set(0,0, data.firstObject().getX());
			p1.set(1,0, data.firstObject().getY());
			p1.set(2,0, 1);
		
			Matrix p2_est = homography.times(p1);
		
			double dx = data.secondObject().getX() - p2_est.get(0,0);
			double dy = data.secondObject().getY() - p2_est.get(1,0);
		
			error += (dx*dx + dy*dy);
		}
		
		return error;
	}

	@Override
	public Point2d predict(Point2d data) {
		Matrix p1 = new Matrix(3,1);
		p1.set(0,0, data.getX());
		p1.set(1,0, data.getY());
		p1.set(2,0, 1);
	
		Matrix p2_est = homography.times(p1);
	
		Point2d out = new Point2dImpl((float)p2_est.get(0,0), (float)p2_est.get(1,0));
		
		return out;
	}
}
