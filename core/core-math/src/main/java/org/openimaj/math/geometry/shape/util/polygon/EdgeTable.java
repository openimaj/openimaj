package org.openimaj.math.geometry.shape.util.polygon;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.util.PolygonUtils;

public class EdgeTable
{
	private List<EdgeNode> m_List = new ArrayList<EdgeNode>();

	public void addNode( double x, double y )
	{
		EdgeNode node = new EdgeNode();
		node.vertex.x = x;
		node.vertex.y = y;
		m_List.add( node );
	}

	public EdgeNode getNode( int index )
	{
		return (EdgeNode) m_List.get( index );
	}

	public boolean FWD_MIN( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return ((prev.vertex.getY() >= ith.vertex.getY()) && (next.vertex.getY() > ith.vertex.getY()));
	}

	public boolean NOT_FMAX( int i )
	{
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return (next.vertex.getY() > ith.vertex.getY());
	}

	public boolean REV_MIN( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return ((prev.vertex.getY() > ith.vertex.getY()) && (next.vertex.getY() >= ith.vertex.getY()));
	}

	public boolean NOT_RMAX( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return (prev.vertex.getY() > ith.vertex.getY());
	}
}
