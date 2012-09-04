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

import Jama.Matrix;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

public class PointAlign3d {
	/**
	 * Make some random 3d points
	 * 
	 * @param numPoints
	 * @return
	 */
	static Vector3D[] makeRandomPoints(int numPoints) {
		final Vector3D[] pts = new Vector3D[numPoints];

		for (int i = 0; i < numPoints; i++) {
			final double x = Math.random();
			final double y = Math.random();
			final double z = Math.random();
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
		final Vector3D[] tpts = new Vector3D[pts.length];

		for (int i = 0; i < pts.length; i++) {
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
		final Matrix x = new Matrix(new double[][] { { vec.getX() }, { vec.getY() }, { vec.getZ() }, { 1 } });
		final Matrix tx = t.times(x);

		return new Vector3D(tx.get(0, 0) / tx.get(3, 0), tx.get(1, 0) / tx.get(3, 0), tx.get(2, 0) / tx.get(3, 0));
	}

	/**
	 * Make a transform matrix from 3d rotation and translation
	 * 
	 * @param alpha
	 * @param beta
	 * @param gamma
	 * @param tx
	 * @param ty
	 * @param tz
	 * @return
	 */
	static Matrix makeTransform(double alpha, double beta, double gamma, double tx, double ty, double tz) {
		final double cosAlpha = Math.cos(alpha);
		final double sinAlpha = Math.sin(alpha);
		final double cosBeta = Math.cos(beta);
		final double sinBeta = Math.sin(beta);
		final double cosGamma = Math.cos(gamma);
		final double sinGamma = Math.sin(gamma);

		final Matrix t = new Matrix(new double[][] {
				{ cosAlpha * cosBeta, cosAlpha * sinBeta * sinGamma - sinAlpha * cosGamma, cosAlpha * sinBeta * cosGamma
						+ sinAlpha * sinGamma, tx },
				{ sinAlpha * cosBeta, sinAlpha * sinBeta * sinGamma + cosAlpha * cosGamma, sinAlpha * sinBeta * cosGamma
						- cosAlpha * sinGamma, ty },
				{ -sinBeta, cosBeta * sinGamma, cosBeta * cosGamma, tz },
				{ 0, 0, 0, 1 }
		});

		return t;
	}

	static Matrix affineFit(Vector3D[] pc1, Vector3D[] pc2) {
		final double[][] dpc1 = new double[pc1.length][3];
		final double[][] dpc2 = new double[pc1.length][3];

		for (int i = 0; i < pc1.length; i++) {
			dpc1[i] = new double[] { pc1[i].getX(), pc1[i].getY(), pc1[i].getZ() };
			dpc2[i] = new double[] { pc2[i].getX(), pc2[i].getY(), pc2[i].getZ() };
		}

		return affineFit(dpc1, dpc2);
	}

	/**
	 * Find the affine transform between pairs of matching points in
	 * n-dimensional space. The transform is the "best" possible in the
	 * least-squares sense.
	 * 
	 * @article {IOPORT.02109311, author = {Sp\"ath, Helmuth}, title = {Fitting
	 *          affine and orthogonal transformations between two sets of
	 *          points.}, year = {2004}, journal = {Mathematical
	 *          Communications}, volume = {9}, number = {1}, issn = {1331-0623},
	 *          pages = {27-34}, publisher = {Croatian Mathematical Society,
	 *          Division Osijek, Osijek; Faculty of Electrical Engineering,
	 *          University of Osijek, Osijek}, abstract = {Summary: Let two
	 *          point sets $P$ and $Q$ be given in $\bbfR^n$. We determine a
	 *          translation and an affine transformation or an isometry such
	 *          that the image of $Q$ approximates $P$ as best as possible in
	 *          the least squares sense.}, identifier = {02109311}, }
	 * 
	 * @param q
	 * @param p
	 * @return
	 */
	static Matrix affineFit(double[][] q, double[][] p) {
		final int dim = q[0].length;

		final double[][] c = new double[dim + 1][dim];
		for (int j = 0; j < dim; j++) {
			for (int k = 0; k < dim + 1; k++) {
				for (int i = 0; i < q.length; i++) {
					double qtk = 1;
					if (k < 3)
						qtk = q[i][k];
					c[k][j] += qtk * p[i][j];
				}
			}
		}

		final double[][] Q = new double[dim + 1][dim + 1];
		for (final double[] qt : q) {
			for (int i = 0; i < dim + 1; i++) {
				for (int j = 0; j < dim + 1; j++) {
					double qti = 1;
					if (i < 3)
						qti = qt[i];
					double qtj = 1;
					if (j < 3)
						qtj = qt[j];
					Q[i][j] += qti * qtj;
				}
			}
		}

		final Matrix Qm = new Matrix(Q);
		final Matrix cm = new Matrix(c);
		final Matrix a = Qm.solve(cm);

		final Matrix t = Matrix.identity(dim + 1, dim + 1);
		t.setMatrix(0, dim - 1, 0, dim, a.transpose());

		return t;
	}

	static Matrix rigidFit(Vector3D[] pc1, Vector3D[] pc2) {
		final double[][] dpc1 = new double[pc1.length][3];
		final double[][] dpc2 = new double[pc1.length][3];

		for (int i = 0; i < pc1.length; i++) {
			dpc1[i] = new double[] { pc1[i].getX(), pc1[i].getY(), pc1[i].getZ() };
			dpc2[i] = new double[] { pc2[i].getX(), pc2[i].getY(), pc2[i].getZ() };
		}

		return rigidFit(dpc1, dpc2);
	}

	static Matrix rigidFit(double[][] q, double[][] p) {
		final int dim = q[0].length;
		final int nitems = q.length;

		final double[] qmean = new double[dim];
		final double[] pmean = new double[dim];
		for (int j = 0; j < nitems; j++) {
			for (int i = 0; i < dim; i++) {
				qmean[i] += q[j][i];
				pmean[i] += p[j][i];
			}
		}
		for (int i = 0; i < dim; i++) {
			qmean[i] /= nitems;
			pmean[i] /= nitems;
		}

		final double[][] M = new double[dim][dim];

		for (int k = 0; k < nitems; k++) {
			for (int j = 0; j < dim; j++) {
				for (int i = 0; i < dim; i++) {
					M[j][i] += (p[k][j] - pmean[j]) * (q[k][i] - qmean[i]);
				}
			}
		}

		final Matrix Mm = new Matrix(M);
		final Matrix Qm = Mm.transpose().times(Mm);
		final Matrix QmInvSqrt = MatrixUtils.invSqrtSym(Qm);
		final Matrix R = Mm.times(QmInvSqrt);

		final Matrix pm = new Matrix(new double[][] { pmean }).transpose();
		final Matrix qm = new Matrix(new double[][] { qmean }).transpose();
		final Matrix T = pm.minus(R.times(qm));

		final Matrix tf = Matrix.identity(dim + 1, dim + 1);
		tf.setMatrix(0, dim - 1, 0, dim - 1, R);
		tf.setMatrix(0, dim - 1, dim, dim, T);

		return tf;
	}

	/**
	 * Test code. Generates random transforms and attempts to recover them.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final int numPoints = 100;
		final MersenneTwister mt = new MersenneTwister();
		final Uniform u = new Uniform(mt);

		for (int i = 0; i < 100; i++) {
			// make some random points
			final Vector3D[] pointCloud1 = makeRandomPoints(numPoints);

			// generate a random transform
			final double roll = u.nextDoubleFromTo(-Math.PI, Math.PI);
			final double pitch = u.nextDoubleFromTo(-Math.PI, Math.PI);
			final double yaw = u.nextDoubleFromTo(-Math.PI, Math.PI);
			final double tx = u.nextDoubleFromTo(-100, 100);
			final double ty = u.nextDoubleFromTo(-100, 100);
			final double tz = u.nextDoubleFromTo(-100, 100);
			final Matrix t = makeTransform(roll, pitch, yaw, tx, ty, tz);

			// make a second set of points by transforming the first set
			final Vector3D[] pointCloud2 = transformPoints(pointCloud1, t);

			// predict the transform that maps from the first set to the second
			// Matrix predicted = affineFit(pointCloud1, pointCloud2);
			final Matrix predicted = rigidFit(pointCloud1, pointCloud2);

			// check that the original matrix and the predicted one are the same
			for (int r = 0; r < 4; r++)
				for (int c = 0; c < 4; c++)
					if (Math.abs(t.get(r, c) - predicted.get(r, c)) > 0.00001)
						System.err.println("Error");
		}
	}
}
