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
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.camera.Camera;
import org.openimaj.image.camera.CameraIntrinsics;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point3dImpl;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * Implementation of Zhengyou Zhang's camera calibration routine using a planar
 * calibration pattern. This calibration routine assumes a camera with a 2-term
 * radial distortion; the third radial distortion term (k3) and tangential terms
 * (p1, p1) of the {@link CameraIntrinsics} will be set to zero.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Zhengyou Zhang" },
		title = "A flexible new technique for camera calibration",
		year = "2000",
		journal = "Pattern Analysis and Machine Intelligence, IEEE Transactions on",
		pages = { "1330", "1334" },
		month = "Nov",
		number = "11",
		volume = "22",
		customData = {
				"keywords",
				"calibration;computer vision;geometry;image sensors;matrix algebra;maximum likelihood estimation;optimisation;3D computer vision;camera calibration;flexible technique;maximum likelihood criterion;planar pattern;radial lens distortion;Calibration;Cameras;Closed-form solution;Computer simulation;Computer vision;Layout;Lenses;Maximum likelihood estimation;Nonlinear distortion;Testing",
				"doi", "10.1109/34.888718",
				"ISSN", "0162-8828"
		})
public class CameraCalibrationZhang {
	protected List<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>> points;
	protected List<Camera> cameras;

	/**
	 * Calibrate a camera using Zhang's method based on the given model-image
	 * point pairs across a number of images. The model points are in the world
	 * coordinate system and assumed to be on the Z=0 plane.
	 * 
	 * @param points
	 *            the pairs of model-image points to calibrate the camera with
	 * @param width
	 *            the image width of the camera in pixels
	 * @param height
	 *            the image height of the camera in pixels
	 */
	public CameraCalibrationZhang(List<List<? extends IndependentPair<? extends Point2d, ? extends Point2d>>> points,
			int width, int height)
	{
		this.points = points;

		performCalibration(width, height);
	}

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

		// initial estimate of radial distortion
		estimateRadialDistortion();

		// non-linear optimisation using analytic jacobian
		refine();
	}

	/**
	 * Get the computed (extrinsic and intrinsic) camera parameters for all
	 * images used at construction time (in the same order).
	 * 
	 * @return the camera parameters for each image
	 */
	public List<Camera> getCameras() {
		return cameras;
	}

	/**
	 * Get the computed intrinsic parameters calculated during construction.
	 * 
	 * @return the intrinsic parameters of the calibrated camera
	 */
	public CameraIntrinsics getIntrisics() {
		return cameras.get(0).intrinsicParameters;
	}

	private double[] vij(Matrix h, int i, int j) {
		h = h.transpose();

		final double[] vij = new double[] {
				h.get(i, 0) * h.get(j, 0),
				h.get(i, 0) * h.get(j, 1) + h.get(i, 1) * h.get(j, 0),
				h.get(i, 1) * h.get(j, 1),
				h.get(i, 2) * h.get(j, 0) + h.get(i, 0) * h.get(j, 2),
				h.get(i, 2) * h.get(j, 1) + h.get(i, 1) * h.get(j, 2),
				h.get(i, 2) * h.get(j, 2)
		};

		return vij;
	}

	/**
	 * Compute the initial estimate of the intrinsics
	 * 
	 * @param homographies
	 *            the homographies
	 * @param height
	 * @param width
	 * @return the intrinsics
	 */
	private CameraIntrinsics estimateIntrinsics(List<Matrix> homographies, int width, int height) {
		final double[][] V = new double[homographies.size() == 2 ? 5 : 2 * homographies.size()][];

		for (int i = 0, j = 0; i < homographies.size(); i++, j += 2) {
			final Matrix h = homographies.get(i);

			V[j] = vij(h, 0, 1); // v12
			V[j + 1] = ArrayUtils.subtract(vij(h, 0, 0), vij(h, 1, 1)); // v11-v22
		}

		if (homographies.size() == 2) {
			V[V.length - 1] = new double[] { 0, 1, 0, 0, 0, 0 };
		}

		final double[] b = MatrixUtils.solveHomogeneousSystem(V);
		final double v0 = (b[1] * b[3] - b[0] * b[4]) / (b[0] * b[2] - b[1] * b[1]);
		final double lamda = b[5] - (b[3] * b[3] + v0 * (b[1] * b[3] - b[0] * b[4])) / b[0];
		final double alpha = Math.sqrt(lamda / b[0]);
		final double beta = Math.sqrt(lamda * b[0] / (b[0] * b[2] - b[1] * b[1]));
		final double gamma = -b[1] * alpha * alpha * beta / lamda;
		final double u0 = gamma * v0 / beta - b[3] * alpha * alpha / lamda;

		final Matrix A = new Matrix(new double[][] {
				{ alpha, gamma, u0 },
				{ 0, beta, v0 },
				{ 0, 0, 1 }
		});

		return new CameraIntrinsics(A, width, height);
	}

	/**
	 * Produce an initial estimate of the radial distortion parameters, and
	 * update the intrinsics
	 */
	protected void estimateRadialDistortion()
	{
		final CameraIntrinsics ci = cameras.get(0).intrinsicParameters;

		int totalPoints = 0;
		for (int i = 0; i < points.size(); i++)
			totalPoints += points.get(i).size();

		final Matrix D = new Matrix(2 * totalPoints, 2);
		final Matrix d = new Matrix(2 * totalPoints, 1);

		for (int i = 0, k = 0; i < points.size(); i++) {
			final List<? extends IndependentPair<? extends Point2d, ? extends Point2d>> pointPairs = points.get(i);
			final Matrix idealH = cameras.get(i).computeHomography();

			for (int j = 0; j < pointPairs.size(); j++, k++) {
				// model point
				final Point2d XY = pointPairs.get(j).firstObject();
				// transformed ideal point
				final Point2d uv = XY.transform(idealH);
				// observed point
				final Point2d ipt = pointPairs.get(j).secondObject();

				d.set(k * 2 + 0, 0, ipt.getX() - uv.getX());
				d.set(k * 2 + 1, 0, ipt.getY() - uv.getY());

				final double tmp1 = uv.getX() - ci.getPrincipalPointX(); // u-u0
				final double tmp2 = uv.getY() - ci.getPrincipalPointY(); // v-v0
				final double x = tmp1 / ci.getFocalLengthX(); // (u-u0)/fx
				final double y = tmp2 / ci.getFocalLengthY(); // (v-v0)/fy
				final double r2 = x * x + y * y;
				final double r4 = r2 * r2;

				D.set(k * 2 + 0, 0, tmp1 * r2);
				D.set(k * 2 + 0, 1, tmp1 * r4);
				D.set(k * 2 + 1, 0, tmp2 * r2);
				D.set(k * 2 + 1, 1, tmp2 * r4);
			}
		}

		final Matrix result = D.solve(d);
		ci.k1 = result.get(0, 0);
		ci.k2 = result.get(1, 0);
		ci.k3 = 0;
	}

	/**
	 * Compute the initial estimate of the intrinsic parameters and then the
	 * extrinsic parameters assuming zero distortion.
	 * 
	 * @param homographies
	 *            the homographies
	 * @param height
	 * @param width
	 */
	protected void estimateIntrisicAndExtrinsics(List<Matrix> homographies, int width, int height) {
		cameras = new ArrayList<Camera>(homographies.size());
		final CameraIntrinsics intrinsic = estimateIntrinsics(homographies, width, height);

		for (int i = 0; i < homographies.size(); i++) {
			cameras.add(estimateExtrinsics(homographies.get(i), intrinsic));
		}
	}

	/**
	 * Estimate the extrinsic parameters for a single camera given its
	 * homography and intrinsic parameters.
	 * 
	 * @param h
	 *            the homography
	 * @param intrinsic
	 *            the intrinsic parameters
	 * @return the extrinsic parameters
	 */
	private Camera estimateExtrinsics(Matrix h, CameraIntrinsics intrinsic) {
		final Matrix Ainv = intrinsic.calibrationMatrix.inverse();
		final Matrix h1 = h.getMatrix(0, 2, 0, 0);
		final Matrix h2 = h.getMatrix(0, 2, 1, 1);
		final Matrix h3 = h.getMatrix(0, 2, 2, 2);

		final Matrix r1 = Ainv.times(h1);
		final double lamda = 1 / r1.norm2();
		MatrixUtils.times(r1, lamda);

		final Matrix r2 = Ainv.times(h2);
		MatrixUtils.times(r2, lamda);

		final Matrix r3 = new Matrix(new double[][] {
				{ r1.get(1, 0) * r2.get(2, 0) - r1.get(2, 0) * r2.get(1, 0) },
				{ r1.get(2, 0) * r2.get(0, 0) - r1.get(0, 0) * r2.get(2, 0) },
				{ r1.get(0, 0) * r2.get(1, 0) - r1.get(1, 0) * r2.get(0, 0) }
		});

		final Matrix R = TransformUtilities.approximateRotationMatrix(MatrixUtils.hstack(r1, r2, r3));

		final Matrix t = Ainv.times(h3);
		MatrixUtils.times(t, lamda);

		final Camera ce = new Camera();
		ce.intrinsicParameters = intrinsic;
		ce.rotation = R;
		ce.translation = new Point3dImpl(t.getColumnPackedCopy());

		return ce;
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
	 * syms k1 k2 real
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
	 * uv = [u + uu0*(k1*r2 + k2*r4); v + vv0*(k1*r2 + k2*r4)];
	 * ccode(uv, 'file', 'zhang-value.c')
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

			final double wx = params[img * 6 + 7];
			final double wy = params[img * 6 + 8];
			final double wz = params[img * 6 + 9];
			final double tx = params[img * 6 + 10];
			final double ty = params[img * 6 + 11];
			final double tz = params[img * 6 + 12];

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
			final double t47 = fx * t27;
			final double t33 = t28 + t32 - t47;
			final double t34 = Y * t33;
			final double t35 = fx * tx;
			final double t36 = sk * ty;
			final double t37 = tz * u0;
			final double t40 = t15 - t22;
			final double t43 = sk * t40;
			final double t44 = t14 * u0;
			final double t45 = t26 + t43 - t44;
			final double t46 = X * t45;
			final double t48 = t34 + t35 + t36 + t37 + t46;
			final double t49 = t21 * t48;
			final double t38 = -t49 + u0;
			final double t53 = fy * ty;
			final double t54 = fy * t40;
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
			final double t41 = -t64 + v0;
			final double t42 = 1.0 / (fx * fx);
			final double t50 = t38 * t38;
			final double t51 = t42 * t50;
			final double t52 = 1.0 / (fy * fy);
			final double t65 = t41 * t41;
			final double t66 = t52 * t65;
			final double t67 = t51 + t66;
			final double t68 = k1 * t67;
			final double t69 = t67 * t67;
			final double t70 = k2 * t69;
			final double t71 = t68 + t70;
			A0[0][0] = -t38 * t71 + t21
					* (t34 + t35 + t36 + t37 + X * (t26 - t14 * u0 + sk * (t15 - t10 * t11 * wx * wy)));
			A0[1][0] = t64 - t41 * t71;
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
	 * syms k1 k2 real
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
	 * uv = [u + uu0*(k1*r2 + k2*r4); v + vv0*(k1*r2 + k2*r4)];
	 * J=jacobian(uv,[fx,fy,u0,v0,sk,k1,k2, wx wy wz tx ty tz]); 
	 * ccode(J, 'file', 'zhang-jacobian.c')
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
			// points. The params vector is 7 + 6*numCameras elements long (7
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
			final double[][] A0 = new double[2][13];

			final double X = points.get(img).get(point).firstObject().getX();
			final double Y = points.get(img).get(point).firstObject().getY();

			final double fx = params[0];
			final double fy = params[1];
			final double u0 = params[2];
			final double v0 = params[3];
			final double sk = params[4];
			final double k1 = params[5];
			final double k2 = params[6];

			final double wx = params[img * 6 + 7];
			final double wy = params[img * 6 + 8];
			final double wz = params[img * 6 + 9];
			final double tx = params[img * 6 + 10];
			final double ty = params[img * 6 + 11];
			final double tz = params[img * 6 + 12];

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
			final double t75 = 1.0 / (fy * fy * fy);
			final double t76 = t72 * t75 * 2.0;
			final double t77 = X * t40;
			final double t78 = Y * t31;
			final double t79 = t77 + t78 + ty;
			final double t80 = t21 * t56 * t59 * t79 * 2.0;
			final double t81 = t76 + t80;
			final double t82 = k1 * t74;
			final double t83 = t74 * t74;
			final double t84 = k2 * t83;
			final double t85 = t82 + t84;
			final double t86 = 1.0 / pow(t5, 3.0 / 2.0);
			final double t87 = 1.0 / (t5 * t5);
			final double t88 = t9 * t11 * wx * wz;
			final double t89 = t2 * t7 * t86 * wy;
			final double t90 = t2 * t10 * t87 * wy * 2.0;
			final double t91 = t7 * t86 * wx * wy;
			final double t92 = t2 * t7 * t86 * wz;
			final double t93 = t2 * t10 * t87 * wz * 2.0;
			final double t105 = t10 * t11 * wz;
			final double t106 = t9 * t11 * wx * wy;
			final double t94 = t91 + t92 + t93 - t105 - t106;
			final double t95 = t7 * t8;
			final double t96 = t2 * t9 * t11;
			final double t97 = t10 * t87 * wx * wy * wz * 2.0;
			final double t98 = t7 * t86 * wx * wy * wz;
			final double t103 = t2 * t7 * t86;
			final double t99 = t95 + t96 + t97 + t98 - t103;
			final double t100 = t10 * t29 * t87 * wx * 2.0;
			final double t101 = t7 * t29 * t86 * wx;
			final double t116 = t10 * t11 * wx * 2.0;
			final double t102 = t100 + t101 - t116;
			final double t104 = t7 * t86 * wx * wz;
			final double t107 = X * t94;
			final double t108 = Y * t99;
			final double t109 = t107 + t108;
			final double t110 = 1.0 / (t20 * t20);
			final double t111 = t10 * t23 * t87 * wx * 2.0;
			final double t112 = t7 * t23 * t86 * wx;
			final double t113 = t111 + t112;
			final double t117 = t10 * t11 * wy;
			final double t114 = t88 + t89 + t90 - t104 - t117;
			final double t115 = t94 * u0;
			final double t118 = t99 * u0;
			final double t119 = fy * t102;
			final double t262 = t99 * v0;
			final double t120 = t119 - t262;
			final double t121 = Y * t120;
			final double t122 = fy * t114;
			final double t123 = t94 * v0;
			final double t124 = t122 + t123;
			final double t263 = X * t124;
			final double t125 = t121 - t263;
			final double t126 = t21 * t125;
			final double t127 = t70 * t109 * t110;
			final double t128 = t126 + t127;
			final double t129 = sk * t114;
			final double t141 = fx * t113;
			final double t130 = t115 + t129 - t141;
			final double t131 = X * t130;
			final double t132 = -t88 + t89 + t90 + t104 - t117;
			final double t133 = fx * t132;
			final double t142 = sk * t102;
			final double t134 = t118 + t133 - t142;
			final double t135 = Y * t134;
			final double t136 = t131 + t135;
			final double t137 = t21 * t136;
			final double t143 = t46 * t109 * t110;
			final double t138 = t137 - t143;
			final double t139 = t38 * t51 * t138 * 2.0;
			final double t264 = t56 * t59 * t128 * 2.0;
			final double t140 = t139 - t264;
			final double t144 = t3 * t7 * t86 * wz;
			final double t145 = t3 * t10 * t87 * wz * 2.0;
			final double t146 = -t91 - t105 + t106 + t144 + t145;
			final double t147 = t3 * t7 * t86;
			final double t156 = t3 * t9 * t11;
			final double t148 = -t95 + t97 + t98 + t147 - t156;
			final double t149 = t10 * t29 * t87 * wy * 2.0;
			final double t150 = t7 * t29 * t86 * wy;
			final double t151 = t149 + t150;
			final double t152 = t9 * t11 * wy * wz;
			final double t153 = t3 * t7 * t86 * wx;
			final double t154 = t3 * t10 * t87 * wx * 2.0;
			final double t155 = t7 * t86 * wy * wz;
			final double t157 = Y * t146;
			final double t158 = X * t148;
			final double t159 = t157 + t158;
			final double t161 = t10 * t11 * wx;
			final double t160 = t152 + t153 + t154 - t155 - t161;
			final double t162 = fy * t160;
			final double t163 = t148 * v0;
			final double t164 = t162 + t163;
			final double t165 = X * t164;
			final double t166 = fy * t151;
			final double t267 = t146 * v0;
			final double t167 = t166 - t267;
			final double t268 = Y * t167;
			final double t168 = t165 - t268;
			final double t169 = t21 * t168;
			final double t269 = t70 * t110 * t159;
			final double t170 = t169 - t269;
			final double t171 = t56 * t59 * t170 * 2.0;
			final double t172 = -t152 + t153 + t154 + t155 - t161;
			final double t173 = fx * t172;
			final double t174 = t146 * u0;
			final double t189 = sk * t151;
			final double t175 = t173 + t174 - t189;
			final double t176 = Y * t175;
			final double t177 = t10 * t23 * t87 * wy * 2.0;
			final double t178 = t7 * t23 * t86 * wy;
			final double t190 = t10 * t11 * wy * 2.0;
			final double t179 = t177 + t178 - t190;
			final double t180 = sk * t160;
			final double t181 = t148 * u0;
			final double t191 = fx * t179;
			final double t182 = t180 + t181 - t191;
			final double t183 = X * t182;
			final double t184 = t176 + t183;
			final double t185 = t21 * t184;
			final double t192 = t46 * t110 * t159;
			final double t186 = t185 - t192;
			final double t187 = t38 * t51 * t186 * 2.0;
			final double t188 = t171 + t187;
			final double t193 = t4 * t9 * t11;
			final double t194 = t4 * t7 * t86 * wx;
			final double t195 = t4 * t10 * t87 * wx * 2.0;
			final double t196 = -t152 + t155 - t161 + t194 + t195;
			final double t197 = t4 * t7 * t86;
			final double t198 = t10 * t29 * t87 * wz * 2.0;
			final double t199 = t7 * t29 * t86 * wz;
			final double t204 = t10 * t11 * wz * 2.0;
			final double t200 = t198 + t199 - t204;
			final double t201 = t4 * t7 * t86 * wy;
			final double t202 = t4 * t10 * t87 * wy * 2.0;
			final double t203 = t88 - t104 - t117 + t201 + t202;
			final double t205 = t10 * t23 * t87 * wz * 2.0;
			final double t206 = t7 * t23 * t86 * wz;
			final double t207 = t196 * u0;
			final double t208 = t95 + t97 + t98 + t193 - t197;
			final double t209 = t203 * u0;
			final double t210 = -t95 + t97 + t98 - t193 + t197;
			final double t211 = fx * t210;
			final double t231 = sk * t200;
			final double t212 = t209 + t211 - t231;
			final double t213 = Y * t212;
			final double t214 = X * t196;
			final double t215 = Y * t203;
			final double t216 = t214 + t215;
			final double t217 = t196 * v0;
			final double t218 = fy * t208;
			final double t219 = t217 + t218;
			final double t220 = X * t219;
			final double t221 = fy * t200;
			final double t273 = t203 * v0;
			final double t222 = t221 - t273;
			final double t274 = Y * t222;
			final double t223 = t220 - t274;
			final double t224 = t21 * t223;
			final double t275 = t70 * t110 * t216;
			final double t225 = t224 - t275;
			final double t226 = t56 * t59 * t225 * 2.0;
			final double t227 = -t204 + t205 + t206;
			final double t228 = sk * t208;
			final double t237 = fx * t227;
			final double t229 = t207 + t228 - t237;
			final double t230 = X * t229;
			final double t232 = t213 + t230;
			final double t233 = t21 * t232;
			final double t238 = t46 * t110 * t216;
			final double t234 = t233 - t238;
			final double t235 = t38 * t51 * t234 * 2.0;
			final double t236 = t226 + t235;
			final double t239 = 1.0 / fx;
			final double t240 = 1.0 / fy;
			final double t241 = t21 * t56 * t240 * 2.0;
			final double t242 = sk * t21 * t38 * t51 * 2.0;
			final double t243 = t241 + t242;
			final double t244 = t21 * u0;
			final double t248 = t46 * t110;
			final double t245 = t244 - t248;
			final double t246 = t70 * t110;
			final double t285 = t21 * v0;
			final double t247 = t246 - t285;
			final double t249 = t38 * t51 * t245 * 2.0;
			final double t286 = t56 * t59 * t247 * 2.0;
			final double t250 = t249 - t286;
			final double t251 = k1 * t55;
			final double t252 = k2 * t55 * t74 * 2.0;
			final double t253 = t251 + t252;
			final double t254 = k1 * t81;
			final double t255 = k2 * t74 * t81 * 2.0;
			final double t256 = t254 + t255;
			final double t257 = t21 * t79;
			final double t258 = t21 * t79 * t85;
			final double t259 = k1 * t21 * t38 * t51 * t79 * 2.0;
			final double t260 = k2 * t21 * t38 * t51 * t74 * t79 * 4.0;
			final double t261 = t259 + t260;
			final double t265 = k1 * t140;
			final double t266 = k2 * t74 * t140 * 2.0;
			final double t270 = k1 * t188;
			final double t271 = k2 * t74 * t188 * 2.0;
			final double t272 = t270 + t271;
			final double t276 = k1 * t236;
			final double t277 = k2 * t74 * t236 * 2.0;
			final double t278 = t276 + t277;
			final double t279 = k1 * t21 * t38 * t239 * 2.0;
			final double t280 = k2 * t21 * t38 * t74 * t239 * 4.0;
			final double t281 = t279 + t280;
			final double t282 = k1 * t243;
			final double t283 = k2 * t74 * t243 * 2.0;
			final double t284 = t282 + t283;
			final double t287 = k1 * t250;
			final double t288 = k2 * t74 * t250 * 2.0;
			final double t289 = t287 + t288;
			A0[0][0] = t21 * t53 + t253
					* (u0 - t21 * (t34 + t35 + t36 + t37 + X * (t26 - t14 * u0 + sk * (t15 - t10 * t11 * wx * wy))))
					+ t21 * t53 * t85;
			A0[0][1] = t38 * t256;
			A0[0][2] = 1.0;
			A0[0][4] = t257 + t258 + t38 * t261;
			A0[0][5] = -t38 * t74;
			A0[0][6] = -t38 * t83;
			A0[0][7] = t137
					- t143
					+ t38
					* (t265 + t266)
					+ t85
					* (t21
							* (X * (t115 - fx * t113 + sk * (t88 + t89 + t90 - t10 * t11 * wy - t7 * t86 * wx * wz)) + Y
									* (t118 + fx * (-t88 + t89 + t90 + t104 - t10 * t11 * wy) - sk * t102)) - t46 * t109
							* t110);
			A0[0][8] = t185 - t192 + t85 * t186 + t38 * t272;
			A0[0][9] = -t238
					+ t38
					* t278
					+ t85
					* t234
					+ t21
					* (t213 + X
							* (t207 + sk * (t95 + t97 + t98 + t193 - t4 * t7 * t86) - fx
									* (t205 + t206 - t10 * t11 * wz * 2.0)));
			A0[0][10] = fx * t21 + t38 * t281 + fx * t21 * t85;
			A0[0][11] = sk * t21 + t38 * t284 + sk * t21 * t85;
			A0[0][12] = t244 - t46 * t110 + t38 * t289 + t85 * t245;
			A0[1][0] = t56 * t253;
			A0[1][1] = t257 + t258 + t56 * t256;
			A0[1][3] = 1.0;
			A0[1][4] = t56 * t261;
			A0[1][5] = -t56 * t74;
			A0[1][6] = -t56 * t83;
			A0[1][7] = -t126 - t127 + t56 * (t265 + t266) - t85 * t128;
			A0[1][8] = t169 - t269 + t85 * t170 + t56 * t272;
			A0[1][9] = t224 - t275 + t85 * t225 + t56 * t278;
			A0[1][10] = t56 * t281;
			A0[1][11] = fy * t21 + t56 * t284 + fy * t21 * t85;
			A0[1][12] = -t246 + t285 - t85 * t247 + t56 * t289;
			// end matlab code

			final double[][] result = new double[2][7 + 6 * points.size()];
			System.arraycopy(A0[0], 0, result[0], 0, 7);
			System.arraycopy(A0[1], 0, result[1], 0, 7);
			System.arraycopy(A0[0], 7, result[0], 7 + img * 6, 6);
			System.arraycopy(A0[1], 7, result[1], 7 + img * 6, 6);

			return result;
		}
	}

	/**
	 * Stack the observed image locations of the calibration pattern points into
	 * a vector
	 * 
	 * @return the observed vector
	 */
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

		for (int i = 0; i < cameras.size(); i++) {
			final Camera e = cameras.get(i);
			final double[] rv = new double[] { point.getEntry(i * 6 + 7), point.getEntry(i * 6 + 8),
					point.getEntry(i * 6 + 9) };
			e.rotation = TransformUtilities.rodrigues(rv);

			e.translation.setX(point.getEntry(i * 6 + 10));
			e.translation.setY(point.getEntry(i * 6 + 11));
			e.translation.setZ(point.getEntry(i * 6 + 12));
		}
	}

	private RealVector buildInitialVector() {
		final CameraIntrinsics intrinsic = cameras.get(0).intrinsicParameters;

		final double[] vector = new double[7 + cameras.size() * 6];

		vector[0] = intrinsic.getFocalLengthX();
		vector[1] = intrinsic.getFocalLengthY();
		vector[2] = intrinsic.getPrincipalPointX();
		vector[3] = intrinsic.getPrincipalPointY();
		vector[4] = intrinsic.getSkewFactor();
		vector[5] = intrinsic.k1;
		vector[6] = intrinsic.k2;

		for (int i = 0; i < cameras.size(); i++) {
			final Camera e = cameras.get(i);
			final double[] rv = TransformUtilities.rodrigues(e.rotation);

			vector[i * 6 + 7] = rv[0];
			vector[i * 6 + 8] = rv[1];
			vector[i * 6 + 9] = rv[2];
			vector[i * 6 + 10] = e.translation.getX();
			vector[i * 6 + 11] = e.translation.getY();
			vector[i * 6 + 12] = e.translation.getZ();
		}

		return new ArrayRealVector(vector, false);
	}

	/**
	 * Compute the average per-pixel error (in pixels)
	 * 
	 * @return the average per-pixel error
	 */
	public double calculateError() {
		double error = 0;
		int nPoints = 0;

		for (int i = 0; i < points.size(); i++) {
			for (int j = 0; j < points.get(i).size(); j++) {
				nPoints++;
				final Point2d model = points.get(i).get(j).firstObject();
				final Point2d observed = points.get(i).get(j).secondObject();
				final Point2d predicted = cameras.get(i).project(model);

				final float dx = observed.getX() - predicted.getX();
				final float dy = observed.getY() - predicted.getY();
				error += Math.sqrt(dx * dx + dy * dy);
			}
		}

		return error / nPoints;
	}
}
