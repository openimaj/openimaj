/**
 * 
 */
package org.openimaj.math.geometry.triangulation;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Triangle;


/**
 *	
 *
 *	@author David Dupplaw <dpd@ecs.soton.ac.uk>
 *  @created 26 Aug 2011
 *	@version $Author$, $Revision$, $Date$
 */
public class DelaunayTriangulatorTest
{
	@Test
	public void testDelaunaySimple()
	{
		Polygon p = new Rectangle( 100f, 100f, 100f, 100f ).asPolygon();
		List<Triangle> tris = DelaunayTriangulator.triangulate( p.vertices );
		Assert.assertEquals( 2, tris.size() );
	}	
}
