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

import org.openimaj.math.geometry.point.*;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import Jama.*;

/**
 * Concrete implementation of a model of an affine transform.
 * Capable of least-squares estimate of model parameters using 
 * the svd method.
 * 
 * @author Jonathon Hare
 *
 */
public class AffineTransformModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	protected Matrix transform;
	protected float tol;

	/**
	 * Create an AffineTransformModel with a given tolerence for validation
	 * @param tolerance value specifying how close (euclidean distance) a point
	 * must be from its predicted position to sucessfully validate.
	 */
	public AffineTransformModel(float tolerance)
	{
		tol = tolerance;
		transform = new Matrix(3,3);

		transform.set(2, 0, 0);
		transform.set(2, 1, 0);
		transform.set(2, 2, 1);
	}

	@Override
	public AffineTransformModel clone() {
		AffineTransformModel atm = new AffineTransformModel(tol);
		atm.transform = transform.copy();
		return atm;
	}

	@Override
	public Matrix getTransform() {
		return transform;
	}

	/* 
	 * SVD least-squares estimation of affine transform matrix for a set of 2d points
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#estimate(java.util.List)
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Point2d, Point2d>> data) {
		Matrix A, W=null;
		int i, j;

		//Solve Ax=0
		A = new Matrix(data.size()*2, 7);

		for ( i=0, j=0; i<data.size(); i++, j+=2) {
			float x1 = data.get(i).firstObject().getX();
			float y1 = data.get(i).firstObject().getY();
			float x2 = data.get(i).secondObject().getX();
			float y2 = data.get(i).secondObject().getY();

			A.set(j, 0, x1);	
			A.set(j, 1, y1);	
			A.set(j, 2, 1);
			A.set(j, 3, 0);
			A.set(j, 4, 0);
			A.set(j, 5, 0);
			A.set(j, 6, -x2);

			A.set(j+1, 0, 0);	
			A.set(j+1, 1, 0);	
			A.set(j+1, 2, 0);
			A.set(j+1, 3, x1);
			A.set(j+1, 4, y1);
			A.set(j+1, 5, 1);
			A.set(j+1, 6, -y2);
		}

		//This is a hack to use MJT instead -- jama's svd is broken
		try {
			no.uib.cipr.matrix.DenseMatrix mjtA = new no.uib.cipr.matrix.DenseMatrix(A.getArray());
			no.uib.cipr.matrix.SVD svd = no.uib.cipr.matrix.SVD.factorize(mjtA);

			W = new Matrix(svd.getVt().numRows(), 1);

			for (i=0; i<svd.getVt().numRows(); i++) {
				W.set(i, 0, svd.getVt().get(6, i)); //do transpose here too!
			}	
		} catch (no.uib.cipr.matrix.NotConvergedException ex) {
			System.out.println(ex);
			return;
		}
		//End hack

		//build matrix
		transform.set(0,0, W.get(0,0) / W.get(6,0));
		transform.set(0,1, W.get(1,0) / W.get(6,0));
		transform.set(0,2, W.get(2,0) / W.get(6,0));

		transform.set(1,0, W.get(3,0) / W.get(6,0));
		transform.set(1,1, W.get(4,0) / W.get(6,0));
		transform.set(1,2, W.get(5,0) / W.get(6,0));
	}

	/*
	 * Validation based on euclidean distance between actual and predicted points. 
	 * Success if distance is less than threshold
	 * @see uk.ac.soton.ecs.iam.jsh2.util.statistics.Model#validate(uk.ac.soton.ecs.iam.jsh2.util.IPair)
	 */
	@Override
	public boolean validate(IndependentPair<Point2d, Point2d> data) {
		Point2d p2_est = data.firstObject().transform(transform);

		float dx = data.secondObject().getX() - (float)p2_est.getX();
		float dy = data.secondObject().getY() - (float)p2_est.getY();

		float dist = (dx*dx + dy*dy);
		
		if (dist <= tol*tol) return true;

		return false;
	}

	@Override
	public Point2d predict(Point2d p) {
		return p.transform(transform);
	}

	@Override
	public int numItemsToEstimate() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.openimaj.math.model.Model#calculateError(java.util.List)
	 */
	@Override
	public double calculateError(List<? extends IndependentPair<Point2d, Point2d>> alldata)
	{
		double error=0;

		for (IndependentPair<Point2d, Point2d> data : alldata) {
			Point2d p2_est = data.firstObject().transform(transform);

			double dx = data.secondObject().getX() - p2_est.getX();
			double dy = data.secondObject().getY() - p2_est.getY();

			error += (dx*dx + dy*dy);
		}
		
		return error;
	}

	@Override
	public String toString() {
		String str = "";
		double [][] mat = transform.getArray();
		for (double [] r : mat) {
			for (double v : r) {
				str += " "+v;
			}
			str += "\n";
		}
		return str;
	}
}
