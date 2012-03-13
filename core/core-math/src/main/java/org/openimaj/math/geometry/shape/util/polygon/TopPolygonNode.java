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

import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.util.PolygonUtils;

/** */
public class TopPolygonNode
{
	PolygonNode top_node = null;

	/**
	 * @param x
	 * @param y
	 * @return polygon node
	 */
	public PolygonNode add_local_min( double x, double y )
	{
		PolygonNode existing_min = top_node;

		top_node = new PolygonNode( existing_min, x, y );

		return top_node;
	}

	/**
	 * @param p
	 * @param q
	 */
	public void merge_left( PolygonNode p, PolygonNode q )
	{
		/* Label contour as a hole */
		q.proxy.hole = true;

		if( p.proxy != q.proxy )
		{
			/* Assign p's vertex list to the left end of q's list */
			p.proxy.v[PolygonUtils.RIGHT].next = q.proxy.v[PolygonUtils.LEFT];
			q.proxy.v[PolygonUtils.LEFT] = p.proxy.v[PolygonUtils.LEFT];

			/* Redirect any p.proxy references to q.proxy */
			PolygonNode target = p.proxy;
			for( PolygonNode node = top_node; (node != null); node = node.next )
			{
				if( node.proxy == target )
				{
					node.active = 0;
					node.proxy = q.proxy;
				}
			}
		}
	}

	/**
	 * @param p
	 * @param q
	 */
	public void merge_right( PolygonNode p, PolygonNode q )
	{
		/* Label contour as external */
		q.proxy.hole = false;

		if( p.proxy != q.proxy )
		{
			/* Assign p's vertex list to the right end of q's list */
			q.proxy.v[PolygonUtils.RIGHT].next = p.proxy.v[PolygonUtils.LEFT];
			q.proxy.v[PolygonUtils.RIGHT] = p.proxy.v[PolygonUtils.RIGHT];

			/* Redirect any p->proxy references to q->proxy */
			PolygonNode target = p.proxy;
			for( PolygonNode node = top_node; (node != null); node = node.next )
			{
				if( node.proxy == target )
				{
					node.active = 0;
					node.proxy = q.proxy;
				}
			}
		}
	}

	/**
	 * @return count of contours
	 */
	public int count_contours()
	{
		int nc = 0;
		for( PolygonNode polygon = top_node; 
			(polygon != null); polygon = polygon.next )
		{
			if( polygon.active != 0 )
			{
				/* Count the vertices in the current contour */
				int nv = 0;
				for( VertexNode v = polygon.proxy.v[PolygonUtils.LEFT]; 
					(v != null); v = v.next )
				{
					nv++;
				}

				/* Record valid vertex counts in the active field */
				if( nv > 2 )
				{
					polygon.active = nv;
					nc++;
				}
				else
				{
					/* Invalid contour: just free the heap */
					// VertexNode nextv = null ;
					// for (VertexNode v= polygon.proxy.v[LEFT]; (v != null); v
					// = nextv)
					// {
					// nextv= v.next;
					// v = null ;
					// }
					polygon.active = 0;
				}
			}
		}
		return nc;
	}

	/**
	 * @param polyClass
	 * @return a polygon
	 */
	public Polygon getResult( Class<Polygon> polyClass )
	{
		Polygon result = new Polygon();
		int num_contours = count_contours();
		if( num_contours > 0 )
		{
			PolygonNode npoly_node = null;
			for( PolygonNode poly_node = top_node; 
			     (poly_node != null); poly_node = npoly_node )
			{
				npoly_node = poly_node.next;
				
				if( poly_node.active != 0 )
				{
					Polygon polygon = result;
					
					if( num_contours > 1 )
					{
						polygon = new Polygon();
					}
					
					if( poly_node.proxy.hole )
					{
						polygon.setIsHole( poly_node.proxy.hole );
					}

					// ------------------------------------------------------------------------
					// --- This algorithm puts the verticies into the Polygon in
					// reverse order ---
					// ------------------------------------------------------------------------
					for( VertexNode vtx = poly_node.proxy.v[PolygonUtils.LEFT]; 
					     (vtx != null); vtx = vtx.next )
					{
						polygon.addVertex( (float) vtx.x, (float) vtx.y );
					}
					
					if( num_contours > 1 )
					{
						result.addInnerPolygon( polygon );
					}
				}
			}

			// -----------------------------------------
			// --- Sort holes to the end of the list ---
			// -----------------------------------------
			Polygon orig = result;
			result = new Polygon();
			for( int i = 0; i < orig.getNumInnerPoly(); i++ )
			{
				Polygon inner = orig.getInnerPoly( i );
				if( !inner.isHole() )
				{
					result.addInnerPolygon( inner );
				}
			}
			for( int i = 0; i < orig.getNumInnerPoly(); i++ )
			{
				Polygon inner = orig.getInnerPoly( i );
				if( inner.isHole() )
				{
					result.addInnerPolygon( inner );
				}
			}
		}
		return result;
	}

	/** */
	public void print()
	{
		System.out.println( "---- out_poly ----" );
		int c = 0;
		PolygonNode npoly_node = null;
		for( PolygonNode poly_node = top_node; (poly_node != null); 
			poly_node = npoly_node )
		{
			System.out.println( "contour=" + c + "  active=" + 
					poly_node.active + "  hole=" + poly_node.proxy.hole );
			npoly_node = poly_node.next;
			if( poly_node.active != 0 )
			{
				int v = 0;
				for( VertexNode vtx = poly_node.proxy.v[PolygonUtils.LEFT]; (vtx != null); vtx = vtx.next )
				{
					System.out.println( "v=" + v + "  vtx.x=" + vtx.x + "  vtx.y=" + vtx.y );
				}
				c++;
			}
		}
	}
}
