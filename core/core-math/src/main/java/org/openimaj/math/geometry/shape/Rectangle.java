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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

import Jama.Matrix;

/**
 * A rectangle shape oriented to the axes. For non-oriented versions, use a
 * polygon.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Rectangle implements Shape, ReadWriteable, Serializable {
	private static final long serialVersionUID = 1L;

	/** The x-coordinate of the top-left of the rectangle */
	public float x;

	/** The y-coordinate of the top-left of the rectangle */
	public float y;

	/** The width of the rectangle */
	public float width;

	/** The height of the rectangle */
	public float height;

	/**
	 * Construct a unit rectangle
	 */
	public Rectangle() {
		this(0, 0, 1, 1);
	}

	/**
	 * Construct a Rectangle with the given parameters.
	 *
	 * @param x
	 *            x-coordinate of top-left
	 * @param y
	 *            y-coordinate of top-left
	 * @param width
	 *            width
	 * @param height
	 *            height
	 */
	public Rectangle(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
	 * Construct a Rectangle by copying from another rectangle.
	 *
	 * @param r
	 *            rectangle to copy from
	 */
	public Rectangle(Rectangle r) {
		this.x = r.x;
		this.y = r.y;
		this.width = r.width;
		this.height = r.height;
	}

	/**
	 * Construct a Rectangle with the given parameters.
	 *
	 * @param topLeft
	 *            top-left corner
	 * @param bottomRight
	 *            bottom-right corner
	 */
	public Rectangle(Point2d topLeft, Point2d bottomRight) {
		x = topLeft.getX();
		y = topLeft.getY();
		width = bottomRight.getX() - x;
		height = bottomRight.getY() - y;
	}

	@Override
	public boolean isInside(Point2d point) {
		final float px = point.getX();
		final float py = point.getY();

		if (px >= x && px <= x + width && py >= y && py <= y + height)
			return true;

		return false;
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {
		return new Rectangle(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
	}

	@Override
	public void translate(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public void scale(float sc) {
		x *= sc;
		y *= sc;
		width *= sc;
		height *= sc;
	}

	@Override
	public void scale(Point2d centre, float sc) {
		translate(-centre.getX(), -centre.getY());
		scale(sc);
		translate(centre.getX(), centre.getY());
	}

	@Override
	public void scaleCentroid(float sc) {
		final Point2d centre = this.calculateCentroid();
		translate(-centre.getX(), -centre.getY());
		scale(sc);
		translate(centre.getX(), centre.getY());
	}

	@Override
	public Point2d calculateCentroid() {
		return new Point2dImpl(x + width / 2, y + height / 2);
	}

	@Override
	public double calculateArea() {
		return width * height;
	}

	@Override
	public double minX() {
		return x;
	}

	@Override
	public double minY() {
		return y;
	}

	@Override
	public double maxX() {
		return x + width;
	}

	@Override
	public double maxY() {
		return y + height;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	/**
	 * @return The top-left coordinate
	 */
	public Point2d getTopLeft() {
		return new Point2dImpl((float) minX(), (float) minY());
	}

	/**
	 * @return The bottom-right coordinate
	 */
	public Point2d getBottomRight() {
		return new Point2dImpl((float) maxX(), (float) maxY());
	}

	@Override
	public Shape transform(Matrix transform) {
		// TODO: could handle different cases and hand
		// back correct shape here depending on transform
		return asPolygon().transform(transform);
	}

	@Override
	public Polygon asPolygon() {
		final Polygon polygon = new Polygon();
		polygon.points.add(new Point2dImpl(x, y));
		polygon.points.add(new Point2dImpl(x + width, y));
		polygon.points.add(new Point2dImpl(x + width, y + height));
		polygon.points.add(new Point2dImpl(x, y + height));
		return polygon;
	}

	/**
	 * Set the position and size of this rectangle
	 *
	 * @param x
	 *            x-coordinate of top-left
	 * @param y
	 *            y-coordinate of top-left
	 * @param width
	 *            width
	 * @param height
	 *            height
	 */
	public void setBounds(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public String toString() {
		return String.format("Rectangle[x=%2.2f,y=%2.2f,width=%2.2f,height=%2.2f]", x, y, width, height);
	}

	/**
	 * Test if rectangles overlap.
	 *
	 * @param other
	 *            the rectangle to test with.
	 * @return true if there is overlap; false otherwise.
	 */
	public boolean isOverlapping(Rectangle other) {
		final float left = x;
		final float right = x + width;
		final float top = y;
		final float bottom = y + height;
		final float otherleft = other.x;
		final float otherright = other.x + other.width;
		final float othertop = other.y;
		final float otherbottom = other.y + other.height;
		return !(left > otherright || right < otherleft || top > otherbottom || bottom < othertop);
	}

	/**
	 * Test if the given rectangle is inside this one.
	 *
	 * @param rect
	 *            the rectangle to test with.
	 * @return true if this rectangle is inside the other; false otherwise.
	 */
	public boolean isInside(Rectangle rect) {
		return this.x <= rect.x && this.y <= rect.y && this.x + this.width >= rect.x + rect.width
				&& this.y + this.height >= rect.y + rect.height;
	}

	/**
	 * Get the overlapping rectangle between this rectangle and another.
	 *
	 * @param other
	 *            the rectangle to test with.
	 * @return the overlap rectangle, or null if there is no overlap.
	 */
	public Rectangle overlapping(Rectangle other) {
		if (!isOverlapping(other))
			return null;

		final float left = x;
		final float right = x + width;
		final float top = y;
		final float bottom = y + height;
		final float otherleft = other.x;
		final float otherright = other.x + other.width;
		final float othertop = other.y;
		final float otherbottom = other.y + other.height;
		final float overlapleft = Math.max(left, otherleft);
		final float overlaptop = Math.max(top, othertop);
		final float overlapwidth = Math.min(right, otherright) - overlapleft;
		final float overlapheight = Math.min(bottom, otherbottom) - overlaptop;

		return new Rectangle(overlapleft, overlaptop, overlapwidth, overlapheight);
	}

	/**
	 * Compute the percentage by which the given rectangle overlaps this one.
	 *
	 * @param other
	 * @return the percentage overlap
	 */
	public double percentageOverlap(Rectangle other) {
		final Rectangle overlap = overlapping(other);

		if (overlap == null)
			return 0;

		return (overlap.calculateArea() / calculateArea());
	}

	/**
	 * Find the rectangle that just contains this rectangle and another
	 * rectangle.
	 *
	 * @param other
	 *            the other rectangle
	 * @return a rectangle
	 */
	public Rectangle union(Rectangle other) {
		final float left = x;
		final float right = x + width;
		final float top = y;
		final float bottom = y + height;
		final float otherleft = other.x;
		final float otherright = other.x + other.width;
		final float othertop = other.y;
		final float otherbottom = other.y + other.height;
		final float intersectleft = Math.min(left, otherleft);
		final float intersecttop = Math.min(top, othertop);
		final float intersectwidth = Math.max(right, otherright) - intersectleft;
		final float intersectheight = Math.max(bottom, otherbottom) - intersecttop;
		return new Rectangle(intersectleft, intersecttop, intersectwidth, intersectheight);
	}

	@Override
	public double intersectionArea(Shape that) {
		return intersectionArea(that, 1);
	}

	@Override
	public double intersectionArea(Shape that, int nStepsPerDimension) {
		final Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if (overlapping == null)
			return 0;
		if (that instanceof Rectangle) {
			// Special case
			return overlapping.calculateArea();
		} else {
			double intersection = 0;
			final double step = Math.max(overlapping.width, overlapping.height) / (double) nStepsPerDimension;
			double nReads = 0;
			for (float x = overlapping.x; x < overlapping.x + overlapping.width; x += step) {
				for (float y = overlapping.y; y < overlapping.y + overlapping.height; y += step) {
					final boolean insideThis = this.isInside(new Point2dImpl(x, y));
					final boolean insideThat = that.isInside(new Point2dImpl(x, y));
					nReads++;
					if (insideThis && insideThat) {
						intersection++;
					}
				}
			}

			return (intersection / nReads) * (overlapping.width * overlapping.height);
		}
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		x = in.nextFloat();
		y = in.nextFloat();
		width = in.nextFloat();
		height = in.nextFloat();
	}

	@Override
	public String asciiHeader() {
		return "Rectangle";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		x = in.readFloat();
		y = in.readFloat();
		width = in.readFloat();
		height = in.readFloat();
	}

	@Override
	public byte[] binaryHeader() {
		return "Rectangle".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.write(String.format("%f %f %f %f\n", x, y, width, height));
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(width);
		out.writeFloat(height);
	}

	@Override
	public Rectangle clone() {
		return new Rectangle(x, y, width, height);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(height);
		result = prime * result + Float.floatToIntBits(width);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Rectangle other = (Rectangle) obj;
		if (Float.floatToIntBits(height) != Float.floatToIntBits(other.height))
			return false;
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width))
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	@Override
	public double calculatePerimeter() {
		return 2 * (width + height);
	}

	@Override
	public RotatedRectangle minimumBoundingRectangle() {
		return new RotatedRectangle(this, 0);
	}

	/**
	 * Rotate the {@link Rectangle} about the given pivot with the given angle
	 * (in radians)
	 *
	 * @param p
	 *            the pivot of the rotation
	 * @param angle
	 *            the angle in radians
	 * @return the rotated rectangle
	 */
	public RotatedRectangle rotate(Point2d p, double angle) {
		final Point2dImpl c = (Point2dImpl) this.calculateCentroid();
		final float sin = (float) Math.sin(angle);
		final float cos = (float) Math.cos(angle);

		c.translate(-p.getX(), -p.getY());

		final float xnew = c.x * cos - c.y * sin;
		final float ynew = c.x * sin + c.y * cos;

		c.x = xnew;
		c.y = ynew;

		c.translate(p);

		return new RotatedRectangle(c.x, c.y, width, height, angle);
	}

	@Override
	public boolean isConvex() {
		return true;
	}
}
