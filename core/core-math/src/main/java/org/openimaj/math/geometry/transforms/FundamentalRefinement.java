package org.openimaj.math.geometry.transforms;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
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
	// NONE,
	EPIPOLAR {
		@Override
		protected MultivariateVectorFunction getValueFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p)
		{
			switch (p) {
			case F12:
				break;
			case F13:
				break;
			case F23:
				break;
			}
			return null;
		}

		@Override
		protected MultivariateMatrixFunction getJacobianFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p)
		{
			switch (p) {
			case F12:
				break;
			case F13:
				break;
			case F23:
				break;
			}
			return null;
		}
	},
	// SAMPSON
	;

	protected abstract MultivariateVectorFunction getValueFunction(
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p);

	protected abstract MultivariateMatrixFunction getJacobianFunction(
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data, Parameterisation p);

	/**
	 * Refine an initial guess at the homography that takes the first points in
	 * data to the second using non-linear Levenberg Marquardt optimisation. The
	 * initial guess would normally be computed using the direct linear
	 * transform ({@link TransformUtilities#homographyMatrix(List)}).
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

		final MultivariateVectorFunction value = getValueFunction(data, p);
		final MultivariateMatrixFunction jacobian = getJacobianFunction(data, p);
		final MultivariateJacobianFunction model = LeastSquaresFactory.model(value, jacobian);

		final Optimum result = lm.optimize(LeastSquaresFactory.create(model,
				observed, start, null, maxEvaluations, maxIterations));

		final Matrix improved = p.paramsToMatrix(result.getPoint().toArray());
		MatrixUtils.times(improved, 1.0 / improved.get(2, 2));

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
	private enum Parameterisation {
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

	private static class FastSolveNormal3x2 {
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
		private static FastSolveNormal3x2 solve(double[] r1, double[] r2, double[] r3) {
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
}
