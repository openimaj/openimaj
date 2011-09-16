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
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 26 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class PolygonTest
{
	@Test
	public void testPolygonIsInside()
	{
		Polygon p = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Assert.assertTrue( p.isInside( new Point2dImpl( 150f, 150f ) ) );
	}
	
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
	
	@Test
	public void testPolygonIntersection()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 150f, 150f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.intersect( p2 );
		Polygon p4 = new Rectangle( 150f, 150f, 50f, 50f ).asPolygon();
		Assert.assertEquals( p4, p3 );
	}

	@Test
	public void testPolygonUnion()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 200f, 100f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.union( p2 );
		Polygon p4 = new Rectangle( 100f, 100f, 200f, 100f ).asPolygon();
		Assert.assertEquals( p4, p3 );
	}
	
	@Test
	public void testPolygonXOR()
	{
		Polygon p1 = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		Polygon p2 = new Rectangle( 200f, 100f, 100f, 100f ).asPolygon();
		Polygon p3 = p1.union( p2 );
		Polygon p4 = new Rectangle( 100f, 100f, 200f, 100f ).asPolygon();
		Assert.assertEquals( p4, p3 );		
	}
}
