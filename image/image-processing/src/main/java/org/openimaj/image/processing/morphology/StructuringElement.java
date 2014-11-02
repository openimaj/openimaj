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
package org.openimaj.image.processing.morphology;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openimaj.image.pixel.Pixel;

/**
 * Morphological structuring element
 * 
 * The central element is the pixel 0,0. Other s.e. pixels are relative to this.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class StructuringElement {
	/**
	 * Standard 3x3 box structuring element
	 */
	public final static StructuringElement BOX;

	/**
	 * Standard 3x3 cross structuring element
	 */
	public final static StructuringElement CROSS;

	/**
	 * Standard horizontal pit structuring element [x . x]
	 */
	public final static StructuringElement HPIT;

	// build the elements
	static {
		BOX = new StructuringElement();
		BOX.positive.add(new Pixel(-1, -1));
		BOX.positive.add(new Pixel(0, -1));
		BOX.positive.add(new Pixel(1, -1));
		BOX.positive.add(new Pixel(-1, 0));
		BOX.positive.add(new Pixel(0, 0));
		BOX.positive.add(new Pixel(1, 0));
		BOX.positive.add(new Pixel(-1, 1));
		BOX.positive.add(new Pixel(0, 1));
		BOX.positive.add(new Pixel(1, 1));

		CROSS = new StructuringElement();
		CROSS.positive.add(new Pixel(0, -1));
		CROSS.positive.add(new Pixel(-1, 0));
		CROSS.positive.add(new Pixel(0, 0));
		CROSS.positive.add(new Pixel(1, 0));
		CROSS.positive.add(new Pixel(0, 1));

		HPIT = new StructuringElement();
		HPIT.positive.add(new Pixel(-1, 0));
		HPIT.positive.add(new Pixel(1, 0));
	}

	/**
	 * Set of positive pixels in the structuring element
	 */
	public Set<Pixel> positive = new HashSet<Pixel>();

	/**
	 * Set of negative pixels in the structuring element
	 */
	public Set<Pixel> negative = new HashSet<Pixel>();

	/**
	 * Construct an empty structuring element
	 */
	public StructuringElement() {

	}

	/**
	 * Construct a structuring element with the given positive and negative
	 * pixels
	 * 
	 * @param positive
	 *            the positive pixels
	 * @param negative
	 *            the negative pixels
	 */
	public StructuringElement(Set<Pixel> positive, Set<Pixel> negative) {
		if (positive != null)
			this.positive.addAll(positive);
		if (negative != null)
			this.negative.addAll(negative);
	}

	/**
	 * Construct a structuring element with the given positive and negative
	 * pixels
	 * 
	 * @param positive
	 *            the positive pixels
	 * @param negative
	 *            the negative pixels
	 */
	public StructuringElement(Pixel[] positive, Pixel[] negative) {
		if (positive != null)
			this.positive.addAll(Arrays.asList(positive));
		if (negative != null)
			this.negative.addAll(Arrays.asList(negative));
	}

	/**
	 * Get the size of the structuring element in the form [width, height, x, y]
	 * 
	 * @return the size of the structuring element
	 */
	public int[] size() {
		int xmin = Integer.MAX_VALUE;
		int xmax = -Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = -Integer.MAX_VALUE;

		for (final Pixel p : positive) {
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}
		for (final Pixel p : negative) {
			if (p.x < xmin)
				xmin = p.x;
			if (p.x > xmax)
				xmax = p.x;
			if (p.y < ymin)
				ymin = p.y;
			if (p.y > ymax)
				ymax = p.y;
		}

		return new int[] { 1 + xmax - xmin, 1 + ymax - ymin, xmin, ymin };
	}

	/**
	 * Construct a structuring element from a @link{String} of the form produced
	 * by @link{#toString()}.
	 * 
	 * @see #toString()
	 * 
	 * @param ele
	 *            the string defining the element
	 * @param cx
	 *            the top-left x-coordinate
	 * @param cy
	 *            the top-left y-coordinate
	 * @return a new structuring element
	 */
	public static StructuringElement parseElement(String ele, int cx, int cy) {
		final String[] lines = ele.split("\\n");
		final int height = lines.length;
		final int width = lines[0].length();

		final StructuringElement se = new StructuringElement();

		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				final char c = lines[j].charAt(i);

				if (c == '*') {
					se.positive.add(new Pixel(i - cx, j - cy));
				} else if (c == 'o') {
					se.negative.add(new Pixel(i - cx, j - cy));
				}
			}
		}

		return se;
	}

	@Override
	public String toString() {
		final int[] sz = size();
		String s = "";

		for (int j = 0; j < sz[1]; j++) {
			for (int i = 0; i < sz[0]; i++) {
				final Pixel p = new Pixel(i + sz[2], j + sz[3]);

				if (positive.contains(p))
					s += "*";
				else if (negative.contains(p))
					s += "o";
				else
					s += ".";
			}
			s += "\n";
		}

		return s;
	}

	/**
	 * Determine if this structuring element is completely contained in the
	 * pixels centered at p.
	 * 
	 * @param p
	 *            the centre
	 * @param pixels
	 *            the pixels
	 * @return true if completely contained, false otherwise
	 */
	public boolean matches(Pixel p, Set<Pixel> pixels) {
		// is the s.e completely contained in the pixels centered at p?
		return (intersect(p, pixels).size() == countActive());
	}

	Set<Pixel> intersect(Pixel p, Set<Pixel> pixels) {
		final Set<Pixel> intersect = new HashSet<Pixel>();

		// positive
		for (final Pixel sep : positive) {
			final Pixel imp = new Pixel(p.x + sep.x, p.y + sep.y);

			if (pixels.contains(imp))
				intersect.add(imp);
		}

		// negative
		for (final Pixel sep : negative) {
			final Pixel imp = new Pixel(p.x + sep.x, p.y + sep.y);

			if (!pixels.contains(imp))
				intersect.add(imp);
		}

		return intersect;
	}

	/**
	 * Count the total (positive and negative) number of pixels in this
	 * structuring element
	 * 
	 * @return the total number of pixels
	 */
	public int countActive() {
		return positive.size() + negative.size();
	}

	/**
	 * Build a disk shaped structuring element with the given radius.
	 * 
	 * @param radius
	 *            the disk radius
	 * @return the disk shaped S.E.
	 */
	public static StructuringElement disk(int radius) {
		final StructuringElement se = new StructuringElement();
		final int r2 = radius * radius;

		for (int j = -radius; j <= radius; j++) {
			final int j2 = j * j;
			for (int i = -radius; i <= radius; i++) {
				if ((i * i + j2) <= r2) {
					se.positive.add(new Pixel(i, j));
				}
			}
		}

		return se;
	}
}
