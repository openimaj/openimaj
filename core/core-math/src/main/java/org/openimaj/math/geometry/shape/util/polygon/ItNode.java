package org.openimaj.math.geometry.shape.util.polygon;

import java.awt.geom.Point2D;

/**
 * Intersection table
 */
public class ItNode
{
	public EdgeNode[] ie = new EdgeNode[2]; /* Intersecting edge (bundle) pair */

	public Point2D.Double point = new Point2D.Double(); /* Point of intersection */

	public ItNode next; /* The next intersection table node */

	public ItNode( EdgeNode edge0, EdgeNode edge1, double x, double y, ItNode next )
	{
		this.ie[0] = edge0;
		this.ie[1] = edge1;
		this.point.x = x;
		this.point.y = y;
		this.next = next;
	}
}
