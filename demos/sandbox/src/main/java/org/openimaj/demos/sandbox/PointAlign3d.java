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
package org.openimaj.demos.sandbox;

import org.apache.commons.math.geometry.Vector3D;
import org.openimaj.math.matrix.MatrixUtils;

import cern.jet.random.engine.MersenneTwister;

import Jama.Matrix;

public class PointAlign3d {
	/**
	 * Make some random 3d points
	 * @param numPoints
	 * @return
	 */
	static Vector3D[] makeRandomPoints(int numPoints) {
		Vector3D[] pts = new Vector3D[numPoints];

		for (int i=0; i<numPoints; i++) {
			double x = Math.random();
			double y = Math.random();
			double z = Math.random();
			pts[i] = new Vector3D(x, y, z);
		}

		return pts;
	}

	/**
	 * Transform a list of points
	 * 
	 * @param pts
	 * @param tf
	 * @return
	 */
	static Vector3D[] transformPoints(Vector3D[] pts, Matrix tf) {
		Vector3D[] tpts = new Vector3D[pts.length];

		for (int i=0; i<pts.length; i++) {
			tpts[i] = transform(pts[i], tf);
		}

		return tpts;
	}

	/**
	 * Transform a vector by a homogeneous transform matrix 
	 * 
	 * @param vec
	 * @param t
	 * @return
	 */
	static Vector3D transform(Vector3D vec, Matrix t) {
		Matrix x = new Matrix(new double[][] {{vec.getX()}, {vec.getY()}, {vec.getZ()}, {1}});
		Matrix tx = t.times(x);

		return new Vector3D(tx.get(0, 0) / tx.get(3, 0), tx.get(1, 0) / tx.get(3, 0), tx.get(2, 0) / tx.get(3, 0));
	}

	/**
	 * Make a transform matrix from 3d rotation and translation
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return
	 */
	static Matrix makeTransform(double alpha, double beta, double gamma, double tx, double ty, double tz) {
		double cosAlpha = Math.cos(alpha);
		double sinAlpha = Math.sin(alpha);
		double cosBeta = Math.cos(beta);
		double sinBeta = Math.sin(beta);
		double cosGamma = Math.cos(gamma);
		double sinGamma = Math.sin(gamma);

		Matrix t = new Matrix(new double[][] {
				{cosAlpha*cosBeta, cosAlpha*sinBeta*sinGamma - sinAlpha * cosGamma, cosAlpha*sinBeta*cosGamma + sinAlpha*sinGamma, tx},
				{sinAlpha*cosBeta, sinAlpha*sinBeta*sinGamma + cosAlpha*cosGamma, sinAlpha*sinBeta*cosGamma - cosAlpha*sinGamma, ty},
				{-sinBeta, cosBeta*sinGamma, cosBeta*cosGamma, tz},
				{0, 0, 0, 1}
		});

		return t;
	}

	static Matrix affineFit( Vector3D[] pc1, Vector3D[] pc2 ) {
		double[][] dpc1 = new double[pc1.length][3];
		double[][] dpc2 = new double[pc1.length][3];

		for (int i=0; i<pc1.length; i++) {
			dpc1[i] = new double[] { pc1[i].getX(), pc1[i].getY(), pc1[i].getZ() };
			dpc2[i] = new double[] { pc2[i].getX(), pc2[i].getY(), pc2[i].getZ() };
		}

		return affineFit(dpc1, dpc2);
	}

	/**
	 * Find the affine transform between pairs of matching points in n-dimensional
	 * space. The transform is the "best" possible in the least-squares sense.
	 * 
	 * @article {IOPORT.02109311,
	 * author       = {Sp\"ath, Helmuth},
	 * title        = {Fitting affine and orthogonal transformations between two sets of points.},
	 * year         = {2004},
	 * journal      = {Mathematical Communications},
	 * volume       = {9},
	 * number       = {1},
	 * issn         = {1331-0623},
	 * pages        = {27-34},
	 * publisher    = {Croatian Mathematical Society, Division Osijek, Osijek; Faculty of Electrical Engineering, University of Osijek, Osijek},
	 * abstract     = {Summary: Let two point sets $P$ and $Q$ be given in $\bbfR^n$. We determine a translation and an affine transformation or an isometry such that the image of $Q$ approximates $P$ as best as possible in the least squares sense.},
	 * identifier   = {02109311},
	 *}
	 *
	 * @param q
	 * @param p
	 * @return
	 */
	static Matrix affineFit( double[][] q, double[][] p ) {
		int dim = q[0].length;

		double[][] c = new double[dim+1][dim];
		for (int j=0; j<dim; j++) {
			for (int k=0; k<dim+1; k++) {
				for (int i=0; i<q.length; i++) {
					double qtk = 1;
					if (k<3) qtk = q[i][k];
					c[k][j] += qtk * p[i][j];
				}
			}
		}

		double [][] Q = new double[dim+1][dim+1];
		for (double[] qt : q) {
			for (int i=0; i<dim+1; i++) {
				for (int j=0; j<dim+1; j++) {
					double qti = 1;
					if (i<3) qti = qt[i];
					double qtj = 1;
					if (j<3) qtj = qt[j];
					Q[i][j] += qti * qtj;
				}
			}
		}

		Matrix Qm = new Matrix(Q);
		Matrix cm = new Matrix(c);
		Matrix a = Qm.solve(cm);

		Matrix t = Matrix.identity(dim+1, dim+1);
		t.setMatrix(0, dim-1, 0, dim, a.transpose());

		return t;
	}

	static Matrix rigidFit( Vector3D[] pc1, Vector3D[] pc2 ) {
		double[][] dpc1 = new double[pc1.length][3];
		double[][] dpc2 = new double[pc1.length][3];

		for (int i=0; i<pc1.length; i++) {
			dpc1[i] = new double[] { pc1[i].getX(), pc1[i].getY(), pc1[i].getZ() };
			dpc2[i] = new double[] { pc2[i].getX(), pc2[i].getY(), pc2[i].getZ() };
		}

		return rigidFit(dpc1, dpc2);
	}
	
	static Matrix rigidFit( double[][] q, double[][] p ) {
		int dim = q[0].length;
		int nitems = q.length;

		double[] qmean = new double[dim];
		double[] pmean = new double[dim];
		for (int j=0; j<nitems; j++) {
			for (int i=0; i<dim; i++) {
				qmean[i] += q[j][i];
				pmean[i] += p[j][i];
			}
		}
		for (int i=0; i<dim; i++) {
			qmean[i] /= nitems;
			pmean[i] /= nitems;
		}

		double[][] M = new double[dim][dim];

		for (int k=0; k<nitems; k++) {
			for (int j=0; j<dim; j++) {
				for (int i=0; i<dim; i++) {
					M[j][i] += (p[k][j] - pmean[j]) * (q[k][i] - qmean[i]); 
				}
			}
		}
		
		Matrix Mm = new Matrix(M);
		Matrix Qm = Mm.transpose().times(Mm);
		Matrix QmInvSqrt = MatrixUtils.invSqrtSym(Qm);
		Matrix R = Mm.times(QmInvSqrt);
		
		Matrix pm = new Matrix(new double[][] {pmean}).transpose();
		Matrix qm = new Matrix(new double[][] {qmean}).transpose();
		Matrix T = pm.minus(R.times(qm));
		
		Matrix tf = Matrix.identity(dim+1, dim+1);
		tf.setMatrix(0, dim-1, 0, dim-1, R);
		tf.setMatrix(0, dim-1, dim, dim, T);
		
		return tf;
	}

	/**
	 * Test code. Generates random transforms and attempts
	 * to recover them.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int numPoints = 100;
		MersenneTwister mt = new MersenneTwister();

		for (int i=0; i<100; i++) {
			//make some random points
			Vector3D[] pointCloud1 = makeRandomPoints(numPoints);

			//generate a random transform 
			double roll = mt.uniform(-Math.PI, Math.PI);
			double pitch = mt.uniform(-Math.PI, Math.PI);
			double yaw = mt.uniform(-Math.PI, Math.PI);
			double tx = mt.uniform(-100, 100);
			double ty = mt.uniform(-100, 100);
			double tz = mt.uniform(-100, 100);
			Matrix t = makeTransform(roll, pitch, yaw, tx, ty, tz);

			//make a second set of points by transforming the first set
			Vector3D[] pointCloud2 = transformPoints(pointCloud1, t);

			//predict the transform that maps from the first set to the second
			//Matrix predicted = affineFit(pointCloud1, pointCloud2);
			Matrix predicted = rigidFit(pointCloud1, pointCloud2);

			//check that the original matrix and the predicted one are the same
			for (int r=0; r<4; r++) 
				for (int c=0; c<4; c++)
					if (Math.abs(t.get(r, c) - predicted.get(r, c)) > 0.00001) 
						System.err.println("Error");
		}
	}
}
