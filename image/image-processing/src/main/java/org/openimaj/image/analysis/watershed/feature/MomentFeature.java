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
package org.openimaj.image.analysis.watershed.feature;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.IntValuePixel;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.util.QuadraticEquation;

import Jama.Matrix;

/**
 *	Accumulate the values of u11, u20 and u02 required to fit an
 *	ellipse to the feature.
 *
 *	@author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 */
public class MomentFeature implements ComponentFeature
{
	int n = 0;
	double mx = 0;
	double my = 0;
	double Mx2 = 0;
	double My2 = 0;
	double sxy = 0;
	double sx = 0;
	double sy = 0;

	@Override
	public void merge(ComponentFeature f) {
		MomentFeature mf = (MomentFeature) f;

		double dx = mf.mx - mx;
		double dy = mf.my - my;

		mx = (n*mx + mf.n*mf.mx) / (n + mf.n);
		my = (n*my + mf.n*mf.my) / (n + mf.n);

		Mx2 += mf.Mx2 + dx*dx*n*mf.n / (n + mf.n);
		My2 += mf.My2 + dy*dy*n*mf.n / (n + mf.n);

		n += mf.n;
		sxy += mf.sxy;
		sx += mf.sx;
		sy += mf.sy;
	}

	@Override
	public void addSample(IntValuePixel p) {
		n++;
		double dx = p.x - mx;
		double dy = p.y - my;

		mx += dx / n;
		my += dy / n;

		Mx2 += dx * (p.x - mx);
		My2 += dy * (p.y - my);

		sx += p.x;
		sy += p.y;
		sxy += p.x*p.y;
	}

	/**
	 * Get the number of pixels accumulated
	 * @return the number of pixels
	 */
	public double n() {
		return n;
	}

	/**
	 * Get the value of u11
	 * @return the value of u11
	 */
	public double u11() {
		return (sxy - sx*sy / n) / n;
	}

	/**
	 * Get the value of u20
	 * @return the value of u20
	 */
	public double u20() {
		return Mx2 / n;
	}

	/**
	 * Get the value of u02
	 * @return the value of u02
	 */
	public double u02() {
		return My2 / n;
	}

	/**
	 * Get the value of m10
	 * @return the value of m10
	 */
	public double m10() {
		return mx;
	}

	/**
	 * Get the value of m01
	 * @return the value of m01
	 */
	public double m01() {
		return my;
	}

	@Override
	public MomentFeature clone() {
		try {
			return (MomentFeature) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Create an ellipse based on the features parameters
	 * @return an ellipse
	 */
	public Ellipse getEllipse() {
		return getEllipse(1);
	}
	/**
	 * Create an ellipse based on the features parameters. Scale
	 * the ellipse about its centre with the given scale factor.
	 * @param sf the scale factor
	 * @return an ellipse
	 */
	public Ellipse getEllipse(float sf) {
		float u = (float) m10();
		float v = (float) m01();
		Matrix sm = new Matrix(new double[][]{
				{u20(),u11()},
				{u11(),u02()}
		});
		return EllipseUtilities.ellipseFromCovariance(u, v, sm, sf);
	}
	
	/**
	 * Create an ellipse based on the features parameters. Scale
	 * the ellipse about its centre with the given scale factor.
	 * @param sf the scale factor
	 * @return an ellipse
	 */
	public Circle getCircle(float sf) {
		Ellipse e = getEllipse(sf);
		Point2d p = e.calculateCentroid();
		return new Circle(p.getX(),p.getY(),(float)(e.getMajor() + e.getMinor())/2);
	}

	/**
	 * Create a rotated rectangle that fits around an ellipse
	 * based on the features parameters. Scale the ellipse about 
	 * its centre with the given scale factor.
	 * @param sf the scale factor
	 * @return a polygon representing a rotated rectangle
	 */
	public Polygon getEllipseBoundingBox(float sf) {
//		double xx = u20();
//		double xy = u11();
//		double yy = u02();
//
//		double theta = 0.5 * Math.atan2(2*xy, xx-yy);
//
//		double trace = xx + yy;
//		double det = (xx*yy) - (xy*xy);
//		double [] eigval = QuadraticEquation.solveGeneralQuadratic(1, -1*trace, det);
//
//		double a = Math.sqrt(eigval[1]) * sf * 2;
//		double b = Math.sqrt(eigval[0]) * sf * 2;
//
//		float Ax = (float) (a*Math.cos(theta) + b*Math.cos((Math.PI/2) + theta) + mx);
//		float Ay = (float) (a*Math.sin(theta) + b*Math.sin((Math.PI/2) + theta) + my);
//		float Bx = (float) (a*Math.cos(theta) - b*Math.cos((Math.PI/2) + theta) + mx);
//		float By = (float) (a*Math.sin(theta) - b*Math.sin((Math.PI/2) + theta) + my);
//		float Cx = (float) (-a*Math.cos(theta) - b*Math.cos((Math.PI/2) + theta) + mx);
//		float Cy = (float) (-a*Math.sin(theta) - b*Math.sin((Math.PI/2) + theta) + my);
//		float Dx = (float) (-a*Math.cos(theta) + b*Math.cos((Math.PI/2) + theta) + mx);
//		float Dy = (float) (-a*Math.sin(theta) + b*Math.sin((Math.PI/2) + theta) + my);
//
//		return new Polygon(new Point2dImpl(Ax,Ay), new Point2dImpl(Bx,By), new Point2dImpl(Cx,Cy), new Point2dImpl(Dx,Dy));
		return this.getEllipse(sf).calculateOrientedBoundingBox();
	}

	/**
	 * Get the primary orientation of the feature.
	 * @return the orientation
	 */
	public double getOrientation() {
		double xx = u20();
		double xy = u11();
		double yy = u02();

		return 0.5 * Math.atan2(2*xy, xx-yy);
	}
	
	protected double[] getEllipseBoundingRectsData(double sf) {
		double xx = u20();
		double xy = u11();
		double yy = u02();

		double theta = 0.5 * Math.atan2(2*xy, xx-yy);

		double trace = xx + yy;
		double det = (xx*yy) - (xy*xy);
		double [] eigval = QuadraticEquation.solveGeneralQuadratic(1, -1*trace, det);

		double a = 1.0 + Math.sqrt(Math.abs(eigval[1])) * sf * 4;
		double b = 1.0 + Math.sqrt(Math.abs(eigval[0])) * sf * 4;

		double [] data = {Math.max(a, b), Math.min(a, b), theta};
		
		return data;
	}
	
	/**
	 * Extract a rectangular image patch centered on the feature
	 * with the same primary orientation and a given scale factor. 
	 * @param image the image to extract from
	 * @param sf the scale factor
	 * @return a rectangular image patch
	 */
	public FImage extractEllipsePatch(FImage image, double sf) {
		double [] data = getEllipseBoundingRectsData(sf);
		double height = data[1], width = data[0], ori = data[2];		
		
		int sx = (int) Math.rint(width);
		int sy = (int) Math.rint(height);

		FImage patch = new FImage(sx, sy);

		//extract pixels
		for (int y=0; y<sy; y++) {
			for (int x=0; x<sx; x++) {
				double xbar = x - sx / 2.0;
				double ybar = y - sy / 2.0;

				double xx = (xbar * Math.cos(ori) - ybar * Math.sin(ori)) + mx;
				double yy = (xbar * Math.sin(ori) + ybar * Math.cos(ori)) + my;

				patch.setPixel(x, y, image.getPixelInterp(xx, yy));
			}
		}

		return patch;
	}
}
