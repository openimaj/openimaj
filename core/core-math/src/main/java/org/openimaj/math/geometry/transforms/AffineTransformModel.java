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
 * Concrete implementation of a model of an Affine transform.
 * Capable of least-squares estimate of model parameters using 
 * the SVD.
 * 
 * @author Jonathon Hare
 *
 */
public class AffineTransformModel implements Model<Point2d, Point2d>, MatrixTransformProvider {
	protected Matrix transform;
	protected float tol;

	/**
	 * Create an AffineTransformModel with a given tolerance for validation
	 * @param tolerance value specifying how close (Euclidean distance) a point
	 * must be from its predicted position to successfully validate.
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
		this.transform = TransformUtilities.affineMatrix(data);
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
