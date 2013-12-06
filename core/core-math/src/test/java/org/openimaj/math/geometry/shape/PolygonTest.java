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
/**
 * 
 */
package org.openimaj.math.geometry.shape;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * 
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @created 26 Aug 2011
 * 
 */
public class PolygonTest
{
	/**
	 * Test checking of point inside polygon
	 */
	@Test
	public void testPolygonIsInside()
	{
		final Polygon p = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		Assert.assertTrue(p.isInside(new Point2dImpl(150f, 150f)));
	}

	/**
	 * Tests for the polygon's get centroid (i.e. first moment)
	 */
	@Test
	public void testPolygonCentroid() {
		final Polygon p = new Polygon(new Point2dImpl[] {
				new Point2dImpl(2, 0),
				new Point2dImpl(0, 4),
				new Point2dImpl(8, 8),
				new Point2dImpl(10, 4),
		});

		final Point2d centroid = p.calculateCentroid();
		assertEquals(centroid.getX(), 5, 0.001);
		assertEquals(centroid.getY(), 4, 0.001);
	}

	/**
	 * Tests for the polygon's second moments
	 */
	@Test
	public void testPolygonSecondMoment() {
		final Polygon p = new Polygon(new Point2dImpl[] {
				new Point2dImpl(2, 0),
				new Point2dImpl(0, 4),
				new Point2dImpl(8, 8),
				new Point2dImpl(10, 4),
		});

		final double[] secondMoment = p.calculateSecondMoment();
		assertEquals(secondMoment[0], 30 + 2f / 3f, 0.0001);
		assertEquals(secondMoment[1], 22, 0.0001);
		assertEquals(secondMoment[2], 18 + 2f / 3f, 0.0001);

	}

	/**
	 * Tests for the polygon's second moments
	 */
	@Test
	public void testPolygonSecondMomentCentralised() {
		final Polygon p = new Polygon(new Point2dImpl[] {
				new Point2dImpl(2, 0),
				new Point2dImpl(0, 4),
				new Point2dImpl(8, 8),
				new Point2dImpl(10, 4),
		});

		final double[] secondMoment = p.calculateSecondMomentCentralised();
		assertEquals(secondMoment[0], 17f / 3f, 0.0001);
		assertEquals(secondMoment[1], 2, 0.0001);
		assertEquals(secondMoment[2], 8f / 3f, 0.0001);

	}

	/**
	 * Tests for the polygon's second moments
	 */
	@Test
	public void testPolygonDirection() {
		final Polygon p = new Polygon(new Point2dImpl[] {
				new Point2dImpl(2, 0),
				new Point2dImpl(0, 4),
				new Point2dImpl(8, 8),
				new Point2dImpl(10, 4),
		});

		final double direction = p.calculateDirection();
		System.out.println(direction);

	}

	/**
	 * Test checking of point inside polygon with hole
	 */
	@Test
	public void testPolygonWithHoleIsInside()
	{
		final Polygon p = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		final Polygon hole = new Rectangle(125f, 125f, 50f, 50f).asPolygon();
		hole.setIsHole(true);
		p.addInnerPolygon(hole);

		Assert.assertTrue(p.isInside(new Point2dImpl(110f, 110f)));
		Assert.assertTrue(!p.isInside(new Point2dImpl(150f, 150f)));
	}

	/**
	 * Test cloning
	 */
	@Test
	public void testPolygonClone()
	{
		final Polygon p = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		final Polygon hole = new Rectangle(125f, 125f, 50f, 50f).asPolygon();
		hole.setIsHole(true);
		p.addInnerPolygon(hole);

		final Polygon p2 = p.clone();
		Assert.assertTrue(p2.isInside(new Point2dImpl(110f, 110f)));
		Assert.assertTrue(!p2.isInside(new Point2dImpl(150f, 150f)));
	}

	/**
	 * Test intersection
	 */
	@Test
	public void testPolygonIntersection()
	{
		final Polygon p1 = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		final Polygon p2 = new Rectangle(150f, 150f, 100f, 100f).asPolygon();
		final Polygon p3 = p1.intersect(p2);
		final Polygon p4 = new Rectangle(150f, 150f, 50f, 50f).asPolygon();
		Assert.assertEquals(p4, p3);
	}

	/**
	 * Test union
	 */
	@Test
	public void testPolygonUnion()
	{
		final Polygon p1 = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		final Polygon p2 = new Rectangle(200f, 100f, 100f, 100f).asPolygon();
		final Polygon p3 = p1.union(p2);
		final Polygon p4 = new Rectangle(100f, 100f, 200f, 100f).asPolygon();
		Assert.assertEquals(p4, p3);
	}

	/**
	 * Test XOR
	 */
	@Test
	public void testPolygonXOR()
	{
		final Polygon p1 = new Rectangle(100f, 100f, 100f, 100f).asPolygon();
		final Polygon p2 = new Rectangle(200f, 100f, 100f, 100f).asPolygon();
		final Polygon p3 = p1.union(p2);
		final Polygon p4 = new Rectangle(100f, 100f, 200f, 100f).asPolygon();
		Assert.assertEquals(p4, p3);
	}

	/**
	 * Test vertex reduction
	 */
	@Test
	public void testPolygonReduction()
	{
		final Polygon p1 = new Circle(100f, 100f, 50f).asPolygon();
		Assert.assertEquals(360, p1.nVertices());

		final Polygon p2 = p1.reduceVertices(0.3f);
		System.out.println(p2.nVertices());
	}
}
