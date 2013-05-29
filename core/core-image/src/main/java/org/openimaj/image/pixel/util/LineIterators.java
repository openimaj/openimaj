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
