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


import junit.framework.Assert;

import org.junit.Test;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 *	
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 26 Aug 2011
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
		Polygon p = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Assert.assertTrue( p.isInside( new Point2dImpl( 150f, 150f ) ) );
	}
	
	/**
	 * Test checking of point inside polygon with hole
	 */
	@Test
	public void testPolygonWithHoleIsInside()
	{
		Polygon p = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon hole = new Rectangle( 125f, 125f, 50f, 50f ).asPolygon();
		hole.setIsHole( true );
		p.addInnerPolygon( hole );
		
		Assert.assertTrue(  p.isInside( new Point2dImpl( 110f, 110f ) ) );
		Assert.assertTrue( !p.isInside( new Point2dImpl( 150f, 150f ) ) );
	}
	
	/**
	 * Test cloning
	 */
	@Test
	public void testPolygonClone()
	{
		Polygon p = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon hole = new Rectangle( 125f, 125f, 50f, 50f ).asPolygon();
		hole.setIsHole( true );
		p.addInnerPolygon( hole );

		Polygon p2 = p.clone();
		Assert.assertTrue(  p2.isInside( new Point2dImpl( 110f, 110f ) ) );
		Assert.assertTrue( !p2.isInside( new Point2dImpl( 150f, 150f ) ) );
	}
	
	/**
	 * Test intersection
	 */
	@Test
	public void testPolygonIntersection()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 150f, 150f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.intersect( p2 );
		Polygon p4 = new Rectangle( 150f, 150f, 50f, 50f ).asPolygon();
		Assert.assertEquals( p4, p3 );
	}

	/**
	 * Test union
	 */
	@Test
	public void testPolygonUnion()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 200f, 100f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.union( p2 );
		Polygon p4 = new Rectangle( 100f, 100f, 200f, 100f ).asPolygon();
		Assert.assertEquals( p4, p3 );
	}
	
	/**
	 * Test XOR
	 */
	@Test
	public void testPolygonXOR()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 200f, 100f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.union( p2 );
		Polygon p4 = new Rectangle( 100f, 100f, 200f, 100f ).asPolygon();
		Assert.assertEquals( p4, p3 );		
	}
	
	/**
	 * Test vertex reduction
	 */
	@Test
	public void testPolygonReduction()
	{
		Polygon p1 = new Circle( 100f, 100f, 50f ).asPolygon();
		Assert.assertEquals( 360, p1.nVertices() );
		
		Polygon p2 = p1.reduceVertices( 0.3f );
		System.out.println( p2.nVertices() );
	}
}
