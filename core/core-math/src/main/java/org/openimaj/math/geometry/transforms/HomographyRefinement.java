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

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Refinement of homographies estimates using non-linear optimisation
 * (Levenberg-Marquardt) under different geometric distance/error assumptions.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Book,
		author = { "Hartley, R.~I.", "Zisserman, A." },
		title = "Multiple View Geometry in Computer Vision",
		year = "2004",
		edition = "Second",
		publisher = "Cambridge University Press, ISBN: 0521540518")
public enum HomographyRefinement {
	/**
	 * Don't perform any refinement and just return the initial input matrix
	 */
	NONE {
		@Override
		protected MultivariateVectorFunction getValueFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return null;
		}

		@Override
		public double computeError(Matrix h, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			return 0; // points are ideal!
		}

		@Override
		protected MultivariateMatrixFunction getJacobianFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return null;
		}

		@Override
		public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			return initial; // initial is the result
		}
	},
	/**
	 * The points in the first image are projected by the homography matrix to
	 * produce new estimates of the second image points from which the residuals
	 * are computed and minimised by the optimiser. The assumption is that there
	 * is only noise in the second image, and that the first image is noise
	 * free.
	 * <p>
	 * Value and analytic Jacobian implementations auto-generated from Matlab
	 * using the following:
	 * 
	 * <pre>
	 * <code>
	 * syms h0 h1 h2 h3 h4 h5 h6 h7 h8 real
	 * syms X1 Y1 real
	 * Mi = [X1 Y1 1]';
	 * H1 = [h0 h1 h2];
	 * H2 = [h3 h4 h5];
	 * H3 = [h6 h7 h8];
	 * mihat = (1 / (H3*Mi)) * [H1*Mi; H2*Mi];
	 * J = jacobian(mihat, [h0,h1,h2,h3,h4,h5,h6,h7,h8]);
	 * ccode(mihat, 'file', 'singleImageTransfer_value.c')
	 * ccode(J, 'file', 'singleImageTransfer_jacobian.c')
	 * </code>
	 * </pre>
	 */
	SINGLE_IMAGE_TRANSFER {
		@Override
		protected MultivariateVectorFunction getValueFunction(
				final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return new MultivariateVectorFunction() {
				@Override
				public double[] value(double[] h) throws IllegalArgumentException {
					final double[] result = new double[data.size() * 2];

					for (int i = 0; i < data.size(); i++) {
						final float X1 = data.get(i).firstObject().getX();
						final float Y1 = data.get(i).firstObject().getY();

						final double t2 = X1 * h[6];
						final double t3 = Y1 * h[7];
						final double t4 = h[8] + t2 + t3;
						final double t5 = 1.0 / t4;
						result[i * 2 + 0] = t5 * (h[2] + X1 * h[0] + Y1 * h[1]);
						result[i * 2 + 1] = t5 * (h[5] + X1 * h[3] + Y1 * h[4]);

					}
					return result;
				}
			};
		}

		@Override
		protected MultivariateMatrixFunction getJacobianFunction(
				final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return new MultivariateMatrixFunction() {
				// See Multi View Geometry in Computer Vision, eq 4.21, p129
				@Override
				public double[][] value(double[] h)
				{
					final double[][] result = new double[2 * data.size()][9];

					for (int i = 0; i < data.size(); i++) {
						final float X1 = data.get(i).firstObject().getX();
						final float Y1 = data.get(i).firstObject().getY();

						final double t2 = X1 * h[6];
						final double t3 = Y1 * h[7];
						final double t4 = h[8] + t2 + t3;
						final double t5 = 1.0 / t4;
						final double t6 = X1 * h[0];
						final double t7 = Y1 * h[1];
						final double t8 = h[2] + t6 + t7;
						final double t9 = 1.0 / (t4 * t4);
						final double t10 = X1 * t5;
						final double t11 = Y1 * t5;
						final double t12 = X1 * h[3];
						final double t13 = Y1 * h[4];
						final double t14 = h[5] + t12 + t13;
						result[i * 2 + 0][0] = t10;
						result[i * 2 + 0][1] = t11;
						result[i * 2 + 0][2] = t5;
						result[i * 2 + 0][6] = -X1 * t8 * t9;
						result[i * 2 + 0][7] = -Y1 * t8 * t9;
						result[i * 2 + 0][8] = -t8 * t9;
						result[i * 2 + 1][3] = t10;
						result[i * 2 + 1][4] = t11;
						result[i * 2 + 1][5] = t5;
						result[i * 2 + 1][6] = -X1 * t9 * t14;
						result[i * 2 + 1][7] = -Y1 * t9 * t14;
						result[i * 2 + 1][8] = -t9 * t14;
					}

					return result;
				}
			};
		}

		@Override
		public double computeError(Matrix h, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			double error = 0;
			for (int i = 0; i < data.size(); i++) {
				final Point2d p1 = data.get(i).firstObject().transform(h);
				final Point2d p2 = data.get(i).secondObject();

				final float dx = p1.getX() - p2.getX();
				final float dy = p1.getY() - p2.getY();
				error += dx * dx + dy * dy;
			}
			return error;
		}

		@Override
		public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			final LevenbergMarquardtOptimizer lm = new LevenbergMarquardtOptimizer();
			final RealVector start = new ArrayRealVector(initial.getRowPackedCopy());
			final RealVector observed = toRealVector(data, false);
			final int maxEvaluations = 1000;
			final int maxIterations = 1000;

			final MultivariateVectorFunction value = getValueFunction(data);
			final MultivariateMatrixFunction jacobian = getJacobianFunction(data);
			final MultivariateJacobianFunction model = LeastSquaresFactory.model(value, jacobian);

			final Optimum result = lm.optimize(LeastSquaresFactory.create(model,
					observed, start, null, maxEvaluations, maxIterations));

			final Matrix improved = MatrixUtils.fromRowPacked(result.getPoint().toArray(), 3);
			MatrixUtils.times(improved, 1.0 / improved.get(2, 2));

			return improved;
		}
	},
	/**
	 * The points in the second image are projected by the inverse homography
	 * matrix to produce new estimates of the first image points from which the
	 * residuals are computed and minimised by the optimiser. Technically, the
	 * optimiser optimises the inverse of the initial homography and then
	 * inverts the result. The assumption is that there is only noise in the
	 * first image.
	 */
	SINGLE_IMAGE_TRANSFER_INVERSE {

		@Override
		protected MultivariateVectorFunction getValueFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			final List<IndependentPair<? extends Point2d, ? extends Point2d>> dataInv = IndependentPair.swapList(data);
			return SINGLE_IMAGE_TRANSFER.getValueFunction(dataInv);
		}

		@Override
		public double computeError(Matrix h, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			final Matrix hInv = h.inverse();
			final List<IndependentPair<? extends Point2d, ? extends Point2d>> dataInv = IndependentPair.swapList(data);
			return SINGLE_IMAGE_TRANSFER.computeError(hInv, dataInv);
		}

		@Override
		protected MultivariateMatrixFunction getJacobianFunction(
				List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			final List<IndependentPair<? extends Point2d, ? extends Point2d>> dataInv = IndependentPair.swapList(data);
			return SINGLE_IMAGE_TRANSFER.getJacobianFunction(dataInv);
		}

		@Override
		public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			final Matrix hInv = initial.inverse();
			final List<IndependentPair<? extends Point2d, ? extends Point2d>> dataInv = IndependentPair.swapList(data);

			return SINGLE_IMAGE_TRANSFER.refine(hInv, dataInv).inverse();
		}
	},
	/**
	 * The points in the first image are projected by the homography matrix to
	 * produce new estimates of the second image points and the second image
	 * point projected by the inverse homography to produce estimates of the
	 * first. Residuals are computed from both point sets and minimised by the
	 * optimiser.
	 * <p>
	 * Value and analytic Jacobian implementations auto-generated from Matlab
	 * using the following:
	 * 
	 * <pre>
	 * <code>
	 * syms h0 h1 h2 h3 h4 h5 h6 h7 h8 real
	 * syms X1 Y1 X2 Y2 real
	 * M1 = [X1 Y1 1]';
	 * M2 = [X2 Y2 1]';
	 * H = [h0 h1 h2; h3 h4 h5; h6 h7 h8];
	 * Hi = inv(H);
	 * H1 = H(1,:);
	 * H2 = H(2,:);
	 * H3 = H(3,:);
	 * H1i = Hi(1,:);
	 * H2i = Hi(2,:);
	 * H3i = Hi(3,:);
	 * mihat = [(1 / (H3i*M2)) * [H1i*M2; H2i*M2]; (1 / (H3*M1)) * [H1*M1; H2*M1]];
	 * J = jacobian(mihat, [h0,h1,h2,h3,h4,h5,h6,h7,h8]);
	 * ccode(mihat, 'file', 'symImageTransfer_value.c')
	 * ccode(J, 'file', 'symImageTransfer_jacobian.c')
	 * 
	 * </code>
	 * </pre>
	 */
	SYMMETRIC_TRANSFER {
		@Override
		protected MultivariateVectorFunction getValueFunction(
				final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return new MultivariateVectorFunction() {
				@Override
				public double[] value(double[] h) throws IllegalArgumentException {
					final double[] result = new double[data.size() * 4];

					for (int i = 0; i < data.size(); i++) {
						final float X1 = data.get(i).firstObject().getX();
						final float Y1 = data.get(i).firstObject().getY();
						final float X2 = data.get(i).secondObject().getX();
						final float Y2 = data.get(i).secondObject().getY();

						final double t2 = h[0] * h[4] * h[8];
						final double t3 = h[1] * h[5] * h[6];
						final double t4 = h[2] * h[3] * h[7];
						final double t7 = h[0] * h[5] * h[7];
						final double t8 = h[1] * h[3] * h[8];
						final double t9 = h[2] * h[4] * h[6];
						final double t5 = t2 + t3 + t4 - t7 - t8 - t9;
						final double t6 = 1.0 / t5;
						final double t10 = h[0] * h[4];
						final double t11 = t10 - h[1] * h[3];
						final double t12 = t6 * t11;
						final double t13 = h[3] * h[7];
						final double t14 = t13 - h[4] * h[6];
						final double t15 = X2 * t6 * t14;
						final double t16 = h[0] * h[7];
						final double t17 = t16 - h[1] * h[6];
						final double t18 = t12 + t15 - Y2 * t6 * t17;
						final double t19 = 1.0 / t18;
						final double t20 = X1 * h[6];
						final double t21 = Y1 * h[7];
						final double t22 = h[8] + t20 + t21;
						final double t23 = 1.0 / t22;
						result[4 * i + 0] = t19
								* (t6 * (h[1] * h[5] - h[2] * h[4]) + X2 * t6 * (h[4] * h[8] - h[5] * h[7]) - Y2 * t6
										* (h[1] * h[8] - h[2] * h[7]));
						result[4 * i + 1] = -t19
								* (t6 * (h[0] * h[5] - h[2] * h[3]) + X2 * t6 * (h[3] * h[8] - h[5] * h[6]) - Y2 * t6
										* (h[0] * h[8] - h[2] * h[6]));
						result[4 * i + 2] = t23 * (h[2] + X1 * h[0] + Y1 * h[1]);
						result[4 * i + 3] = t23 * (h[5] + X1 * h[3] + Y1 * h[4]);
					}
					return result;
				}
			};
		}

		@Override
		public double computeError(Matrix h, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			final Matrix hInv = h.inverse();
			double error = 0;
			for (int i = 0; i < data.size(); i++) {
				final Point2d p1 = data.get(i).firstObject();
				final Point2d p1t = p1.transform(h);
				final Point2d p2 = data.get(i).secondObject();
				final Point2d p2t = p2.transform(hInv);

				final float dx1 = p1t.getX() - p2.getX();
				final float dy1 = p1t.getY() - p2.getY();
				final float dx2 = p1.getX() - p2t.getX();
				final float dy2 = p1.getY() - p2t.getY();
				error += dx1 * dx1 + dy1 * dy1 + dx2 * dx2 + dy2 * dy2;
			}
			return error;
		}

		@Override
		protected MultivariateMatrixFunction getJacobianFunction(
				final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
		{
			return new MultivariateMatrixFunction() {
				// See Multi View Geometry in Computer Vision, p147
				@Override
				public double[][] value(double[] h)
				{
					final double[][] result = new double[4 * data.size()][9];

					for (int i = 0; i < data.size(); i++) {
						final float X1 = data.get(i).firstObject().getX();
						final float Y1 = data.get(i).firstObject().getY();
						final float X2 = data.get(i).secondObject().getX();
						final float Y2 = data.get(i).secondObject().getY();

						final double t3 = h[4] * h[8];
						final double t4 = h[5] * h[7];
						final double t2 = t3 - t4;
						final double t5 = h[0] * h[4] * h[8];
						final double t6 = h[1] * h[5] * h[6];
						final double t7 = h[2] * h[3] * h[7];
						final double t10 = h[0] * h[5] * h[7];
						final double t11 = h[1] * h[3] * h[8];
						final double t12 = h[2] * h[4] * h[6];
						final double t8 = t5 + t6 + t7 - t10 - t11 - t12;
						final double t9 = 1.0 / (t8 * t8);
						final double t13 = 1.0 / t8;
						final double t14 = h[0] * h[4];
						final double t27 = h[1] * h[3];
						final double t15 = t14 - t27;
						final double t16 = t13 * t15;
						final double t17 = h[3] * h[7];
						final double t28 = h[4] * h[6];
						final double t18 = t17 - t28;
						final double t19 = X2 * t13 * t18;
						final double t20 = h[0] * h[7];
						final double t29 = h[1] * h[6];
						final double t21 = t20 - t29;
						final double t30 = Y2 * t13 * t21;
						final double t22 = t16 + t19 - t30;
						final double t23 = h[1] * h[5];
						final double t32 = h[2] * h[4];
						final double t24 = t23 - t32;
						final double t25 = h[1] * h[8];
						final double t35 = h[2] * h[7];
						final double t26 = t25 - t35;
						final double t31 = 1.0 / t22;
						final double t33 = h[3] * h[8];
						final double t36 = h[5] * h[6];
						final double t34 = t33 - t36;
						final double t37 = 1.0 / (t22 * t22);
						final double t38 = t13 * t24;
						final double t39 = X2 * t2 * t13;
						final double t43 = Y2 * t13 * t26;
						final double t40 = t38 + t39 - t43;
						final double t41 = Y2 * h[7] * t13;
						final double t42 = X2 * t2 * t9 * t18;
						final double t44 = h[0] * h[8];
						final double t46 = h[2] * h[6];
						final double t45 = t44 - t46;
						final double t47 = X2 * h[7] * t13;
						final double t48 = h[0] * h[5];
						final double t50 = h[2] * h[3];
						final double t49 = t48 - t50;
						final double t51 = t9 * t15 * t24;
						final double t52 = X2 * h[4] * t13;
						final double t53 = h[5] * t13;
						final double t54 = X2 * t2 * t9 * t34;
						final double t55 = h[4] * t13;
						final double t56 = t2 * t9 * t15;
						final double t57 = t13 * t49;
						final double t58 = X2 * t13 * t34;
						final double t69 = Y2 * t13 * t45;
						final double t59 = t57 + t58 - t69;
						final double t60 = t9 * t15 * t34;
						final double t61 = Y2 * h[6] * t13;
						final double t62 = X2 * t9 * t18 * t34;
						final double t64 = h[3] * t13;
						final double t63 = t60 + t61 + t62 - t64 - Y2 * t9 * t21 * t34;
						final double t65 = t18 * t18;
						final double t66 = X2 * t9 * t65;
						final double t67 = t9 * t15 * t18;
						final double t68 = t66 + t67 - Y2 * t9 * t18 * t21;
						final double t70 = h[2] * t13;
						final double t71 = h[1] * t13;
						final double t72 = t9 * t15 * t26;
						final double t73 = X2 * t9 * t18 * t26;
						final double t74 = t9 * t15 * t45;
						final double t75 = X2 * h[6] * t13;
						final double t76 = X2 * t9 * t18 * t45;
						final double t78 = h[0] * t13;
						final double t79 = Y2 * t9 * t21 * t45;
						final double t77 = t74 + t75 + t76 - t78 - t79;
						final double t80 = t21 * t21;
						final double t81 = t9 * t15 * t21;
						final double t82 = X2 * t9 * t18 * t21;
						final double t83 = t81 + t82 - Y2 * t9 * t80;
						final double t84 = t9 * t24 * t49;
						final double t85 = Y2 * h[2] * t13;
						final double t86 = Y2 * h[1] * t13;
						final double t87 = X2 * t9 * t18 * t24;
						final double t88 = t9 * t15 * t49;
						final double t89 = X2 * h[3] * t13;
						final double t90 = X2 * t9 * t18 * t49;
						final double t92 = Y2 * h[0] * t13;
						final double t91 = t88 + t89 + t90 - t92 - Y2 * t9 * t21 * t49;
						final double t93 = t15 * t15;
						final double t94 = t9 * t93;
						final double t95 = X2 * t9 * t15 * t18;
						final double t96 = t94 + t95 - Y2 * t9 * t15 * t21;
						final double t97 = X1 * h[6];
						final double t98 = Y1 * h[7];
						final double t99 = h[8] + t97 + t98;
						final double t100 = 1.0 / t99;
						final double t101 = X1 * h[0];
						final double t102 = Y1 * h[1];
						final double t103 = h[2] + t101 + t102;
						final double t104 = 1.0 / (t99 * t99);
						final double t105 = X1 * t100;
						final double t106 = Y1 * t100;
						final double t107 = X1 * h[3];
						final double t108 = Y1 * h[4];
						final double t109 = h[5] + t107 + t108;
						result[4 * i + 0][0] = -t31 * (t2 * t9 * t24 + X2 * (t2 * t2) * t9 - Y2 * t2 * t9 * t26) + t37
								* t40
								* (t41 + t42 + t56 - h[4] * t13 - Y2 * t2 * t9 * t21);
						result[4 * i + 0][1] = t31 * (t53 + t54 - Y2 * h[8] * t13 + t9 * t24 * t34 - Y2 * t9 * t26 * t34)
								- t37 * t40
								* t63;
						result[4 * i + 0][2] = -t31 * (-t41 + t42 + t55 + t9 * t18 * t24 - Y2 * t9 * t18 * t26) + t37
								* t40 * t68;
						result[4 * i + 0][3] = t31 * (t9 * t24 * t26 - Y2 * t9 * (t26 * t26) + X2 * t2 * t9 * t26) - t37
								* t40
								* (t47 + t72 + t73 - h[1] * t13 - Y2 * t9 * t21 * t26);
						result[4 * i + 0][4] = -t31
								* (t70 - X2 * h[8] * t13 + t9 * t24 * t45 + X2 * t2 * t9 * t45 - Y2 * t9 * t26 * t45)
								+ t37 * t40 * t77;
						result[4 * i + 0][5] = t31
								* (-t47 + t71 + t9 * t21 * t24 + X2 * t2 * t9 * t21 - Y2 * t9 * t21 * t26) - t37
								* t40 * t83;
						result[4 * i + 0][6] = -t31 * (t9 * (t24 * t24) + X2 * t2 * t9 * t24 - Y2 * t9 * t24 * t26) + t37
								* t40
								* (t51 + t52 + t87 - Y2 * h[1] * t13 - Y2 * t9 * t21 * t24);
						result[4 * i + 0][7] = t31
								* (t84 + t85 - X2 * h[5] * t13 + X2 * t2 * t9 * t49 - Y2 * t9 * t26 * t49) - t37
								* t40 * t91;
						result[4 * i + 0][8] = -t31 * (t51 - t52 + t86 + X2 * t2 * t9 * t15 - Y2 * t9 * t15 * t26) + t37
								* t40 * t96;
						result[4 * i + 1][0] = t31 * (-t53 + t54 + Y2 * h[8] * t13 + t2 * t9 * t49 - Y2 * t2 * t9 * t45)
								- t37 * t59
								* (t41 + t42 - t55 + t56 - Y2 * t2 * t9 * t21);
						result[4 * i + 1][1] = -t31 * (t9 * t34 * t49 + X2 * t9 * (t34 * t34) - Y2 * t9 * t34 * t45)
								+ t37 * t59
								* t63;
						result[4 * i + 1][2] = t31 * (-t61 + t62 + t64 + t9 * t18 * t49 - Y2 * t9 * t18 * t45) - t37
								* t59 * t68;
						result[4 * i + 1][3] = -t31
								* (-t70 + X2 * h[8] * t13 + t9 * t26 * t49 + X2 * t9 * t26 * t34 - Y2 * t9 * t26 * t45)
								+ t37 * t59 * (t47 - t71 + t72 + t73 - Y2 * t9 * t21 * t26);
						result[4 * i + 1][4] = t31 * (t9 * t45 * t49 - Y2 * t9 * (t45 * t45) + X2 * t9 * t34 * t45) - t37
								* t59 * t77;
						result[4 * i + 1][5] = -t31 * (-t75 + t78 - t79 + t9 * t21 * t49 + X2 * t9 * t21 * t34) + t37
								* t59 * t83;
						result[4 * i + 1][6] = t31
								* (t84 - t85 + X2 * h[5] * t13 + X2 * t9 * t24 * t34 - Y2 * t9 * t24 * t45) - t37
								* t59 * (t51 + t52 - t86 + t87 - Y2 * t9 * t21 * t24);
						result[4 * i + 1][7] = -t31 * (t9 * (t49 * t49) + X2 * t9 * t34 * t49 - Y2 * t9 * t45 * t49)
								+ t37 * t59
								* t91;
						result[4 * i + 1][8] = t31 * (t88 - t89 + t92 + X2 * t9 * t15 * t34 - Y2 * t9 * t15 * t45) - t37
								* t59 * t96;
						result[4 * i + 2][0] = t105;
						result[4 * i + 2][1] = t106;
						result[4 * i + 2][2] = t100;
						result[4 * i + 2][6] = -X1 * t103 * t104;
						result[4 * i + 2][7] = -Y1 * t103 * t104;
						result[4 * i + 2][8] = -t103 * t104;
						result[4 * i + 3][3] = t105;
						result[4 * i + 3][4] = t106;
						result[4 * i + 3][5] = t100;
						result[4 * i + 3][6] = -X1 * t104 * t109;
						result[4 * i + 3][7] = -Y1 * t104 * t109;
						result[4 * i + 3][8] = -t104 * t109;

					}

					return result;
				}
			};
		}

		@Override
		public Matrix refine(Matrix initial, List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data) {
			final LevenbergMarquardtOptimizer lm = new LevenbergMarquardtOptimizer();
			final RealVector start = new ArrayRealVector(initial.getRowPackedCopy());
			final RealVector observed = toRealVector(data);
			final int maxEvaluations = 1000;
			final int maxIterations = 1000;

			final MultivariateVectorFunction value = getValueFunction(data);
			final MultivariateMatrixFunction jacobian = getJacobianFunction(data);
			final MultivariateJacobianFunction model = LeastSquaresFactory.model(value, jacobian);

			final Optimum result = lm.optimize(LeastSquaresFactory.create(model,
					observed, start, null, maxEvaluations, maxIterations));

			final Matrix improved = MatrixUtils.fromRowPacked(result.getPoint().toArray(), 3);
			MatrixUtils.times(improved, 1.0 / improved.get(2, 2));

			return improved;
		}
	};

	protected abstract MultivariateVectorFunction getValueFunction(
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data);

	protected abstract MultivariateMatrixFunction getJacobianFunction(
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data);

	/**
	 * Compute the error value being optimised between the two point sets.
	 * Actual computation depends on the specific {@link HomographyRefinement}
	 * method in use.
	 * 
	 * @param h
	 *            the homography
	 * @param data
	 *            the data point-pairs
	 * @return the error value
	 */
	public abstract double computeError(Matrix h,
			List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data);

	private static RealVector toRealVector(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data,
			boolean first)
	{
		final double[] vec = new double[data.size() * 2];

		if (first) {
			for (int i = 0; i < data.size(); i++) {
				vec[i * 2 + 0] = data.get(i).firstObject().getX();
				vec[i * 2 + 1] = data.get(i).firstObject().getY();
			}
		} else {
			for (int i = 0; i < data.size(); i++) {
				vec[i * 2 + 0] = data.get(i).secondObject().getX();
				vec[i * 2 + 1] = data.get(i).secondObject().getY();
			}
		}

		return new ArrayRealVector(vec);
	}

	private static RealVector toRealVector(List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data)
	{
		final double[] vec = new double[data.size() * 4];

		for (int i = 0; i < data.size(); i++) {
			vec[i * 4 + 0] = data.get(i).firstObject().getX();
			vec[i * 4 + 1] = data.get(i).firstObject().getY();
			vec[i * 4 + 2] = data.get(i).secondObject().getX();
			vec[i * 4 + 3] = data.get(i).secondObject().getY();
		}

		return new ArrayRealVector(vec);
	}

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
	public abstract Matrix refine(Matrix initial,
			List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data);
}
