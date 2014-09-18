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

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Refinement of fundamental matrix estimates using non-linear optimisation
 * (Levenberg-Marquardt) under different geometric distance/error assumptions.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public enum FundamentalRefinement {
	/**
	 * Don't perform any refinement and just return the initial input matrix
	 */
	NONE {
		@Override
		public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			// just return the initial estimate
			return initial;
		}

		@Override
		protected MultivariateJacobianFunction getFunctions(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p)
		{
			return null;
		}
	},
	/**
	 * Minimise the symmetric epipolar distance
	 */
	EPIPOLAR {
		@Override
		protected MultivariateJacobianFunction getFunctions(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p)
		{
			switch (p) {
			case F12:
				return new F12Epipolar(data);
			case F13:
				return new F13Epipolar(data);
			case F23:
				return new F23Epipolar(data);
			}
			return null;
		}
	},
	/**
	 * Minimise the Sampson distance (the first-order estimate of geometric
	 * error)
	 */
	SAMPSON {
		@Override
		protected MultivariateJacobianFunction getFunctions(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p)
		{
			switch (p) {
			case F12:
				return new F12Sampson(data);
			case F13:
				return new F13Sampson(data);
			case F23:
				return new F23Sampson(data);
			}
			return null;
		}
	};

	protected abstract MultivariateJacobianFunction getFunctions(
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p);

	/**
	 * Refine an initial guess at the homography that takes the first points in
	 * data to the second using non-linear Levenberg Marquardt optimisation. The
	 * initial guess would normally be computed using the direct linear
	 * transform ({@link TransformUtilities#homographyMatrixNorm(List)}).
	 * 
	 * @param initial
	 *            the initial estimate (probably from the DLT technique)
	 * @param data
	 *            the pairs of data points
	 * @return the optimised estimate
	 */
	public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
		final double[] params = new double[8];
		final Parameterisation p = Parameterisation.chooseOptimalParameterisation(initial, params);

		final LevenbergMarquardtOptimizer lm = new LevenbergMarquardtOptimizer();
		final RealVector start = new ArrayRealVector(params, false);

		// target values are all zero as we're computing the distances ourselves
		final RealVector observed = new ArrayRealVector(data.size());

		final int maxEvaluations = 1000;
		final int maxIterations = 1000;

		final MultivariateJacobianFunction model = getFunctions(data, p);

		final Optimum result = lm.optimize(LeastSquaresFactory.create(model,
				observed, start, null, maxEvaluations, maxIterations));

		final Matrix improved = p.paramsToMatrix(result.getPoint().toArray());

		// normalise
		MatrixUtils.times(improved, 1.0 / improved.normInf());

		return improved;
	}

	/**
	 * Parameterisations of the fundamental matrix that preserve the rank-2
	 * constraint by writing one of the rows as a weighted combination of the
	 * other two.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	protected enum Parameterisation {
		/**
		 * Use the first two rows and estimate the third as a weighted
		 * combination
		 */
		F12 {
			@Override
			void matrixToParams(double[][] Fv, double r, double s, double[] params) {
				matrixToParams(Fv[0], Fv[1], r, s, params);
			}

			@Override
			Matrix paramsToMatrix(double[] params) {
				return new Matrix(
						new double[][] {
								{ params[0], params[1], params[2] },
								{ params[3], params[4], params[5] },
								{ params[0] * params[6] + params[3] * params[7], params[1] * params[6] + params[4]
										* params[7], params[2] * params[6] + params[5] * params[7] }
						});
			}
		},
		/**
		 * Use the first and last rows and estimate the middle one as a weighted
		 * combination
		 */
		F13 {
			@Override
			void matrixToParams(double[][] Fv, double r, double s, double[] params) {
				matrixToParams(Fv[0], Fv[2], r, s, params);
			}

			@Override
			Matrix paramsToMatrix(double[] params) {
				return new Matrix(
						new double[][] {
								{ params[0], params[1], params[2] },
								{ params[0] * params[6] + params[3] * params[7], params[1] * params[6] + params[4]
										* params[7], params[2] * params[6] + params[5] * params[7] },
								{ params[3], params[4], params[5] }
						});
			}
		},
		/**
		 * Use the last two rows and estimate the first as a weighted
		 * combination
		 */
		F23 {
			@Override
			void matrixToParams(double[][] Fv, double r, double s, double[] params) {
				matrixToParams(Fv[1], Fv[2], r, s, params);
			}

			@Override
			Matrix paramsToMatrix(double[] params) {
				return new Matrix(
						new double[][] {
								{ params[0] * params[6] + params[3] * params[7], params[1] * params[6] + params[4]
										* params[7], params[2] * params[6] + params[5] * params[7] },
								{ params[0], params[1], params[2] },
								{ params[3], params[4], params[5] }
						});
			}
		};

		abstract Matrix paramsToMatrix(double[] params);

		abstract void matrixToParams(double[][] Fv, double r, double s, double[] params);

		void matrixToParams(double[] Fv1, double[] Fv2, double r, double s, double[] params) {
			params[0] = Fv1[0];
			params[1] = Fv1[1];
			params[2] = Fv1[2];
			params[3] = Fv2[0];
			params[4] = Fv2[1];
			params[5] = Fv2[2];
			params[6] = r;
			params[7] = s;
		}

		/**
		 * Choose the optimal parameterisation of F that preserves the rank-2
		 * constraint by re-writing one of the rows as a weighted combination of
		 * the other two. The params vector will be filled accordingly.
		 * 
		 * @param F
		 *            the initial Fundamental matrix
		 * @param params
		 *            the 8 dimensional params vector
		 * @return the chosen parameterisation
		 */
		public static Parameterisation chooseOptimalParameterisation(Matrix F, double[] params) {
			final double[][] Fv = F.getArray();

			final FastSolveNormal3x2 f12 = FastSolveNormal3x2.solve(Fv[0], Fv[1], Fv[2]);
			final FastSolveNormal3x2 f13 = FastSolveNormal3x2.solve(Fv[0], Fv[2], Fv[1]);
			final FastSolveNormal3x2 f23 = FastSolveNormal3x2.solve(Fv[1], Fv[2], Fv[0]);

			if (f12.absDet + f13.absDet + f23.absDet == 0) {
				throw new IllegalArgumentException("F matrix is probably zero");
			}

			if (f12.absDet > f13.absDet) {
				if (f12.absDet > f23.absDet) {
					F12.matrixToParams(Fv, f12.r, f12.s, params);
					return F12;
				} else {
					F23.matrixToParams(Fv, f23.r, f23.s, params);
					return F23;
				}
			} else {
				if (f13.absDet > f23.absDet) {

					return F13;
				} else {
					F23.matrixToParams(Fv, f23.r, f23.s, params);
					return F23;
				}
			}
		}
	}

	protected static class FastSolveNormal3x2 {
		double r;
		double s;
		double absDet;

		/**
		 * Solve the over-determined system [r1 r2]x = [r3] where r1, r2 and r3
		 * are 3-dimensional column vectors and x is a 2 dimensional column
		 * vector x=[r; s]. Least-squares solution is found by solving the
		 * normal equations x=(A^T * A)^-1 *(A^T * b) where [r1 r2]=A and b=r3.
		 * 
		 * @param r1
		 *            vector 1
		 * @param r2
		 *            vector 2
		 * @param r3
		 *            vector 3
		 * @return the solution
		 */
		protected static FastSolveNormal3x2 solve(double[] r1, double[] r2, double[] r3) {
			final FastSolveNormal3x2 s = new FastSolveNormal3x2();

			// compute A^T * A, where A^T * A=[a b; b c]:
			final double a = r1[0] * r1[0] + r1[1] * r1[1] + r1[2] * r1[2];
			final double b = r1[0] * r2[0] + r1[1] * r2[1] + r1[2] * r2[2];
			final double c = r2[0] * r2[0] + r2[1] * r2[1] + r2[2] * r2[2];

			// compute A^T * b where A^T * b = [d; e]:
			final double d = r1[0] * r3[0] + r1[1] * r3[1] + r1[2] * r3[2];
			final double e = r2[0] * r3[0] + r2[1] * r3[1] + r2[2] * r3[2];

			// |det(A^T * A)|
			final double det = a * c - b * b;
			s.absDet = Math.abs(det);

			// x=(A^T * A)^-1 *(A^T * b) where x=[r;s]
			// recall for 2x2 matrix [a b; b c], [a b; b c]^-1 = 1/det([a b; b
			// c]) * [c -b; -b a]
			s.r = (c * d - b * e) / det;
			s.s = (-b * d + a * e) / det;

			return s;
		}
	}

	protected abstract static class Base implements MultivariateJacobianFunction {
		List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data;

		public Base(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			this.data = data;
		}

		@Override
		public Pair<RealVector, RealMatrix> value(RealVector point) {
			final double[] params = point.toArray();
			return new Pair<RealVector, RealMatrix>(value(params), jacobian(params));
		}

		RealVector value(double[] params) {
			final double[] result = new double[data.size()];

			for (int i = 0; i < data.size(); i++) {
				final IndependentPair<? extends Point2d, ? extends Point2d> pair = data.get(i);
				final Point2d p1 = pair.firstObject();
				final Point2d p2 = pair.secondObject();
				final double x = p1.getX();
				final double y = p1.getY();
				final double X = p2.getX();
				final double Y = p2.getY();

				result[i] = computeValue(x, y, X, Y, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
			}

			return new ArrayRealVector(result, false);
		}

		abstract double computeValue(double x, double y, double X, double Y, double f1, double f2, double f3, double f4,
				double f5, double f6, double r, double s);

		RealMatrix jacobian(double[] params) {
			final double[][] result = new double[data.size()][];

			for (int i = 0; i < data.size(); i++) {
				final IndependentPair<? extends Point2d, ? extends Point2d> pair = data.get(i);
				final Point2d p1 = pair.firstObject();
				final Point2d p2 = pair.secondObject();
				final double x = p1.getX();
				final double y = p1.getY();
				final double X = p2.getX();
				final double Y = p2.getY();

				result[i] = computeJacobian(x, y, X, Y, params[0], params[1], params[2], params[3], params[4], params[5],
						params[6], params[7]);
			}

			return new Array2DRowRealMatrix(result, false);
		}

		abstract double[] computeJacobian(double x, double y, double X, double Y, double f1, double f2,
				double f3, double f4, double f5, double f6, double r, double s);
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.10 in H&Z ("Symmetric Epipolar Distance")
	 * syms f1 f2 f3 f4 f5 f6 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 3 is parameterised
	 * f7 = r*f1 + s*f4;
	 * f8 = r*f2 + s*f5;
	 * f9 = r*f3 + s*f6;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the symmetric epipolar distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 * (( 1 / (Fx(1)^2 + Fx(2)^2)) + (1 / (FtX(1)^2 + FtX(2)^2)));
	 * J = jacobian(d, [f1 f2 f3 f4 f5 f6 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f12_epi_value.c')
	 * ccode(J, 'file', 'ccode/f12_epi_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F12Epipolar extends Base {
		public F12Epipolar(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f1, double f2, double f3, double f4,
				double f5, double f6, double r, double s)
		{
			final double t2 = f3 + f1 * x + f2 * y;
			final double t3 = f6 + f4 * x + f5 * y;
			final double t6 = X * f1;
			final double t7 = Y * f4;
			final double t8 = f1 * r;
			final double t9 = f4 * s;
			final double t4 = t6 + t7 + t8 + t9;
			final double t10 = X * f2;
			final double t11 = Y * f5;
			final double t12 = f2 * r;
			final double t13 = f5 * s;
			final double t5 = t10 + t11 + t12 + t13;
			final double t14 = X * f3 + Y * f6 + f3 * r + f6 * s + t4 * x + t5 * y;
			final double t0 = (t14 * t14) * (1.0 / (t2 * t2 + t3 * t3) + 1.0 / (t4 * t4 + t5 * t5));

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f1, double f2, double f3, double f4,
				double f5, double f6, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t4 = f1 * x;
			final double t5 = f2 * y;
			final double t2 = f3 + t4 + t5;
			final double t19 = f4 * x;
			final double t20 = f5 * y;
			final double t3 = f6 + t19 + t20;
			final double t8 = X * f1;
			final double t9 = Y * f4;
			final double t10 = f1 * r;
			final double t11 = f4 * s;
			final double t6 = t8 + t9 + t10 + t11;
			final double t12 = X * f2;
			final double t13 = Y * f5;
			final double t14 = f2 * r;
			final double t15 = f5 * s;
			final double t7 = t12 + t13 + t14 + t15;
			final double t26 = X * f3;
			final double t27 = Y * f6;
			final double t28 = f3 * r;
			final double t29 = f6 * s;
			final double t30 = t6 * x;
			final double t31 = t7 * y;
			final double t16 = t26 + t27 + t28 + t29 + t30 + t31;
			final double t17 = X + r;
			final double t18 = t2 * t2;
			final double t21 = t3 * t3;
			final double t22 = t18 + t21;
			final double t23 = t6 * t6;
			final double t24 = t7 * t7;
			final double t25 = t23 + t24;
			final double t32 = 1.0 / (t22 * t22);
			final double t33 = 1.0 / (t25 * t25);
			final double t34 = t16 * t16;
			final double t35 = 1.0 / t22;
			final double t36 = 1.0 / t25;
			final double t37 = t35 + t36;
			final double t38 = Y + s;
			A0[0] = -t34 * (t6 * t17 * t33 * 2.0 + t2 * t32 * x * 2.0) + t16 * t17 * t37 * x * 2.0;
			A0[1] = -t34 * (t7 * t17 * t33 * 2.0 + t2 * t32 * y * 2.0) + t16 * t17 * t37 * y * 2.0;
			A0[2] = t16 * t17 * t37 * 2.0 - t32 * t34 * (f3 * 2.0 + f1 * x * 2.0 + f2 * y * 2.0);
			A0[3] = -t34 * (t6 * t33 * t38 * 2.0 + t3 * t32 * x * 2.0) + t16 * t37 * t38 * x * 2.0;
			A0[4] = -t34 * (t7 * t33 * t38 * 2.0 + t3 * t32 * y * 2.0) + t16 * t37 * t38 * y * 2.0;
			A0[5] = t16 * t37 * t38 * 2.0 - t32 * t34 * (f6 * 2.0 + f4 * x * 2.0 + f5 * y * 2.0);
			A0[6] = -t33 * t34 * (f1 * t6 * 2.0 + f2 * t7 * 2.0) + t2 * t16 * t37 * 2.0;
			A0[7] = -t33 * t34 * (f4 * t6 * 2.0 + f5 * t7 * 2.0) + t3 * t16 * t37 * 2.0;

			return A0;
		}
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.10 in H&Z ("Symmetric Epipolar Distance")
	 * syms f1 f2 f3 f7 f8 f9 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 2 is parameterised
	 * f4 = r*f1 + s*f7;
	 * f5 = r*f2 + s*f8;
	 * f6 = r*f3 + s*f9;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the symmetric epipolar distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 * (( 1 / (Fx(1)^2 + Fx(2)^2)) + (1 / (FtX(1)^2 + FtX(2)^2)));
	 * J = jacobian(d, [f1 f2 f3 f7 f8 f9 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f13_epi_value.c')
	 * ccode(J, 'file', 'ccode/f13_epi_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F13Epipolar extends Base {
		public F13Epipolar(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f1, double f2, double f3, double f7,
				double f8, double f9, double r, double s)
		{
			final double t4 = f1 * r;
			final double t5 = f7 * s;
			final double t6 = t4 + t5;
			final double t12 = X * f1;
			final double t13 = Y * t6;
			final double t2 = f7 + t12 + t13;
			final double t7 = f2 * r;
			final double t8 = f8 * s;
			final double t9 = t7 + t8;
			final double t14 = X * f2;
			final double t15 = Y * t9;
			final double t3 = f8 + t14 + t15;
			final double t16 = f3 * r;
			final double t17 = f9 * s;
			final double t10 = t16 + t17 + t6 * x + t9 * y;
			final double t11 = f3 + f1 * x + f2 * y;
			final double t18 = f9 + X * f3 + t2 * x + t3 * y + Y * (t16 + t17);
			final double t0 = (t18 * t18) * (1.0 / (t2 * t2 + t3 * t3) + 1.0 / (t10 * t10 + t11 * t11));

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f1, double f2, double f3, double f7,
				double f8, double f9, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t6 = f3 * r;
			final double t7 = f9 * s;
			final double t8 = f1 * r;
			final double t9 = f7 * s;
			final double t10 = t8 + t9;
			final double t11 = t10 * x;
			final double t12 = f2 * r;
			final double t13 = f8 * s;
			final double t14 = t12 + t13;
			final double t15 = t14 * y;
			final double t2 = t6 + t7 + t11 + t15;
			final double t4 = f1 * x;
			final double t5 = f2 * y;
			final double t3 = f3 + t4 + t5;
			final double t18 = X * f1;
			final double t19 = Y * t10;
			final double t16 = f7 + t18 + t19;
			final double t20 = X * f2;
			final double t21 = Y * t14;
			final double t17 = f8 + t20 + t21;
			final double t31 = X * f3;
			final double t32 = t16 * x;
			final double t33 = t17 * y;
			final double t34 = t6 + t7;
			final double t35 = Y * t34;
			final double t22 = f9 + t31 + t32 + t33 + t35;
			final double t23 = t16 * t16;
			final double t24 = t17 * t17;
			final double t25 = t23 + t24;
			final double t26 = t2 * t2;
			final double t27 = t3 * t3;
			final double t28 = t26 + t27;
			final double t29 = Y * r;
			final double t30 = X + t29;
			final double t36 = 1.0 / (t28 * t28);
			final double t37 = 1.0 / (t25 * t25);
			final double t38 = t22 * t22;
			final double t39 = 1.0 / t25;
			final double t40 = 1.0 / t28;
			final double t41 = t39 + t40;
			final double t42 = Y * s;
			final double t43 = t42 + 1.0;
			A0[0] = -t38 * (t36 * (t3 * x * 2.0 + r * t2 * x * 2.0) + t16 * t30 * t37 * 2.0) + t22 * t30 * t41 * x * 2.0;
			A0[1] = -t38 * (t36 * (t3 * y * 2.0 + r * t2 * y * 2.0) + t17 * t30 * t37 * 2.0) + t22 * t30 * t41 * y * 2.0;
			A0[2] = -t36 * t38 * (f3 * 2.0 + f1 * x * 2.0 + f2 * y * 2.0 + r * t2 * 2.0) + t22 * t30 * t41 * 2.0;
			A0[3] = -t38 * (t16 * t37 * t43 * 2.0 + s * t2 * t36 * x * 2.0) + t22 * t41 * t43 * x * 2.0;
			A0[4] = -t38 * (t17 * t37 * t43 * 2.0 + s * t2 * t36 * y * 2.0) + t22 * t41 * t43 * y * 2.0;
			A0[5] = t22 * t41 * t43 * 2.0 - s * t2 * t36 * t38 * 2.0;
			A0[6] = -t38 * (t37 * (Y * f1 * t16 * 2.0 + Y * f2 * t17 * 2.0) + t2 * t3 * t36 * 2.0) + t22 * t41
					* (Y * f3 + Y * f1 * x + Y * f2 * y) * 2.0;
			A0[7] = -t38 * (t37 * (Y * f7 * t16 * 2.0 + Y * f8 * t17 * 2.0) + t2 * t36 * (f9 + f7 * x + f8 * y) * 2.0)
					+ t22 * t41 * (Y * f9 + Y * f7 * x + Y * f8 * y) * 2.0;

			return A0;
		}
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.10 in H&Z ("Symmetric Epipolar Distance")
	 * syms f4 f5 f6 f7 f8 f9 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 1 is parameterised
	 * f1 = r*f4 + s*f7;
	 * f2 = r*f5 + s*f8;
	 * f3 = r*f6 + s*f9;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the symmetric epipolar distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 * (( 1 / (Fx(1)^2 + Fx(2)^2)) + (1 / (FtX(1)^2 + FtX(2)^2)));
	 * J = jacobian(d, [f4 f5 f6 f7 f8 f9 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f23_epi_value.c')
	 * ccode(J, 'file', 'ccode/f23_epi_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F23Epipolar extends Base {
		public F23Epipolar(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f4, double f5, double f6, double f7,
				double f8, double f9, double r, double s)
		{
			final double t4 = f4 * r;
			final double t5 = f7 * s;
			final double t6 = t4 + t5;
			final double t12 = Y * f4;
			final double t13 = X * t6;
			final double t2 = f7 + t12 + t13;
			final double t7 = f5 * r;
			final double t8 = f8 * s;
			final double t9 = t7 + t8;
			final double t14 = Y * f5;
			final double t15 = X * t9;
			final double t3 = f8 + t14 + t15;
			final double t16 = f6 * r;
			final double t17 = f9 * s;
			final double t10 = t16 + t17 + t6 * x + t9 * y;
			final double t11 = f6 + f4 * x + f5 * y;
			final double t18 = f9 + Y * f6 + t2 * x + t3 * y + X * (t16 + t17);
			final double t0 = (t18 * t18) * (1.0 / (t2 * t2 + t3 * t3) + 1.0 / (t10 * t10 + t11 * t11));

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f4, double f5, double f6, double f7,
				double f8, double f9, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t6 = f6 * r;
			final double t7 = f9 * s;
			final double t8 = f4 * r;
			final double t9 = f7 * s;
			final double t10 = t8 + t9;
			final double t11 = t10 * x;
			final double t12 = f5 * r;
			final double t13 = f8 * s;
			final double t14 = t12 + t13;
			final double t15 = t14 * y;
			final double t2 = t6 + t7 + t11 + t15;
			final double t4 = f4 * x;
			final double t5 = f5 * y;
			final double t3 = f6 + t4 + t5;
			final double t18 = Y * f4;
			final double t19 = X * t10;
			final double t16 = f7 + t18 + t19;
			final double t20 = Y * f5;
			final double t21 = X * t14;
			final double t17 = f8 + t20 + t21;
			final double t31 = Y * f6;
			final double t32 = t16 * x;
			final double t33 = t17 * y;
			final double t34 = t6 + t7;
			final double t35 = X * t34;
			final double t22 = f9 + t31 + t32 + t33 + t35;
			final double t23 = t16 * t16;
			final double t24 = t17 * t17;
			final double t25 = t23 + t24;
			final double t26 = t2 * t2;
			final double t27 = t3 * t3;
			final double t28 = t26 + t27;
			final double t29 = X * r;
			final double t30 = Y + t29;
			final double t36 = 1.0 / (t28 * t28);
			final double t37 = 1.0 / (t25 * t25);
			final double t38 = t22 * t22;
			final double t39 = 1.0 / t25;
			final double t40 = 1.0 / t28;
			final double t41 = t39 + t40;
			final double t42 = X * s;
			final double t43 = t42 + 1.0;
			A0[0] = -t38 * (t36 * (t3 * x * 2.0 + r * t2 * x * 2.0) + t16 * t30 * t37 * 2.0) + t22 * t30 * t41 * x * 2.0;
			A0[1] = -t38 * (t36 * (t3 * y * 2.0 + r * t2 * y * 2.0) + t17 * t30 * t37 * 2.0) + t22 * t30 * t41 * y * 2.0;
			A0[2] = -t36 * t38 * (f6 * 2.0 + f4 * x * 2.0 + f5 * y * 2.0 + r * t2 * 2.0) + t22 * t30 * t41 * 2.0;
			A0[3] = -t38 * (t16 * t37 * t43 * 2.0 + s * t2 * t36 * x * 2.0) + t22 * t41 * t43 * x * 2.0;
			A0[4] = -t38 * (t17 * t37 * t43 * 2.0 + s * t2 * t36 * y * 2.0) + t22 * t41 * t43 * y * 2.0;
			A0[5] = t22 * t41 * t43 * 2.0 - s * t2 * t36 * t38 * 2.0;
			A0[6] = -t38 * (t37 * (X * f4 * t16 * 2.0 + X * f5 * t17 * 2.0) + t2 * t3 * t36 * 2.0) + t22 * t41
					* (X * f6 + X * f4 * x + X * f5 * y) * 2.0;
			A0[7] = -t38 * (t37 * (X * f7 * t16 * 2.0 + X * f8 * t17 * 2.0) + t2 * t36 * (f9 + f7 * x + f8 * y) * 2.0)
					+ t22 * t41 * (X * f9 + X * f7 * x + X * f8 * y) * 2.0;

			return A0;
		}
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.9 in H&Z ("First order geometric error (Sampson distance)")
	 * syms f1 f2 f3 f4 f5 f6 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 3 is parameterised
	 * f7 = r*f1 + s*f4;
	 * f8 = r*f2 + s*f5;
	 * f9 = r*f3 + s*f6;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the sampson distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 / (Fx(1)^2 + Fx(2)^2 + FtX(1)^2 + FtX(2)^2);
	 * J = jacobian(d, [f1 f2 f3 f4 f5 f6 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f12_sampson_value.c')
	 * ccode(J, 'file', 'ccode/f12_sampson_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F12Sampson extends Base {
		public F12Sampson(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f1, double f2, double f3, double f4,
				double f5, double f6, double r, double s)
		{
			final double t6 = X * f1;
			final double t7 = Y * f4;
			final double t8 = f1 * r;
			final double t9 = f4 * s;
			final double t2 = t6 + t7 + t8 + t9;
			final double t10 = X * f2;
			final double t11 = Y * f5;
			final double t12 = f2 * r;
			final double t13 = f5 * s;
			final double t3 = t10 + t11 + t12 + t13;
			final double t4 = f3 + f1 * x + f2 * y;
			final double t5 = f6 + f4 * x + f5 * y;
			final double t14 = X * f3 + Y * f6 + f3 * r + f6 * s + t2 * x + t3 * y;
			final double t0 = (t14 * t14) / (t2 * t2 + t3 * t3 + t4 * t4 + t5 * t5);

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f1, double f2, double f3, double f4,
				double f5, double f6, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t2 = X * f1;
			final double t3 = Y * f4;
			final double t4 = f1 * r;
			final double t5 = f4 * s;
			final double t6 = t2 + t3 + t4 + t5;
			final double t12 = X * f2;
			final double t13 = Y * f5;
			final double t14 = f2 * r;
			final double t15 = f5 * s;
			final double t7 = t12 + t13 + t14 + t15;
			final double t8 = f1 * x;
			final double t9 = f2 * y;
			final double t10 = f3 + t8 + t9;
			final double t21 = f4 * x;
			final double t22 = f5 * y;
			final double t11 = f6 + t21 + t22;
			final double t25 = X * f3;
			final double t26 = Y * f6;
			final double t27 = f3 * r;
			final double t28 = f6 * s;
			final double t29 = t6 * x;
			final double t30 = t7 * y;
			final double t16 = t25 + t26 + t27 + t28 + t29 + t30;
			final double t17 = X + r;
			final double t18 = t6 * t6;
			final double t19 = t7 * t7;
			final double t20 = t10 * t10;
			final double t23 = t11 * t11;
			final double t24 = t18 + t19 + t20 + t23;
			final double t31 = 1.0 / (t24 * t24);
			final double t32 = t16 * t16;
			final double t33 = 1.0 / t24;
			final double t34 = Y + s;
			A0[0] = -t31 * t32 * (t6 * t17 * 2.0 + t10 * x * 2.0) + t16 * t17 * t33 * x * 2.0;
			A0[1] = -t31 * t32 * (t7 * t17 * 2.0 + t10 * y * 2.0) + t16 * t17 * t33 * y * 2.0;
			A0[2] = t16 * t17 * t33 * 2.0 - t31 * t32 * (f3 * 2.0 + f1 * x * 2.0 + f2 * y * 2.0);
			A0[3] = -t31 * t32 * (t6 * t34 * 2.0 + t11 * x * 2.0) + t16 * t33 * t34 * x * 2.0;
			A0[4] = -t31 * t32 * (t7 * t34 * 2.0 + t11 * y * 2.0) + t16 * t33 * t34 * y * 2.0;
			A0[5] = t16 * t33 * t34 * 2.0 - t31 * t32 * (f6 * 2.0 + f4 * x * 2.0 + f5 * y * 2.0);
			A0[6] = -t31 * t32 * (f1 * t6 * 2.0 + f2 * t7 * 2.0) + t10 * t16 * t33 * 2.0;
			A0[7] = -t31 * t32 * (f4 * t6 * 2.0 + f5 * t7 * 2.0) + t11 * t16 * t33 * 2.0;

			return A0;
		}
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.9 in H&Z ("First order geometric error (Sampson distance)")
	 * syms f1 f2 f3 f7 f8 f9 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 2 is parameterised
	 * f4 = r*f1 + s*f7;
	 * f5 = r*f2 + s*f8;
	 * f6 = r*f3 + s*f9;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the sampson distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 / (Fx(1)^2 + Fx(2)^2 + FtX(1)^2 + FtX(2)^2);
	 * J = jacobian(d, [f1 f2 f3 f7 f8 f9 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f13_sampson_value.c')
	 * ccode(J, 'file', 'ccode/f13_sampson_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F13Sampson extends Base {
		public F13Sampson(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f1, double f2, double f3, double f7,
				double f8, double f9, double r, double s)
		{
			final double t4 = f1 * r;
			final double t5 = f7 * s;
			final double t6 = t4 + t5;
			final double t8 = f2 * r;
			final double t9 = f8 * s;
			final double t10 = t8 + t9;
			final double t16 = f3 * r;
			final double t17 = f9 * s;
			final double t2 = t16 + t17 + t6 * x + t10 * y;
			final double t3 = f3 + f1 * x + f2 * y;
			final double t12 = X * f1;
			final double t13 = Y * t6;
			final double t7 = f7 + t12 + t13;
			final double t14 = X * f2;
			final double t15 = Y * t10;
			final double t11 = f8 + t14 + t15;
			final double t18 = f9 + X * f3 + t7 * x + t11 * y + Y * (t16 + t17);
			final double t0 = (t18 * t18) / (t2 * t2 + t3 * t3 + t7 * t7 + t11 * t11);

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f1, double f2, double f3, double f7,
				double f8, double f9, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t2 = f1 * r;
			final double t3 = f7 * s;
			final double t4 = t2 + t3;
			final double t5 = f3 * r;
			final double t6 = f9 * s;
			final double t7 = t4 * x;
			final double t8 = f2 * r;
			final double t9 = f8 * s;
			final double t10 = t8 + t9;
			final double t11 = t10 * y;
			final double t12 = t5 + t6 + t7 + t11;
			final double t13 = f1 * x;
			final double t14 = f2 * y;
			final double t15 = f3 + t13 + t14;
			final double t16 = X * f1;
			final double t17 = Y * t4;
			final double t18 = f7 + t16 + t17;
			final double t20 = X * f2;
			final double t21 = Y * t10;
			final double t19 = f8 + t20 + t21;
			final double t30 = X * f3;
			final double t31 = t18 * x;
			final double t32 = t19 * y;
			final double t33 = t5 + t6;
			final double t34 = Y * t33;
			final double t22 = f9 + t30 + t31 + t32 + t34;
			final double t23 = Y * r;
			final double t24 = X + t23;
			final double t25 = t12 * t12;
			final double t26 = t15 * t15;
			final double t27 = t18 * t18;
			final double t28 = t19 * t19;
			final double t29 = t25 + t26 + t27 + t28;
			final double t35 = 1.0 / (t29 * t29);
			final double t36 = t22 * t22;
			final double t37 = 1.0 / t29;
			final double t38 = Y * s;
			final double t39 = t38 + 1.0;
			A0[0] = -t35 * t36 * (t18 * t24 * 2.0 + t15 * x * 2.0 + r * t12 * x * 2.0) + t22 * t24 * t37 * x * 2.0;
			A0[1] = -t35 * t36 * (t19 * t24 * 2.0 + t15 * y * 2.0 + r * t12 * y * 2.0) + t22 * t24 * t37 * y * 2.0;
			A0[2] = -t35 * t36 * (f3 * 2.0 + f1 * x * 2.0 + f2 * y * 2.0 + r * t12 * 2.0) + t22 * t24 * t37 * 2.0;
			A0[3] = -t35 * t36 * (t18 * t39 * 2.0 + s * t12 * x * 2.0) + t22 * t37 * t39 * x * 2.0;
			A0[4] = -t35 * t36 * (t19 * t39 * 2.0 + s * t12 * y * 2.0) + t22 * t37 * t39 * y * 2.0;
			A0[5] = t22 * t37 * t39 * 2.0 - s * t12 * t35 * t36 * 2.0;
			A0[6] = -t35 * t36 * (t12 * t15 * 2.0 + Y * f1 * t18 * 2.0 + Y * f2 * t19 * 2.0) + t22 * t37
					* (Y * f3 + Y * f1 * x + Y * f2 * y) * 2.0;
			A0[7] = -t35 * t36 * (t12 * (f9 + f7 * x + f8 * y) * 2.0 + Y * f7 * t18 * 2.0 + Y * f8 * t19 * 2.0) + t22
					* t37 * (Y * f9 + Y * f7 * x + Y * f8 * y) * 2.0;

			return A0;
		}
	}

	/**
	 * Based on the following matlab: <code>
	 * <pre>
	 * % Based on Eqn 11.9 in H&Z ("First order geometric error (Sampson distance)")
	 * syms f4 f5 f6 f7 f8 f9 real
	 * syms r s real
	 * syms x y X Y real
	 * % row 1 is parameterised
	 * f1 = r*f4 + s*f7;
	 * f2 = r*f5 + s*f8;
	 * f3 = r*f6 + s*f9;
	 * % build F
	 * F = [f1 f2 f3; f4 f5 f6; f7 f8 f9];
	 * % the sampson distance and its analytic jacobian
	 * Fx = F*[x y 1]';
	 * FtX = F'*[X Y 1]';
	 * XFx = [X Y 1] * F * [x y 1]';
	 * d = XFx^2 / (Fx(1)^2 + Fx(2)^2 + FtX(1)^2 + FtX(2)^2);
	 * J = jacobian(d, [f4 f5 f6 f7 f8 f9 r s]);
	 * % generate code
	 * ccode(d, 'file', 'ccode/f23_sampson_value.c')
	 * ccode(J, 'file', 'ccode/f23_sampson_jac.c')
	 * </pre>
	 * </code>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	protected static class F23Sampson extends Base {
		public F23Sampson(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			super(data);
		}

		@Override
		double computeValue(double x, double y, double X, double Y, double f4, double f5, double f6, double f7,
				double f8, double f9, double r, double s)
		{
			final double t4 = f4 * r;
			final double t5 = f7 * s;
			final double t6 = t4 + t5;
			final double t8 = f5 * r;
			final double t9 = f8 * s;
			final double t10 = t8 + t9;
			final double t16 = f6 * r;
			final double t17 = f9 * s;
			final double t2 = t16 + t17 + t6 * x + t10 * y;
			final double t3 = f6 + f4 * x + f5 * y;
			final double t12 = Y * f4;
			final double t13 = X * t6;
			final double t7 = f7 + t12 + t13;
			final double t14 = Y * f5;
			final double t15 = X * t10;
			final double t11 = f8 + t14 + t15;
			final double t18 = f9 + Y * f6 + t7 * x + t11 * y + X * (t16 + t17);
			final double t0 = (t18 * t18) / (t2 * t2 + t3 * t3 + t7 * t7 + t11 * t11);

			return t0;
		}

		@Override
		double[] computeJacobian(double x, double y, double X, double Y, double f4, double f5, double f6, double f7,
				double f8, double f9, double r, double s)
		{
			final double[] A0 = new double[8];

			final double t2 = f4 * r;
			final double t3 = f7 * s;
			final double t4 = t2 + t3;
			final double t5 = f6 * r;
			final double t6 = f9 * s;
			final double t7 = t4 * x;
			final double t8 = f5 * r;
			final double t9 = f8 * s;
			final double t10 = t8 + t9;
			final double t11 = t10 * y;
			final double t12 = t5 + t6 + t7 + t11;
			final double t13 = f4 * x;
			final double t14 = f5 * y;
			final double t15 = f6 + t13 + t14;
			final double t16 = Y * f4;
			final double t17 = X * t4;
			final double t18 = f7 + t16 + t17;
			final double t20 = Y * f5;
			final double t21 = X * t10;
			final double t19 = f8 + t20 + t21;
			final double t30 = Y * f6;
			final double t31 = t18 * x;
			final double t32 = t19 * y;
			final double t33 = t5 + t6;
			final double t34 = X * t33;
			final double t22 = f9 + t30 + t31 + t32 + t34;
			final double t23 = X * r;
			final double t24 = Y + t23;
			final double t25 = t12 * t12;
			final double t26 = t15 * t15;
			final double t27 = t18 * t18;
			final double t28 = t19 * t19;
			final double t29 = t25 + t26 + t27 + t28;
			final double t35 = 1.0 / (t29 * t29);
			final double t36 = t22 * t22;
			final double t37 = 1.0 / t29;
			final double t38 = X * s;
			final double t39 = t38 + 1.0;
			A0[0] = -t35 * t36 * (t18 * t24 * 2.0 + t15 * x * 2.0 + r * t12 * x * 2.0) + t22 * t24 * t37 * x * 2.0;
			A0[1] = -t35 * t36 * (t19 * t24 * 2.0 + t15 * y * 2.0 + r * t12 * y * 2.0) + t22 * t24 * t37 * y * 2.0;
			A0[2] = -t35 * t36 * (f6 * 2.0 + f4 * x * 2.0 + f5 * y * 2.0 + r * t12 * 2.0) + t22 * t24 * t37 * 2.0;
			A0[3] = -t35 * t36 * (t18 * t39 * 2.0 + s * t12 * x * 2.0) + t22 * t37 * t39 * x * 2.0;
			A0[4] = -t35 * t36 * (t19 * t39 * 2.0 + s * t12 * y * 2.0) + t22 * t37 * t39 * y * 2.0;
			A0[5] = t22 * t37 * t39 * 2.0 - s * t12 * t35 * t36 * 2.0;
			A0[6] = -t35 * t36 * (t12 * t15 * 2.0 + X * f4 * t18 * 2.0 + X * f5 * t19 * 2.0) + t22 * t37
					* (X * f6 + X * f4 * x + X * f5 * y) * 2.0;
			A0[7] = -t35 * t36 * (t12 * (f9 + f7 * x + f8 * y) * 2.0 + X * f7 * t18 * 2.0 + X * f8 * t19 * 2.0) + t22
					* t37 * (X * f9 + X * f7 * x + X * f8 * y) * 2.0;

			return A0;
		}
	}
}
