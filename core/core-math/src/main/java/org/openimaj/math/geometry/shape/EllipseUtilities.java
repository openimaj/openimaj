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
package org.openimaj.math.geometry.shape;

import org.openimaj.math.statistics.distribution.CachingMultivariateGaussian;
import org.openimaj.math.statistics.distribution.MultivariateGaussian;
import org.openimaj.math.util.QuadraticEquation;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * An elliptical shape
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class EllipseUtilities {
	/***
	 * Construct an ellipse using a parametric ellipse equation, namely:
	 *
	 * X(t) = centerX + major * cos(t) * cos(rotation) - minor * sin(t) *
	 * sin(rotation) Y(t) = centerY + major * cos(t) * cos(rotation) + minor *
	 * sin(t) * sin(rotation)
	 *
	 * @param centerX
	 * @param centerY
	 * @param major
	 * @param minor
	 * @param rotation
	 * @return an ellipse
	 */
	public static Ellipse
	ellipseFromEquation(double centerX, double centerY, double major, double minor, double rotation)
	{
		return new Ellipse(centerX, centerY, major, minor, rotation);
	}

	/**
	 * Construct ellipse from second moment matrix and centroid.
	 *
	 * @param x
	 *            x-ordinate of centroid
	 * @param y
	 *            y-ordinate of centroid
	 * @param secondMoments
	 *            second moments matrix
	 * @return an ellipse
	 */
	public static Ellipse ellipseFromSecondMoments(float x, float y, Matrix secondMoments) {
		return EllipseUtilities.ellipseFromSecondMoments(x, y, secondMoments, 1);
	}

	/**
	 * Construct ellipse from second moment matrix, scale-factor and centroid.
	 *
	 * @param x
	 *            x-ordinate of centroid
	 * @param y
	 *            y-ordinate of centroid
	 * @param secondMoments
	 *            second moments matrix
	 * @param scaleFactor
	 *            the scale factor
	 * @return an ellipse
	 */
	public static Ellipse ellipseFromSecondMoments(float x, float y, Matrix secondMoments, double scaleFactor) {
		final double divFactor = 1 / Math.sqrt(secondMoments.det());
		final EigenvalueDecomposition rdr = secondMoments.times(divFactor).eig();
		double d1, d2;
		if (rdr.getD().get(0, 0) == 0)
			d1 = 0;
		else
			d1 = 1.0 / Math.sqrt(rdr.getD().get(0, 0));
		if (rdr.getD().get(1, 1) == 0)
			d2 = 0;
		else
			d2 = 1.0 / Math.sqrt(rdr.getD().get(1, 1));

		final double scaleCorrectedD1 = d1 * scaleFactor;
		final double scaleCorrectedD2 = d2 * scaleFactor;

		final Matrix eigenMatrix = rdr.getV();

		final double rotation = Math.atan2(eigenMatrix.get(1, 0), eigenMatrix.get(0, 0));
		return ellipseFromEquation(x, y, scaleCorrectedD1, scaleCorrectedD2, rotation);
	}

	/**
	 * Construct ellipse from covariance matrix, scale-factor and centroid.
	 *
	 * @param x
	 *            x-ordinate of centroid
	 * @param y
	 *            y-ordinate of centroid
	 * @param sm
	 *            covariance matrix
	 * @param sf
	 *            scale-factor
	 * @return an ellipse
	 */
	public static Ellipse ellipseFromCovariance(float x, float y, Matrix sm, float sf) {
		final double xy = sm.get(1, 0);
		final double xx = sm.get(0, 0);
		final double yy = sm.get(1, 1);
		final double theta = 0.5 * Math.atan2(2 * xy, xx - yy);

		final double trace = xx + yy;
		final double det = (xx * yy) - (xy * xy);
		final double[] eigval = QuadraticEquation.solveGeneralQuadratic(1, -trace, det);

		final double a = Math.sqrt(eigval[1]) * sf;
		final double b = Math.sqrt(eigval[0]) * sf;
		return ellipseFromEquation(x, y, a, b, theta);
	}

	/**
	 * Create the covariance matrix of an ellipse.
	 *
	 * @param e
	 *            the ellipse
	 * @return the corresponding covariance matrix
	 */
	public static Matrix ellipseToCovariance(Ellipse e) {
		final Matrix transform = e.transformMatrix();
		final Matrix Q = transform.getMatrix(0, 1, 0, 1);
		return Q.times(Q.transpose());
		// double sinrot = Math.sin(e.getRotation());
		// double cosrot = Math.cos(e.getRotation());
		// double cosrot2 = cosrot * cosrot;
		// double sinrot2 = sinrot * sinrot;
		// double a2 = e.getMajor() * e.getMajor();
		// double b2 = e.getMinor() * e.getMinor();
		// Matrix Q = new Matrix(new double[][]{
		// {cosrot2 / a2 + sinrot2 / b2 , sinrot*cosrot*(1/a2 - 1/b2)},
		// {sinrot*cosrot*(1/a2 - 1/b2) , sinrot2 / a2 + cosrot2 / b2}
		// });
		// return Q.inverse();
	}

	/**
	 * Create an ellipse.
	 *
	 * @param U
	 * @param x
	 * @param y
	 * @param scale
	 * @return an ellipse
	 */
	public static Ellipse fromTransformMatrix2x2(Matrix U, float x, float y, float scale) {
		Matrix uVal, uVec;
		final EigenvalueDecomposition ueig = U.eig();
		uVal = ueig.getD();
		uVec = ueig.getV();

		// Normalize min eigenvalue to 1 to expand patch in the direction of min
		// eigenvalue of U.inv()
		final double uval1 = uVal.get(0, 0);
		final double uval2 = uVal.get(1, 1);

		if (Math.abs(uval1) < Math.abs(uval2))
		{
			uVal.set(0, 0, 1);
			uVal.set(1, 1, uval2 / uval1);

		} else
		{
			uVal.set(1, 1, 1);
			uVal.set(0, 0, uval1 / uval2);
		}

		final float ax1 = (float) (1 / Math.abs(uVal.get(1, 1)) * scale);
		final float ax2 = (float) (1 / Math.abs(uVal.get(0, 0)) * scale);
		final double phi = Math.atan(uVec.get(1, 1) / uVec.get(0, 1));

		return new Ellipse(x, y, ax1, ax2, phi);
	}

	/**
	 * Construct an ellipse that encompasses the shape of a
	 * {@link CachingMultivariateGaussian}.
	 *
	 * @param gaussian
	 *            the {@link CachingMultivariateGaussian}
	 * @param scale
	 *            the relative size of the ellipse
	 * @return the ellipse
	 */
	public static Ellipse ellipseFromGaussian(MultivariateGaussian gaussian, float scale) {
		final Matrix mean = gaussian.getMean();
		final float x = (float) mean.get(0, 0);
		final float y = (float) mean.get(0, 1);
		final Matrix covar = gaussian.getCovariance();
		return ellipseFromCovariance(x, y, covar, scale);
	}
}
