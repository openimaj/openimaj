package org.openimaj.math.geometry.shape.util.polygon;

public class LmtTable
{
	public LmtNode top_node;

	public void print()
	{
		int n = 0;
		LmtNode lmt = top_node;
		while( lmt != null )
		{
			System.out.println( "lmt(" + n + ")" );
			for( EdgeNode edge = lmt.first_bound; (edge != null); edge = edge.next_bound )
			{
				System.out.println( "edge.vertex.x=" + edge.vertex.x + "  edge.vertex.y=" + edge.vertex.y );
			}
			n++;
			lmt = lmt.next;
		}
	}
}
