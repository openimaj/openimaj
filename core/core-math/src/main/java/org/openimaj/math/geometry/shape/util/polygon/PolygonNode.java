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

	/**
	 * @param next
	 * @param x
	 * @param y
	 */
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

	/**
	 * @param x
	 * @param y
	 */
	public void add_right( double x, double y )
	{
		VertexNode nv = new VertexNode( x, y );

		/* Add vertex nv to the right end of the polygon's vertex list */
		proxy.v[PolygonUtils.RIGHT].next = nv;

		/* Update proxy->v[RIGHT] to point to nv */
		proxy.v[PolygonUtils.RIGHT] = nv;
	}

	/**
	 * @param x
	 * @param y
	 */
	public void add_left( double x, double y )
	{
		VertexNode nv = new VertexNode( x, y );

		/* Add vertex nv to the left end of the polygon's vertex list */
		nv.next = proxy.v[PolygonUtils.LEFT];

		/* Update proxy->[LEFT] to point to nv */
		proxy.v[PolygonUtils.LEFT] = nv;
	}

}
