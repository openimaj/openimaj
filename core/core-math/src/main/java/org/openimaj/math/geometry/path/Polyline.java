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
package org.openimaj.math.geometry.path;

import java.util.Iterator;
import java.util.List;

import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;

/**
 * A polyline is a {@link Path2d} implicitly made up of {@link Line2d} segments
 * based on the assumption of ordered points held in a {@link PointList}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class Polyline extends PointList implements Path2d {
	/**
	 * Construct a {@link Polyline} from points
	 *
	 * @param points
	 *            the points
	 */
	public Polyline(Point2d... points) {
		super(points);
	}

	/**
	 * Construct a {@link Polyline} from points
	 *
	 * @param points
	 *            the points
	 */
	public Polyline(List<? extends Point2d> points) {
		this(points, false);
	}

	/**
	 * Construct a {@link Polyline} from the points, possibly copying the points
	 * first
	 *
	 * @param points
	 *            the points
	 * @param copy
	 *            should the points be copied
	 */
	public Polyline(List<? extends Point2d> points, boolean copy) {
		super(points, copy);
	}

	/**
	 * Construct a {@link Polyline} from line segments
	 *
	 * @param lineIterator
	 *            a line segment iterator
	 */
	public Polyline(Iterator<Line2d> lineIterator) {
		Point2d end = null;
		while (lineIterator.hasNext()) {
			final Line2d line = lineIterator.next();
			this.points.add(line.begin);
			end = line.end;
		}
		if (end != null)
			this.points.add(end);
	}

	@Override
	public Point2d begin() {
		return points.get(0);
	}

	@Override
	public Point2d end() {
		return points.get(points.size() - 1);
	}

	@Override
	public Polyline asPolyline() {
		return this;
	}

	@Override
	public Iterator<Line2d> lineIterator() {
		return new Iterator<Line2d>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < points.size() - 1;
			}

			@Override
			public Line2d next() {
				final Line2d line = new Line2d(points.get(i), points.get(i + 1));
				i++;
				return line;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public double calculateLength() {
		double length = 0;
		for (int i = 0; i < points.size() - 1; i++) {
			length += Line2d.distance(points.get(i), points.get(i + 1));
		}
		return length;
	}
}
