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
package org.openimaj.math.geometry.line;

import java.util.Arrays;
import java.util.Iterator;

import org.openimaj.math.geometry.path.Path2d;
import org.openimaj.math.geometry.path.Polyline;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * A line in two-dimensional space.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class Line2d implements Path2d, Cloneable {
	/**
	 * Start point of line
	 */
	public Point2d begin;

	/**
	 * End point of line
	 */
	public Point2d end;

	/**
	 * Construct a line
	 */
	public Line2d() {
	}

	/**
	 * Construct a line
	 *
	 * @param begin
	 *            start point
	 * @param end
	 *            end point
	 */
	public Line2d(Point2d begin, Point2d end) {
		this.begin = begin;
		this.end = end;
	}

	/**
	 * Construct a line
	 *
	 * @param x1
	 *            x-ordinate of start point
	 * @param y1
	 *            y-ordinate of start point
	 * @param x2
	 *            x-ordinate of end point
	 * @param y2
	 *            y-ordinate of end point
	 *
	 */
	public Line2d(float x1, float y1, float x2, float y2) {
		this.begin = new Point2dImpl(x1, y1);
		this.end = new Point2dImpl(x2, y2);
	}

	/**
	 * Set the start point
	 *
	 * @param begin
	 *            start point
	 */
	public void setBeginPoint(Point2d begin) {
		this.begin = begin;
	}

	/**
	 * Set the end point
	 *
	 * @param end
	 *            end point
	 */
	public void setEndPoint(Point2d end) {
		this.end = end;
	}

	/**
	 * Get the start point
	 *
	 * @return the start point
	 */
	public Point2d getBeginPoint() {
		return begin;
	}

	/**
	 * Get the end point
	 *
	 * @return The end point
	 */
	public Point2d getEndPoint() {
		return end;
	}

	/**
	 * Get the end point
	 *
	 * @return the end point
	 */
	public Point2d setEndPoint() {
		return end;
	}

	/**
	 * The type of a result of a line intersection
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public enum IntersectionType
	{
		/**
		 * Intersecting line (lines that cross)
		 */
		INTERSECTING,
		/**
		 * Parallel line
		 */
		PARALLEL,
		/**
		 * Co-incident line (on top of each other)
		 */
		COINCIDENT,
		/**
		 * non-intersecting line
		 */
		NOT_INTERESECTING
	}

	/**
	 * The result of a line intersection.
	 *
	 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
	 */
	static public class IntersectionResult
	{
		/**
		 * The type of intersection
		 */
		public IntersectionType type;

		/**
		 * The point at which the lines intersect (if the type is INTERSECTING)
		 */
		public Point2d intersectionPoint;

		/**
		 * Construct the IntersectionResult with the given type
		 *
		 * @param type
		 *            the type
		 */
		public IntersectionResult(IntersectionType type) {
			this.type = type;
		}

		/**
		 * Construct the IntersectionResult with the given intersection point
		 *
		 * @param point
		 *            the intersection point
		 */
		public IntersectionResult(Point2d point) {
			this.type = IntersectionType.INTERSECTING;
			this.intersectionPoint = point;
		}
	}

	/**
	 * Calculates the intersection point of this line and another line
	 *
	 * @param otherLine
	 *            The other line segment
	 * @return a {@link IntersectionResult}
	 * @see "http://local.wasp.uwa.edu.au/~pbourke/geometry/lineline2d/"
	 */
	public IntersectionResult getIntersection(Line2d otherLine) {
		final double denom = ((otherLine.end.getY() - otherLine.begin.getY()) * (end.getX() - begin.getX())) -
				((otherLine.end.getX() - otherLine.begin.getX()) * (end.getY() - begin.getY()));

		final double numea = ((otherLine.end.getX() - otherLine.begin.getX()) * (begin.getY() - otherLine.begin.getY())) -
				((otherLine.end.getY() - otherLine.begin.getY()) * (begin.getX() - otherLine.begin.getX()));

		final double numeb = ((end.getX() - begin.getX()) * (begin.getY() - otherLine.begin.getY())) -
				((end.getY() - begin.getY()) * (begin.getX() - otherLine.begin.getX()));

		if (denom == 0.0)
		{
			if (numea == 0.0 && numeb == 0.0)
			{
				return new IntersectionResult(IntersectionType.COINCIDENT);
			}

			return new IntersectionResult(IntersectionType.PARALLEL);
		}

		final double ua = numea / denom;
		final double ub = numeb / denom;

		if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0 && ub <= 1.0)
		{
			// Get the intersection point.
			final double intX = begin.getX() + ua * (end.getX() - begin.getX());
			final double intY = begin.getY() + ua * (end.getY() - begin.getY());

			return new IntersectionResult(new Point2dImpl((float) intX, (float) intY));
		}

		return new IntersectionResult(IntersectionType.NOT_INTERESECTING);
	}

	/**
	 * Reflects a point around a this line.
	 *
	 * @param pointToReflect
	 *            The point to reflect
	 * @return The reflected point
	 *
	 * @see "http://algorithmist.wordpress.com/2009/09/15/reflecting-a-point-about-a-line/"
	 */
	public Point2d reflectAroundLine(Point2d pointToReflect) {
		double nx = end.getX() - begin.getX();
		double ny = end.getY() - begin.getY();
		final double d = Math.sqrt(nx * nx + ny * ny);
		nx /= d;
		ny /= d;

		final double px = pointToReflect.getX() - begin.getX();
		final double py = pointToReflect.getY() - begin.getY();
		final double w = nx * px + ny * py;
		final double rx = 2 * begin.getX() - pointToReflect.getX() + 2 * w * nx;
		final double ry = 2 * begin.getY() - pointToReflect.getY() + 2 * w * ny;
		return new Point2dImpl((float) rx, (float) ry);
	}

	float CalcY(float xval, float x0, float y0, float x1, float y1)
	{
		if (x1 == x0)
			return Float.NaN;
		return y0 + (xval - x0) * (y1 - y0) / (x1 - x0);
	}

	float CalcX(float yval, float x0, float y0, float x1, float y1)
	{
		if (y1 == y0)
			return Float.NaN;
		return x0 + (yval - y0) * (x1 - x0) / (y1 - y0);
	}

	/**
	 * Given a rectangle, return the line that actually fits inside the
	 * rectangle for this line
	 *
	 * @param r
	 *            the bounds
	 * @return the line
	 */
	public Line2d lineWithinSquare(Rectangle r)
	{
		final boolean beginInside = r.isInside(begin);
		final int nInside = (beginInside ? 1 : 0) + (r.isInside(end) ? 1 : 0);
		if (nInside == 2) {
			return new Line2d(this.begin.copy(), this.end.copy());
		}
		Point2d begin = null;
		Point2d end = null;

		final float x0 = this.begin.getX();
		final float y0 = this.begin.getY();
		final float x1 = this.end.getX();
		final float y1 = this.end.getY();
		final float bottom = r.y + r.height;
		final float top = r.y;
		final float left = r.x;
		final float right = r.x + r.width;
		final float bottomIntersect = CalcX(bottom, x0, y0, x1, y1);
		final float topIntersect = CalcX(top, x0, y0, x1, y1);
		final float leftIntersect = CalcY(left, x0, y0, x1, y1);
		final float rightIntersect = CalcY(right, x0, y0, x1, y1);
		if (bottomIntersect <= right && bottomIntersect >= left) {
			if (end == null)
				end = new Point2dImpl(bottomIntersect, bottom);
		}
		if (topIntersect <= right && topIntersect >= left) {
			if (end == null)
				end = new Point2dImpl(topIntersect, top);
			else if (begin == null) {
				begin = new Point2dImpl(topIntersect, top);
				if (end.equals(begin))
					end = null;
			}

		}

		if (leftIntersect >= top && leftIntersect <= bottom) {
			if (end == null)
				end = new Point2dImpl(left, leftIntersect);
			else if (begin == null) {
				begin = new Point2dImpl(left, leftIntersect);
				if (end.equals(begin))
					end = null;
			}
		}
		if (rightIntersect >= top && rightIntersect <= bottom) {
			if (end == null)
				end = new Point2dImpl(right, rightIntersect);
			else if (begin == null) {
				begin = new Point2dImpl(right, rightIntersect);
				if (end.equals(begin))
					end = null;
			}
		}
		if (end == null || begin == null)
			return null;

		if (nInside == 0)
		{
			if (distance(this.end, end) < distance(this.end, begin) == distance(this.begin, end) < distance(this.begin,
					begin))
				return null;
			else
				return new Line2d(begin, end);
		}

		// Complex case
		if (beginInside) {
			if (distance(this.end, end) < distance(this.end, begin))
				return new Line2d(this.begin, end);
			else
				return new Line2d(this.begin, begin);
		}
		else {
			if (distance(this.begin, begin) < distance(this.begin, end))
				return new Line2d(begin, this.end);
			else
				return new Line2d(end, this.end);
		}
	}

	/**
	 * Get the Euclidean distance between two points
	 *
	 * @param p1
	 *            the first point
	 * @param p2
	 *            the second point
	 * @return the distance
	 */
	public static double distance(Point2d p1, Point2d p2) {
		return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY())
				* (p1.getY() - p2.getY()));
	}

	/**
	 * Get the Euclidean distance between two points
	 *
	 * @param p1x
	 *            the first point
	 * @param p1y
	 *            the first point
	 * @param p2x
	 *            the second point
	 * @param p2y
	 *            the first point
	 * @return the distance
	 */
	public static double distance(float p1x, float p1y, float p2x, float p2y) {
		return Math.sqrt((p1x - p2x) * (p1x - p2x) + (p1y - p2y) * (p1y - p2y));
	}

	/**
	 * Create a line of a given length that starts at a point and has a given
	 * angle.
	 *
	 * @param x1
	 *            x-ordinate of starting point
	 * @param y1
	 *            y-ordinate of starting point
	 * @param theta
	 *            angle in radians
	 * @param length
	 *            line length
	 * @return the line
	 */
	public static Line2d lineFromRotation(int x1, int y1, double theta, int length) {
		final int x2 = x1 + (int) Math.round(Math.cos(theta) * length);
		final int y2 = y1 + (int) Math.round(Math.sin(theta) * length);

		return new Line2d(new Point2dImpl(x1, y1), new Point2dImpl(x2, y2));
	}

	/**
	 * @return The length of the line.
	 */
	@Override
	public double calculateLength() {
		return distance(begin, end);
	}

	/**
	 * Returns the angle (radians) this line makes with a horizontal line
	 *
	 * @return the angle this line makes with a horizontal line
	 */
	public double calculateHorizontalAngle()
	{
		return Math.atan((end.getY() - begin.getY()) / (end.getX() - begin.getX()));
	}

	/**
	 * Returns the angle (radians) this line makes with a vertical line
	 *
	 * @return the angle this line makes with a vertical line
	 */
	public double calculateVerticalAngle()
	{
		return Math.atan((end.getX() - begin.getX()) / (end.getY() - begin.getY()));
	}

	/**
	 * Transform a line.
	 *
	 * @param transform
	 *            the transform matrix.
	 * @return the transformed line.
	 */
	@Override
	public Line2d transform(Matrix transform) {
		return new Line2d(begin.transform(transform), end.transform(transform));
	}

	/**
	 * Returns a line that is at 90 degrees to the original line.
	 *
	 * @return the normal line
	 */
	public Line2d getNormal()
	{
		final float dx = end.getX() - begin.getX();
		final float dy = end.getY() - begin.getY();
		return new Line2d(new Point2dImpl(-dy, dx), new Point2dImpl(dy, -dx));
	}

	/**
	 * Returns a line that is at 90 degrees to the original line and also passes
	 * through the given point.
	 *
	 * @param p
	 *            A point that must exist on the normal line
	 * @return a new line at right-angles to this
	 */
	public Line2d getNormal(Point2d p)
	{
		return new Line2d(this.reflectAroundLine(p), p);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openimaj.math.geometry.GeometricObject#translate(float, float)
	 */
	@Override
	public void translate(float x, float y)
	{
		this.begin.translate(x, y);
		this.end.translate(x, y);
	}

	/**
	 * Tests whether the given point lies on this line. Note that this will test
	 * whether the point sits on a line that travels to infinity in both
	 * directions.
	 *
	 * @param p
	 *            The point to test.
	 * @param tolerance
	 *            The tolerance to use in the test
	 * @return TRUE if the point lies on this line.
	 */
	public boolean isOnLine(Point2d p, float tolerance)
	{
		// vertical line
		if (begin.getX() == end.getX() && begin.getX() == p.getX())
			return true;
		// Horizontal line
		if (begin.getY() == end.getY() && begin.getY() == p.getY())
			return true;

		final float a = (end.getY() - begin.getY()) / (end.getX() - begin.getX());
		final float b = begin.getY() - a * begin.getX();
		if (Math.abs(p.getY() - (a * p.getX() + b)) < tolerance)
			return true;

		return false;
	}

	/**
	 * Tests whether the given point lies on this line. If the point sits on the
	 * line but is outside of the end points, then this function will return
	 * false.
	 *
	 * @param p
	 *            The point to test.
	 * @param tolerance
	 *            The tolerance to use in the test
	 * @return TRUE if the point lies on this line.
	 */
	public boolean isInLine(Point2d p, float tolerance)
	{
		final float bx = (begin.getX() <= end.getX() ? begin.getX() : end.getX());
		final float ex = (begin.getX() > end.getX() ? begin.getX() : end.getX());
		return isOnLine(p, tolerance) && p.getX() + tolerance > bx &&
				p.getX() + tolerance < ex;
	}

	@Override
	public String toString()
	{
		return "Line(" + begin + "->" + end + ")";
	}

	@Override
	public Rectangle calculateRegularBoundingBox() {
		final float x = Math.min(begin.getX(), end.getX());
		final float y = Math.min(begin.getY(), end.getY());
		final float width = Math.abs(begin.getX() - end.getX());
		final float height = Math.abs(begin.getY() - end.getY());

		return new Rectangle(x, y, width, height);
	}

	@Override
	public void scale(float sc) {
		begin.setX(begin.getX() * sc);
		begin.setY(begin.getY() * sc);
		end.setX(end.getX() * sc);
		end.setY(end.getY() * sc);
	}

	@Override
	public void scale(Point2d centre, float sc) {
		this.translate(-centre.getX(), -centre.getY());

		begin.setX(begin.getX() * sc);
		begin.setY(begin.getY() * sc);
		end.setX(end.getX() * sc);
		end.setY(end.getY() * sc);

		this.translate(centre.getX(), centre.getY());
	}

	@Override
	public void scaleCentroid(float sc) {
		scale(this.calculateCentroid(), sc);
	}

	@Override
	public Point2d calculateCentroid() {
		float xSum = begin.getX() + end.getX();
		float ySum = begin.getY() + end.getY();

		xSum /= 2;
		ySum /= 2;

		return new Point2dImpl(xSum, ySum);
	}

	@Override
	public double minX() {
		return Math.min(begin.getX(), end.getX());
	}

	@Override
	public double minY() {
		return Math.min(begin.getY(), end.getY());
	}

	@Override
	public double maxX() {
		return Math.max(begin.getX(), end.getX());
	}

	@Override
	public double maxY() {
		return Math.max(begin.getY(), end.getY());
	}

	@Override
	public double getWidth() {
		return Math.abs(begin.getX() - end.getX());
	}

	@Override
	public double getHeight() {
		return Math.abs(begin.getY() - end.getY());
	}

	/**
	 * Convert the line to a unit vector
	 *
	 * @return unit vector in the same direction as the line
	 */
	public Point2dImpl toUnitVector() {
		final float dx = end.getX() - begin.getX();
		final float dy = end.getY() - begin.getY();
		final float norm = (float) Math.sqrt(dx * dx + dy * dy);

		return new Point2dImpl(dx / norm, dy / norm);
	}

	@Override
	public Line2d clone() {
		return new Line2d(begin.copy(), end.copy());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(new Object[] { this.begin, this.end });
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Line2d))
			return false;
		final Line2d o = (Line2d) other;
		if (!(o.begin.equals(begin) && o.end.equals(end)))
			return false;
		return true;
	}

	@Override
	public Point2d begin() {
		return begin;
	}

	@Override
	public Point2d end() {
		return end;
	}

	@Override
	public Polyline asPolyline() {
		return new Polyline(lineIterator());
	}

	@Override
	public Iterator<Line2d> lineIterator() {
		return new Iterator<Line2d>() {
			boolean hasNext = true;

			@Override
			public boolean hasNext() {
				return hasNext;
			}

			@Override
			public Line2d next() {
				hasNext = false;
				return Line2d.this;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
