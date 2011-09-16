package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Local minima table
 */
public class LmtNode
{
	public double y; /* Y coordinate at local minimum */

	public EdgeNode first_bound; /* Pointer to bound list */

	public LmtNode next; /* Pointer to next local minimum */

	public LmtNode( double yvalue )
	{
		y = yvalue;
	}
}
