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
package org.openimaj.math.geometry.shape;

import java.util.List;

import org.openimaj.math.geometry.triangulation.DelaunayTriangulator;

/**
 * A polygon that has been broken into triangles.
 * 
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class TriangulatedPolygon extends Polygon {
	protected List<Triangle> triangles;

	/**
	 * Construct a {@link TriangulatedPolygon} with a polygon.
	 * Triangulation is performed with a {@link DelaunayTriangulator}.
	 * @param p The polygon.
	 */
	public TriangulatedPolygon(Polygon p) {
		super(p.isHole());
		this.points = p.points;
		triangles = DelaunayTriangulator.triangulate(p.points);
	}
	
	/**
	 * Construct a {@link TriangulatedPolygon} with a shape.
	 * Triangulation is performed with a {@link DelaunayTriangulator}.
	 * @param shape The shape.
	 */
	public TriangulatedPolygon(Shape shape) {
		this(shape.asPolygon());
	}

//	@Override
//	public boolean isInside(Point2d point){
//		for(Triangle t : triangles){
//			if(t.isInside(point)) return true;
//		}
//		return false;
//	}
}
