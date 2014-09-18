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
package org.openimaj.image.camera.calibration;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresFactory;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.openimaj.image.camera.Camera;
import org.openimaj.image.camera.CameraIntrinsics;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

public class CameraCalibration extends CameraCalibrationZhang {

	public CameraCalibration(List<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>> points,
			int width, int height)
	{
		super(points, width, height);
	}

	@Override
	protected void performCalibration(int width, int height) {
		// compute the homographies
		final List<Matrix> homographies = new ArrayList<Matrix>();
		for (int i = 0; i < points.size(); i++) {
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> data = points.get(i);
			final Matrix h = HomographyRefinement.SINGLE_IMAGE_TRANSFER.refine(
					TransformUtilities.homographyMatrixNorm(data), data);

			homographies.add(h);
		}

		// intial estimate of intrisics and extrinsics
		estimateIntrisicAndExtrinsics(homographies, width, height);

		// non-linear optimisation using analytic jacobian
		refine();
	}

	/**
	 * This is the implementation of the value function for the optimiser. It
	 * computes the predicted location of an image point by projecting a model
	 * point through the camera homography and then applying the distortion. The
	 * implementation is converted from the C code produced by the following
	 * matlab symbolic code:
	 * 
	 * <pre>
	 * <code>
	 * syms u0 v0 fx fy sk real
	 * syms tx ty tz wx wy wz real
	 * syms k1 k2 k3 p1 p2 real
	 * syms X Y real
	 * 
	 * % the intrinsic parameter matrix
	 * K=[fx sk u0; 0 fy v0; 0 0 1];
	 * 
	 * % Expression for the rotation matrix based on the Rodrigues formula
	 * theta=sqrt(wx^2+wy^2+wz^2);
	 * omega=[0 -wz wy; wz 0 -wx; -wy wx 0];
	 * R = eye(3) + (sin(theta)/theta)*omega + ((1-cos(theta))/theta^2)*(omega*omega);
	 * 
	 * % Expression for the translation vector
	 * t=[tx;ty;tz];
	 * 
	 * % perspective projection of the model point (X,Y)
	 * uvs=K*[R(:,1) R(:,2) t]*[X; Y; 1];
	 * u=uvs(1)/uvs(3);
	 * v=uvs(2)/uvs(3);
	 * 
	 * % application of 2-term radial distortion
	 * uu0 = u - u0;
	 * vv0 = v - v0;
	 * x =  uu0/fx;
	 * y =  vv0/fy;
	 * r2 = x*x + y*y;
	 * r4 = r2*r2;
	 * r6 = r2*r2*r2;
	 * uv = [u + uu0*(k1*r2 + k2*r4 + k3*r6) + 2*p1*vv0 + p2*(r2 + 2*uu0^2);
	 *       v + vv0*(k1*r2 + k2*r4 + k3*r6) + p1*(r2 + 2*vv0^2) + 2*p2*uu0];
	 * ccode(uv, 'file', 'calibrate-value.c')
	 * </code>
	 * </pre>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	private class Value implements MultivariateVectorFunction {
		@Override
		public double[] value(double[] params) throws IllegalArgumentException {
			int totalPoints = 0;
			for (int i = 0; i < points.size(); i++)
				totalPoints += points.get(i).size();

			final double[] result = new double[2 * totalPoints];

			for (int i = 0, k = 0; i < points.size(); i++) {
				for (int j = 0; j < points.get(i).size(); j++, k++) {
					final double[] tmp = computeValue(i, j, params);
					result[k * 2 + 0] = tmp[0];
					result[k * 2 + 1] = tmp[1];
				}
			}

			return result;
		}

		private double[] computeValue(int img, int point, double[] params) {
			final double[][] A0 = new double[2][1];

			final double X = points.get(img).get(point).firstObject().getX();
			final double Y = points.get(img).get(point).firstObject().getY();

			final double fx = params[0];
			final double fy = params[1];
			final double u0 = params[2];
			final double v0 = params[3];
			final double sk = params[4];
			final double k1 = params[5];
			final double k2 = params[6];
			final double k3 = params[7];
			final double p1 = params[8];
			final double p2 = params[9];

			final double wx = params[img * 6 + 10];
			final double wy = params[img * 6 + 11];
			final double wz = params[img * 6 + 12];
			final double tx = params[img * 6 + 13];
			final double ty = params[img * 6 + 14];
			final double tz = params[img * 6 + 15];

			// begin matlab code
			final double t2 = wx * wx;
			final double t3 = wy * wy;
			final double t4 = wz * wz;
			final double t5 = t2 + t3 + t4;
			final double t6 = sqrt(t5);
			final double t7 = sin(t6);
			final double t8 = 1.0 / sqrt(t5);
			final double t9 = cos(t6);
			final double t10 = t9 - 1.0;
			final double t11 = 1.0 / t5;
			final double t12 = t7 * t8 * wy;
			final double t13 = t10 * t11 * wx * wz;
			final double t14 = t12 + t13;
			final double t15 = t7 * t8 * wz;
			final double t16 = t7 * t8 * wx;
			final double t18 = t10 * t11 * wy * wz;
			final double t17 = t16 - t18;
			final double t19 = Y * t17;
			final double t38 = X * t14;
			final double t20 = t19 - t38 + tz;
			final double t21 = 1.0 / t20;
			final double t22 = t10 * t11 * wx * wy;
			final double t23 = t3 + t4;
			final double t24 = t10 * t11 * t23;
			final double t25 = t24 + 1.0;
			final double t26 = fx * t25;
			final double t27 = t15 + t22;
			final double t28 = t17 * u0;
			final double t29 = t2 + t4;
			final double t30 = t10 * t11 * t29;
			final double t31 = t30 + 1.0;
			final double t32 = sk * t31;
			final double t43 = fx * t27;
			final double t33 = t28 + t32 - t43;
			final double t34 = Y * t33;
			final double t35 = fx * tx;
			final double t36 = sk * ty;
			final double t37 = tz * u0;
			final double t39 = t15 - t22;
			final double t40 = sk * t39;
			final double t48 = t14 * u0;
			final double t41 = t26 + t40 - t48;
			final double t42 = X * t41;
			final double t44 = t34 + t35 + t36 + t37 + t42;
			final double t49 = t21 * t44;
			final double t45 = -t49 + u0;
			final double t53 = fy * ty;
			final double t54 = fy * t39;
			final double t55 = t14 * v0;
			final double t56 = t54 - t55;
			final double t57 = X * t56;
			final double t58 = tz * v0;
			final double t59 = t17 * v0;
			final double t60 = fy * t31;
			final double t61 = t59 + t60;
			final double t62 = Y * t61;
			final double t63 = t53 + t57 + t58 + t62;
			final double t64 = t21 * t63;
			final double t46 = -t64 + v0;
			final double t47 = 1.0 / (fx * fx);
			final double t50 = t45 * t45;
			final double t51 = t47 * t50;
			final double t52 = 1.0 / (fy * fy);
			final double t65 = t46 * t46;
			final double t66 = t52 * t65;
			final double t67 = t51 + t66;
			final double t68 = t67 * t67;
			final double t69 = k1 * t67;
			final double t70 = k2 * t68;
			final double t71 = k3 * t67 * t68;
			final double t72 = t69 + t70 + t71;
			A0[0][0] = -t45 * t72 + t21
					* (t34 + t35 + t36 + t37 + X * (t26 - t14 * u0 + sk * (t15 - t10 * t11 * wx * wy))) + p2
					* (t50 * 2.0 + t51 + t66) + p1 * t45 * t46 * 2.0;
			A0[1][0] = t64 - t46 * t72 + p1 * (t51 + t65 * 2.0 + t66) + p2 * t45 * t46 * 2.0;

			// end matlab code

			return new double[] { A0[0][0], A0[1][0] };
		}
	}

	/**
	 * This is the implementation of the Jacobian function for the optimiser; it
	 * is the partial derivative of the value function with respect to the
	 * parameters. The implementation is based on the matlab symbolic code:
	 * 
	 * <pre>
	 * <code>
	 * syms u0 v0 fx fy sk real
	 * syms tx ty tz wx wy wz real
	 * syms k1 k2 k3 p1 p2 real
	 * syms X Y real
	 * 
	 * % the intrinsic parameter matrix
	 * K=[fx sk u0; 0 fy v0; 0 0 1];
	 * 
	 * % Expression for the rotation matrix based on the Rodrigues formula
	 * theta=sqrt(wx^2+wy^2+wz^2);
	 * omega=[0 -wz wy; wz 0 -wx; -wy wx 0];
	 * R = eye(3) + (sin(theta)/theta)*omega + ((1-cos(theta))/theta^2)*(omega*omega);
	 * 
	 * % Expression for the translation vector
	 * t=[tx;ty;tz];
	 * 
	 * % perspective projection of the model point (X,Y)
	 * uvs=K*[R(:,1) R(:,2) t]*[X; Y; 1];
	 * u=uvs(1)/uvs(3);
	 * v=uvs(2)/uvs(3);
	 * 
	 * % application of 2-term radial distortion
	 * uu0 = u - u0;
	 * vv0 = v - v0;
	 * x =  uu0/fx;
	 * y =  vv0/fy;
	 * r2 = x*x + y*y;
	 * r4 = r2*r2;
	 * r6 = r2*r2*r2;
	 * uv = [u + uu0*(k1*r2 + k2*r4 + k3*r6) + 2*p1*vv0 + p2*(r2 + 2*uu0^2);
	 *       v + vv0*(k1*r2 + k2*r4 + k3*r6) + p1*(r2 + 2*vv0^2) + 2*p2*uu0];
	 * J=jacobian(uv,[fx,fy,u0,v0,sk,k1,k2,k3,p1,p2 wx wy wz tx ty tz]);  
	 * ccode(J, 'file', 'calibrate-jacobian.c')
	 * </code>
	 * </pre>
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	private class Jacobian implements MultivariateMatrixFunction {
		@Override
		public double[][] value(double[] params) {
			// Note that we're building the jacobian for all cameras/images and
			// points. The params vector is 10 + 6*numCameras elements long (10
			// intrinsic params and 6 extrinsic per camera)
			int totalPoints = 0;
			for (int i = 0; i < points.size(); i++)
				totalPoints += points.get(i).size();

			final double[][] result = new double[2 * totalPoints][];

			for (int i = 0, k = 0; i < points.size(); i++) {
				for (int j = 0; j < points.get(i).size(); j++, k++) {
					final double[][] tmp = computeJacobian(i, j, params);

					result[k * 2 + 0] = tmp[0];
					result[k * 2 + 1] = tmp[1];
				}
			}

			return result;
		}

		private double[][] computeJacobian(int img, int point, double[] params) {
			final double[][] A0 = new double[2][16];

			final double X = points.get(img).get(point).firstObject().getX();
			final double Y = points.get(img).get(point).firstObject().getY();

			final double fx = params[0];
			final double fy = params[1];
			final double u0 = params[2];
			final double v0 = params[3];
			final double sk = params[4];
			final double k1 = params[5];
			final double k2 = params[6];
			final double k3 = params[7];
			final double p1 = params[8];
			final double p2 = params[9];

			final double wx = params[img * 6 + 10];
			final double wy = params[img * 6 + 11];
			final double wz = params[img * 6 + 12];
			final double tx = params[img * 6 + 13];
			final double ty = params[img * 6 + 14];
			final double tz = params[img * 6 + 15];

			// begin matlab code
			final double t2 = wx * wx;
			final double t3 = wy * wy;
			final double t4 = wz * wz;
			final double t5 = t2 + t3 + t4;
			final double t6 = sqrt(t5);
			final double t7 = sin(t6);
			final double t8 = 1.0 / sqrt(t5);
			final double t9 = cos(t6);
			final double t10 = t9 - 1.0;
			final double t11 = 1.0 / t5;
			final double t12 = t7 * t8 * wy;
			final double t13 = t10 * t11 * wx * wz;
			final double t14 = t12 + t13;
			final double t15 = t7 * t8 * wz;
			final double t16 = t7 * t8 * wx;
			final double t18 = t10 * t11 * wy * wz;
			final double t17 = t16 - t18;
			final double t19 = Y * t17;
			final double t39 = X * t14;
			final double t20 = t19 - t39 + tz;
			final double t21 = 1.0 / t20;
			final double t22 = t10 * t11 * wx * wy;
			final double t23 = t3 + t4;
			final double t24 = t10 * t11 * t23;
			final double t25 = t24 + 1.0;
			final double t26 = fx * t25;
			final double t27 = t15 + t22;
			final double t28 = t17 * u0;
			final double t29 = t2 + t4;
			final double t30 = t10 * t11 * t29;
			final double t31 = t30 + 1.0;
			final double t32 = sk * t31;
			final double t45 = fx * t27;
			final double t33 = t28 + t32 - t45;
			final double t34 = Y * t33;
			final double t35 = fx * tx;
			final double t36 = sk * ty;
			final double t37 = tz * u0;
			final double t40 = t15 - t22;
			final double t41 = sk * t40;
			final double t42 = t14 * u0;
			final double t43 = t26 + t41 - t42;
			final double t44 = X * t43;
			final double t46 = t34 + t35 + t36 + t37 + t44;
			final double t47 = t21 * t46;
			final double t38 = -t47 + u0;
			final double t48 = 1.0 / (fx * fx * fx);
			final double t49 = t38 * t38;
			final double t50 = t48 * t49 * 2.0;
			final double t51 = 1.0 / (fx * fx);
			final double t52 = X * t25;
			final double t57 = Y * t27;
			final double t53 = t52 - t57 + tx;
			final double t54 = t21 * t38 * t51 * t53 * 2.0;
			final double t55 = t50 + t54;
			final double t60 = fy * ty;
			final double t61 = fy * t40;
			final double t62 = t14 * v0;
			final double t63 = t61 - t62;
			final double t64 = X * t63;
			final double t65 = tz * v0;
			final double t66 = t17 * v0;
			final double t67 = fy * t31;
			final double t68 = t66 + t67;
			final double t69 = Y * t68;
			final double t70 = t60 + t64 + t65 + t69;
			final double t71 = t21 * t70;
			final double t56 = -t71 + v0;
			final double t58 = t49 * t51;
			final double t59 = 1.0 / (fy * fy);
			final double t72 = t56 * t56;
			final double t73 = t59 * t72;
			final double t74 = t58 + t73;
			final double t75 = t74 * t74;
			final double t76 = 1.0 / (fy * fy * fy);
			final double t77 = t72 * t76 * 2.0;
			final double t78 = X * t40;
			final double t79 = Y * t31;
			final double t80 = t78 + t79 + ty;
			final double t81 = t21 * t56 * t59 * t80 * 2.0;
			final double t82 = t77 + t81;
			final double t83 = k1 * t74;
			final double t84 = k2 * t75;
			final double t85 = k3 * t74 * t75;
			final double t86 = t83 + t84 + t85;
			final double t87 = 1.0 / pow(t5, 3.0 / 2.0);
			final double t88 = 1.0 / (t5 * t5);
			final double t89 = t7 * t87 * wx * wy;
			final double t90 = t2 * t7 * t87 * wz;
			final double t91 = t2 * t10 * t88 * wz * 2.0;
			final double t102 = t10 * t11 * wz;
			final double t103 = t9 * t11 * wx * wy;
			final double t92 = t89 + t90 + t91 - t102 - t103;
			final double t93 = t7 * t8;
			final double t94 = t2 * t9 * t11;
			final double t95 = t10 * t88 * wx * wy * wz * 2.0;
			final double t96 = t7 * t87 * wx * wy * wz;
			final double t109 = t2 * t7 * t87;
			final double t97 = t93 + t94 + t95 + t96 - t109;
			final double t98 = t9 * t11 * wx * wz;
			final double t99 = t2 * t7 * t87 * wy;
			final double t100 = t2 * t10 * t88 * wy * 2.0;
			final double t107 = t10 * t11 * wy;
			final double t108 = t7 * t87 * wx * wz;
			final double t101 = t98 + t99 + t100 - t107 - t108;
			final double t104 = t10 * t29 * t88 * wx * 2.0;
			final double t105 = t7 * t29 * t87 * wx;
			final double t114 = t10 * t11 * wx * 2.0;
			final double t106 = t104 + t105 - t114;
			final double t110 = X * t92;
			final double t111 = Y * t97;
			final double t112 = t110 + t111;
			final double t113 = 1.0 / (t20 * t20);
			final double t115 = fy * t106;
			final double t142 = t97 * v0;
			final double t116 = t115 - t142;
			final double t117 = Y * t116;
			final double t118 = fy * t101;
			final double t119 = t92 * v0;
			final double t120 = t118 + t119;
			final double t143 = X * t120;
			final double t121 = t117 - t143;
			final double t122 = t21 * t121;
			final double t123 = t70 * t112 * t113;
			final double t124 = t122 + t123;
			final double t125 = t10 * t23 * t88 * wx * 2.0;
			final double t126 = t7 * t23 * t87 * wx;
			final double t127 = t125 + t126;
			final double t128 = sk * t101;
			final double t129 = t92 * u0;
			final double t145 = fx * t127;
			final double t130 = t128 + t129 - t145;
			final double t131 = X * t130;
			final double t132 = -t98 + t99 + t100 - t107 + t108;
			final double t133 = fx * t132;
			final double t134 = t97 * u0;
			final double t146 = sk * t106;
			final double t135 = t133 + t134 - t146;
			final double t136 = Y * t135;
			final double t137 = t131 + t136;
			final double t138 = t21 * t137;
			final double t147 = t46 * t112 * t113;
			final double t139 = t138 - t147;
			final double t140 = t38 * t51 * t139 * 2.0;
			final double t144 = t56 * t59 * t124 * 2.0;
			final double t141 = t140 - t144;
			final double t148 = t7 * t87 * wy * wz;
			final double t149 = t3 * t7 * t87 * wx;
			final double t150 = t3 * t10 * t88 * wx * 2.0;
			final double t151 = t10 * t29 * t88 * wy * 2.0;
			final double t152 = t7 * t29 * t87 * wy;
			final double t153 = t151 + t152;
			final double t154 = t9 * t11 * wy * wz;
			final double t155 = t3 * t7 * t87 * wz;
			final double t156 = t3 * t10 * t88 * wz * 2.0;
			final double t157 = -t89 - t102 + t103 + t155 + t156;
			final double t158 = t157 * u0;
			final double t159 = t10 * t23 * t88 * wy * 2.0;
			final double t160 = t7 * t23 * t87 * wy;
			final double t174 = t10 * t11 * wy * 2.0;
			final double t161 = t159 + t160 - t174;
			final double t170 = t10 * t11 * wx;
			final double t162 = -t148 + t149 + t150 + t154 - t170;
			final double t163 = sk * t162;
			final double t164 = t3 * t7 * t87;
			final double t169 = t3 * t9 * t11;
			final double t165 = -t93 + t95 + t96 + t164 - t169;
			final double t166 = t165 * u0;
			final double t175 = fx * t161;
			final double t167 = t163 + t166 - t175;
			final double t168 = X * t167;
			final double t171 = Y * t157;
			final double t172 = X * t165;
			final double t173 = t171 + t172;
			final double t176 = t148 + t149 + t150 - t154 - t170;
			final double t177 = fx * t176;
			final double t183 = sk * t153;
			final double t178 = t158 + t177 - t183;
			final double t179 = Y * t178;
			final double t180 = t168 + t179;
			final double t181 = t21 * t180;
			final double t184 = t46 * t113 * t173;
			final double t182 = t181 - t184;
			final double t185 = fy * t162;
			final double t186 = t165 * v0;
			final double t187 = t185 + t186;
			final double t188 = X * t187;
			final double t189 = fy * t153;
			final double t196 = t157 * v0;
			final double t190 = t189 - t196;
			final double t197 = Y * t190;
			final double t191 = t188 - t197;
			final double t192 = t21 * t191;
			final double t198 = t70 * t113 * t173;
			final double t193 = t192 - t198;
			final double t194 = t56 * t59 * t193 * 2.0;
			final double t195 = t38 * t51 * t182 * 2.0;
			final double t199 = t194 + t195;
			final double t200 = t4 * t9 * t11;
			final double t201 = t4 * t7 * t87 * wx;
			final double t202 = t4 * t10 * t88 * wx * 2.0;
			final double t203 = t148 - t154 - t170 + t201 + t202;
			final double t204 = t4 * t7 * t87 * wy;
			final double t205 = t4 * t10 * t88 * wy * 2.0;
			final double t206 = t98 - t107 - t108 + t204 + t205;
			final double t207 = t4 * t7 * t87;
			final double t208 = t10 * t29 * t88 * wz * 2.0;
			final double t209 = t7 * t29 * t87 * wz;
			final double t214 = t10 * t11 * wz * 2.0;
			final double t210 = t208 + t209 - t214;
			final double t211 = X * t203;
			final double t212 = Y * t206;
			final double t213 = t211 + t212;
			final double t215 = t10 * t23 * t88 * wz * 2.0;
			final double t216 = t7 * t23 * t87 * wz;
			final double t217 = t203 * u0;
			final double t218 = t93 + t95 + t96 + t200 - t207;
			final double t219 = t206 * u0;
			final double t220 = -t93 + t95 + t96 - t200 + t207;
			final double t221 = fx * t220;
			final double t238 = sk * t210;
			final double t222 = t219 + t221 - t238;
			final double t223 = Y * t222;
			final double t224 = t203 * v0;
			final double t225 = fy * t218;
			final double t226 = t224 + t225;
			final double t227 = X * t226;
			final double t228 = fy * t210;
			final double t244 = t206 * v0;
			final double t229 = t228 - t244;
			final double t245 = Y * t229;
			final double t230 = t227 - t245;
			final double t231 = t21 * t230;
			final double t246 = t70 * t113 * t213;
			final double t232 = t231 - t246;
			final double t233 = t56 * t59 * t232 * 2.0;
			final double t234 = -t214 + t215 + t216;
			final double t235 = sk * t218;
			final double t247 = fx * t234;
			final double t236 = t217 + t235 - t247;
			final double t237 = X * t236;
			final double t239 = t223 + t237;
			final double t240 = t21 * t239;
			final double t248 = t46 * t113 * t213;
			final double t241 = t240 - t248;
			final double t242 = t38 * t51 * t241 * 2.0;
			final double t243 = t233 + t242;
			final double t249 = 1.0 / fx;
			final double t250 = 1.0 / fy;
			final double t251 = t21 * t56 * t250 * 2.0;
			final double t252 = sk * t21 * t38 * t51 * 2.0;
			final double t253 = t251 + t252;
			final double t254 = t21 * u0;
			final double t255 = t70 * t113;
			final double t260 = t21 * v0;
			final double t256 = t255 - t260;
			final double t262 = t46 * t113;
			final double t257 = t254 - t262;
			final double t258 = t38 * t51 * t257 * 2.0;
			final double t261 = t56 * t59 * t256 * 2.0;
			final double t259 = t258 - t261;
			final double t263 = k1 * t55;
			final double t264 = k2 * t55 * t74 * 2.0;
			final double t265 = k3 * t55 * t75 * 3.0;
			final double t266 = t263 + t264 + t265;
			final double t267 = k1 * t82;
			final double t268 = k3 * t75 * t82 * 3.0;
			final double t269 = k2 * t74 * t82 * 2.0;
			final double t270 = t267 + t268 + t269;
			final double t271 = t21 * t80;
			final double t272 = t21 * t80 * t86;
			final double t273 = k1 * t21 * t38 * t51 * t80 * 2.0;
			final double t274 = k2 * t21 * t38 * t51 * t74 * t80 * 4.0;
			final double t275 = k3 * t21 * t38 * t51 * t75 * t80 * 6.0;
			final double t276 = t273 + t274 + t275;
			final double t277 = t38 * t56 * 2.0;
			final double t278 = k1 * t141;
			final double t279 = k2 * t74 * t141 * 2.0;
			final double t280 = k3 * t75 * t141 * 3.0;
			final double t281 = k1 * t199;
			final double t282 = k2 * t74 * t199 * 2.0;
			final double t283 = k3 * t75 * t199 * 3.0;
			final double t284 = t281 + t282 + t283;
			final double t285 = k1 * t243;
			final double t286 = k2 * t74 * t243 * 2.0;
			final double t287 = k3 * t75 * t243 * 3.0;
			final double t288 = t285 + t286 + t287;
			final double t289 = k1 * t21 * t38 * t249 * 2.0;
			final double t290 = k2 * t21 * t38 * t74 * t249 * 4.0;
			final double t291 = k3 * t21 * t38 * t75 * t249 * 6.0;
			final double t292 = t289 + t290 + t291;
			final double t293 = k1 * t253;
			final double t294 = k3 * t75 * t253 * 3.0;
			final double t295 = k2 * t74 * t253 * 2.0;
			final double t296 = t293 + t294 + t295;
			final double t297 = k1 * t259;
			final double t298 = k2 * t74 * t259 * 2.0;
			final double t299 = k3 * t75 * t259 * 3.0;
			final double t300 = t297 + t298 + t299;
			A0[0][0] = t21 * t53 + t266
					* (u0 - t21 * (t34 + t35 + t36 + t37 + X * (t26 - t14 * u0 + sk * (t15 - t10 * t11 * wx * wy)))) - p2
					* (t50 + t54 + t21 * t38 * t53 * 4.0) + t21 * t53 * t86 - p1 * t21 * t53 * t56 * 2.0;
			A0[0][1] = -p2 * t82 + t38 * t270 - p1 * t21 * t38 * t80 * 2.0;
			A0[0][2] = 1.0;
			A0[0][4] = t271 + t272 - p2 * (t21 * t38 * t80 * 4.0 + t21 * t38 * t51 * t80 * 2.0) + t38 * t276 - p1 * t21
					* t56 * t80 * 2.0;
			A0[0][5] = -t38 * t74;
			A0[0][6] = -t38 * t75;
			A0[0][7] = -t38 * t74 * t75;
			A0[0][8] = t277;
			A0[0][9] = t49 * 2.0 + t58 + t73;
			A0[0][10] = t138 - t147 + t86 * t139 + t38 * (t278 + t279 + t280) - p2 * (t140 - t144 + t38 * t139 * 4.0)
					+ p1 * t38 * t124 * 2.0 - p1 * t56 * t139 * 2.0;
			A0[0][11] = -t184 + t86 * t182 + t38 * t284 - p2 * (t194 + t195 + t38 * t182 * 4.0) + t21
					* (t168 + Y * (t158 - sk * t153 + fx * (t148 + t149 + t150 - t10 * t11 * wx - t9 * t11 * wy * wz)))
					- p1 * t38 * t193 * 2.0 - p1 * t56 * t182 * 2.0;
			A0[0][12] = t240
					- t248
					+ t38
					* t288
					- p2
					* (t233 + t242 + t38 * t241 * 4.0)
					+ t86
					* (t21
							* (t223 + X
									* (t217 + sk * (t93 + t95 + t96 + t200 - t4 * t7 * t87) - fx
											* (t215 + t216 - t10 * t11 * wz * 2.0))) - t46 * t113 * t213) - p1 * t38
					* t232 * 2.0 - p1 * t56 * t241 * 2.0;
			A0[0][13] = fx * t21 + t38 * t292 - p2 * (fx * t21 * t38 * 4.0 + t21 * t38 * t249 * 2.0) + fx * t21 * t86
					- fx * p1 * t21 * t56 * 2.0;
			A0[0][14] = sk * t21 + t38 * t296 - p2 * (t251 + t252 + sk * t21 * t38 * 4.0) + sk * t21 * t86 - fy * p1
					* t21 * t38 * 2.0 - p1 * sk * t21 * t56 * 2.0;
			A0[0][15] = t254 - t46 * t113 + t38 * t300 + t86 * t257 - p2 * (t258 - t261 + t38 * t257 * 4.0) - p1 * t56
					* t257 * 2.0 + p1 * t38 * (t255 - t260) * 2.0;
			A0[1][0] = -p1 * t55 + t56 * t266 - p2 * t21 * t53 * t56 * 2.0;
			A0[1][1] = t271 + t272 + t56 * t270 - p1 * (t77 + t81 + t21 * t56 * t80 * 4.0) - p2 * t21 * t38 * t80 * 2.0;
			A0[1][3] = 1.0;
			A0[1][4] = t56 * t276 - p2 * t21 * t56 * t80 * 2.0 - p1 * t21 * t38 * t51 * t80 * 2.0;
			A0[1][5] = -t56 * t74;
			A0[1][6] = -t56 * t75;
			A0[1][7] = -t56 * t74 * t75;
			A0[1][8] = t58 + t72 * 2.0 + t73;
			A0[1][9] = t277;
			A0[1][10] = -t122 - t123 - t86 * t124 + t56 * (t278 + t279 + t280) + p1 * (-t140 + t144 + t56 * t124 * 4.0)
					+ p2 * t38 * t124 * 2.0 - p2 * t56 * t139 * 2.0;
			A0[1][11] = t192 - t198 + t86 * t193 + t56 * t284 - p1 * (t194 + t195 + t56 * t193 * 4.0) - p2 * t38 * t193
					* 2.0 - p2 * t56 * t182 * 2.0;
			A0[1][12] = t231 - t246 + t86 * t232 + t56 * t288 - p1 * (t233 + t242 + t56 * t232 * 4.0) - p2 * t38 * t232
					* 2.0 - p2 * t56 * t241 * 2.0;
			A0[1][13] = t56 * t292 - fx * p2 * t21 * t56 * 2.0 - p1 * t21 * t38 * t249 * 2.0;
			A0[1][14] = fy * t21 + t56 * t296 - p1 * (t251 + t252 + fy * t21 * t56 * 4.0) + fy * t21 * t86 - fy * p2
					* t21 * t38 * 2.0 - p2 * sk * t21 * t56 * 2.0;
			A0[1][15] = -t255 + t260 - t86 * t256 + t56 * t300 + p1 * (-t258 + t261 + t56 * t256 * 4.0) - p2 * t56 * t257
					* 2.0 + p2 * t38 * (t255 - t260) * 2.0;
			// end matlab code

			final double[][] result = new double[2][10 + 6 * points.size()];
			System.arraycopy(A0[0], 0, result[0], 0, 10);
			System.arraycopy(A0[1], 0, result[1], 0, 10);
			System.arraycopy(A0[0], 10, result[0], 10 + img * 6, 6);
			System.arraycopy(A0[1], 10, result[1], 10 + img * 6, 6);

			// result[0][7] = 0;
			// result[1][7] = 0;
			// result[0][8] = 0;
			// result[1][8] = 0;
			// result[0][9] = 0;
			// result[1][9] = 0;

			return result;
		}
	}

	/**
	 * Stack the observed image locations of the calibration pattern points into
	 * a vector
	 * 
	 * @return the observed vector
	 */
	@Override
	protected RealVector buildObservedVector()
	{
		int totalPoints = 0;
		for (int i = 0; i < points.size(); i++)
			totalPoints += points.get(i).size();

		final double[] vec = new double[totalPoints * 2];

		for (int i = 0, k = 0; i < points.size(); i++) {
			for (int j = 0; j < points.get(i).size(); j++, k++) {
				vec[k * 2 + 0] = points.get(i).get(j).secondObject().getX();
				vec[k * 2 + 1] = points.get(i).get(j).secondObject().getY();
			}
		}

		return new ArrayRealVector(vec, false);
	}

	/**
	 * Perform Levenburg-Marquardt non-linear optimisation to get better
	 * estimates of the parameters
	 */
	private void refine()
	{
		final LevenbergMarquardtOptimizer lm = new LevenbergMarquardtOptimizer();
		final RealVector start = buildInitialVector();
		final RealVector observed = buildObservedVector();
		final int maxEvaluations = 1000;
		final int maxIterations = 1000;

		final MultivariateVectorFunction value = new Value();
		final MultivariateMatrixFunction jacobian = new Jacobian();
		final MultivariateJacobianFunction model = LeastSquaresFactory.model(value, jacobian);

		final Optimum result = lm.optimize(LeastSquaresFactory.create(model,
				observed, start, null, maxEvaluations, maxIterations));

		updateEstimates(result.getPoint());
	}

	/**
	 * Extract the data from the optimised parameter vector and put it back into
	 * our camera model
	 * 
	 * @param point
	 *            the optimised parameter vector
	 */
	private void updateEstimates(RealVector point) {
		final CameraIntrinsics intrinsic = cameras.get(0).intrinsicParameters;

		intrinsic.setFocalLengthX(point.getEntry(0));
		intrinsic.setFocalLengthY(point.getEntry(1));
		intrinsic.setPrincipalPointX(point.getEntry(2));
		intrinsic.setPrincipalPointY(point.getEntry(3));
		intrinsic.setSkewFactor(point.getEntry(4));
		intrinsic.k1 = point.getEntry(5);
		intrinsic.k2 = point.getEntry(6);
		intrinsic.k3 = point.getEntry(7);
		intrinsic.p1 = point.getEntry(8);
		intrinsic.p2 = point.getEntry(9);

		for (int i = 0; i < cameras.size(); i++) {
			final Camera e = cameras.get(i);
			final double[] rv = new double[] { point.getEntry(i * 6 + 10), point.getEntry(i * 6 + 11),
					point.getEntry(i * 6 + 12) };
			e.rotation = TransformUtilities.rodrigues(rv);

			e.translation.setX(point.getEntry(i * 6 + 13));
			e.translation.setY(point.getEntry(i * 6 + 14));
			e.translation.setZ(point.getEntry(i * 6 + 15));
		}
	}

	private RealVector buildInitialVector() {
		final CameraIntrinsics intrinsic = cameras.get(0).intrinsicParameters;

		final double[] vector = new double[10 + cameras.size() * 6];

		vector[0] = intrinsic.getFocalLengthX();
		vector[1] = intrinsic.getFocalLengthY();
		vector[2] = intrinsic.getPrincipalPointX();
		vector[3] = intrinsic.getPrincipalPointY();
		vector[4] = intrinsic.getSkewFactor();
		vector[5] = intrinsic.k1;
		vector[6] = intrinsic.k2;
		vector[7] = intrinsic.k3;
		vector[8] = intrinsic.p1;
		vector[9] = intrinsic.p2;

		for (int i = 0; i < cameras.size(); i++) {
			final Camera e = cameras.get(i);
			final double[] rv = TransformUtilities.rodrigues(e.rotation);

			vector[i * 6 + 10] = rv[0];
			vector[i * 6 + 11] = rv[1];
			vector[i * 6 + 12] = rv[2];
			vector[i * 6 + 13] = e.translation.getX();
			vector[i * 6 + 14] = e.translation.getY();
			vector[i * 6 + 15] = e.translation.getZ();
		}

		return new ArrayRealVector(vector, false);
	}
}
