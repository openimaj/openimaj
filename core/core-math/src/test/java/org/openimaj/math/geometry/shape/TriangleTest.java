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

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TriangleTest {

	/**
	 * Test line intersection with a triangle
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLineIntersection() throws Exception {
		Triangle t = new Triangle(
				new Point2dImpl(0, 0),
				new Point2dImpl(0.5f, 1f),
				new Point2dImpl(1, 0)
				);
		final Point2d begin = new Point2dImpl(0f, 0.5f);
		final Point2d end = new Point2dImpl(1f, 0.5f);

		final Line2d midline = new Line2d(begin, end);

		Map<Line2d, Point2d> points = t.intersectionSides(midline);
		assertTrue(points.size() == 2);

		t = new Triangle(
				new Point2dImpl(0, 0),
				new Point2dImpl(0.5f, 1f),
				new Point2dImpl(0.5f, 0f)
				);
		points = t.intersectionSides(midline);
		assertTrue(points.size() == 2);

		t = new Triangle(
				new Point2dImpl(0, 0.5f),
				new Point2dImpl(0.5f, 1f),
				new Point2dImpl(1, 0.5f)
				);
		points = t.intersectionSides(midline);
	}

	/**
	 * Test that testing if a point is inside a triangle works
	 */
	@Test
	public void testIsInside() {
		final Triangle t = new Triangle(
				new Point2dImpl(0, 0),
				new Point2dImpl(0.5f, 1f),
				new Point2dImpl(0.5f, 0f)
				);
		assertTrue(!t.isInside(new Point2dImpl(0.25f, 0f)));
		assertTrue(t.isInsideOnLine(new Point2dImpl(0.25f, 0f)));
		assertTrue(!t.isInside(new Point2dImpl(0.5f, 0.5f)));
		assertTrue(t.isInsideOnLine(new Point2dImpl(0.5f, 0.5f)));

	}

}
