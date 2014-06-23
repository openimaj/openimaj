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
package org.openimaj.math.geometry.point;

import Jama.Matrix;

/**
 * Interface representing a point in 3d space with x and y coordinates
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public interface Point3d extends Coordinate {
	/**
	 * @return x coordinate of point
	 */
	public double getX();

	/**
	 * Set x coordinate of point
	 * 
	 * @param x
	 *            x-coordinate
	 */
	public void setX(double x);

	/**
	 * @return y coordinate of point
	 */
	public double getY();

	/**
	 * Set y coordinate of point
	 * 
	 * @param y
	 *            y-coordinate
	 */
	public void setY(double y);

	/**
	 * @return z coordinate of point
	 */
	public double getZ();

	/**
	 * Set z coordinate of point
	 * 
	 * @param z
	 *            z-coordinate
	 */
	public void setZ(double z);

	/**
	 * Copy the values of the given point into this point.
	 * 
	 * @param p
	 *            The point to copy values from.
	 */
	public void copyFrom(Point3d p);

	/**
	 * Clone the point
	 * 
	 * @return a copy of the point
	 */
	public Point3d copy();

	/**
	 * Translate the position of the point by the given amounts
	 * 
	 * @param x
	 *            x-amount
	 * @param y
	 *            y-amount
	 * @param z
	 *            z-amount
	 */
	public void translate(double x, double y, double z);

	/**
	 * Transform the point by the given matrix
	 * 
	 * @param m
	 * @return a copy
	 */
	public Point3d transform(Matrix m);

	/**
	 * Take point point from another point such that return = this - a
	 * 
	 * @param a
	 * @return a new point
	 */
	public Point3d minus(Point3d a);

	/**
	 * Translate the position of the point by the given amounts
	 * 
	 * @param v
	 *            the vector to translate by
	 */
	void translate(Point3d v);
}
