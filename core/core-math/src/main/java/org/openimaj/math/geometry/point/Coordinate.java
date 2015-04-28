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

import org.openimaj.io.ReadWriteable;

/**
 * Generic interface to get the distance along a dimension of an object
 * representing a point in an n-dimensional space
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public interface Coordinate extends ReadWriteable {
	/**
	 * Get the ordinate value for a specific dimension.
	 * 
	 * @param dimension
	 *            The index of the dimension we are interested in
	 * @return The value of the ordinate of the given dimension.
	 * @exception IllegalArgumentException
	 *                if the Coordinate does not support the dimension.
	 */
	public Number getOrdinate(int dimension);

	/**
	 * Set the ordinate value for a specific dimension.
	 * 
	 * @param dimension
	 *            The index of the dimension we are interested in
	 * @param value
	 *            The value of the ordinate of the given dimension.
	 * @exception IllegalArgumentException
	 *                if the Coordinate does not support the dimension.
	 */
	public void setOrdinate(int dimension, Number value);

	/**
	 * @return The number of dimensions in the coordinate.
	 */
	public int getDimensions();
}
