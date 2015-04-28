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
package org.openimaj.math.geometry;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Rectangle;

import Jama.Matrix;

/**
 * A generalised 2D geometric object that has a calculable centre of gravity and
 * regular bounding box. The object can also be transformed in a variety of
 * ways.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface GeometricObject2d {
	/**
	 * Compute the regular (oriented to the axes) bounding box of the shape.
	 * 
	 * @return the regular bounding box as [x,y,width,height]
	 */
	public Rectangle calculateRegularBoundingBox();

	/**
	 * Translate the shapes position
	 * 
	 * @param x
	 *            x-translation
	 * @param y
	 *            y-translation
	 */
	public void translate(float x, float y);

	/**
	 * Scale the shape by the given amount about (0,0). Scalefactors between 0
	 * and 1 shrink the shape.
	 * 
	 * @param sc
	 *            the scale factor.
	 */
	public void scale(float sc);

	/**
	 * Scale the shape by the given amount about the given point. Scalefactors
	 * between 0 and 1 shrink the shape.
	 * 
	 * @param centre
	 *            the centre of the scaling operation
	 * @param sc
	 *            the scale factor
	 */
	public void scale(Point2d centre, float sc);

	/**
	 * Scale the shape about its centroid. Scalefactors between 0 and 1 shrink
	 * the shape.
	 * 
	 * @param sc
	 *            the scale factor
	 */
	public void scaleCentroid(float sc);

	/**
	 * Calculate the centroid of the shape
	 * 
	 * @return the centroid of the shape
	 */
	public Point2d calculateCentroid();

	/**
	 * @return the minimum x-ordinate
	 */
	public double minX();

	/**
	 * @return the minimum y-ordinate
	 */
	public double minY();

	/**
	 * @return the maximum x-ordinate
	 */
	public double maxX();

	/**
	 * @return the maximum y-ordinate
	 */
	public double maxY();

	/**
	 * @return the width of the regular bounding box
	 */
	public double getWidth();

	/**
	 * @return the height of the regular bounding box
	 */
	public double getHeight();

	/**
	 * Apply a 3x3 transform matrix to a copy of the {@link GeometricObject2d} and
	 * return it
	 * 
	 * @param transform
	 *            3x3 transform matrix
	 * @return the transformed shape
	 */
	public GeometricObject2d transform(Matrix transform);
}
