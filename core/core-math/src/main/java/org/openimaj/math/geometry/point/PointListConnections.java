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
package org.openimaj.math.geometry.point;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;

/**
 * Class to model the connections between points in a {@link PointList}. The
 * connections are based on the indices of the points in the model, so it is
 * easy to apply the connections to any variant of a {@link PointList}
 * representing a given geometry.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class PointListConnections {
	List<int[]> connections;

	/**
	 * Default constructor. Makes an empty list of connections.
	 */
	public PointListConnections() {
		connections = new ArrayList<int[]>();
	}

	/**
	 * Construct with a {@link PointList} and a list of lines between points in
	 * the {@link PointList}.
	 * 
	 * @param pl
	 *            the {@link PointList}.
	 * @param lines
	 *            the lines.
	 */
	public PointListConnections(PointList pl, List<Line2d> lines) {
		this.connections = new ArrayList<int[]>();

		for (final Line2d line : lines) {
			final int i1 = pl.points.indexOf(line.begin);
			final int i2 = pl.points.indexOf(line.end);

			connections.add(new int[] { i1, i2 });
		}
	}

	/**
	 * Add a connection between points with the given indices.
	 * 
	 * @param from
	 *            first point
	 * @param to
	 *            second point
	 */
	public void addConnection(int from, int to) {
		if (from == to)
			return;
		connections.add(new int[] { from, to });
	}

	/**
	 * Add a connection between two points in the given {@link PointList}.
	 * 
	 * @param pl
	 *            the {@link PointList}
	 * @param from
	 *            the first point
	 * @param to
	 *            the second point
	 */
	public void addConnection(PointList pl, Point2d from, Point2d to) {
		addConnection(pl.points.indexOf(from), pl.points.indexOf(to));
	}

	/**
	 * Add a connection between the two end points of the given line in the
	 * given {@link PointList}.
	 * 
	 * @param pl
	 *            the {@link PointList}
	 * @param line
	 *            the line
	 */
	public void addConnection(PointList pl, Line2d line) {
		addConnection(pl.points.indexOf(line.begin), pl.points.indexOf(line.end));
	}

	/**
	 * Get the points connected to the given point.
	 * 
	 * @param pt
	 *            The target point.
	 * @param pl
	 *            The {@link PointList} in whioch to search.
	 * @return the connected points.
	 */
	public Point2d[] getConnections(Point2d pt, PointList pl) {
		final int[] conns = getConnections(pl.points.indexOf(pt));
		final Point2d[] pts = new Point2d[conns.length];

		for (int i = 0; i < conns.length; i++) {
			pts[i] = pl.points.get(conns[i]);
		}

		return pts;
	}

	/**
	 * Get the indices of all the points connected to the point with the given
	 * index.
	 * 
	 * @param id
	 *            The point to search for
	 * @return the indices of the connected points.
	 */
	public int[] getConnections(int id) {
		final TIntArrayList conns = new TIntArrayList();

		for (final int[] c : connections) {
			if (c[0] == id)
				conns.add(c[1]);
			if (c[1] == id)
				conns.add(c[0]);
		}

		return conns.toArray();
	}

	/**
	 * Calculate a normal line for a given vertex.
	 * 
	 * @param pt
	 *            the vertex
	 * @param pointList
	 *            the {@link PointList} in which to search/
	 * @param scale
	 *            The scaling to apply to the line; a scale of 1.0 will lead to
	 *            a line that is 2.0 units long (1.0 either side of the vertex).
	 * @return the normal line.
	 */
	public Line2d calculateNormalLine(Point2d pt, PointList pointList, float scale) {
		final Point2dImpl normal = calculateNormal(pointList.points.indexOf(pt), pointList);

		if (normal == null)
			return null;

		final float nx = normal.x;
		final float ny = normal.y;

		final Point2dImpl start = new Point2dImpl((nx * scale) + pt.getX(), (ny * scale) + pt.getY());
		final Point2dImpl end = new Point2dImpl(-(nx * scale) + pt.getX(), -(ny * scale) + pt.getY());

		return new Line2d(start, end);
	}

	/**
	 * Calculate a normal line for a given vertex.
	 * 
	 * @param idx
	 *            the vertex index
	 * @param pointList
	 *            the {@link PointList} in which to search/
	 * @param scale
	 *            The scaling to apply to the line; a scale of 1.0 will lead to
	 *            a line that is 2.0 units long (1.0 either side of the vertex).
	 * @return the normal line.
	 */
	public Line2d calculateNormalLine(int idx, PointList pointList, float scale) {
		return calculateNormalLine(pointList.points.get(idx), pointList, scale);
	}

	/**
	 * Calculate the normal vector at a given vertex.
	 * 
	 * @param pt
	 *            the vertex.
	 * @param pointList
	 *            the {@link PointList} in which to search.
	 * @return the unit normal vector of the vertex.
	 */
	public Point2dImpl calculateNormal(Point2d pt, PointList pointList) {
		return calculateNormal(pointList.points.indexOf(pt), pointList);
	}

	/**
	 * Calculate the normal vector at a given vertex id.
	 * 
	 * @param id
	 *            the vertex id.
	 * @param pointList
	 *            the {@link PointList} in which to search.
	 * @return the unit normal vector of the vertex.
	 */
	public Point2dImpl calculateNormal(int id, PointList pointList) {
		final int[] conns = getConnections(id);

		if (conns.length == 1) {
			final Point2d p0 = pointList.points.get(id);
			final Point2d p1 = pointList.points.get(conns[0]);

			final Line2d line = new Line2d(p0, p1);
			final Line2d normal = line.getNormal();

			return normal.toUnitVector();
		} else if (conns.length == 2) {
			final Point2d p0 = pointList.points.get(id);
			final Point2d p1 = pointList.points.get(conns[0]);
			final Point2d p2 = pointList.points.get(conns[1]);

			final Line2d line1 = new Line2d(p0, p1);
			final Line2d normal1 = line1.getNormal();

			final Line2d line2 = new Line2d(p0, p2);
			final Line2d normal2 = line2.getNormal();

			final Point2dImpl n1 = normal1.toUnitVector();
			final Point2dImpl n2 = normal2.toUnitVector();

			double dx = (n1.x - n2.x);
			double dy = (n1.y - n2.y);
			final double norm = Math.sqrt(dx * dx + dy * dy);
			dx /= norm;
			dy /= norm;

			return new Point2dImpl((float) dx, (float) dy);
		} else {
			final Point2d p0 = pointList.points.get(id);

			final Line2d line = new Line2d(p0.getX() - 1, p0.getY(), p0.getX() + 1, p0.getY());

			return line.toUnitVector();
		}
	}

	/**
	 * Get the connections as a list of lines based on the points in the given
	 * {@link PointList}.
	 * 
	 * @param pointList
	 *            the {@link PointList}.
	 * @return the lines.
	 */
	public List<Line2d> getLines(PointList pointList) {
		final List<Line2d> lines = new ArrayList<Line2d>(connections.size());

		for (final int[] conn : connections) {
			lines.add(new Line2d(
					pointList.points.get(conn[0]),
					pointList.points.get(conn[1])
					));
		}

		return lines;
	}
}
