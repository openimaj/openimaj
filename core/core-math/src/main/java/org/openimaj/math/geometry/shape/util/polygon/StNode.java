package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Sorted edge table
 */
public class StNode
{
	public EdgeNode edge; /* Pointer to AET edge */

	public double xb; /* Scanbeam bottom x coordinate */

	public double xt; /* Scanbeam top x coordinate */

	public double dx; /* Change in x for a unit y increase */

	public StNode prev; /* Previous edge in sorted list */

	public StNode( EdgeNode edge, StNode prev )
	{
		this.edge = edge;
		this.xb = edge.xb;
		this.xt = edge.xt;
		this.dx = edge.dx;
		this.prev = prev;
	}
}
