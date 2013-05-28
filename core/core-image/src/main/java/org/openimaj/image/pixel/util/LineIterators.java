package org.openimaj.image.pixel.util;

import java.util.Iterator;

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
	 * discrete endpoints.
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
	 * discrete endpoints.
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
	 * given point, with an angle given by the provided x and y deltas. <b>Note:
	 * The returned iterator is infinite; that is it won't ever end.</b>
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
}
