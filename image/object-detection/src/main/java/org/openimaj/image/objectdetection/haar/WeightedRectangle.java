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
package org.openimaj.image.objectdetection.haar;

/**
 * A Rectangle with an associated weight. This is used to represent part of a
 * haar-like feature.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class WeightedRectangle {
	/**
	 * The weight assigned to the rectangle
	 */
	public float weight;

	/**
	 * The height
	 */
	public int height;

	/**
	 * The width
	 */
	public int width;

	/**
	 * The top-left y-ordinate
	 */
	public int y;

	/**
	 * The top-left x-ordinate
	 */
	public int x;

	/**
	 * Construct a {@link WeightedRectangle} with the given parameters.
	 * 
	 * @param x
	 *            x-coordinate of top-left
	 * @param y
	 *            y-coordinate of top-left
	 * @param width
	 *            width
	 * @param height
	 *            height
	 * @param weight
	 *            weight
	 */
	public WeightedRectangle(int x, int y, int width, int height, float weight) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.weight = weight;
	}

	/**
	 * parse a string of the form "x y width height weight" to construct a
	 * WeightedRectangle.
	 * 
	 * @param str
	 *            the string to parse
	 * @return the constructed WeightedRectangle
	 */
	public static WeightedRectangle parse(String str) {
		final String[] parts = str.trim().split(" ");

		final int x = Integer.parseInt(parts[0]);
		final int y = Integer.parseInt(parts[1]);
		final int width = Integer.parseInt(parts[2]);
		final int height = Integer.parseInt(parts[3]);
		final float weight = Float.parseFloat(parts[4]);

		return new WeightedRectangle(x, y, width, height, weight);
	}
}
