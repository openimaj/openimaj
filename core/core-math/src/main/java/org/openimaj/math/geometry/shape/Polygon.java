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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.shape.util.PolygonUtils;
import org.openimaj.math.geometry.shape.util.RotatingCalipers;

import Jama.Matrix;

/**
 * A polygon, modelled as a list of vertices. Polygon extends {@link PointList},
 * so the vertices are the underlying {@link PointList#points}, and they are
 * considered to be joined in order.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Polygon extends PointList implements Shape
{
	/**
	 * Polygons can contain other polygons. If the polygon is representing a
	 * shape, then the inner polygons can represent holes in the polygon or
	 * other polygons within the polygon.
	 */
	private List<Polygon> innerPolygons = new ArrayList<Polygon>();

	/** If this polygon is a hole within another polygon, this is set to true */
	private boolean isHole = false;

	/**
	 * Constructs an empty polygon to which vertices may be added.
	 */
	public Polygon()
	{
		this(false);
	}

	/**
	 * Constructs an empty polygon to which vertices may be added. The boolean
	 * parameter determines whether this polygon will represent a hole (rather
	 * than a solid).
	 * 
	 * @param representsHole
	 *            Whether the polygon will represent a hole.
	 */
	public Polygon(boolean representsHole)
	{
		this.isHole = representsHole;
	}

	/**
	 * Construct a Polygon from vertices
	 * 
	 * @param vertices
	 *            the vertices
	 */
	public Polygon(Point2d... vertices) {
		super(vertices);
	}

	/**
	 * Construct a Polygon from vertices
	 * 
	 * @param vertices
	 *            the vertices
	 */
	public Polygon(Collection<? extends Point2d> vertices) {
		this(vertices, false);
	}

	/**
	 * Construct a Polygon from the vertices, possibly copying the vertices
	 * first
	 * 
	 * @param vertices
	 *            the vertices
	 * @param copy
	 *            should the vertices be copied
	 */
	public Polygon(Collection<? extends Point2d> vertices, boolean copy) {
		super(vertices, copy);
	}

	/**
	 * Get the vertices of the polygon
	 * 
	 * @return the vertices
	 */
	public List<Point2d> getVertices() {
		return points;
	}

	/**
	 * Get the number of vertices
	 * 
	 * @return the number of vertices
	 */
	public int nVertices() {
		if (isClosed())
			return points.size() - 1;
		return points.size();
	}

	/**
	 * Is the polygon closed (i.e. is the last vertex equal to the first)?
	 * 
	 * @return true if closed; false if open
	 */
	public boolean isClosed() {
		if (points.size() > 0 && points.get(0).getX() == points.get(points.size() - 1).getX()
				&& points.get(0).getY() == points.get(points.size() - 1).getY())
			return true;
		return false;
	}

	/**
	 * Close the polygon if it's not already closed
	 */
	public void close() {
		if (!isClosed() && points.size() > 0)
			points.add(points.get(0));
	}

	/**
	 * Open the polygon if it's closed
	 */
	public void open() {
		if (isClosed() && points.size() > 1)
			points.remove(points.size() - 1);
	}

	/**
	 * Test whether the point p is inside the polygon using the winding rule
	 * algorithm. Also tests whether the point is in any of the inner polygons.
	 * If the inner polygon represents a hole and the point is within that
	 * polygon then the point is not within this polygon.
	 * 
	 * @param point
	 *            the point to test
	 * @return true if the point is inside; false otherwise
	 */
	@Override
	public boolean isInside(Point2d point) {
		final boolean isClosed = isClosed();
		if (!isClosed)
			close();

		boolean isOdd = false;

		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final List<Point2d> v = getInnerPoly(pp).getVertices();
			int j = v.size() - 1;
			for (int i = 0; i < v.size(); i++) {
				if (v.get(i).getY() < point.getY() && v.get(j).getY() >= point.getY() ||
						v.get(j).getY() < point.getY() && v.get(i).getY() >= point.getY())
				{
					if (v.get(i).getX() + (point.getY() - v.get(i).getY()) /
							(v.get(j).getY() - v.get(i).getY()) * (v.get(j).getX() - v.get(i).getX()) < point.getX())
					{
						isOdd = !isOdd;
					}
				}
				j = i;
			}
		}

		if (!isClosed)
			open();
		return isOdd;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Polygon clone() {
		final Polygon clone = new Polygon();
		clone.setIsHole(isHole);

		for (final Point2d p : points)
			clone.points.add(p.copy());

		for (final Polygon innerPoly : innerPolygons)
			clone.addInnerPolygon(innerPoly.clone());

		return clone;
	}

	/**
	 * Calculates the difference between two polygons and returns a new polygon.
	 * It assumes that the given polygon and this polygon have the same number
	 * of vertices.
	 * 
	 * @param p
	 *            the polygon to subtract.
	 * @return the difference polygon
	 */
	public Polygon difference(Polygon p)
	{
		final List<Point2d> v = new ArrayList<Point2d>();

		for (int i = 0; i < points.size(); i++)
			v.add(new Point2dImpl(
					points.get(i).getX() - p.getVertices().get(i).getX(),
					points.get(i).getY() - p.getVertices().get(i).getY()));

		final Polygon p2 = new Polygon(v);
		for (int i = 0; i < innerPolygons.size(); i++)
			p2.addInnerPolygon(innerPolygons.get(i).difference(
					p2.getInnerPoly(i + 1)));

		return p2;
	}

	@Override
	public double calculateArea() {
		return Math.abs(calculateSignedArea());
	}

	/**
	 * Calculate the area of the polygon. This does not take into account holes
	 * in the polygon.
	 * 
	 * @return the area of the polygon
	 */
	public double calculateSignedArea() {
		final boolean closed = isClosed();
		double area = 0;

		if (!closed)
			close();

		// TODO: This does not take into account the winding
		// rule and therefore holes
		for (int k = 0; k < points.size() - 1; k++) {
			final float ik = points.get(k).getX();
			final float jk = points.get(k).getY();
			final float ik1 = points.get(k + 1).getX();
			final float jk1 = points.get(k + 1).getY();

			area += ik * jk1 - ik1 * jk;
		}

		if (!closed)
			open();

		return 0.5 * area;
	}

	/**
	 * Calls {@link Polygon#intersectionArea(Shape, int)} with 1 step per pixel
	 * dimension. Subsequently this function returns the shared whole pixels of
	 * this polygon and that.
	 * 
	 * @param that
	 * @return intersection area
	 */
	@Override
	public double intersectionArea(Shape that) {
		return this.intersectionArea(that, 1);
	}

	/**
	 * Return an estimate for the area of the intersection of this polygon and
	 * another polygon. For each pixel step 1 is added if the point is inside
	 * both polygons. For each pixel, perPixelPerDimension steps are taken.
	 * Subsequently the intersection is:
	 * 
	 * sumIntersections / (perPixelPerDimension * perPixelPerDimension)
	 * 
	 * @param that
	 * @return normalised intersection area
	 */
	@Override
	public double intersectionArea(Shape that, int nStepsPerDimension) {
		final Rectangle overlapping = this.calculateRegularBoundingBox().overlapping(that.calculateRegularBoundingBox());
		if (overlapping == null)
			return 0;
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

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.openimaj.math.geometry.shape.Shape#asPolygon()
	 */
	@Override
	public Polygon asPolygon() {
		return this;
	}

	/**
	 * Add a vertex to the polygon
	 * 
	 * @param x
	 *            x-coordinate of the vertex
	 * @param y
	 *            y-coordinate of the vertex
	 */
	public void addVertex(float x, float y) {
		points.add(new Point2dImpl(x, y));
	}

	/**
	 * Add a vertex to the polygon
	 * 
	 * @param pt
	 *            coordinate of the vertex
	 */
	public void addVertex(Point2d pt) {
		points.add(pt);
	}

	/**
	 * Iterates through the vertices and rounds all vertices to round integers.
	 * Side-affects this polygon.
	 * 
	 * @return this polygon
	 */
	public Polygon roundVertices()
	{
		final Iterator<Point2d> i = this.iterator();
		while (i.hasNext())
		{
			final Point2d p = i.next();
			final Point2dImpl p2 = new Point2dImpl((int) p.getX(), (int) p.getY());

			int xx = -1;
			if ((xx = this.points.indexOf(p2)) != -1 &&
					this.points.get(xx) != p)
				i.remove();
			else
			{
				p.setX(p2.x);
				p.setY(p2.y);
			}
		}

		for (final Polygon pp : innerPolygons)
			pp.roundVertices();

		return this;
	}

	/**
	 * Return whether this polygon has no vertices or not.
	 * 
	 * @return TRUE if this polygon has no vertices
	 */
	public boolean isEmpty()
	{
		return this.points.isEmpty() && innerPolygons.isEmpty();
	}

	/**
	 * Returns the number of inner polygons in this polygon including this
	 * polygon.
	 * 
	 * @return the number of inner polygons in this polygon.
	 */
	public int getNumInnerPoly()
	{
		return innerPolygons.size() + 1;
	}

	/**
	 * Get the inner polygon at the given index. Note that index 0 will return
	 * this polygon, while index i+1 will return the inner polygon i.
	 * 
	 * @param index
	 *            the index of the polygon to get
	 * @return The inner polygon at the given index.
	 */
	public Polygon getInnerPoly(int index)
	{
		if (index == 0)
			return this;
		return innerPolygons.get(index - 1);
	}

	/**
	 * Add an inner polygon to this polygon. If there is no main polygon defined
	 * (the number of vertices is zero) then the given inner polygon will become
	 * the main polygon if the <code>inferOuter</code> boolean is true. If this
	 * variable is false, the inner polygon will be added to the list of inner
	 * polygons for this polygon regardless of whether a main polygon exists.
	 * When the main polygon is inferred from the given polygon, the vertices
	 * are copied into this polygon's vertex list.
	 * 
	 * @param p
	 *            The inner polygon to add
	 * @param inferOuter
	 *            Whether to infer the outer polygon from this inner one
	 */
	public void addInnerPolygon(Polygon p, boolean inferOuter)
	{
		if (!inferOuter)
		{
			this.innerPolygons.add(p);
		}
		else
		{
			if (this.points.size() == 0)
			{
				this.points.addAll(p.points);
				this.isHole = p.isHole;
			}
			else
			{
				this.addInnerPolygon(p, false);
			}
		}
	}

	/**
	 * Add an inner polygon to this polygon. If there is no main polygon defined
	 * (the number of vertices is zero) then the given inner polygon will become
	 * the main polygon.
	 * 
	 * @param p
	 *            The inner polygon to add
	 */
	public void addInnerPolygon(Polygon p)
	{
		this.addInnerPolygon(p, true);
	}

	/**
	 * Returns the list of inner polygons.
	 * 
	 * @return the list of inner polygons
	 */
	public List<Polygon> getInnerPolys()
	{
		return this.innerPolygons;
	}

	/**
	 * Set whether this polygon represents a hole in another polygon.
	 * 
	 * @param isHole
	 *            Whether this polygon is a whole.
	 */
	public void setIsHole(boolean isHole)
	{
		this.isHole = isHole;
	}

	/**
	 * Returns whether this polygon is representing a hole in another polygon.
	 * 
	 * @return Whether this polygon is representing a hole in another polygon.
	 */
	public boolean isHole()
	{
		return this.isHole;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Polygon) &&
				this.equals((Polygon) obj);
	}

	/**
	 * Specific equals method for polygons where the polgyons are tested for the
	 * vertices being in the same order. If the vertices are not in the vertex
	 * list in the same manner but are in the same order (when wrapped around)
	 * the method will return true. So the triangles below will return true:
	 * 
	 * {[1,1],[2,2],[1,2]} and {[1,2],[1,1],[2,2]}
	 * 
	 * @param p
	 *            The polygon to test against
	 * @return TRUE if the polygons are the same.
	 */
	public boolean equals(Polygon p)
	{
		if (isHole() != p.isHole())
			return false;
		if (this.points.size() != p.points.size())
			return false;
		if (this.isEmpty() && p.isEmpty())
			return true;

		final int i = this.points.indexOf(p.points.get(0));
		if (i == -1)
			return false;

		final int s = this.points.size();
		for (int n = 0; n < s; n++)
		{
			if (!p.points.get(n).equals(this.points.get((n + i) % s)))
				return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return points.hashCode() * (isHole() ? -1 : 1);
	}

	/**
	 * Displays the complete list of vertices unless the number of vertices is
	 * greater than 10 - then a sublist is shown of 5 from the start and 5 from
	 * the end separated by ellipses.
	 * 
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer();
		if (isHole())
			sb.append("H");
		final int len = 10;
		if (points.size() < len)
			sb.append(points.toString());
		else
			sb.append(points.subList(0, len / 2).toString() + "..." +
					points.subList(points.size() - len / 2, points.size())
							.toString());

		if (innerPolygons.size() > 0)
		{
			sb.append("\n    - " + innerPolygons.size() + " inner polygons:");
			for (final Polygon ip : innerPolygons)
				sb.append("\n       + " + ip.toString());
		}

		return sb.toString();
	}

	/**
	 * Returns the intersection of this polygon and the given polygon.
	 * 
	 * @param p2
	 *            The polygon to intersect with.
	 * @return The resulting polygon intersection
	 */
	public Polygon intersect(Polygon p2)
	{
		return new PolygonUtils().intersection(this, p2);
	}

	/**
	 * Returns the union of this polygon and the given polygon.
	 * 
	 * @param p2
	 *            The polygon to union with.
	 * @return The resulting polygon union
	 */
	public Polygon union(Polygon p2)
	{
		return new PolygonUtils().union(this, p2);
	}

	/**
	 * Returns the XOR of this polygon and the given polygon.
	 * 
	 * @param p2
	 *            The polygon to XOR with.
	 * @return The resulting polygon
	 */
	public Polygon xor(Polygon p2)
	{
		return new PolygonUtils().xor(this, p2);
	}

	/**
	 * Reduce the number of vertices in a polygon. This implementation is based
	 * on a modified Ramer-Douglas–Peucker algorithm that is designed to work
	 * with polygons rather than polylines. The implementation searches for two
	 * initialisation points that are approximatatly maximally distant, then
	 * performs the polyline algorithm on the two segments, and finally ensures
	 * that the joins in the segments are consistent with the given epsilon
	 * value.
	 * 
	 * @param eps
	 *            distance value below which a vertex can be ignored
	 * @return new polygon that approximates this polygon, but with fewer
	 *         vertices
	 */
	public Polygon reduceVertices(double eps)
	{
		if (eps == 0 || nVertices() <= 3)
			return this.clone();

		int prevStartIndex = 0;
		int startIndex = 0;
		for (int init = 0; init < 3; init++) {
			double dmax = 0;
			prevStartIndex = startIndex;

			final Point2d first = points.get(startIndex);
			for (int i = 0; i < points.size(); i++) {
				final double d;
				d = Line2d.distance(first, points.get(i));

				if (d > dmax) {
					startIndex = i;
					dmax = d;
				}
			}
		}

		if (prevStartIndex > startIndex) {
			final int tmp = prevStartIndex;
			prevStartIndex = startIndex;
			startIndex = tmp;
		}

		final List<Point2d> l1 = new ArrayList<Point2d>();
		l1.addAll(points.subList(prevStartIndex, startIndex + 1));

		final List<Point2d> l2 = new ArrayList<Point2d>();
		l2.addAll(points.subList(startIndex, points.size()));
		l2.addAll(points.subList(0, prevStartIndex + 1));

		final Polygon newPoly = new Polygon();
		final List<Point2d> line1 = ramerDouglasPeucker(l1, eps);
		final List<Point2d> line2 = ramerDouglasPeucker(l2, eps);
		newPoly.points.addAll(line1.subList(0, line1.size() - 1));
		newPoly.points.addAll(line2.subList(0, line2.size() - 1));

		// deal with the joins
		// if (newPoly.nVertices() > 3) {
		// Point2d r1 = null, r2 = null;
		// Point2d p0 = newPoly.points.get(newPoly.points.size() - 1);
		// Point2d p1 = newPoly.points.get(0);
		// Point2d p2 = newPoly.points.get(1);
		//
		// Line2d l = new Line2d(p0, p2);
		// Line2d norm = l.getNormal(p1);
		// Point2d intersect = l.getIntersection(norm).intersectionPoint;
		// if (intersect != null && Line2d.distance(p1, intersect) <= eps) {
		// // remove p1
		// r1 = p1;
		// }
		//
		// p0 = newPoly.points.get(line1.size() - 1);
		// p1 = newPoly.points.get(line1.size());
		// p2 = newPoly.points.get((line1.size() + 1) >= newPoly.size() ? 0 :
		// (line1.size() + 1));
		//
		// l = new Line2d(p0, p2);
		// norm = l.getNormal(p1);
		// intersect = l.getIntersection(norm).intersectionPoint;
		// if (intersect != null && Line2d.distance(p1, intersect) <= eps) {
		// // remove p2
		// r2 = p2;
		// }
		//
		// if (r1 != null) {
		// newPoly.points.remove(r1);
		// }
		// if (newPoly.nVertices() > 3 && r2 != null) {
		// newPoly.points.remove(r2);
		// }
		// }

		if (!newPoly.isClockwise()) {
			Collections.reverse(newPoly.points);
		}

		for (final Polygon ppp : innerPolygons)
			newPoly.addInnerPolygon(ppp.reduceVertices(eps));

		return newPoly;
	}

	/**
	 * Ramer Douglas Peucker polyline algorithm
	 * 
	 * @param p
	 *            the polyline to simplify
	 * @param eps
	 *            distance value below which a vertex can be ignored
	 * @return the simplified polyline
	 */
	private static List<Point2d> ramerDouglasPeucker(List<Point2d> p, double eps) {
		// Find the point with the maximum distance
		double dmax = 0;
		int index = 0;
		final int end = p.size() - 1;
		final Line2d l = new Line2d(p.get(0), p.get(end));
		for (int i = 1; i < end - 1; i++) {
			final double d;

			final Point2d p1 = p.get(i);
			final Line2d norm = l.getNormal(p1);
			final Point2d p2 = l.getIntersection(norm).intersectionPoint;
			if (p2 == null)
				continue;
			d = Line2d.distance(p1, p2);

			if (d > dmax) {
				index = i;
				dmax = d;
			}
		}

		final List<Point2d> newPoly = new ArrayList<Point2d>();

		// If max distance is greater than epsilon, recursively simplify
		if (dmax > eps) {
			// Recursively call RDP
			final List<Point2d> line1 = ramerDouglasPeucker(p.subList(0, index + 1), eps);
			final List<Point2d> line2 = ramerDouglasPeucker(p.subList(index, end + 1), eps);
			newPoly.addAll(line1.subList(0, line1.size() - 1));
			newPoly.addAll(line2);
		} else {
			newPoly.add(p.get(0));
			newPoly.add(p.get(end));
		}

		// Return the result
		return newPoly;
	}

	/**
	 * Apply a 3x3 transform matrix to a copy of the polygon and return it
	 * 
	 * @param transform
	 *            3x3 transform matrix
	 * @return the transformed polygon
	 */
	@Override
	public Polygon transform(Matrix transform) {
		final List<Point2d> newVertices = new ArrayList<Point2d>();

		for (final Point2d p : points) {
			final Matrix p1 = new Matrix(3, 1);
			p1.set(0, 0, p.getX());
			p1.set(1, 0, p.getY());
			p1.set(2, 0, 1);

			final Matrix p2_est = transform.times(p1);

			final Point2d out = new Point2dImpl((float) (p2_est.get(0, 0) / p2_est.get(2, 0)),
					(float) (p2_est.get(1, 0) / p2_est.get(2, 0)));

			newVertices.add(out);
		}

		final Polygon p = new Polygon(newVertices);
		for (final Polygon pp : innerPolygons)
			p.addInnerPolygon(pp.transform(transform));
		return p;
	}

	/**
	 * Compute the regular (oriented to the axes) bounding box of the polygon.
	 * 
	 * @return the regular bounding box as [x,y,width,height]
	 */
	@Override
	public Rectangle calculateRegularBoundingBox() {
		float xmin = Float.MAX_VALUE, xmax = -Float.MAX_VALUE, ymin = Float.MAX_VALUE, ymax = -Float.MAX_VALUE;

		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				final float px = p.getX();
				final float py = p.getY();
				if (px < xmin)
					xmin = px;
				if (px > xmax)
					xmax = px;
				if (py < ymin)
					ymin = py;
				if (py > ymax)
					ymax = py;

			}
		}

		return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
	}

	/**
	 * Translate the polygons position
	 * 
	 * @param x
	 *            x-translation
	 * @param y
	 *            y-translation
	 */
	@Override
	public void translate(float x, float y) {
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setX(p.getX() + x);
				p.setY(p.getY() + y);
			}
		}
	}

	/**
	 * Scale the polygon by the given amount about (0,0). Scalefactors between 0
	 * and 1 shrink the polygon.
	 * 
	 * @param sc
	 *            the scale factor.
	 */
	@Override
	public void scale(float sc) {
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setX(p.getX() * sc);
				p.setY(p.getY() * sc);
			}
		}
	}

	/**
	 * Scale the polygon only in the x-direction by the given amount about
	 * (0,0). Scale factors between 0 and 1 will shrink the polygon
	 * 
	 * @param sc
	 *            The scale factor
	 * @return this polygon
	 */
	@Override
	public Polygon scaleX(float sc)
	{
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setX(p.getX() * sc);
			}
		}
		return this;
	}

	/**
	 * Scale the polygon only in the y-direction by the given amount about
	 * (0,0). Scale factors between 0 and 1 will shrink the polygon
	 * 
	 * @param sc
	 *            The scale factor
	 * @return this polygon
	 */
	@Override
	public Polygon scaleY(float sc)
	{
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setY(p.getY() * sc);
			}
		}
		return this;
	}

	/**
	 * Scale the polygon by the given amount about (0,0). Scale factors between
	 * 0 and 1 shrink the polygon.
	 * 
	 * @param scx
	 *            the scale factor in the x direction
	 * @param scy
	 *            the scale factor in the y direction.
	 * @return this polygon
	 */
	@Override
	public Polygon scaleXY(float scx, float scy)
	{
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setX(p.getX() * scx);
				p.setY(p.getY() * scy);
			}
		}
		return this;
	}

	/**
	 * Scale the polygon by the given amount about the given point. Scalefactors
	 * between 0 and 1 shrink the polygon.
	 * 
	 * @param centre
	 *            the centre of the scaling operation
	 * @param sc
	 *            the scale factor
	 */
	@Override
	public void scale(Point2d centre, float sc) {
		this.translate(-centre.getX(), -centre.getY());
		for (int pp = 0; pp < getNumInnerPoly(); pp++)
		{
			final Polygon ppp = getInnerPoly(pp);
			for (final Point2d p : ppp.getVertices()) {
				p.setX(p.getX() * sc);
				p.setY(p.getY() * sc);
			}
		}
		this.translate(centre.getX(), centre.getY());
	}

	/**
	 * Calculate the centroid of the polygon.
	 * 
	 * @return calls {@link #calculateFirstMoment()};
	 */
	@Override
	public Point2d calculateCentroid() {
		final double[] pt = calculateFirstMoment();
		return new Point2dImpl((float) pt[0], (float) pt[1]);
	}

	/**
	 * Treating the polygon as a continuous piecewise function, calculate
	 * exactly the first moment. This follows working presented by Carsten
	 * Steger in "On the Calculation of Moments of Polygons" ,
	 * 
	 * @return the first moment
	 */
	@Reference(
			author = { "Carsten Steger" },
			title = "On the Calculation of Moments of Polygons",
			type = ReferenceType.Techreport,
			month = "August",
			year = "1996",
			url = "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.29.8765&rep=rep1&type=pdf"
			)
			public double[] calculateFirstMoment() {
		final boolean closed = isClosed();

		if (!closed)
			close();

		double area = 0;
		double ax = 0;
		double ay = 0;
		// TODO: This does not take into account the winding
		// rule and therefore holes
		for (int k = 0; k < points.size() - 1; k++) {
			final float xk = points.get(k).getX();
			final float yk = points.get(k).getY();
			final float xk1 = points.get(k + 1).getX();
			final float yk1 = points.get(k + 1).getY();

			final float shared = xk * yk1 - xk1 * yk;
			area += shared;
			ax += (xk + xk1) * shared;
			ay += (yk + yk1) * shared;
		}

		if (!closed)
			open();

		area *= 0.5;

		return new double[] { ax / (6 * area), ay / (6 * area) };
	}

	/**
	 * Treating the polygon as a continuous piecewise function, calculate
	 * exactly the second moment. This follows working presented by Carsten
	 * Steger in "On the Calculation of Moments of Polygons" ,
	 * 
	 * @return the second moment as an array with values: (axx,axy,ayy)
	 */
	@Reference(
			author = { "Carsten Steger" },
			title = "On the Calculation of Moments of Polygons",
			type = ReferenceType.Techreport,
			month = "August",
			year = "1996",
			url = "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.29.8765&rep=rep1&type=pdf"
			)
			public double[] calculateSecondMoment() {
		final boolean closed = isClosed();
		final double area = calculateSignedArea();

		if (!closed)
			close();

		double axx = 0;
		double ayy = 0;
		double axy = 0;
		// TODO: This does not take into account the winding
		// rule and therefore holes
		for (int k = 0; k < points.size() - 1; k++) {
			final float xk = points.get(k).getX();
			final float yk = points.get(k).getY();
			final float xk1 = points.get(k + 1).getX();
			final float yk1 = points.get(k + 1).getY();

			final float shared = xk * yk1 - xk1 * yk;
			axx += (xk * xk + xk * xk1 + xk1 * xk1) * shared;
			ayy += (yk * yk + yk * yk1 + yk1 * yk1) * shared;
			axy += (2 * xk * yk + xk * yk1 + xk1 * yk + 2 * xk1 * yk1) * shared;
		}

		if (!closed)
			open();

		return new double[] {
				axx / (12 * area),
				axy / (24 * area),
				ayy / (12 * area)
		};
	}

	/**
	 * Treating the polygon as a continuous piecewise function, calculate
	 * exactly the centralised second moment. This follows working presented by
	 * Carsten Steger in "On the Calculation of Moments of Polygons" ,
	 * 
	 * @return the second moment as an array with values: (axx,axy,ayy)
	 */
	@Reference(
			author = { "Carsten Steger" },
			title = "On the Calculation of Moments of Polygons",
			type = ReferenceType.Techreport,
			month = "August",
			year = "1996",
			url = "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.29.8765&rep=rep1&type=pdf"
			)
			public double[] calculateSecondMomentCentralised() {
		final double[] firstMoment = this.calculateFirstMoment();
		final double[] secondMoment = this.calculateSecondMoment();

		return new double[] {
				secondMoment[0] - firstMoment[0] * firstMoment[0],
				secondMoment[1] - firstMoment[0] * firstMoment[1],
				secondMoment[2] - firstMoment[1] * firstMoment[1],
		};

	}

	/**
	 * Calculates the principle direction of the connected component. This is
	 * given by
	 * <code>0.5 * atan( (M<sub>20</sub>-M<sub>02</sub>) / 2 * M<sub>11</sub> )</code>
	 * so results in an angle between -PI and +PI.
	 * 
	 * @return The principle direction (-PI/2 to +PI/2 radians) of the connected
	 *         component.
	 */
	public double calculateDirection() {
		final double[] secondMoment = calculateSecondMomentCentralised();
		final double u20 = secondMoment[0];
		final double u02 = secondMoment[1];
		final double u11 = secondMoment[2];
		final double theta = 0.5 * Math.atan2((2 * u11), (u20 - u02));

		return theta;
	}

	/**
	 * Using
	 * {@link EllipseUtilities#ellipseFromCovariance(float, float, Matrix, float)}
	 * and the {@link #calculateSecondMomentCentralised()} return the Ellipse
	 * best fitting the shape of this polygon.
	 * 
	 * @return {@link Ellipse} defining the shape of the polygon
	 */
	public Ellipse toEllipse() {
		final double[] secondMoment = calculateSecondMomentCentralised();
		final double u20 = secondMoment[0];
		final double u11 = secondMoment[1];
		final double u02 = secondMoment[2];
		final Point2d center = calculateCentroid();
		final Matrix sm = new Matrix(new double[][] {
				new double[] { u20, u11 },
				new double[] { u11, u02 },
		});
		// Used the sqrt(3) as the scale, not sure why. This is not correct.
		// Find the correct value!
		return EllipseUtilities.ellipseFromCovariance(
				center.getX(),
				center.getY(),
				sm,
				(float) Math.sqrt(3)
				);
	}

	/**
	 * Test if the outer polygon is convex.
	 * 
	 * @return true if the outer polygon is convex; false if non-convex or less
	 *         than two vertices
	 */
	@Override
	public boolean isConvex() {
		final boolean isOriginallyClosed = this.isClosed();
		if (isOriginallyClosed)
			this.open();

		final int size = size();

		if (size < 3)
			return false;

		float res = 0;
		for (int i = 0; i < size; i++) {
			final Point2d p = points.get(i);
			final Point2d tmp = points.get((i + 1) % size);
			final Point2dImpl v = new Point2dImpl();
			v.x = tmp.getX() - p.getX();
			v.y = tmp.getY() - p.getY();
			final Point2d u = points.get((i + 2) % size);

			if (i == 0) // in first loop direction is unknown, so save it in res
				res = u.getX() * v.y - u.getY() * v.x + v.x * p.getY() - v.y * p.getX();
			else
			{
				final float newres = u.getX() * v.y - u.getY() * v.x + v.x * p.getY() - v.y * p.getX();
				if ((newres > 0 && res < 0) || (newres < 0 && res > 0))
					return false;
			}
		}

		if (isOriginallyClosed)
			close();

		return true;
	}

	/**
	 * Test if this has no inner polygons or sub-parts
	 * 
	 * @see Polygon#getInnerPolys()
	 * 
	 * @return true if this is polygon has no inner polygons; false otherwise.
	 */
	public boolean hasNoInnerPolygons() {
		return innerPolygons == null || innerPolygons.size() == 0;
	}

	@Override
	public double calculatePerimeter() {
		final Point2d p1 = points.get(0);
		float p1x = p1.getX();
		float p1y = p1.getY();

		Point2d p2 = points.get(points.size() - 1);
		float p2x = p2.getX();
		float p2y = p2.getY();

		double peri = Line2d.distance(p1x, p1y, p2x, p2y);
		for (int i = 1; i < this.points.size(); i++) {
			p2 = points.get(i);
			p2x = p2.getX();
			p2y = p2.getY();
			peri += Line2d.distance(p1x, p1y, p2x, p2y);
			p1x = p2x;
			p1y = p2y;
		}

		return peri;
	}

	/**
	 * Test (outer) polygon path direction
	 * 
	 * @return true is the outer path direction is clockwise w.r.t OpenIMAJ
	 *         coordinate system
	 */
	public boolean isClockwise() {
		double signedArea = 0;
		for (int i = 0; i < this.points.size() - 1; i++) {
			final float x1 = points.get(i).getX();
			final float y1 = points.get(i).getY();
			final float x2 = points.get(i + 1).getX();
			final float y2 = points.get(i + 1).getY();

			signedArea += (x1 * y2 - x2 * y1);
		}

		final float x1 = points.get(points.size() - 1).getX();
		final float y1 = points.get(points.size() - 1).getY();
		final float x2 = points.get(0).getX();
		final float y2 = points.get(0).getY();
		signedArea += (x1 * y2 - x2 * y1);

		return signedArea >= 0;
	}

	/**
	 * Calculate convex hull using Melkman's algorithm. This is faster than the
	 * {@link #calculateConvexHull()} method, but will only work for simple
	 * polygons (those that don't self-intersect)
	 * <p>
	 * Based on http://softsurfer.com/Archive/algorithm_0203/algorithm_0203.htm,
	 * but modified (fixed) to deal with the problem of the initialisation
	 * points potentially being co-linear.
	 * <p>
	 * Copyright 2001, softSurfer (www.softsurfer.com) This code may be freely
	 * used and modified for any purpose providing that this copyright notice is
	 * included with it. SoftSurfer makes no warranty for this code, and cannot
	 * be held liable for any real or imagined damage resulting from its use.
	 * Users of this code must verify correctness for their application.
	 * 
	 * @return A polygon defining the shape of the convex hull
	 */
	public Polygon calculateConvexHullMelkman() {
		if (this.points.size() <= 3)
			return new Polygon(this.points);

		final int n = this.points.size();
		int i = 1;
		while (i + 1 < n && isLeft(points.get(0), points.get(i), points.get(i + 1)) == 0)
			i++;

		if (n - i <= 3)
			return new Polygon(this.points);

		// initialize a deque D[] from bottom to top so that the
		// 1st three vertices of V[] are a counterclockwise triangle
		final Point2d[] D = new Point2d[2 * n + 1];
		int bot = n - 2, top = bot + 3; // initial bottom and top deque indices
		D[bot] = D[top] = points.get(i + 1); // 3rd vertex is at both bot and
		// top
		if (isLeft(points.get(0), points.get(i), points.get(i + 1)) > 0) {
			D[bot + 1] = points.get(0);
			D[bot + 2] = points.get(i); // ccw vertices are: 2,0,1,2
		} else {
			D[bot + 1] = points.get(i);
			D[bot + 2] = points.get(0); // ccw vertices are: 2,1,0,2
		}

		i += 2;

		// compute the hull on the deque D[]
		for (; i < n; i++) { // process the rest of vertices
			// test if next vertex is inside the deque hull
			if ((isLeft(D[bot], D[bot + 1], points.get(i)) > 0) &&
					(isLeft(D[top - 1], D[top], points.get(i)) > 0))
				continue; // skip an interior vertex

			// incrementally add an exterior vertex to the deque hull
			// get the rightmost tangent at the deque bot
			while (isLeft(D[bot], D[bot + 1], points.get(i)) <= 0)
				++bot; // remove bot of deque
			D[--bot] = points.get(i); // insert V[i] at bot of deque

			// get the leftmost tangent at the deque top
			while (isLeft(D[top - 1], D[top], points.get(i)) <= 0)
				--top; // pop top of deque
			D[++top] = points.get(i); // push V[i] onto top of deque
		}

		// transcribe deque D[] to the output hull array H[]
		final Polygon H = new Polygon();
		final List<Point2d> vertices = H.getVertices();
		for (int h = 0; h <= (top - bot); h++)
			vertices.add(D[bot + h]);

		return H;
	}

	private float isLeft(Point2d P0, Point2d P1, Point2d P2) {
		return (P1.getX() - P0.getX()) * (P2.getY() - P0.getY()) - (P2.getX() -
				P0.getX()) * (P1.getY() - P0.getY());
	}

	/**
	 * Compute the minimum size rotated bounding rectangle that contains this
	 * shape using the rotating calipers approach.
	 * 
	 * @see org.openimaj.math.geometry.shape.Shape#minimumBoundingRectangle()
	 * @see RotatingCalipers#getMinimumBoundingRectangle(Polygon, boolean)
	 */
	@Override
	public RotatedRectangle minimumBoundingRectangle() {
		return RotatingCalipers.getMinimumBoundingRectangle(this, false);
	}

	/**
	 * Compute the minimum size rotated bounding rectangle that contains this
	 * shape using the rotating calipers approach.
	 * 
	 * @see RotatingCalipers#getMinimumBoundingRectangle(Polygon, boolean)
	 * 
	 * @param assumeSimple
	 *            can the algorithm assume the polygon is simple and use an
	 *            optimised (Melkman's) convex hull?
	 * 
	 * @return the minimum bounding box
	 */
	public RotatedRectangle minimumBoundingRectangle(boolean assumeSimple) {
		return RotatingCalipers.getMinimumBoundingRectangle(this, assumeSimple);
	}

	/**
	 * Find the closest point on the polygon to the given point
	 * 
	 * @param pt
	 *            the point
	 * @return the closest point
	 */
	public Point2d closestPoint(Point2d pt) {
		final boolean closed = isClosed();

		if (!closed)
			close();

		final float x = pt.getX();
		final float y = pt.getY();
		float minDist = Float.MAX_VALUE;
		final Point2dImpl min = new Point2dImpl();
		final Point2dImpl tpt = new Point2dImpl();

		for (int k = 0; k < points.size() - 1; k++) {
			final float vx = points.get(k).getX();
			final float vy = points.get(k).getY();
			final float wx = points.get(k + 1).getX();
			final float wy = points.get(k + 1).getY();

			// Return minimum distance between line segment vw and point p
			final float l2 = (wx - vx) * (wx - vx) + (wy - vy) * (wy - vy);

			if (l2 == 0.0) {
				tpt.x = vx;
				tpt.y = vy;
			} else {
				final float t = ((x - vx) * (wx - vx) + (y - vy) * (wy - vy)) / l2;

				if (t < 0.0) {
					tpt.x = vx;
					tpt.y = vy;
				} else if (t > 1.0) {
					tpt.x = wx;
					tpt.y = wy;
				} else {
					tpt.x = vx + t * (wx - vx);
					tpt.y = vy + t * (wy - vy);
				}
			}

			final float dist = (float) Line2d.distance(x, y, tpt.x, tpt.y);
			if (dist < minDist) {
				minDist = dist;
				min.x = tpt.x;
				min.y = tpt.y;
			}
		}

		if (!closed)
			open();

		return min;
	}
}
