/**
 * FaceTracker Licence
 * -------------------
 * (Academic, non-commercial, not-for-profit licence)
 *
 * Copyright (c) 2010 Jason Mora Saragih
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * The software is provided under the terms of this licence stricly for
 *       academic, non-commercial, not-for-profit purposes.
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions (licence) and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions (licence) and the following disclaimer
 *       in the documentation and/or other materials provided with the
 *       distribution.
 *     * The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *     * As this software depends on other libraries, the user must adhere to and
 *       keep in place any licencing terms of those libraries.
 *     * Any publications arising from the use of this software, including but
 *       not limited to academic journal and conference publications, technical
 *       reports and manuals, must cite the following work:
 *
 *       J. M. Saragih, S. Lucey, and J. F. Cohn. Face Alignment through Subspace
 *       Constrained Mean-Shifts. International Journal of Computer Vision
 *       (ICCV), September, 2009.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.openimaj.math.matrix.MatrixUtils;

import com.jsaragih.CLM.SimTData;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * 3D Point Distribution Model
 *
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PDM {
	static { Tracker.init(); }
	
	/** basis of variation */
	public Matrix _V;

	/** vector of eigenvalues (row vector) */
	public Matrix _E;

	/** mean 3D shape vector [x1,..,xn,y1,...yn] */
	public Matrix _M;

	private Matrix S_, R_, P_, Px_, Py_, Pz_, R1_, R2_, R3_;

	/**
	 * Returns a copy of this PDM.
	 * 
	 * @return A copy of this PDM.
	 */
	public PDM copy() {
		PDM p = new PDM();
		p._V = _V.copy();
		p._E = _E.copy();
		p._M = _M.copy();
		p.S_ = S_.copy();
		p.R_ = R_.copy();
		p.P_ = P_.copy();
		p.Px_ = Px_.copy();
		p.Py_ = Py_.copy();
		p.Pz_ = Pz_.copy();
		p.R1_ = R1_.copy();
		p.R2_ = R2_.copy();
		p.R3_ = R3_.copy();
		return p;
	}

	void addOrthRow(Matrix R) {
		assert ((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));

		R.set(2, 0, R.get(0, 1) * R.get(1, 2) - R.get(0, 2) * R.get(1, 1));
		R.set(2, 1, R.get(0, 2) * R.get(1, 0) - R.get(0, 0) * R.get(1, 2));
		R.set(2, 2, R.get(0, 0) * R.get(1, 1) - R.get(0, 1) * R.get(1, 0));
	}

	void metricUpgrade(Matrix R) {
		assert ((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));
		SingularValueDecomposition svd = R.svd();

		Matrix X = svd.getU().times(svd.getV().transpose());
		Matrix W = Matrix.identity(3, 3);
		W.set(2, 2, X.det());

		R.setMatrix(0, 3 - 1, 0, 3 - 1,
				svd.getU().times(W).times(svd.getV().transpose()));
	}

	Matrix euler2Rot(final double pitch, final double yaw, final double roll) {
		return euler2Rot(pitch, yaw, roll, true);
	}

	Matrix euler2Rot(final double pitch, final double yaw, final double roll,
			boolean full) {
		Matrix R;
		if (full) {
			R = new Matrix(3, 3);
		} else {
			R = new Matrix(2, 3);
		}

		double sina = Math.sin(pitch), sinb = Math.sin(yaw), sinc = Math
				.sin(roll);
		double cosa = Math.cos(pitch), cosb = Math.cos(yaw), cosc = Math
				.cos(roll);
		R.set(0, 0, cosb * cosc);
		R.set(0, 1, -cosb * sinc);
		R.set(0, 2, sinb);
		R.set(1, 0, cosa * sinc + sina * sinb * cosc);
		R.set(1, 1, cosa * cosc - sina * sinb * sinc);
		R.set(1, 2, -sina * cosb);

		if (full)
			addOrthRow(R);

		return R;
	}

	Matrix euler2Rot(Matrix p) {
		return euler2Rot(p, true);
	}

	Matrix euler2Rot(Matrix p, boolean full) {
		assert ((p.getRowDimension() == 6) && (p.getColumnDimension() == 1));
		return euler2Rot(p.get(1, 0), p.get(2, 0), p.get(3, 0), full);
	}

	double[] rot2Euler(Matrix R) {
		assert ((R.getRowDimension() == 3) && (R.getColumnDimension() == 3));
		double[] q = new double[4];
		q[0] = Math.sqrt(1 + R.get(0, 0) + R.get(1, 1) + R.get(2, 2)) / 2;
		q[1] = (R.get(2, 1) - R.get(1, 2)) / (4 * q[0]);
		q[2] = (R.get(0, 2) - R.get(2, 0)) / (4 * q[0]);
		q[3] = (R.get(1, 0) - R.get(0, 1)) / (4 * q[0]);
		double yaw = Math.asin(2 * (q[0] * q[2] + q[1] * q[3]));
		double pitch = Math.atan2(2 * (q[0] * q[1] - q[2] * q[3]), q[0] * q[0]
				- q[1] * q[1] - q[2] * q[2] + q[3] * q[3]);
		double roll = Math.atan2(2 * (q[0] * q[3] - q[1] * q[2]), q[0] * q[0]
				+ q[1] * q[1] - q[2] * q[2] - q[3] * q[3]);
		return new double[] { pitch, roll, yaw };
	}

	void rot2Euler(Matrix R, Matrix p) {
		assert ((p.getRowDimension() == 6) && (p.getColumnDimension() == 1));
		double[] pry = rot2Euler(R);

		p.set(1, 0, pry[0]);
		p.set(2, 0, pry[2]);
		p.set(3, 0, pry[1]);
	}

	class AlignmentParams {
		double scale;
		double pitch;
		double yaw;
		double roll;
		double x;
		double y;
	}

	void align3Dto2DShapes(AlignmentParams ap, Matrix s2D, Matrix s3D) {
		assert ((s2D.getColumnDimension() == 1)
				&& (s3D.getRowDimension() == 3 * (s2D.getRowDimension() / 2)) && (s3D
				.getColumnDimension() == 1));

		final int n = s2D.getRowDimension() / 2;
		double[] t2 = new double[2];
		double[] t3 = new double[3];

		Matrix X = MatrixUtils.reshape(s2D, 2).transpose();
		Matrix S = MatrixUtils.reshape(s3D, 3).transpose();

		for (int i = 0; i < 2; i++) {
			t2[i] = MatrixUtils.sumColumn(X, i) / n;
			MatrixUtils.incrColumn(X, i, -t2[i]);
		}

		for (int i = 0; i < 3; i++) {
			t3[i] = MatrixUtils.sumColumn(S, i) / n;
			MatrixUtils.incrColumn(S, i, -t3[i]);
		}

		Matrix M = ((S.transpose().times(S)).inverse()).times(S.transpose())
				.times(X);

		Matrix MtM = M.transpose().times(M);

		SingularValueDecomposition svd = MtM.svd();
		Matrix svals = svd.getS();
		svals.set(0, 0, 1.0 / Math.sqrt(svals.get(0, 0)));
		svals.set(1, 1, 1.0 / Math.sqrt(svals.get(1, 1)));

		Matrix T = new Matrix(3, 3);
		T.setMatrix(
				0,
				2 - 1,
				0,
				3 - 1,
				svd.getU().times(svals).times(svd.getV().transpose())
						.times(M.transpose()));

		ap.scale = 0;
		for (int r = 0; r < 2; r++)
			for (int c = 0; c < 3; c++)
				ap.scale += T.get(r, c) * M.get(c, r);
		ap.scale *= 0.5;

		addOrthRow(T);

		double[] pyr = rot2Euler(T);
		ap.pitch = pyr[0];
		ap.roll = pyr[1];
		ap.yaw = pyr[2];

		T = T.times(ap.scale);

		ap.x = t2[0]
				- (T.get(0, 0) * t3[0] + T.get(0, 1) * t3[1] + T.get(0, 2)
						* t3[2]);
		ap.y = t2[1]
				- (T.get(1, 0) * t3[0] + T.get(1, 1) * t3[1] + T.get(1, 2)
						* t3[2]);
	}

	void clamp(Matrix p, double c) {
		assert ((p.getRowDimension() == _E.getColumnDimension()) && (p
				.getColumnDimension() == 1));

		for (int i = 0; i < p.getRowDimension(); i++) {
			double v = c * Math.sqrt(_E.get(0, i));
			double p1 = p.get(i, 0);

			if (Math.abs(p1) > v) {
				if (p1 > 0.0) {
					p1 = v;
				} else {
					p1 = -v;
				}
			}
		}
	}

	Matrix calcShape3D(Matrix plocal) {
		assert ((plocal.getRowDimension() == _E.getColumnDimension()) && (plocal
				.getColumnDimension() == 1));

		Matrix s = _M.plus(_V.times(plocal));

		return s;
	}

	/**
	 * Calculate Shape 2D
	 * 
	 * @param s
	 * @param plocal
	 * @param pglobl
	 */
	public void calcShape2D(Matrix s, Matrix plocal, Matrix pglobl) {
		assert ((plocal.getRowDimension() == _E.getColumnDimension()) && (plocal
				.getColumnDimension() == 1));
		assert ((pglobl.getRowDimension() == 6) && (pglobl.getColumnDimension() == 1));

		int n = _M.getRowDimension() / 3;
		double a = pglobl.get(0, 0);
		double x = pglobl.get(4, 0);
		double y = pglobl.get(5, 0);

		R_ = euler2Rot(pglobl);

		S_ = _M.plus(_V.times(plocal));

		for (int i = 0; i < n; i++) {
			s.set(i,
					0,
					a
							* (R_.get(0, 0) * S_.get(i, 0) + R_.get(0, 1)
									* S_.get(i + n, 0) + R_.get(0, 2)
									* S_.get(i + n * 2, 0)) + x);
			s.set(i + n,
					0,
					a
							* (R_.get(1, 0) * S_.get(i, 0) + R_.get(1, 1)
									* S_.get(i + n, 0) + R_.get(1, 2)
									* S_.get(i + n * 2, 0)) + y);
		}
	}

	/**
	 * Calculate the PDM parameters
	 * 
	 * @param s
	 * @param plocal
	 * @param pglobl
	 */
	public void calcParams(Matrix s, Matrix plocal, Matrix pglobl) {
		assert ((s.getRowDimension() == 2 * (_M.getRowDimension() / 3)) && (s
				.getColumnDimension() == 1));

		int n = _M.getRowDimension() / 3;

		Matrix R = new Matrix(3, 3);
		Matrix t = new Matrix(3, 1);
		Matrix p = new Matrix(_V.getColumnDimension(), 1);

		MatrixUtils.zero(plocal);

		AlignmentParams ap = new AlignmentParams();

		for (int iter = 0; iter < 100; iter++) {
			S_ = calcShape3D(plocal);

			align3Dto2DShapes(ap, s, S_);

			R = euler2Rot(ap.pitch, ap.yaw, ap.roll);

			Matrix r = new Matrix(new double[][] { R.getArray()[2] });

			Matrix S = MatrixUtils.reshape(S_, 3).transpose();

			Matrix z = (S.times(r.transpose())).times(ap.scale);
			double si = 1.0 / ap.scale;

			double Tx = -si * (R.get(0, 0) * ap.x + R.get(1, 0) * ap.y);
			double Ty = -si * (R.get(0, 1) * ap.x + R.get(1, 1) * ap.y);
			double Tz = -si * (R.get(0, 2) * ap.x + R.get(1, 2) * ap.y);

			for (int j = 0; j < n; j++) {
				t.set(0, 0, s.get(j, 0));
				t.set(1, 0, s.get(j + n, 0));
				t.set(2, 0, z.get(j, 0));

				S_.set(j, 0, si * dotCol(t, R, 0) + Tx);
				S_.set(j + n, 0, si * dotCol(t, R, 1) + Ty);
				S_.set(j + n * 2, 0, si * dotCol(t, R, 2) + Tz);
			}

			plocal.setMatrix(0, p.getRowDimension() - 1, 0, 1 - 1, _V
					.transpose().times(S_.minus(_M)));

			if (iter > 0) {
				double norm = 0;
				for (int i = 0; i < plocal.getRowDimension(); i++) {
					double diff = plocal.get(i, 0) - p.get(i, 0);
					norm += Math.abs(diff * diff);
				}
				norm = Math.sqrt(norm);

				if (norm < 1.0e-5)
					break;
			}

			p.setMatrix(0, p.getRowDimension() - 1, 0, 1 - 1, plocal);
		}

		pglobl.set(0, 0, ap.scale);
		pglobl.set(1, 0, ap.pitch);
		pglobl.set(2, 0, ap.yaw);
		pglobl.set(3, 0, ap.roll);
		pglobl.set(4, 0, ap.x);
		pglobl.set(5, 0, ap.y);

		return;
	}

	private double dotCol(Matrix colvec, Matrix m, int col) {
		final int rows = colvec.getRowDimension();

		final double[][] colvec_arr = colvec.getArray();
		final double[][] m_arr = m.getArray();

		double dp = 0;
		for (int i = 0; i < rows; i++)
			dp += colvec_arr[i][0] * m_arr[i][col];

		return dp;
	}

	/**
	 * Initialise the identify face parameters
	 * @param plocal
	 * @param pglobl
	 */
	public void identity(Matrix plocal, Matrix pglobl) {
		MatrixUtils.zero(plocal);

		MatrixUtils.zero(pglobl);
		pglobl.set(0, 0, 1);
	}

	void calcRigidJacob(Matrix plocal, Matrix pglobl, Matrix Jacob) {
		final int n = _M.getRowDimension() / 3;
		final int m = _V.getColumnDimension();

		assert ((plocal.getRowDimension() == m)
				&& (plocal.getColumnDimension() == 1)
				&& (pglobl.getRowDimension() == 6)
				&& (pglobl.getColumnDimension() == 1)
				&& (Jacob.getRowDimension() == 2 * n) && (Jacob
				.getColumnDimension() == 6));

		Matrix Rx = new Matrix(new double[][] { { 0, 0, 0 }, { 0, 0, -1 },
				{ 0, 1, 0 } });
		Matrix Ry = new Matrix(new double[][] { { 0, 0, 1 }, { 0, 0, 0 },
				{ -1, 0, 0 } });
		Matrix Rz = new Matrix(new double[][] { { 0, -1, 0 }, { 1, 0, 0 },
				{ 0, 0, 0 } });

		double s = pglobl.get(0, 0);

		S_ = calcShape3D(plocal);

		R_ = euler2Rot(pglobl);

		P_ = R_.getMatrix(0, 2 - 1, 0, 3 - 1).times(s);
		Px_ = P_.times(Rx);
		Py_ = P_.times(Ry);
		Pz_ = P_.times(Rz);

		final double[][] px = Px_.getArray();
		final double[][] py = Py_.getArray();
		final double[][] pz = Pz_.getArray();
		final double[][] r = R_.getArray();
		final double[][] J = Jacob.getArray();

		for (int i = 0; i < n; i++) {
			double X = S_.get(i, 0);
			double Y = S_.get(i + n, 0);
			double Z = S_.get(i + n * 2, 0);
			J[i][0] = r[0][0] * X + r[0][1] * Y + r[0][2] * Z;
			J[i + n][0] = r[1][0] * X + r[1][1] * Y + r[1][2] * Z;
			J[i][1] = px[0][0] * X + px[0][1] * Y + px[0][2] * Z;
			J[i + n][1] = px[1][0] * X + px[1][1] * Y + px[1][2] * Z;
			J[i][2] = py[0][0] * X + py[0][1] * Y + py[0][2] * Z;
			J[i + n][2] = py[1][0] * X + py[1][1] * Y + py[1][2] * Z;
			J[i][3] = pz[0][0] * X + pz[0][1] * Y + pz[0][2] * Z;
			J[i + n][3] = pz[1][0] * X + pz[1][1] * Y + pz[1][2] * Z;
			J[i][4] = 1.0;
			J[i + n][4] = 0.0;
			J[i][5] = 0.0;
			J[i + n][5] = 1.0;
		}
	}

	void calcJacob(Matrix plocal, Matrix pglobl, Matrix Jacob) {
		final int n = _M.getRowDimension() / 3;
		final int m = _V.getColumnDimension();

		assert ((plocal.getRowDimension() == m)
				&& (plocal.getColumnDimension() == 1)
				&& (pglobl.getRowDimension() == 6)
				&& (pglobl.getColumnDimension() == 1)
				&& (Jacob.getRowDimension() == 2 * n) && (Jacob
				.getColumnDimension() == 6 + m));
		double s = pglobl.get(0, 0);

		Matrix Rx = new Matrix(new double[][] { { 0, 0, 0 }, { 0, 0, -1 },
				{ 0, 1, 0 } });
		Matrix Ry = new Matrix(new double[][] { { 0, 0, 1 }, { 0, 0, 0 },
				{ -1, 0, 0 } });
		Matrix Rz = new Matrix(new double[][] { { 0, -1, 0 }, { 1, 0, 0 },
				{ 0, 0, 0 } });

		S_ = calcShape3D(plocal);

		R_ = euler2Rot(pglobl);

		P_ = R_.getMatrix(0, 2 - 1, 0, 3 - 1).times(s);
		Px_ = P_.times(Rx);
		Py_ = P_.times(Ry);
		Pz_ = P_.times(Rz);

		final double[][] px = Px_.getArray();
		final double[][] py = Py_.getArray();
		final double[][] pz = Pz_.getArray();
		final double[][] p = P_.getArray();
		final double[][] r = R_.getArray();

		final double[][] V = _V.getArray();

		final double[][] J = Jacob.getArray();

		for (int i = 0; i < n; i++) {
			double X = S_.get(i, 0);
			double Y = S_.get(i + n, 0);
			double Z = S_.get(i + n * 2, 0);

			J[i][0] = r[0][0] * X + r[0][1] * Y + r[0][2] * Z;
			J[i + n][0] = r[1][0] * X + r[1][1] * Y + r[1][2] * Z;
			J[i][1] = px[0][0] * X + px[0][1] * Y + px[0][2] * Z;
			J[i + n][1] = px[1][0] * X + px[1][1] * Y + px[1][2] * Z;
			J[i][2] = py[0][0] * X + py[0][1] * Y + py[0][2] * Z;
			J[i + n][2] = py[1][0] * X + py[1][1] * Y + py[1][2] * Z;
			J[i][3] = pz[0][0] * X + pz[0][1] * Y + pz[0][2] * Z;
			J[i + n][3] = pz[1][0] * X + pz[1][1] * Y + pz[1][2] * Z;
			J[i][4] = 1.0;
			J[i + n][4] = 0.0;
			J[i][5] = 0.0;
			J[i + n][5] = 1.0;

			for (int j = 0; j < m; j++) {
				J[i][6 + j] = p[0][0] * V[i][j] + p[0][1] * V[i + n][j]
						+ p[0][2] * V[i + 2 * n][j];
				J[i + n][6 + j] = p[1][0] * V[i][j] + p[1][1] * V[i + n][j]
						+ p[1][2] * V[i + 2 * n][j];
			}
		}
	}

	void calcReferenceUpdate(Matrix dp, Matrix plocal, Matrix pglobl) {
		assert ((dp.getRowDimension() == 6 + _V.getColumnDimension()) && (dp
				.getColumnDimension() == 1));

		plocal.setMatrix(0, plocal.getRowDimension() - 1, 0, plocal
				.getColumnDimension() - 1, plocal.plus(dp.getMatrix(6,
				6 + _V.getColumnDimension() - 1, 0, 1 - 1)));

		pglobl.set(0, 0, pglobl.get(0, 0) + dp.get(0, 0));
		pglobl.set(4, 0, pglobl.get(4, 0) + dp.get(4, 0));
		pglobl.set(5, 0, pglobl.get(5, 0) + dp.get(5, 0));

		R1_ = euler2Rot(pglobl);

		R2_ = Matrix.identity(3, 3);
		R2_.set(2, 1, dp.get(1, 0));
		R2_.set(1, 2, -R2_.get(2, 1));

		R2_.set(0, 2, dp.get(2, 0));
		R2_.set(2, 0, -R2_.get(0, 2));

		R2_.set(1, 0, dp.get(3, 0));
		R2_.set(0, 1, -R2_.get(1, 0));

		metricUpgrade(R2_);
		R3_ = R1_.times(R2_);
		rot2Euler(R3_, pglobl);
	}

	void applySimT(SimTData data, Matrix pglobl) {
		assert ((pglobl.getRowDimension() == 6) && (pglobl.getColumnDimension() == 1));

		double angle = Math.atan2(data.b, data.a);
		double scale = data.a / Math.cos(angle);
		double ca = Math.cos(angle);
		double sa = Math.sin(angle);
		double xc = pglobl.get(4, 0);
		double yc = pglobl.get(5, 0);

		MatrixUtils.zero(R1_);
		R1_.set(2, 2, 1.0);
		R1_.set(0, 0, ca);
		R1_.set(0, 1, -sa);
		R1_.set(1, 0, sa);
		R1_.set(1, 1, ca);

		R2_ = euler2Rot(pglobl);
		R3_ = R1_.times(R2_);

		pglobl.set(0, 0, pglobl.get(0, 0) * scale);
		rot2Euler(R3_, pglobl);

		pglobl.set(4, 0, data.a * xc - data.b * yc + data.tx);
		pglobl.set(5, 0, data.b * xc + data.a * yc + data.ty);
	}

	static PDM read(Scanner s, boolean readType) {
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.PDM.ordinal());
		}

		PDM pdm = new PDM();

		pdm._V = IO.readMat(s);
		pdm._E = IO.readMat(s);
		pdm._M = IO.readMat(s);

		pdm.S_ = new Matrix(pdm._M.getRowDimension(), 1);
		pdm.R_ = new Matrix(3, 3);
		pdm.P_ = new Matrix(2, 3);
		pdm.Px_ = new Matrix(2, 3);
		pdm.Py_ = new Matrix(2, 3);
		pdm.Pz_ = new Matrix(2, 3);
		pdm.R1_ = new Matrix(3, 3);
		pdm.R2_ = new Matrix(3, 3);
		pdm.R3_ = new Matrix(3, 3);

		return pdm;
	}

	void write(BufferedWriter s) throws IOException {
		s.write(IO.Types.PDM.ordinal() + " ");

		IO.writeMat(s, _V);
		IO.writeMat(s, _E);
		IO.writeMat(s, _M);
	}

	static PDM load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	void save(final String fname) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			write(bw);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * @return the number of points in the face model
	 */
	public final int nPoints() {
		return _M.getRowDimension() / 3;
	}

	int nModes() {
		return _V.getColumnDimension();
	}

	double var(int i) {
		assert (i < _E.getColumnDimension());

		return _E.get(0, i);
	}
}
