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
import java.util.List;
import java.util.PriorityQueue;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Static methods for the computation of a Voronoi diagram (aka Dirichlet
 * tessellation) from a set of points. Internally, these use Fortune's algorithm
 * to do the work.
 * 
 * @see "http://en.wikipedia.org/wiki/Fortune%27s_algorithm"
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class Voronoi {
	private Voronoi() {
	}

	/**
	 * Compute the Voronoi diagram as a graph of its vertices
	 * 
	 * @param points
	 *            the vertices
	 * @return the graph
	 */
	public static Graph<Point2d, DefaultEdge> computeVoronoiGraph(List<? extends Point2d> points) {
		final double[] wh = computeWidthHeight(points);
		return computeVoronoiGraph(points, wh[0], wh[1]);
	}

	/**
	 * Compute the Voronoi diagram as a graph of its vertices
	 * 
	 * @param points
	 *            the vertices
	 * @param width
	 *            the width of the diagram
	 * @param height
	 *            the height of the diagram
	 * @return the graph
	 */
	public static Graph<Point2d, DefaultEdge> computeVoronoiGraph(List<? extends Point2d> points, double width,
			double height)
	{
		final FortunesAlgorithm f = new FortunesAlgorithm(width, height);
		final List<Line2d> edges = f.runFortune(points);

		final Graph<Point2d, DefaultEdge> graph = new SimpleGraph<Point2d, DefaultEdge>(DefaultEdge.class);
		for (final Line2d l : edges) {
			graph.addEdge(l.begin, l.end);
		}

		return graph;
	}

	/**
	 * Compute the Voronoi diagram as a list of its edges
	 * 
	 * @param points
	 *            the vertices
	 * @return the graph
	 */
	public static List<Line2d> computeVoronoiEdges(List<? extends Point2d> points) {
		final double[] wh = computeWidthHeight(points);

		return computeVoronoiEdges(points, wh[0], wh[1]);
	}

	/**
	 * Compute the Voronoi diagram as a list of its edges
	 * 
	 * @param points
	 *            the vertices
	 * @param width
	 *            the width of the diagram
	 * @param height
	 *            the height of the diagram
	 * @return the graph
	 */
	public static List<Line2d> computeVoronoiEdges(List<? extends Point2d> points, double width,
			double height)
	{
		final FortunesAlgorithm f = new FortunesAlgorithm(width, height);
		return f.runFortune(points);
	}

	private static double[] computeWidthHeight(List<? extends Point2d> points) {
		float maxx = -Float.MAX_VALUE;
		float minx = Float.MAX_VALUE;
		float maxy = -Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;

		for (final Point2d pt : points) {
			final float x = pt.getX();
			final float y = pt.getY();

			if (maxx < x)
				maxx = x;
			if (minx > x)
				minx = x;
			if (maxy < y)
				maxy = y;
			if (miny > y)
				miny = y;
		}

		final float w = maxx - minx;
		final float h = maxy - miny;
		return new double[] { maxx + 0.1 * w, maxy + 0.1 * h };
	}

	/**
	 * Implementation of fortune's algorithm.
	 * 
	 * @see "http://stackoverflow.com/questions/2346148/fastest-way-to-get-the-set-of-convex-polygons-formed-by-voronoi-line-segments"
	 */
	private static class FortunesAlgorithm {
		// A list of line segments that defines where the cells are divided
		private List<Line2d> output = new ArrayList<Line2d>();
		// The sites that have not yet been processed, in acending order of X
		// coordinate
		private PriorityQueue<Point2d> sites = new PriorityQueue<Point2d>();
		// Possible upcoming cirlce events in acending order of X coordinate
		private PriorityQueue<CircleEvent> events = new PriorityQueue<CircleEvent>();
		// The root of the binary search tree of the parabolic wave front
		private Arc root;
		private double height;
		private double width;

		FortunesAlgorithm(double width, double height) {
			this.width = width;
			this.height = height;
		}

		List<Line2d> runFortune(List<? extends Point2d> points) {
			for (final Point2d pt : points)
				sites.add(new ComparablePoint(pt));

			// Process the queues; select the top element with smaller x
			// coordinate.
			while (sites.size() > 0) {
				if ((events.size() > 0) && ((events.peek().xpos) <= (((ComparablePoint) sites.peek()).x))) {
					processCircleEvent(events.poll());
				} else {
					// process a site event by adding a curve to the parabolic
					// front
					frontInsert((ComparablePoint) sites.poll());
				}
			}

			// After all points are processed, do the remaining circle events.
			while (events.size() > 0) {
				processCircleEvent(events.poll());
			}

			// Clean up dangling edges.
			finishEdges();

			return output;
		}

		private void processCircleEvent(CircleEvent event) {
			if (event.valid) {
				// start a new edge
				final Edge edgy = new Edge(event.p);

				// Remove the associated arc from the front.
				final Arc parc = event.a;
				if (parc.prev != null) {
					parc.prev.next = parc.next;
					parc.prev.edge1 = edgy;
				}
				if (parc.next != null) {
					parc.next.prev = parc.prev;
					parc.next.edge0 = edgy;
				}

				// Finish the edges before and after this arc.
				if (parc.edge0 != null) {
					parc.edge0.finish(event.p);
				}
				if (parc.edge1 != null) {
					parc.edge1.finish(event.p);
				}

				// Recheck circle events on either side of p:
				if (parc.prev != null) {
					checkCircleEvent(parc.prev, event.xpos);
				}
				if (parc.next != null) {
					checkCircleEvent(parc.next, event.xpos);
				}

			}
		}

		void frontInsert(ComparablePoint focus) {
			if (root == null) {
				root = new Arc(focus);
				return;
			}

			Arc parc = root;
			while (parc != null) {
				final CircleResultPack rez = intersect(focus, parc);
				if (rez.valid) {
					// New parabola intersects parc. If necessary, duplicate
					// parc.

					if (parc.next != null) {
						final CircleResultPack rezz = intersect(focus, parc.next);
						if (!rezz.valid) {
							final Arc bla = new Arc(parc.focus);
							bla.prev = parc;
							bla.next = parc.next;
							parc.next.prev = bla;
							parc.next = bla;
						}
					} else {
						parc.next = new Arc(parc.focus);
						parc.next.prev = parc;
					}
					parc.next.edge1 = parc.edge1;

					// Add new arc between parc and parc.next.
					final Arc bla = new Arc(focus);
					bla.prev = parc;
					bla.next = parc.next;
					parc.next.prev = bla;
					parc.next = bla;

					parc = parc.next; // Now parc points to the new arc.

					// Add new half-edges connected to parc's endpoints.
					parc.edge0 = new Edge(rez.center);
					parc.prev.edge1 = parc.edge0;
					parc.edge1 = new Edge(rez.center);
					parc.next.edge0 = parc.edge1;

					// Check for new circle events around the new arc:
					checkCircleEvent(parc, focus.x);
					checkCircleEvent(parc.prev, focus.x);
					checkCircleEvent(parc.next, focus.x);

					return;
				}

				// proceed to next arc
				parc = parc.next;
			}

			// Special case: If p never intersects an arc, append it to the
			// list.
			parc = root;
			while (parc.next != null) {
				parc = parc.next; // Find the last node.
			}
			parc.next = new Arc(focus);
			parc.next.prev = parc;
			final ComparablePoint start = new ComparablePoint(0, (parc.next.focus.y + parc.focus.y) / 2);
			parc.next.edge0 = new Edge(start);
			parc.edge1 = parc.next.edge0;

		}

		void checkCircleEvent(Arc parc, double xpos) {
			// Invalidate any old event.
			if ((parc.event != null) && (parc.event.xpos != xpos)) {
				parc.event.valid = false;
			}
			parc.event = null;

			if ((parc.prev == null) || (parc.next == null)) {
				return;
			}

			final CircleResultPack result = circle(parc.prev.focus, parc.focus, parc.next.focus);
			if (result.valid && result.rightmostX > xpos) {
				// Create new event.
				parc.event = new CircleEvent(result.rightmostX, result.center, parc);
				events.offer(parc.event);
			}

		}

		// Find the rightmost point on the circle through a,b,c.
		CircleResultPack circle(ComparablePoint a, ComparablePoint b, ComparablePoint c) {
			final CircleResultPack result = new CircleResultPack();

			// Check that bc is a "right turn" from ab.
			if ((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y) > 0) {
				result.valid = false;
				return result;
			}

			// Algorithm from O'Rourke 2ed p. 189.
			final double A = b.x - a.x;
			final double B = b.y - a.y;
			final double C = c.x - a.x;
			final double D = c.y - a.y;
			final double E = A * (a.x + b.x) + B * (a.y + b.y);
			final double F = C * (a.x + c.x) + D * (a.y + c.y);
			final double G = 2 * (A * (c.y - b.y) - B * (c.x - b.x));

			if (G == 0) { // Points are co-linear.
				result.valid = false;
				return result;
			}

			// centerpoint of the circle.
			final ComparablePoint o = new ComparablePoint((D * E - B * F) / G, (A * F - C * E) / G);
			result.center = o;

			// o.x plus radius equals max x coordinate.
			result.rightmostX = o.x + Math.sqrt(Math.pow(a.x - o.x, 2.0) + Math.pow(a.y - o.y, 2.0));

			result.valid = true;
			return result;
		}

		// Will a new parabola at point p intersect with arc i?
		CircleResultPack intersect(ComparablePoint p, Arc i) {
			final CircleResultPack res = new CircleResultPack();
			res.valid = false;
			if (i.focus.x == p.x) {
				return res;
			}

			double a = 0.0;
			double b = 0.0;
			if (i.prev != null) // Get the intersection of i->prev, i.
			{
				a = intersection(i.prev.focus, i.focus, p.x).y;
			}
			if (i.next != null) // Get the intersection of i->next, i.
			{
				b = intersection(i.focus, i.next.focus, p.x).y;
			}

			if ((i.prev == null || a <= p.y) && (i.next == null || p.y <= b)) {
				res.center = new ComparablePoint(0, p.y);

				// Plug it back into the parabola equation to get the x
				// coordinate
				res.center.x = (i.focus.x * i.focus.x + (i.focus.y - res.center.y) * (i.focus.y - res.center.y) - p.x
						* p.x)
						/ (2 * i.focus.x - 2 * p.x);

				res.valid = true;
				return res;
			}
			return res;
		}

		// Where do two parabolas intersect?
		ComparablePoint intersection(ComparablePoint p0, ComparablePoint p1, double l) {
			final ComparablePoint res = new ComparablePoint(0, 0);
			ComparablePoint p = p0;

			if (p0.x == p1.x) {
				res.y = (p0.y + p1.y) / 2;
			} else if (p1.x == l) {
				res.y = p1.y;
			} else if (p0.x == l) {
				res.y = p0.y;
				p = p1;
			} else {
				// Use the quadratic formula.
				final double z0 = 2 * (p0.x - l);
				final double z1 = 2 * (p1.x - l);

				final double a = 1 / z0 - 1 / z1;
				final double b = -2 * (p0.y / z0 - p1.y / z1);
				final double c = (p0.y * p0.y + p0.x * p0.x - l * l) / z0 - (p1.y * p1.y + p1.x * p1.x - l * l) / z1;

				res.y = (float) ((-b - Math.sqrt((b * b - 4 * a * c))) / (2 * a));
			}
			// Plug back into one of the parabola equations.
			res.x = (float) ((p.x * p.x + (p.y - res.y) * (p.y - res.y) - l * l) / (2 * p.x - 2 * l));
			return res;
		}

		void finishEdges() {
			// Advance the sweep line so no parabolas can cross the bounding
			// box.
			final double l = width * 2 + height;

			// Extend each remaining segment to the new parabola intersections.
			Arc i = root;
			while (i != null) {
				if (i.edge1 != null) {
					i.edge1.finish(intersection(i.focus, i.next.focus, l * 2));
				}
				i = i.next;
			}
		}

		class ComparablePoint extends Point2dImpl implements Comparable<ComparablePoint> {
			public ComparablePoint(double X, double Y) {
				x = (float) X;
				y = (float) Y;
			}

			public ComparablePoint(Point2d pt) {
				this.x = pt.getX();
				this.y = pt.getY();
			}

			@Override
			public int compareTo(ComparablePoint foo) {
				return ((Float) this.x).compareTo(foo.x);
			}
		}

		private static class CircleEvent implements Comparable<CircleEvent> {
			double xpos;
			ComparablePoint p;
			Arc a;
			boolean valid;

			CircleEvent(double X, ComparablePoint P, Arc A) {
				xpos = X;
				a = A;
				p = P;
				valid = true;
			}

			@Override
			public int compareTo(CircleEvent foo) {
				return ((Double) this.xpos).compareTo(foo.xpos);
			}
		}

		private class Edge extends Line2d {
			boolean done;

			Edge(ComparablePoint p) {
				begin = p;
				end = new ComparablePoint(0, 0);
				done = false;
				output.add(this);
			}

			void finish(ComparablePoint p) {
				if (done) {
					return;
				}
				end = p;
				done = true;
			}
		}

		/**
		 * Parabolic arc: the set of points equidistant from a focus point and
		 * the beach line
		 */
		private static class Arc {
			ComparablePoint focus;

			// Arcs exist in a linked list
			Arc next, prev;

			CircleEvent event;

			Edge edge0, edge1;

			Arc(ComparablePoint p) {
				focus = p;
				next = null;
				prev = null;
				event = null;
				edge0 = null;
				edge1 = null;
			}
		}

		private static class CircleResultPack {
			boolean valid;
			ComparablePoint center;
			double rightmostX;
		}
	}
}
