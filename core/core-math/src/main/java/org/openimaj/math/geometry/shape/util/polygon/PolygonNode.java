package org.openimaj.math.geometry.shape.util.polygon;

import org.openimaj.math.geometry.shape.util.PolygonUtils;

/**
 * Internal contour / tristrip type
 */
public class PolygonNode
{
	int active; /* Active flag / vertex count */

	boolean hole; /* Hole / external contour flag */

	VertexNode[] v = new VertexNode[2]; /* Left and right vertex list ptrs */

	PolygonNode next; /* Pointer to next polygon contour */

	PolygonNode proxy; /* Pointer to actual structure used */

	public PolygonNode( PolygonNode next, double x, double y )
	{
		/* Make v[LEFT] and v[RIGHT] point to new vertex */
		VertexNode vn = new VertexNode( x, y );
		this.v[PolygonUtils.LEFT] = vn;
		this.v[PolygonUtils.RIGHT] = vn;

		this.next = next;
		this.proxy = this; /* Initialise proxy to point to p itself */
		this.active = 1; // TRUE
	}

	public void add_right( double x, double y )
	{
		VertexNode nv = new VertexNode( x, y );

		/* Add vertex nv to the right end of the polygon's vertex list */
		proxy.v[PolygonUtils.RIGHT].next = nv;

		/* Update proxy->v[RIGHT] to point to nv */
		proxy.v[PolygonUtils.RIGHT] = nv;
	}

	public void add_left( double x, double y )
	{
		VertexNode nv = new VertexNode( x, y );

		/* Add vertex nv to the left end of the polygon's vertex list */
		nv.next = proxy.v[PolygonUtils.LEFT];

		/* Update proxy->[LEFT] to point to nv */
		proxy.v[PolygonUtils.LEFT] = nv;
	}

}
