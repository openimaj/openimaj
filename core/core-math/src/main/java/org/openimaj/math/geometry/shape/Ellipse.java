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

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

/**
 * An ellipse shape
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class Ellipse implements Shape, Cloneable {
	private double x;
	private double y;
	private double major;
	private double minor;
	private double rotation;

	/**
	 * Construct with centroid, semi-major and -minor axes, and rotation.
	 * 
	 * @param x
	 *            x-ordinate of centroid
	 * @param y
	 *            y-ordinate of centroid
	 * @param major
	 *            semi-major axis length
	 * @param minor
	 *            semi-minor axis length
	 * @param rotation
	 *            rotation
	 */
	public Ellipse(double x, double y, double major, double minor, double rotation) {
		this.x = x;
		this.y = y;
		this.major = major;
		this.minor = minor;
		this.rotation = rotation;
	}

	@Override
	public boolean isInside(Point2d point) {
		// Unrotate the point relative to the center of the ellipse
		final double cosrot = Math.cos(-rotation);
		final double sinrot = Math.sin(-rotation);
		final double relx = (point.getX() - x);
		final double rely = (point.getY() - y);

		final double xt = cosrot * relx - sinrot * rely;
		final double yt = sinrot * relx + cosrot * rely;

		final double ratiox = xt / major;
		final double ratioy = yt / minor;

		return ratiox * ratiox + ratioy * ratioy <= 1;
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {

		// Differentiate the parametrics form of the ellipse equation to get
		// tan(t) = -semiMinor * tan(rotation) / semiMajor (which gives us the
		// min/max X)
		// tan(t) = semiMinor * cot(rotation) / semiMajor (which gives us the
		// min/max Y)
		//
		// We find a value for t, add PI to get another value of t, we use this
		// to find our min/max x/y

		final double[] minmaxx = new double[2];
		final double[] minmaxy = new double[2];
		final double tanrot = Math.tan(rotation);
		final double cosrot = Math.cos(rotation);
		final double sinrot = Math.sin(rotation);

		double tx = Math.atan(-minor * tanrot / major);
		double ty = Math.atan(minor * (1 / tanrot) / major);

		minmaxx[0] = x + (major * Math.cos(tx) * cosrot - minor * Math.sin(tx) * sinrot);
		tx += Math.PI;
		minmaxx[1] = x + (major * Math.cos(tx) * cosrot - minor * Math.sin(tx) * sinrot);
		minmaxy[0] = y + (major * Math.cos(ty) * sinrot + minor * Math.sin(ty) * cosrot);
		ty += Math.PI;
		minmaxy[1] = y + (major * Math.cos(ty) * sinrot + minor * Math.sin(ty) * cosrot);

		double minx, miny, maxx, maxy;
		minx = minmaxx[ArrayUtils.minIndex(minmaxx)];
		miny = minmaxy[ArrayUtils.minIndex(minmaxy)];
		maxx = minmaxx[ArrayUtils.maxIndex(minmaxx)];
		maxy = minmaxy[ArrayUtils.maxIndex(minmaxy)];

		return new Rectangle((float) minx, (float) miny, (float) (maxx - minx), (float) (maxy - miny));
	}

	/**
	 * Calculate the oriented bounding box. This is the smallest rotated
	 * rectangle that will fit around the ellipse.
	 * 
	 * @return the oriented bounding box.
	 */
	public Polygon calculateOrientedBoundingBox() {
		final double minx = (-major);
		final double miny = (-minor);
		final double maxx = (+major);
		final double maxy = (+minor);

		Matrix corners = new Matrix(new double[][] {
				{ minx, miny, 1 },
				{ minx, maxy, 1 },
				{ maxx, miny, 1 },
				{ maxx, maxy, 1 },
		});
		corners = corners.transpose();
		final Matrix rot = TransformUtilities.rotationMatrix(rotation);
		final Matrix rotated = rot.times(corners);
		final double[][] rotatedData = rotated.getArray();
		final double[] rx = ArrayUtils.add(rotatedData[0], this.x);
		final double[] ry = ArrayUtils.add(rotatedData[1], this.y);
		final Polygon ret = new Polygon();
		ret.points.add(new Point2dImpl((float) rx[0], (float) ry[0]));
		ret.points.add(new Point2dImpl((float) rx[2], (float) ry[2]));
		ret.points.add(new Point2dImpl((float) rx[3], (float) ry[3]));
		ret.points.add(new Point2dImpl((float) rx[1], (float) ry[1]));
		return ret;
	}

	@Override
	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public void scale(float sc) {
		this.x *= sc;
		this.y *= sc;
		this.major *= sc;
		this.minor *= sc;
	}

	@Override
	public void scale(Point2d centre, float sc) {
		this.translate(-centre.getX(), -centre.getY());
		scale(sc);
		this.translate(centre.getX(), centre.getY());
	}

	@Override
	public void scaleCentroid(float sc) {
		this.major *= sc;
		this.minor *= sc;
	}

	@Override
	public Point2d calculateCentroid() {
		return new Point2dImpl((float) x, (float) y);
	}

	@Override
	public double calculateArea() {
		return Math.PI * major * minor;
	}

	@Override
	public double minX() {
		return this.calculateRegularBoundingBox().minX();
	}

	@Override
	public double minY() {
		return this.calculateRegularBoundingBox().minY();
	}

	@Override
	public double maxX() {
		return this.calculateRegularBoundingBox().maxX();
	}

	@Override
	public double maxY() {
		return this.calculateRegularBoundingBox().maxY();
	}

	@Override
	public double getWidth() {
		return this.calculateRegularBoundingBox().getWidth();
	}

	@Override
	public double getHeight() {
		return this.calculateRegularBoundingBox().getHeight();
	}

	@Override
	public Shape transform(Matrix transform) {
		return this.asPolygon().transform(transform);
	}

	/**
	 * @param transform
	 * @return transformed ellipse
	 */
	public Matrix transformAffineCovar(Matrix transform) {
		// Matrix translated =
		// transform.times(TransformUtilities.translateMatrix((float)this.x,
		// (float)this.y));
		// Matrix affineTransform =
		// TransformUtilities.homographyToAffine(translated);
		// affineTransform = affineTransform.times(1/affineTransform.get(2, 2));
		final Matrix affineTransform = TransformUtilities.homographyToAffine(transform, this.x, this.y);

		final Matrix affineCovar = EllipseUtilities.ellipseToCovariance(this);

		Matrix newTransform = new Matrix(3, 3);
		newTransform.setMatrix(0, 1, 0, 1, affineCovar);
		newTransform.set(2, 2, 1);

		newTransform = affineTransform.times(newTransform).times(affineTransform.transpose());
		return newTransform;
	}

	/**
	 * @param transform
	 * @return transformed ellipse
	 */
	public Ellipse transformAffine(Matrix transform) {
		final Point2d newCOG = this.calculateCentroid().transform(transform);
		return EllipseUtilities
				.ellipseFromCovariance(newCOG.getX(), newCOG.getY(), transformAffineCovar(transform), 1.0f);
	}

	/**
	 * Get the normalised transform matrix such that the scale of this ellipse
	 * is removed (i.e. the semi-major axis is 1)
	 * 
	 * @return the transform matrix
	 */
	public Matrix normTransformMatrix() {
		final double cosrot = Math.cos(rotation);
		final double sinrot = Math.sin(rotation);
		//
		// double scaledMajor = 1.0;
		// double scaledMinor = minor / major;
		//
		// double xMajor = cosrot * scaledMajor;
		// double yMajor = sinrot * scaledMajor;
		// double xMinor = -sinrot * scaledMinor;
		// double yMinor = cosrot * scaledMinor;
		// return new Matrix(new double[][]{
		// {xMajor,xMinor,this.x},
		// {yMajor,yMinor,this.y},
		// {0,0,1}
		// });
		final double cosrotsq = cosrot * cosrot;
		final double sinrotsq = sinrot * sinrot;

		final double scale = Math.sqrt(major * minor);

		final double majorsq = (major * major) / (scale * scale);
		final double minorsq = (minor * minor) / (scale * scale);
		final double Cxx = (cosrotsq / majorsq) + (sinrotsq / minorsq);
		final double Cyy = (sinrotsq / majorsq) + (cosrotsq / minorsq);
		final double Cxy = sinrot * cosrot * ((1 / majorsq) - (1 / minorsq));
		final double detC = Cxx * Cyy - (Cxy * Cxy);

		Matrix cMat = new Matrix(new double[][] {
				{ Cyy / detC, -Cxy / detC },
				{ -Cxy / detC, Cxx / detC }
		});

		cMat = MatrixUtils.sqrt(cMat);
		// cMat = cMat.inverse();
		final Matrix retMat = new Matrix(new double[][] {
				{ cMat.get(0, 0), cMat.get(0, 1), this.x },
				{ cMat.get(1, 0), cMat.get(1, 1), this.y },
				{ 0, 0, 1 },
		});
		return retMat;
	}

	/**
	 * Get the transform matrix required to turn points on a unit circle into
	 * the points on this ellipse. This function is used by
	 * {@link Ellipse#asPolygon}
	 * 
	 * @return the transform matrix
	 */
	public Matrix transformMatrix() {
		final double cosrot = Math.cos(rotation);
		final double sinrot = Math.sin(rotation);

		final double xMajor = cosrot * major;
		final double yMajor = sinrot * major;
		final double xMinor = -sinrot * minor;
		final double yMinor = cosrot * minor;
		return new Matrix(new double[][] {
				{ xMajor, xMinor, this.x },
				{ yMajor, yMinor, this.y },
				{ 0, 0, 1 }
		});

		// double cosrotsq = cosrot * cosrot;
		// double sinrotsq = sinrot * sinrot;
		//
		// double scale = Math.sqrt(major * minor);
		//
		// double majorsq = (major * major) / (scale * scale);
		// double minorsq = (minor * minor) / (scale * scale);
		// double Cxx = (cosrotsq / majorsq) + (sinrotsq/minorsq);
		// double Cyy = (sinrotsq / majorsq) + (cosrotsq/minorsq);
		// double Cxy = sinrot * cosrot * ((1/majorsq) - (1/minorsq));
		// double detC = Cxx*Cyy - (Cxy*Cxy);
		//
		// Matrix cMat = new Matrix(new double[][]{
		// {Cxx/detC,-Cxy/detC},
		// {-Cxy/detC,Cyy/detC}
		// });
		//
		// cMat = cMat.inverse();
		// Matrix retMat = new Matrix(new double[][]{
		// {cMat.get(0,0),cMat.get(0,1),this.x},
		// {cMat.get(1,0),cMat.get(1,1),this.y},
		// {0,0,1},
		// });
		// return retMat;

	}

	@Override
	public Polygon asPolygon() {
		final Polygon e = new Polygon();

		final Matrix transformMatrix = this.transformMatrix();
		final Point2dImpl circlePoint = new Point2dImpl(0, 0);
		for (double t = -Math.PI; t < Math.PI; t += Math.PI / 360) {
			circlePoint.x = (float) Math.cos(t);
			circlePoint.y = (float) Math.sin(t);
			e.points.add(circlePoint.transform(transformMatrix));
		}
		return e;
	}

	@Override
	public double intersectionArea(Shape that) {
		return intersectionArea(that, 1);
	}

	@Override
	public double intersectionArea(Shape that, int nStepsPerDimension) {
		// Rectangle overlapping =
		// this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		// if(overlapping==null)
		// return 0;
		// // if(that instanceof Ellipse) return
		// intersectionAreaEllipse((Ellipse) that);
		// double intersection = 0;
		// double step = Math.max(overlapping.width,
		// overlapping.height)/(double)nStepsPerDimention;
		// double nReads = 0;
		// for(float x = overlapping.x; x < overlapping.x + overlapping.width;
		// x+=step){
		// for(float y = overlapping.y; y < overlapping.y + overlapping.height;
		// y+=step){
		// boolean insideThis = this.isInside(new Point2dImpl(x,y));
		// boolean insideThat = that.isInside(new Point2dImpl(x,y));
		// nReads++;
		// if(insideThis && insideThat) {
		// intersection++;
		// }
		// }
		// }
		//
		// return (intersection/nReads) * (overlapping.width *
		// overlapping.height);

		if (!this.calculateRegularBoundingBox().isOverlapping(that.calculateRegularBoundingBox())) {
			return 0;
		}
		final Rectangle union = this.calculateRegularBoundingBox().union(that.calculateRegularBoundingBox());
		final float dr = (Math.min(union.width, union.height) / nStepsPerDimension);

		// System.out.println("Union rectangle: " + union);
		// System.out.println("Union step: " + dr);
		int bua = 0;
		int bna = 0;
		int total = 0;
		// compute the area
		for (float rx = union.x; rx <= union.x + union.width; rx += dr) {
			for (float ry = union.y; ry <= union.y + union.height; ry += dr) {
				// compute the distance from the ellipse center
				final Point2dImpl p = new Point2dImpl(rx, ry);
				final boolean inThis = this.isInside(p);
				final boolean inThat = that.isInside(p);
				total++;
				// compute the area
				if (inThis && inThat)
					bna++;
				if (inThis || inThat)
					bua++;

			}
		}
		final double rectShapeProp = ((double) bua) / ((double) total);
		final double intersectProp = ((double) bna / (double) bua) * rectShapeProp;
		return union.calculateArea() * intersectProp;
	}

	/**
	 * @return The semi-minor axis length
	 */
	public double getMinor() {
		return this.minor;
	}

	/**
	 * @return The semi-major axis length
	 */
	public double getMajor() {
		return this.major;
	}

	/**
	 * @return The second moment matrix and scale factor.
	 */
	public IndependentPair<Matrix, Double> secondMomentsAndScale() {
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Ellipse))
			return false;
		final Ellipse that = (Ellipse) other;
		return this.major == that.major && this.minor == that.minor && this.x == that.x && this.y == that.y
				&& this.rotation == that.rotation;
	}

	@Override
	public Ellipse clone() {
		Ellipse e;
		try {
			e = (Ellipse) super.clone();
		} catch (final CloneNotSupportedException e1) {
			return null;
		}
		return e;
	}

	/**
	 * @return The rotation of the semi-major axis
	 */
	public double getRotation() {
		return this.rotation;
	}

	@Override
	public String toString() {
		return String.format("Ellipse(x=%4.2f,y=%4.2f,major=%4.2f,minor=%4.2f,rot=%4.2f(%4.2f))", this.x, this.y,
				this.major, this.minor, this.rotation, this.rotation * (180.0 / Math.PI));
	}

	@Override
	public double calculatePerimeter() {
		// Ramanujan's second approximation

		final double a = major;
		final double b = minor;
		final double h = ((a - b) * (a - b)) / ((a + b) * (a + b));

		return Math.PI * (a + b) * (1 + ((3 * h) / (10 + Math.sqrt(4 - 3 * h))));
	}

	@Override
	public RotatedRectangle minimumBoundingRectangle() {
		return new RotatedRectangle(x, y, 2 * major, 2 * minor, rotation);
	}

	@Override
	public boolean isConvex() {
		return true;
	}
}
