package org.openimaj.math.geometry.shape.util.polygon;

/**
 * Internal vertex list datatype
 */
class VertexNode
{
	double x; // X coordinate component

	double y; // Y coordinate component

	VertexNode next; // Pointer to next vertex in list

	public VertexNode( double x, double y )
	{
		this.x = x;
		this.y = y;
		this.next = null;
	}
}
