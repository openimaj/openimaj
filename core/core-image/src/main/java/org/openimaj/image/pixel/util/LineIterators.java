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
package org.openimaj.image.pixel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.pixel.Pixel;

/**
 * Iterators for producing discrete pixel positions along a line.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LineIterators {
	/**
	 * Pixel iterator based on Bresenham's algorithm for a line between two
	 * discrete endpoints. <b>The pixel returned by the iterator will always be
	 * the same object for efficiency; if you need to hold on to it, you should
	 * clone it first.<b>
	 * 
	 * @param start
	 *            the coordinate of the start point
	 * @param end
	 *            the coordinate of the end point
	 * @return an iterator over the pixels in the line
	 */
	public static Iterator<Pixel> bresenham(final Pixel start, final Pixel end) {
		return bresenham(start.x, start.y, end.x, end.y);
	}

	/**
	 * Pixel iterator based on Bresenham's algorithm for a line between two
	 * discrete endpoints. <b>The pixel returned by the iterator will always be
	 * the same object for efficiency; if you need to hold on to it, you should
	 * clone it first.<b>
	 * 
	 * @param x0
	 *            the x-ordinate of the start point
	 * @param y0
	 *            the y-ordinate of the start point
	 * @param x1
	 *            the x-ordinate of the end point
	 * @param y1
	 *            the y-ordinate of the end point
	 * @return an iterator over the pixels in the line
	 */
	public static Iterator<Pixel> bresenham(final int x0, final int y0, final int x1, final int y1) {
		return new Iterator<Pixel>() {
			int x = x0;
			int y = y0;
			final int dx = Math.abs(x1 - x);
			final int dy = Math.abs(y1 - y);
			final int sx = (x < x1) ? 1 : -1;
			final int sy = (y < y1) ? 1 : -1;
			int err = dx - dy;
			Pixel p = new Pixel();
			boolean finished = false;

			@Override
			public boolean hasNext() {
				return !finished;
			}

			@Override
			public Pixel next() {
				p.x = x;
				p.y = y;

				if (x == x1 && y == y1)
					finished = true;

				final int e2 = 2 * err;
				if (e2 > -dy) {
					err = err - dy;
					x = x + sx;
				}
				if (e2 < dx) {
					err = err + dx;
					y = y + sy;
				}

				return p;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Pixel iterator based on Bresenham's algorithm for a line starting at a
	 * given point, with an angle given by the provided x and y deltas. <b>The
	 * pixel returned by the iterator will always be the same object for
	 * efficiency; if you need to hold on to it, you should clone it first.<b>
	 * <b>Note: The returned iterator is infinite; that is it won't ever
	 * end.</b>
	 * 
	 * @param x0
	 *            the x-ordinate of the start point
	 * @param y0
	 *            the y-ordinate of the start point
	 * @param fdx
	 *            the x-gradient
	 * @param fdy
	 *            the y-gradient
	 * @return an iterator over the pixels in the line
	 */
	public static Iterator<Pixel> bresenham(final int x0, final int y0, final float fdx, final float fdy) {
		return new Iterator<Pixel>() {
			int x = x0;
			int y = y0;
			final float dx = Math.abs(fdx);
			final float dy = Math.abs(fdy);
			final int sx = (fdx > 0) ? 1 : -1;
			final int sy = (fdy > 0) ? 1 : -1;
			float err = dx - dy;
			Pixel p = new Pixel();

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Pixel next() {
				p.x = x;
				p.y = y;

				final float e2 = 2 * err;
				if (e2 > -dy) {
					err = err - dy;
					x = x + sx;
				}
				if (e2 < dx) {
					err = err + dx;
					y = y + sy;
				}

				return p;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Generate the pixels for the supercover of the line between two points.
	 * Based directly on code from Eugen Dedu.
	 * 
	 * @see "http://lifc.univ-fcomte.fr/~dedu/projects/bresenham/index.html"
	 * 
	 * @param x1
	 *            the x-ordinate of the start point
	 * @param y1
	 *            the y-ordinate of the start point
	 * @param x2
	 *            the x-ordinate of the end point
	 * @param y2
	 *            the y-ordinate of the end point
	 * 
	 * @return the iterator of pixels representing the supercover line
	 */
	public static Iterator<Pixel> supercover(int x1, int y1, int x2, int y2) {
		return supercoverAsList(x1, y1, x2, y2).iterator();
	}

	/**
	 * Generate the pixels for the supercover of the line between two points.
	 * Based directly on code from Eugen Dedu.
	 * 
	 * @see "http://lifc.univ-fcomte.fr/~dedu/projects/bresenham/index.html"
	 * 
	 * @param x1
	 *            the x-ordinate of the start point
	 * @param y1
	 *            the y-ordinate of the start point
	 * @param x2
	 *            the x-ordinate of the end point
	 * @param y2
	 *            the y-ordinate of the end point
	 * 
	 * @return the list of pixels representing the supercover line
	 */
	public static List<Pixel> supercoverAsList(int x1, int y1, int x2, int y2) {
		final List<Pixel> pixels = new ArrayList<Pixel>();

		int ystep, xstep; // the step on y and x axis
		int error; // the error accumulated during the increment
		int errorprev; // *vision the previous value of the error variable
		int y = y1, x = x1; // the line points
		int ddy, ddx; // compulsory variables: the double values of dy and dx
		int dx = x2 - x1;
		int dy = y2 - y1;

		pixels.add(new Pixel(x1, y1)); // first point
		// NB the last point can't be here, because of its previous point (which
		// has to be verified)
		if (dy < 0) {
			ystep = -1;
			dy = -dy;
		} else
			ystep = 1;
		if (dx < 0) {
			xstep = -1;
			dx = -dx;
		} else
			xstep = 1;

		ddy = 2 * dy; // work with double values for full precision
		ddx = 2 * dx;
		if (ddx >= ddy) { // first octant (0 <= slope <= 1)
			// compulsory initialization (even for errorprev, needed when
			// dx==dy)
			errorprev = error = dx; // start in the middle of the square
			for (int i = 0; i < dx; i++) { // do not use the first point
											// (already
											// done)
				x += xstep;
				error += ddy;
				if (error > ddx) { // increment y if AFTER the middle ( > )
					y += ystep;
					error -= ddx;
					// three cases (octant == right->right-top for directions
					// below):
					if (error + errorprev < ddx) // bottom square also
						pixels.add(new Pixel(x, y - ystep));
					else if (error + errorprev > ddx) // left square also
						pixels.add(new Pixel(x - xstep, y));
					else { // corner: bottom and left squares also
						pixels.add(new Pixel(x, y - ystep));
						pixels.add(new Pixel(x - xstep, y));
					}
				}
				pixels.add(new Pixel(x, y));
				errorprev = error;
			}
		} else { // the same as above
			errorprev = error = dy;
			for (int i = 0; i < dy; i++) {
				y += ystep;
				error += ddx;
				if (error > ddy) {
					x += xstep;
					error -= ddy;
					if (error + errorprev < ddy)
						pixels.add(new Pixel(x - xstep, y));
					else if (error + errorprev > ddy)
						pixels.add(new Pixel(x, y - ystep));
					else {
						pixels.add(new Pixel(x - xstep, y));
						pixels.add(new Pixel(x, y - ystep));
					}
				}
				pixels.add(new Pixel(x, y));
				errorprev = error;
			}
		}

		return pixels;
	}

	/**
	 * Generate the pixels for the supercover of the line between two points.
	 * Based directly on code from Eugen Dedu.
	 * 
	 * @see "http://lifc.univ-fcomte.fr/~dedu/projects/bresenham/index.html"
	 * 
	 * @param start
	 *            the coordinate of the start point
	 * @param end
	 *            the coordinate of the end point
	 * 
	 * @return the list of pixels representing the supercover line
	 */
	public static List<Pixel> supercoverAsList(Pixel start, Pixel end) {
		return supercoverAsList(start.x, start.y, end.x, end.y);
	}
}
