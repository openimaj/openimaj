/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.math.geometry.shape.util.polygon;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.shape.util.PolygonUtils;

/** */
public class EdgeTable
{
	private List<EdgeNode> m_List = new ArrayList<EdgeNode>();

	/**
	 * @param x
	 * @param y
	 */
	public void addNode( double x, double y )
	{
		EdgeNode node = new EdgeNode();
		node.vertex.x = x;
		node.vertex.y = y;
		m_List.add( node );
	}

	/**
	 * @param index
	 * @return edge node
	 */
	public EdgeNode getNode( int index )
	{
		return (EdgeNode) m_List.get( index );
	}

	/**
	 * @param i
	 * @return FWD_MIN
	 */
	public boolean FWD_MIN( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return ((prev.vertex.getY() >= ith.vertex.getY()) && (next.vertex.getY() > ith.vertex.getY()));
	}

	/**
	 * @param i
	 * @return NOT_FMAX
	 */
	public boolean NOT_FMAX( int i )
	{
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return (next.vertex.getY() > ith.vertex.getY());
	}

	/**
	 * @param i
	 * @return REV_MIN
	 */
	public boolean REV_MIN( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode next = (EdgeNode) m_List.get( PolygonUtils.NEXT_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return ((prev.vertex.getY() > ith.vertex.getY()) && (next.vertex.getY() >= ith.vertex.getY()));
	}

	/**
	 * @param i
	 * @return NOT_RMAX
	 */
	public boolean NOT_RMAX( int i )
	{
		EdgeNode prev = (EdgeNode) m_List.get( PolygonUtils.PREV_INDEX( i, m_List.size() ) );
		EdgeNode ith = (EdgeNode) m_List.get( i );
		return (prev.vertex.getY() > ith.vertex.getY());
	}
}
