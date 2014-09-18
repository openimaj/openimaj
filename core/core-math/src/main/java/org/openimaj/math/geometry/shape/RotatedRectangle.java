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

import Jama.Matrix;

/**
 * A rectangle rotated by an angle.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class RotatedRectangle implements Shape, Cloneable {
	/**
	 * The width of the rotated rectangle (Note that this is different to the
	 * width returned by {@link #getWidth()} which is the width of the regular
	 * bounding box)
	 */
	public float width;

	/**
	 * The height of the rotated rectangle (Note that this is different to the
	 * height returned by {@link #getHeight()} which is the height of the
	 * regular bounding box)
	 */
	public float height;

	/**
	 * The rotation angle in radians
	 */
	public float rotation;

	/**
	 * The x-ordinate of the centroid
	 */
	public float cx;

	/**
	 * The y-ordinate of the centroid
	 */
	public float cy;

	/**
	 * Construct with a regular {@link Rectangle} rotated about its centroid
	 * 
	 * @param regRect
	 *            the regular rectangle
	 * @param rotation
	 *            the rotation angle in radians
	 */
	public RotatedRectangle(Rectangle regRect, float rotation) {
		this.width = regRect.width;
		this.height = regRect.height;
		this.cx = regRect.x + regRect.width / 2f;
		this.cy = regRect.y + regRect.height / 2f;
		this.rotation = rotation;
	}

	/**
	 * Construct with the given parameters
	 * 
	 * @param x
	 *            the x-ordinate of the centroid
	 * @param y
	 *            the y-ordinate of the centroid
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param rotation
	 *            the rotation
	 */
	public RotatedRectangle(double x, double y, double width, double height, double rotation) {
		this.cx = (float) x;
		this.cy = (float) y;
		this.width = (float) width;
		this.height = (float) height;
		this.rotation = (float) rotation;
	}

	/**
	 * Construct with the given parameters
	 * 
	 * @param x
	 *            the x-ordinate of the centroid
	 * @param y
	 *            the y-ordinate of the centroid
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param rotation
	 *            the rotation
	 */
	public RotatedRectangle(float x, float y, float width, float height, float rotation) {
		this.cx = x;
		this.cy = y;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {
		return this.asPolygon().calculateRegularBoundingBox();
	}

	@Override
	public void translate(float x, float y) {
		cx += x;
		cy += y;
	}

	@Override
	public void scale(float sc) {
		cx *= sc;
		cy *= sc;
		width *= sc;
		height *= sc;
	}

	@Override
	public void scale(Point2d centre, float sc) {
		this.translate(-centre.getX(), -centre.getY());
		scale(sc);
		this.translate(centre.getX(), centre.getY());

	}

	@Override
	public void scaleCentroid(float sc) {
		scale(calculateCentroid(), sc);
	}

	@Override
	public Point2d calculateCentroid() {
		return new Point2dImpl(cx, cy);
	}

	@Override
	public double minX() {
		return this.asPolygon().minX();
	}

	@Override
	public double minY() {
		return this.asPolygon().minY();
	}

	@Override
	public double maxX() {
		return this.asPolygon().maxX();
	}

	@Override
	public double maxY() {
		return this.asPolygon().maxY();
	}

	@Override
	public double getWidth() {
		return this.asPolygon().getWidth();
	}

	@Override
	public double getHeight() {
		return this.asPolygon().getHeight();
	}

	@Override
	public boolean isInside(Point2d point) {
		return this.asPolygon().isInside(point);
	}

	@Override
	public double calculateArea() {
		return width * height;
	}

	@Override
	public double calculatePerimeter() {
		return 2 * (width + height);
	}

	@Override
	public Polygon asPolygon() {
		final float b = (float) Math.cos(rotation) * 0.5f;
		final float a = (float) Math.sin(rotation) * 0.5f;

		final Point2dImpl[] pts = new Point2dImpl[4];
		pts[0] = new Point2dImpl();
		pts[0].x = cx - a * height - b * width;
		pts[0].y = cy + b * height - a * width;

		pts[1] = new Point2dImpl();
		pts[1].x = cx + a * height - b * width;
		pts[1].y = cy - b * height - a * width;

		pts[2] = new Point2dImpl();
		pts[2].x = 2 * cx - pts[0].x;
		pts[2].y = 2 * cy - pts[0].y;

		pts[3] = new Point2dImpl();
		pts[3].x = 2 * cx - pts[1].x;
		pts[3].y = 2 * cy - pts[1].y;

		return new Polygon(pts);
	}

	@Override
	public double intersectionArea(Shape that) {
		return this.asPolygon().intersectionArea(that);
	}

	@Override
	public double intersectionArea(Shape that, int nStepsPerDimension) {
		return this.asPolygon().intersectionArea(that, nStepsPerDimension);
	}

	@Override
	public Shape transform(Matrix transform) {
		return this.asPolygon().transform(transform);
	}

	@Override
	public RotatedRectangle minimumBoundingRectangle() {
		return this.clone();
	}

	@Override
	public RotatedRectangle clone() {
		try {
			return (RotatedRectangle) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return String.format("RotatedRect[angle=%2.2f, cx=%2.2f, cy=%2.2f, width=%2.2f, height=%2.2f]", rotation, cx, cy,
				width, height);
	}

	@Override
	public boolean isConvex() {
		return true;
	}
}
