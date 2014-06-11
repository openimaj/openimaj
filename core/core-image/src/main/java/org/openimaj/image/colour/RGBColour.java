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
package org.openimaj.image.colour;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenience constants and methods for RGB colours for use in MBFImages
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class RGBColour {
	/** White colour as RGB */
	public final static Float[] WHITE = { 1f, 1f, 1f };

	/** Light gray colour as RGB */
	public final static Float[] LIGHT_GRAY = { 0.75f, 0.75f, 0.75f };

	/** Gray colour as RGB */
	public final static Float[] GRAY = { 0.5f, 0.5f, 0.5f };

	/** Dark gray colour as RGB */
	public final static Float[] DARK_GRAY = { 0.25f, 0.25f, 0.25f };

	/** Black colour as RGB */
	public final static Float[] BLACK = { 0f, 0f, 0f };

	/** Red colour as RGB */
	public final static Float[] RED = { 1f, 0f, 0f };

	/** Pink colour as RGB */
	public final static Float[] PINK = { 1f, 175f / 256f, 175f / 256f };

	/** Orange colour as RGB */
	public final static Float[] ORANGE = { 1f, 200f / 256f, 0f };

	/** Yellow colour as RGB */
	public final static Float[] YELLOW = { 1f, 1f, 0f };

	/** Green colour as RGB */
	public final static Float[] GREEN = { 0f, 1f, 0f };

	/** Magenta colour as RGB */
	public final static Float[] MAGENTA = { 1f, 0f, 1f };

	/** Cyan colour as RGB */
	public final static Float[] CYAN = { 0f, 1f, 1f };

	/** Blue colour as RGB */
	public final static Float[] BLUE = { 0f, 0f, 1f };

	private RGBColour() {
	}

	/**
	 * Make an OpenImaj colour from a java.awt.Color.
	 * 
	 * @param c
	 *            the color to convert
	 * @return a colour for using as RGB MBFImages
	 */
	public static Float[] fromColor(Color c) {
		final Float r = c.getRed() / 255f;
		final Float g = c.getRed() / 255f;
		final Float b = c.getRed() / 255f;

		return new Float[] { r, g, b };
	}

	/**
	 * Get a colour from a string representation. The string may contain any of
	 * the predefined colour names described in this class (in upper, lower or
	 * mixed case), or it can be a comma separated triplet of floating point
	 * values.
	 * 
	 * @param s
	 *            the string
	 * @return the colour described by the string, or BLACK if a problem occurs.
	 */
	public static Float[] fromString(String s) {
		try {
			if (s.contains(",")) {
				final String[] parts = s.split(",");
				final Float[] col = new Float[3];
				for (int i = 0; i < 3; i++) {
					col[i] = Float.parseFloat(parts[i].trim());
				}
				return col;
			} else {
				final Field f = RGBColour.class.getField(s.toUpperCase());
				return (Float[]) f.get(null);
			}
		} catch (final Exception e) {
			return RGBColour.BLACK;
		}
	}

	/**
	 * Generate a random colour
	 * 
	 * @return a random colour
	 */
	public static Float[] randomColour() {
		final Float[] c = new Float[3];

		c[0] = (float) Math.random();
		c[1] = (float) Math.random();
		c[2] = (float) Math.random();

		return c;
	}

	/**
	 * Generate a list of random colours.
	 * 
	 * @param n
	 *            number of colours
	 * @return list of randomly generated colours
	 */
	public static List<Float[]> randomColours(int n) {
		final List<Float[]> colours = new ArrayList<Float[]>();

		for (int i = 0; i < n; i++)
			colours.add(randomColour());

		return colours;
	}

	/**
	 * Generate a range of colours from a {@link ColourMap}.
	 * 
	 * @param cm
	 *            the map
	 * @param n
	 *            number of colours
	 * @return the colours
	 */
	public static Float[][] coloursFromMap(ColourMap cm, int n) {
		final Float[][] cols = new Float[n][];
		for (int i = 0; i < n; i++) {
			final float frac = (float) i / (float) n;
			cols[i] = cm.apply(frac);
		}
		return cols;
	}

	/**
	 * Create a colour from an RGB triplet with integer values in the range
	 * 0..255.
	 * 
	 * @param r
	 *            red value 0..255
	 * @param g
	 *            green value 0..255
	 * @param b
	 *            blue value 0..255
	 * @return the colour value
	 */
	public static Float[] RGB(int r, int g, int b) {
		return new Float[] { r / 255f, g / 255f, b / 255f };
	}

	/**
	 * Create a colour from an RGB triplet with float values in the range 0..1.
	 * 
	 * @param r
	 *            red value 0..1
	 * @param g
	 *            green value 0..1
	 * @param b
	 *            blue value 0..1
	 * @return the colour value
	 */
	public static Float[] RGB(float r, float g, float b) {
		return new Float[] { r, g, b };
	}
}
