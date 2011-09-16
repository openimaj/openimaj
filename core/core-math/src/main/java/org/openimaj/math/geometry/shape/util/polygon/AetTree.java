package org.openimaj.math.geometry.shape.util.polygon;

public class AetTree
{
	public EdgeNode top_node;

	public void print()
	{
		System.out.println( "" );
		System.out.println( "aet" );
		for( EdgeNode edge = top_node; (edge != null); edge = edge.next )
		{
			System.out.println( "edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y );
		}
	}
}
