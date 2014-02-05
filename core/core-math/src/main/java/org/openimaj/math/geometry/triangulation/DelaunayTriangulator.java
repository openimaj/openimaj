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
package org.openimaj.math.geometry.triangulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Triangle;

/**
 * The Delaunay Triangulation algorithm. Produces a triangulation of a set of
 * points.
 * <p>
 * Originally ported from <a
 * href="http://paulbourke.net/papers/triangulate/"Paul Bourke's
 * triangulate.c</a>.
 * <p>
 * This OpenIMAJ version is based off <a
 * href="http://www.florianjenett.de/">Florian Jenett's</a> Java port: fjenett,
 * 20th february 2005, offenbach-germany. contact: http://www.florianjenett.de/
 */
public class DelaunayTriangulator {
	/*
	 * From P Bourke's C prototype -
	 * 
	 * qsort(p,nv,sizeof(XYZ),XYZCompare);
	 * 
	 * int XYZCompare(void *v1,void *v2) { XYZ *p1,*p2; p1 = v1; p2 = v2; if
	 * (p1->x < p2->x) return(-1); else if (p1->x > p2->x) return(1); else
	 * return(0); }
	 */
	private static class XComparator implements Comparator<Point2d> {

		@Override
		public int compare(Point2d p1, Point2d p2) {
			if (p1.getX() < p2.getX()) {
				return -1;
			}
			else if (p1.getX() > p2.getX()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

	static final float EPSILON = 0.00000001f;

	/*
	 * Return TRUE if a point (xp,yp) is inside the circumcircle made up of the
	 * points (x1,y1), (x2,y2), (x3,y3) The circumcircle centre is returned in
	 * (xc,yc) and the radius r NOTE: A point on the edge is inside the
	 * circumcircle
	 */
	private static boolean circumCircle(Point2d p, Triangle t, Circle circle) {
		float m1, m2, mx1, mx2, my1, my2;
		float dx, dy, rsqr, drsqr;

		/* Check for coincident points */
		if (Math.abs(t.firstVertex().getY() - t.secondVertex().getY()) < EPSILON
				&& Math.abs(t.secondVertex().getY() - t.thirdVertex().getY()) < EPSILON)
		{
			System.err.println("CircumCircle: Points are coincident.");
			return false;
		}

		if (Math.abs(t.secondVertex().getY() - t.firstVertex().getY()) < EPSILON) {
			m2 = -(t.thirdVertex().getX() - t.secondVertex().getX()) / (t.thirdVertex().getY() - t.secondVertex().getY());
			mx2 = (t.secondVertex().getX() + t.thirdVertex().getX()) / 2.0f;
			my2 = (t.secondVertex().getY() + t.thirdVertex().getY()) / 2.0f;
			circle.setX((t.secondVertex().getX() + t.firstVertex().getX()) / 2.0f);
			circle.setY(m2 * (circle.getX() - mx2) + my2);
		}
		else if (Math.abs(t.thirdVertex().getY() - t.secondVertex().getY()) < EPSILON) {
			m1 = -(t.secondVertex().getX() - t.firstVertex().getX()) / (t.secondVertex().getY() - t.firstVertex().getY());
			mx1 = (t.firstVertex().getX() + t.secondVertex().getX()) / 2.0f;
			my1 = (t.firstVertex().getY() + t.secondVertex().getY()) / 2.0f;
			circle.setX((t.thirdVertex().getX() + t.secondVertex().getX()) / 2.0f);
			circle.setY(m1 * (circle.getX() - mx1) + my1);
		}
		else {
			m1 = -(t.secondVertex().getX() - t.firstVertex().getX()) / (t.secondVertex().getY() - t.firstVertex().getY());
			m2 = -(t.thirdVertex().getX() - t.secondVertex().getX()) / (t.thirdVertex().getY() - t.secondVertex().getY());
			mx1 = (t.firstVertex().getX() + t.secondVertex().getX()) / 2.0f;
			mx2 = (t.secondVertex().getX() + t.thirdVertex().getX()) / 2.0f;
			my1 = (t.firstVertex().getY() + t.secondVertex().getY()) / 2.0f;
			my2 = (t.secondVertex().getY() + t.thirdVertex().getY()) / 2.0f;
			circle.setX((m1 * mx1 - m2 * mx2 + my2 - my1) / (m1 - m2));
			circle.setY(m1 * (circle.getX() - mx1) + my1);
		}

		dx = t.secondVertex().getX() - circle.getX();
		dy = t.secondVertex().getY() - circle.getY();
		rsqr = dx * dx + dy * dy;
		circle.setRadius((float) Math.sqrt(rsqr));

		dx = p.getX() - circle.getX();
		dy = p.getY() - circle.getY();
		drsqr = dx * dx + dy * dy;

		return drsqr <= rsqr;
	}

	/**
	 * Trianglate a set of vertices.
	 * 
	 * @param pxyz
	 *            vertices to triangulate.
	 * @return list of triangles arranged in a consistent clockwise order.
	 */
	public static List<Triangle> triangulate(List<? extends Point2d> pxyz) {

		// sort vertex array in increasing x values
		Collections.sort(pxyz, new XComparator());

		/*
		 * Find the maximum and minimum vertex bounds. This is to allow
		 * calculation of the bounding triangle
		 */
		float xmin = ((Point2d) pxyz.get(0)).getX();
		float ymin = ((Point2d) pxyz.get(0)).getY();
		float xmax = xmin;
		float ymax = ymin;

		for (final Point2d p : pxyz) {
			if (p.getX() < xmin)
				xmin = p.getX();
			if (p.getX() > xmax)
				xmax = p.getX();
			if (p.getY() < ymin)
				ymin = p.getY();
			if (p.getY() > ymax)
				ymax = p.getY();
		}

		final float dx = xmax - xmin;
		final float dy = ymax - ymin;
		final float dmax = (dx > dy) ? dx : dy;
		final float xmid = (xmax + xmin) / 2.0f;
		final float ymid = (ymax + ymin) / 2.0f;

		final ArrayList<Triangle> triangles = new ArrayList<Triangle>(); // for
																			// the
																			// Triangles
		final HashSet<Triangle> complete = new HashSet<Triangle>(); // for
																	// complete
																	// Triangles

		/*
		 * Set up the supertriangle This is a triangle which encompasses all the
		 * sample points. The supertriangle coordinates are added to the end of
		 * the vertex list. The supertriangle is the first triangle in the
		 * triangle list.
		 */
		final Triangle superTriangle = new Triangle(
				new Point2dImpl(xmid - 2.0f * dmax, ymid - dmax),
				new Point2dImpl(xmid, ymid + 2.0f * dmax),
				new Point2dImpl(xmid + 2.0f * dmax, ymid - dmax)
				);
		triangles.add(superTriangle);

		/*
		 * Include each point one at a time into the existing mesh
		 */
		final ArrayList<Line2d> edges = new ArrayList<Line2d>();
		for (final Point2d p : pxyz) {
			edges.clear();

			/*
			 * Set up the edge buffer. If the point (xp,yp) lies inside the
			 * circumcircle then the three edges of that triangle are added to
			 * the edge buffer and that triangle is removed.
			 */
			final Circle circle = new Circle(0, 0, 0);

			for (int j = triangles.size() - 1; j >= 0; j--) {

				final Triangle t = triangles.get(j);
				if (complete.contains(t)) {
					continue;
				}

				final boolean inside = circumCircle(p, t, circle);

				if (circle.getX() + circle.getRadius() < p.getX()) {
					complete.add(t);
				}
				if (inside) {
					edges.add(new Line2d(t.firstVertex(), t.secondVertex()));
					edges.add(new Line2d(t.secondVertex(), t.thirdVertex()));
					edges.add(new Line2d(t.thirdVertex(), t.firstVertex()));
					triangles.remove(j);
				}

			}

			/*
			 * Tag multiple edges Note: if all triangles are specified
			 * anticlockwise then all interior edges are opposite pointing in
			 * direction.
			 */
			for (int j = 0; j < edges.size() - 1; j++) {
				final Line2d e1 = edges.get(j);
				for (int k = j + 1; k < edges.size(); k++) {
					final Line2d e2 = edges.get(k);
					if (e1.begin == e2.end && e1.end == e2.begin) {
						e1.begin = null;
						e1.end = null;
						e2.begin = null;
						e2.end = null;
					}
					/* Shouldn't need the following, see note above */
					if (e1.begin == e2.begin && e1.end == e2.end) {
						e1.begin = null;
						e1.end = null;
						e2.begin = null;
						e2.end = null;
					}
				}
			}

			/*
			 * Form new triangles for the current point Skipping over any tagged
			 * edges. All edges are arranged in clockwise order.
			 */
			for (int j = 0; j < edges.size(); j++) {
				final Line2d e = edges.get(j);
				if (e.begin == null || e.end == null) {
					continue;
				}
				triangles.add(new Triangle(e.begin, e.end, p));
			}

		}

		/*
		 * Remove triangles with supertriangle vertices
		 */
		for (int i = triangles.size() - 1; i >= 0; i--) {
			final Triangle t = triangles.get(i);
			if (t.sharesVertex(superTriangle)) {
				triangles.remove(i);
			}
		}

		return triangles;
	}
}
