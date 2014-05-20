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
package org.openimaj.image.contour;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;

/**
 * A pixel direction
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
enum Direction {
	NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
	static int[] dirx = new int[] { 0, 1, 1, 1, 0, -1, -1, -1 };
	static int[] diry = new int[] { -1, -1, 0, 1, 1, 1, 0, -1 };
	static Direction[] entry = new Direction[] {
			WEST, WEST, NORTH, NORTH, EAST, EAST, SOUTH, SOUTH
	};
	static Direction[] ccentry = new Direction[] {
			EAST, SOUTH, SOUTH, WEST, WEST, NORTH, NORTH, EAST
	};

	/**
	 * @return the direction clockwise from this direction
	 */
	public Direction clockwise() {
		final Direction[] vals = Direction.values();
		final Direction dir = vals[(ordinal() + 1) % vals.length];

		return dir;
	}

	/**
	 * @return the direction counterClockwise from this direction
	 */
	public Direction counterClockwise() {
		final Direction[] vals = Direction.values();
		final int desired = ordinal() - 1;
		final Direction dir = vals[desired == -1 ? desired + vals.length : desired];
		return dir;
	}

	/**
	 * @param img
	 * @param point
	 * @return whether the pixel point to from this point on this img in this
	 *         direction is active
	 */
	public Pixel active(FImage img, Pixel point) {
		final int ord = ordinal();
		final int yy = point.y + diry[ord];
		final int xx = point.x + dirx[ord];
		if (xx < 0 || xx >= img.width || yy < 0 || yy >= img.height)
			return null;
		final float pix = img.pixels[yy][xx];
		return pix != 0 ? new Pixel(xx, yy) : null;
	}

	/**
	 * @return the direction which this pixel was entered from if it were
	 *         entered clockwise
	 */
	public Direction clockwiseEntryDirection() {
		return entry[ordinal()];
	}

	/**
	 * @return the direction which this pixel was entered from if it were
	 *         entered clockwise
	 */
	public Direction counterClockwiseEntryDirection() {
		return ccentry[ordinal()];
	}

	/**
	 * @param from
	 * @param to
	 * @return the DIRECTION from and to adjacent pixels
	 */
	public static Direction fromTo(Pixel from, Pixel to) {
		if (from.equals(to))
			return null;
		if (from.y == to.y) {
			if (from.x < to.x)
				return EAST;
			else
				return WEST;
		}
		else if (from.y < to.y) {
			if (from.x == to.x)
				return SOUTH;
			else if (from.x < to.x)
				return SOUTH_EAST;
			else
				return SOUTH_WEST;
		}
		else {
			if (from.x == to.x)
				return NORTH;
			if (from.x < to.x)
				return NORTH_EAST;
			else
				return NORTH_WEST;
		}
	}

	/**
	 * @param from
	 * @return the pixel in the direction from some point
	 */
	public Pixel pixel(Pixel from) {
		return new Pixel(from.x + dirx[ordinal()], from.y + diry[ordinal()]);
	}
}
