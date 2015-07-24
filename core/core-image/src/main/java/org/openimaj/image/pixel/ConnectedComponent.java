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
package org.openimaj.image.pixel;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.image.renderer.ScanRasteriser;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.RotatedRectangle;
import org.openimaj.math.geometry.shape.Shape;

/**
 * This class represents a connected region within an image and provides methods
 * for accessing and manipulating that region.
 * <p>
 * Nothing stops an unconnected component being constructed, but it is important
 * to realise that some methods may return unexpected results (e.g. boundary
 * tracing). If you are only dealing with unconnected pixel sets, use the
 * {@link PixelSet} superclass instead.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 */
public class ConnectedComponent extends PixelSet {
	/**
	 * For boundary representations of {@link ConnectedComponent}s, this enum
	 * determines and specifies how the boundary is calculated; either using a
	 * 4-connected rule, or an 8-connected rule.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum ConnectMode {
		/** 4-connected edges in the boundary representation */
		CONNECT_4,
		/** 8-connected edges in the boundary representation */
		CONNECT_8;

		/**
		 * Get the neighbouring pixels
		 *
		 * @param image
		 *            the image
		 * @param x
		 *            the x ordinate
		 * @param y
		 *            the y ordinate
		 * @param bgThreshold
		 *            the threshold for below which pixels should be ignored
		 * @return the neighbouring pixels
		 */
		public List<Pixel> getNeighbours(FImage image, int x, int y, float bgThreshold) {
			final List<Pixel> neighbours = new ArrayList<Pixel>(this == CONNECT_8 ? 8 : 4);

			switch (this) {
			case CONNECT_8:
				if (x > 0 && y > 0 && image.pixels[y - 1][x - 1] > bgThreshold)
					neighbours.add(new Pixel(x - 1, y - 1));
				if (x + 1 < image.width && y > 0 && image.pixels[y - 1][x + 1] > bgThreshold)
					neighbours.add(new Pixel(x + 1, y - 1));
				if (x > 0 && y + 1 < image.height && image.pixels[y + 1][x - 1] > bgThreshold)
					neighbours.add(new Pixel(x - 1, y + 1));
				if (x + 1 < image.width && y + 1 < image.height && image.pixels[y + 1][x + 1] > bgThreshold)
					neighbours.add(new Pixel(x + 1, y + 1));
				// Note : no break, so we fall through...
			case CONNECT_4:
				if (x > 0 && image.pixels[y][x - 1] > bgThreshold)
					neighbours.add(new Pixel(x - 1, y));
				if (x + 1 < image.width && image.pixels[y][x + 1] > bgThreshold)
					neighbours.add(new Pixel(x + 1, y));
				if (y > 0 && image.pixels[y - 1][x] > bgThreshold)
					neighbours.add(new Pixel(x, y - 1));
				if (y + 1 < image.height && image.pixels[y + 1][x] > bgThreshold)
					neighbours.add(new Pixel(x, y + 1));
				break;
			}

			return neighbours;
		}
	}

	/**
	 * Default constructor. Has an empty implementation.
	 */
	public ConnectedComponent() {
		super();
	}

	/**
	 * Construct a new connected component from the given shape. Pixels are
	 * created for the connected component that lie within the shape; but these
	 * pixels will not have specific values as they are not associated to any
	 * particular image.
	 *
	 * @param shape
	 *            The shape from which to construct the connected component.
	 */
	public ConnectedComponent(Shape shape) {
		fromShape(shape);
	}

	/**
	 * Construct a new connected component from the given polygon. Pixels are
	 * created for the connected component that lie within the shape; but these
	 * pixels will not have specific values as they are not associated to any
	 * particular image.
	 *
	 * @param poly
	 *            The polygon from which to construct the connected component.
	 */
	public ConnectedComponent(Polygon poly) {
		// FIXME: this can be improved if the scan-fill algorithm
		// could be adapted to deal with holes.
		if (poly.getNumInnerPoly() == 1) {
			ScanRasteriser.scanFill(poly.points, new ScanLineListener() {
				@Override
				public void process(int x1, int x2, int y) {
					for (int x = x1; x <= x2; x++)
						ConnectedComponent.this.addPixel(x, y);
				}
			});
		} else {
			fromShape(poly);
		}
	}

	/**
	 * Construct a rectangular {@link ConnectedComponent}. Pixels are created
	 * for the area within the rectangle but these will not have any specific
	 * value as they are not associated to any particular image.
	 *
	 * @param x
	 *            The top-left x-coordinate of the rectangle
	 * @param y
	 *            The top-left y-coordinate of the rectangle
	 * @param w
	 *            The width of the rectangle
	 * @param h
	 *            The height of the rectangle
	 */
	public ConnectedComponent(int x, int y, int w, int h) {
		super(x, y, w, h);
	}

	/**
	 * Constructs a connected component from a mask image. Pixels are created
	 * for areas within the mask image that are non-zero. Note that this may
	 * result in a connected component definition that is unconnected and some
	 * methods may not return expected results.
	 *
	 * @param img
	 *            The mask image to construct a connected component from.
	 */
	public ConnectedComponent(int[][] img) {
		super(img);
	}

	/**
	 * Constructs a connected component from a mask image. Pixels are created
	 * for areas within the mask image that are above the given threshold. Note
	 * that this may result in a connected component definition that is
	 * unconnected and some methods may not return expected results.
	 *
	 * @param mask
	 *            The mask image to construct a connected component from.
	 * @param thresh
	 *            the threshold value.
	 */
	public ConnectedComponent(FImage mask, float thresh) {
		super(mask, thresh);
	}

	/**
	 * Construct a connected component from the given set of {@link Pixel}s. The
	 * pixels are shallow copied into the connected component. If the pixels do
	 * not form a connected component then some methods in this class may not
	 * return expected results.
	 *
	 * @param pixels
	 *            A {@link Set} of {@link Pixel}s.
	 */
	public ConnectedComponent(Set<Pixel> pixels) {
		super(pixels);
	}

	/**
	 * Estimates how many vertices are required to encode the boundary with the
	 * given smoothness and window width. Basically it determines how many
	 * strong peaks there are in the boundary, where the peak strength is
	 * determined by the two parameters. The window width determines how many
	 * boundary points are considered when iterating through the binary, while
	 * the smoothness determines how smooth the resulting boundary
	 * representation can be.
	 *
	 * @param smoothWidth
	 *            The smoothness of the resulting boundary
	 * @param windowWidth
	 *            The number of edge points to consider
	 * @return The estimated number of vertices required to encode the boundary.
	 */
	public int estimateNumberOfVertices(int smoothWidth, int windowWidth) {
		final TFloatArrayList distances = calculateBoundaryDistanceFromCentre();

		if (smoothWidth % 2 == 0)
			smoothWidth++;
		if (windowWidth % 2 == 0)
			windowWidth++;

		final int n = distances.size();
		final float[] kernel = new float[windowWidth];
		final float[] response = new float[n];

		for (int i = 0; i < n; i++) {
			float sum = 0;
			for (int j = 0; j < smoothWidth; j++) {
				int k = i + j - (smoothWidth / 2);

				if (k < 0) {
					k = n + k;
				} else if (k >= n) {
					k = k - n;
				}
				sum += distances.get(k);
			}
			distances.set(i, sum / smoothWidth);
		}

		for (int i = 0; i < windowWidth; i++)
			kernel[i] = -(windowWidth / 2) + i;

		for (int i = 0; i < n; i++) {
			float sum = 0;
			for (int j = 0; j < windowWidth; j++) {
				int k = i + j - (windowWidth / 2);

				if (k < 0) {
					k = n + k;
				} else if (k >= n) {
					k = k - n;
				}
				sum += kernel[j] * distances.get(k);
			}
			response[i] = sum;
		}

		int peaks = 0;
		for (int i = 1; i < n; i++) {
			if (response[i - 1] >= 0 && response[i] < 0)
				peaks++;
		}
		if (response[n - 1] >= 0 && response[0] < 0)
			peaks++;

		return peaks;
	}

	/**
	 *
	 * @param P0
	 * @param P1
	 * @param P2
	 * @return
	 */
	protected int isLeft(Pixel P0, Pixel P1, Pixel P2) {
		return (P1.x - P0.x) * (P2.y - P0.y) - (P2.x - P0.x) * (P1.y - P0.y);
	}

	/**
	 * Calculates the convex hull polygon for this connected component using
	 * Andrew's montone algorithm.
	 *
	 * @return The polygon defining the convex hull shape for this component.
	 */
	public Polygon calculateConvexHull() {
		return calculateConvexHull_AndrewsMontone();
	}

	/**
	 * Calculate the ratio of the area of the given connected component to the
	 * area of the connected component. This does not consider whether the areas
	 * overlap.
	 *
	 * @param ch
	 *            The connected component to test.
	 * @return The area ratio of the given connected component to this connected
	 *         component.
	 */
	public double calculateAreaRatio(ConnectedComponent ch) {
		return (double) calculateArea() / (double) ch.calculateArea();
	}

	/**
	 * Calculate the ratio of the area of the given polygon to the area of this
	 * connected component. This does not consider whether the areas overlap.
	 *
	 * @param ch
	 *            The polygon to test again.
	 * @return The area ratio of the given polygon to this connected component.
	 */
	public double calculateAreaRatio(Polygon ch) {
		return calculateAreaRatio(new ConnectedComponent(ch));
	}

	/**
	 * Calculate the ratio of the area of this component's convex hull to the
	 * actual area of this connected component. This gives an idea of how well
	 * the calculated convex hull fits the component. The value returned is a
	 * percentage (0-1).
	 *
	 * @return The area ratio of this component's convex hull its area.
	 */
	public double calculatePercentageConvexHullFit() {
		return calculateAreaRatio(calculateConvexHull());
	}

	/**
	 * Calculate convex hull using Melkman's algorithm. Based on
	 * http://softsurfer.com/Archive/algorithm_0203/algorithm_0203.htm
	 * <p>
	 * Copyright 2001, softSurfer (www.softsurfer.com) This code may be freely
	 * used and modified for any purpose providing that this copyright notice is
	 * included with it. SoftSurfer makes no warranty for this code, and cannot
	 * be held liable for any real or imagined damage resulting from its use.
	 * Users of this code must verify correctness for their application.
	 *
	 * @param V
	 *            List of pixels containing within the region
	 * @return A polygon defining the shape of the convex hull
	 */
	protected Polygon calculateConvexHull_Melkman(List<Pixel> V) {
		// initialize a deque D[] from bottom to top so that the
		// 1st three vertices of V[] are a counterclockwise triangle
		final int n = V.size();
		final Pixel[] D = new Pixel[2 * n + 1];
		int bot = n - 2, top = bot + 3; // initial bottom and top deque indices
		D[bot] = D[top] = V.get(2); // 3rd vertex is at both bot and top
		if (isLeft(V.get(0), V.get(1), V.get(2)) > 0) {
			D[bot + 1] = V.get(0);
			D[bot + 2] = V.get(1); // ccw vertices are: 2,0,1,2
		} else {
			D[bot + 1] = V.get(1);
			D[bot + 2] = V.get(0); // ccw vertices are: 2,1,0,2
		}

		// compute the hull on the deque D[]
		for (int i = 3; i < n; i++) { // process the rest of vertices
			// test if next vertex is inside the deque hull
			if ((isLeft(D[bot], D[bot + 1], V.get(i)) > 0) &&
					(isLeft(D[top - 1], D[top], V.get(i)) > 0))
				continue; // skip an interior vertex

			// incrementally add an exterior vertex to the deque hull
			// get the rightmost tangent at the deque bot
			while (isLeft(D[bot], D[bot + 1], V.get(i)) <= 0)
				++bot; // remove bot of deque
			D[--bot] = V.get(i); // insert V[i] at bot of deque

			// get the leftmost tangent at the deque top
			while (isLeft(D[top - 1], D[top], V.get(i)) <= 0)
				--top; // pop top of deque
			D[++top] = V.get(i); // push V[i] onto top of deque
		}

		// transcribe deque D[] to the output hull array H[]
		final Polygon H = new Polygon();
		final List<Point2d> vertices = H.getVertices();
		for (int h = 0; h <= (top - bot); h++)
			vertices.add(D[bot + h]);

		return H;
	}

	/**
	 * Calculate the convex hull using Andrew's monotone chain 2D convex hull
	 * algorithm.
	 *
	 * @return A polygon defining the shape of the convex hull.
	 */
	protected Polygon calculateConvexHull_AndrewsMontone() {
		if (this.calculateArea() == 1) {
			return new Polygon(this.pixels.iterator().next());
		}

		final List<Pixel> P = new ArrayList<Pixel>();

		// sort
		int minx = Integer.MAX_VALUE, maxx = Integer.MIN_VALUE, miny = Integer.MAX_VALUE, maxy = Integer.MIN_VALUE;

		for (final Pixel p : pixels) {
			if (p.x < minx)
				minx = p.x;
			if (p.x > maxx)
				maxx = p.x;
			if (p.y < miny)
				miny = p.y;
			if (p.y > maxy)
				maxy = p.y;
		}

		for (int x = minx; x <= maxx; x++) {
			for (int y = miny; y <= maxy; y++) {
				final Pixel p = new Pixel(x, y);
				if (pixels.contains(p))
					P.add(p);
			}
		}

		// the output array H[] will be used as the stack
		int bot = 0, top = (-1); // indices for bottom and top of the stack
		int i; // array scan index
		final int n = P.size();

		final Polygon poly = new Polygon();
		final Pixel[] H = new Pixel[P.size()];

		// Get the indices of points with min x-coord and min|max y-coord
		final int minmin = 0;
		int minmax;
		final float xmin = P.get(0).x;
		for (i = 1; i < n; i++)
			if (P.get(i).x != xmin)
				break;
		minmax = i - 1;
		if (minmax == n - 1) { // degenerate case: all x-coords == xmin
			H[++top] = P.get(minmin);
			if (P.get(minmax).y != P.get(minmin).y) // a nontrivial segment
				H[++top] = P.get(minmax);
			H[++top] = P.get(minmin); // add polygon endpoint

			for (int k = 0; k < top + 1; k++)
				poly.getVertices().add(H[k]);
			return poly;
		}

		// Get the indices of points with max x-coord and min|max y-coord
		int maxmin;
		final int maxmax = n - 1;
		final float xmax = P.get(n - 1).x;
		for (i = n - 2; i >= 0; i--)
			if (P.get(i).x != xmax)
				break;
		maxmin = i + 1;

		// Compute the lower hull on the stack H
		H[++top] = P.get(minmin); // push minmin point onto stack
		i = minmax;
		while (++i <= maxmin) {
			// the lower line joins P[minmin] with P[maxmin]
			if (isLeft(P.get(minmin), P.get(maxmin), P.get(i)) >= 0 && i < maxmin)
				continue; // ignore P[i] above or on the lower line

			while (top > 0) // there are at least 2 points on the stack
			{
				// test if P[i] is left of the line at the stack top
				if (isLeft(H[top - 1], H[top], P.get(i)) > 0)
					break; // P[i] is a new hull vertex
				else
					top--; // pop top point off stack
			}
			H[++top] = P.get(i); // push P[i] onto stack
		}

		// Next, compute the upper hull on the stack H above the bottom hull
		if (maxmax != maxmin) // if distinct xmax points
			H[++top] = P.get(maxmax); // push maxmax point onto stack
		bot = top; // the bottom point of the upper hull stack
		i = maxmin;
		while (--i >= minmax) {
			// the upper line joins P[maxmax] with P[minmax]
			if (isLeft(P.get(maxmax), P.get(minmax), P.get(i)) >= 0 && i > minmax)
				continue; // ignore P[i] below or on the upper line

			while (top > bot) // at least 2 points on the upper stack
			{
				// test if P[i] is left of the line at the stack top
				if (isLeft(H[top - 1], H[top], P.get(i)) > 0)
					break; // P[i] is a new hull vertex
				else
					top--; // pop top point off stack
			}
			H[++top] = P.get(i); // push P[i] onto stack
		}
		if (minmax != minmin)
			H[++top] = P.get(minmin); // push joining endpoint onto stack

		for (int k = 0; k < top + 1; k++)
			poly.getVertices().add(H[k]);
		return poly;
	}

	/**
	 * Returns the next edge pixel when tracing a boundary in a 4-connected
	 * system.
	 *
	 * @param current
	 *            The current pixel
	 * @param lastdir
	 *            The last direction traversed
	 * @return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW4(Pixel current, int lastdir) {
		return nextEdgePixelACW4(current, lastdir, null);
	}

	/**
	 * Returns the next edge pixel when tracing a boundary in a 4-connected
	 * system.
	 *
	 * @param current
	 *            The current pixel
	 * @param lastdir
	 *            The last direction traversed
	 * @param outer
	 *            A list to fill with the outer boundary
	 * @return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW4(Pixel current, int lastdir, List<Pixel> outer) {
		final int startdir = (lastdir + 3) % 4;

		final Pixel target[] = {
				new Pixel(current.x + 1, current.y), // dir 0
				new Pixel(current.x, current.y - 1), // dir 1
				new Pixel(current.x - 1, current.y), // dir 2
				new Pixel(current.x, current.y + 1) // dir 3
		};

		for (int i = 0; i < 4; i++) {
			int dir = startdir + i;
			if (dir >= 4)
				dir -= 4;

			if (pixels.contains(target[dir])) {
				return target[dir];
			} else if (outer != null)
				outer.add(target[dir]);
		}

		return current;
	}

	/**
	 * Returns the next edge pixel when tracing a boundary in an 8-connected
	 * system.
	 *
	 * @param current
	 *            The current pixel
	 * @param lastdir
	 *            The last direction traversed
	 * @return The next pixel in the edge
	 */
	protected Pixel nextEdgePixelACW8(Pixel current, int lastdir) {
		final int startdir = (lastdir + 7 - (lastdir % 2)) % 8;

		final Pixel target[] = {
				new Pixel(current.x + 1, current.y), // dir 0
				new Pixel(current.x + 1, current.y - 1), // dir 1
				new Pixel(current.x, current.y - 1), // dir 2
				new Pixel(current.x - 1, current.y - 1), // dir 3
				new Pixel(current.x - 1, current.y), // dir 4
				new Pixel(current.x - 1, current.y + 1), // dir 5
				new Pixel(current.x, current.y + 1), // dir 6
				new Pixel(current.x + 1, current.y + 1) // dir 7
		};

		for (int i = 0; i < 8; i++) {
			int dir = startdir + i;
			if (dir >= 8)
				dir -= 8;

			if (pixels.contains(target[dir]))
				return target[dir];
		}

		return current;
	}

	/**
	 * For the two pixels, determines the 4-connected chain code that will move
	 * from the first pixel to the next. If the pixels are not adjacent the
	 * method returns -1.
	 *
	 * @param current
	 *            The current pixel
	 * @param next
	 *            The next pixel
	 * @return The Freeman 4-connected chain code
	 */
	protected int code4(Pixel current, Pixel next) {
		if (current.x - 1 == next.x)
			return 2;
		if (current.y + 1 == next.y)
			return 3;
		if (current.x + 1 == next.x)
			return 0;
		if (current.y - 1 == next.y)
			return 1;

		return -1;
	}

	/**
	 * For the two pixels, determines the 8-connected chain code that will move
	 * from the first pixel to the next. If the pixels are not adjacent or
	 * diagonal then the method returns -1.
	 *
	 * @param current
	 *            The current pixel
	 * @param next
	 *            The next pixel
	 * @return The Freeman 8-connected chain code
	 */
	protected int code8(Pixel current, Pixel next) {
		if (current.x + 1 == next.x && current.y == next.y)
			return 0;
		if (current.x + 1 == next.x && current.y - 1 == next.y)
			return 1;
		if (current.x == next.x && current.y - 1 == next.y)
			return 2;
		if (current.x - 1 == next.x && current.y - 1 == next.y)
			return 3;
		if (current.x - 1 == next.x && current.y == next.y)
			return 4;
		if (current.x - 1 == next.x && current.y + 1 == next.y)
			return 5;
		if (current.x == next.x && current.y + 1 == next.y)
			return 6;
		if (current.x + 1 == next.x && current.y + 1 == next.y)
			return 7;

		return -1;
	}

	/**
	 * Converts this connected component into a {@link Polygon} representation
	 * by performing a 4-connected boundary trace and converting the resulting
	 * pixels into vertices.
	 *
	 * @return A {@link Polygon} representing the inner boundary of the
	 *         component.
	 */
	public Polygon toPolygon() {
		final Polygon poly = new Polygon();

		for (final Pixel p : getInnerBoundary(ConnectMode.CONNECT_4))
			poly.getVertices().add(p);

		return poly;
	}

	/**
	 * Returns an ordered list of pixels that are on the inner boundary of the
	 * shape. That means that the boundary points are all within the region. The
	 * list is ordered such that adjacent boundary pixels are adjacent in the
	 * list. The first pixel in the list should be the same as
	 * {@link #topLeftMostPixel()}.
	 *
	 * @param mode
	 *            The {@link ConnectMode} to use.
	 * @return An ordered list of pixels defining the inner boundary
	 */
	public List<Pixel> getInnerBoundary(ConnectMode mode) {
		final List<Pixel> pset = new ArrayList<Pixel>();

		final Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir;

		switch (mode) {
		case CONNECT_4:
			dir = 3;
			while (true) {
				next = nextEdgePixelACW4(current, dir);
				if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
					break;
				}

				dir = code4(current, next);
				pset.add(current);
				current = next;
			}
			break;
		case CONNECT_8:
			dir = 7;
			while (true) {
				next = nextEdgePixelACW8(current, dir);

				if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
					break;
				}

				dir = code8(current, next);
				pset.add(current);
				current = next;
			}
			break;
		}

		return pset;
	}

	/**
	 * Returns an ordered list of pixels that are on the outer boundary of the
	 * shape. That means that the boundary points are all outside of the region.
	 * The list is ordered such that adjacent boundary pixels are adjacent in
	 * the list.
	 *
	 * @return An ordered list of pixels defining the outer boundary
	 */
	public List<Pixel> getOuterBoundary() {
		final List<Pixel> pset = new ArrayList<Pixel>();
		List<Pixel> outer = new ArrayList<Pixel>();

		final Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir = 3;

		while (true) {
			next = nextEdgePixelACW4(current, dir, outer);
			if (pset.size() >= 2 && next.equals(pset.get(1)) && current.equals(start)) {
				break;
			}
			if (this.pixels.size() == 1)
				break;

			dir = code4(current, next);
			pset.add(current);
			current = next;
		}

		if (outer.size() > 4) {
			for (int i = 3; i > 0; i--) {
				boolean found = true;
				for (int j = 0; j < i; j++) {
					if (!outer.get(j).equals(outer.get(outer.size() - i + j))) {
						found = false;
						break;
					}
				}

				if (found) {
					outer = outer.subList(0, outer.size() - i);
					break;
				}
			}
		}

		return outer;
	}

	/**
	 * Calculates the Freeman chaincode for this connected component. The
	 * chaincode is returned as a list of direction codes defining the path of
	 * the boundary.
	 * <p>
	 * The Freeman chaincode is a means for encoding the paths between nodes on
	 * the boundary of a shape, thereby reducing the encoding of a shape to a
	 * single start coordinate and a list of direction codes. The Freeman
	 * direction codes are 0-4 for 4-connected boundaries and 0-7 for
	 * 8-connected boundaries.
	 *
	 * @param mode
	 *            4 or 8 connectivity
	 * @return the chain code
	 */
	public TIntArrayList freemanChainCode(ConnectMode mode) {
		final TIntArrayList code = new TIntArrayList();

		final Pixel start = topLeftMostPixel();
		Pixel current = start;
		Pixel next;
		int dir;

		switch (mode) {
		case CONNECT_8:
			dir = 7;
			while (!(next = nextEdgePixelACW8(current, dir)).equals(start)) {
				dir = code8(current, next);
				code.add(dir);
				current = next;
			}
			code.add(code8(current, next));
			break;
		case CONNECT_4:
			dir = 3;
			while (!(next = nextEdgePixelACW4(current, dir)).equals(start)) {
				dir = code4(current, next);
				code.add(dir);
				current = next;
			}
			code.add(code4(current, next));
			break;
		}

		return code;
	}

	/**
	 * Process the given set of connected components with the given
	 * {@link ConnectedComponentProcessor}.
	 *
	 * @param components
	 *            The components to process
	 * @param p
	 *            The process to process the components with
	 */
	public static void process(Collection<ConnectedComponent> components, ConnectedComponentProcessor p) {
		for (final ConnectedComponent c : components)
			c.processInplace(p);
	}

	/**
	 * Process this connected component with the given
	 * {@link ConnectedComponentProcessor} and returns a new component
	 * containing the result.
	 *
	 * @param p
	 *            The processor to process this component with
	 * @return A new component containing the result.
	 */
	public ConnectedComponent process(ConnectedComponentProcessor p) {
		final ConnectedComponent tmp = clone();
		p.process(tmp);
		return tmp;
	}

	/**
	 * Process a connected component with the given
	 * {@link ConnectedComponentProcessor}. Side-affects this component.
	 *
	 * @param p
	 *            The processor to process this component with
	 * @return A reference to this connected component.
	 */
	public ConnectedComponent processInplace(ConnectedComponentProcessor p) {
		p.process(this);
		return this;
	}

	/**
	 * Performs a flood fill on the given image starting at the given pixel. The
	 * result of the flood fill is returned as a {@link ConnectedComponent}.
	 *
	 * @param image
	 *            The image on which to perform a flood fill
	 * @param start
	 *            The start pixel to begin the flood
	 * @return A ConnectedComponent containing the resulting region.
	 */
	public static ConnectedComponent floodFill(FImage image, Pixel start) {
		final ConnectedComponent cc = new ConnectedComponent();
		final float val = image.pixels[start.y][start.x];
		final int[][] output = new int[image.height][image.width];

		// Flood-fill (node, target-color, replacement-color):
		// 1. Set Q to the empty queue.
		// Queue<Pixel> queue = new LinkedList<Pixel>();
		final LinkedHashSet<Pixel> queue = new LinkedHashSet<Pixel>();

		// 2. If the color of node is not equal to target-color, return.
		if (image.pixels[start.y][start.x] > val)
			return cc;

		// 3. Add node to Q.
		queue.add(start);

		// 4. For each element n of Q:
		while (queue.size() > 0) {
			// Pixel n = queue.poll();
			final Pixel n = queue.iterator().next();
			queue.remove(n);

			// 5. If the color of n is equal to target-color:
			if (image.pixels[n.y][n.x] <= val && output[n.y][n.x] != 1) {
				// 6. Set w and e equal to n.
				int e = n.x, w = n.x;
				// 7. Move w to the west until the color of the node to the west
				// of w no longer matches target-color.
				while (w > 0 && image.pixels[n.y][w - 1] <= val)
					w--;

				// 8. Move e to the east until the color of the node to the east
				// of e no longer matches target-color.
				while (e < image.width - 1 && image.pixels[n.y][e + 1] <= val)
					e++;

				// 9. Set the color of nodes between w and e to
				// replacement-color.
				for (int i = w; i <= e; i++) {
					output[n.y][i] = 1;
					cc.addPixel(i, n.y);

					// 10. For each node n between w and e:
					final int north = n.y - 1;
					final int south = n.y + 1;
					// 11. If the color of the node to the north of n is
					// target-color, add that node to Q.
					if (north >= 0 && image.pixels[north][i] <= val && output[north][i] != 1)
						queue.add(new Pixel(i, north));
					// If the color of the node to the south of n is
					// target-color, add that node to Q.
					if (south < image.height && image.pixels[south][i] <= val && output[south][i] != 1)
						queue.add(new Pixel(i, south));
				}
				// 12. Continue looping until Q is exhausted.
			}
		}
		// 13. Return.
		return cc;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Performs a deep copy on the connected component; that is, all pixels are
	 * also cloned.
	 */
	@Override
	public ConnectedComponent clone() {
		ConnectedComponent tmp;
		try {
			tmp = (ConnectedComponent) super.clone();
			tmp.pixels = new HashSet<Pixel>();

			for (final Pixel p : pixels)
				tmp.pixels.add(p.clone());

			return tmp;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Calculates the distance from the centroid of every pixel on the
	 * 8-connected boundary of this component. Returns a {@link TFloatArrayList}
	 * that contains the list of distances (in order of the boundary).
	 *
	 * @return A list ({@link TFloatArrayList}) of distances of boundary points
	 *         to the centroid.
	 */
	public TFloatArrayList calculateBoundaryDistanceFromCentre() {
		final TFloatArrayList distances = new TFloatArrayList();
		final List<Pixel> bound = getInnerBoundary(ConnectMode.CONNECT_8);
		final double[] centroid = calculateCentroid();

		for (final Pixel p : bound) {
			final float dist = (float) Math.sqrt(((centroid[0] - p.x) * ((centroid[0] - p.x))) +
					((centroid[1] - p.y) * ((centroid[1] - p.y))));
			distances.add(dist);
		}

		return distances;
	}

	@Override
	public String asciiHeader() {
		return "ConnectedComponent";
	}

	@Override
	public byte[] binaryHeader() {
		return "CC".getBytes();
	}

	/**
	 * Compute the aspect ratio of the oriented bounding box.
	 *
	 * @return the aspect ratio of the oriented bounding box.
	 */
	public double calculateOrientatedBoundingBoxAspectRatio() {
		final RotatedRectangle r = toPolygon().minimumBoundingRectangle();

		return r.height / r.width;
	}

	/**
	 * Calculates the polygon that defines the minimum bounding box that best
	 * fits the connected component, at whatever angle that may be.
	 *
	 * @return A {@link RotatedRectangle} that defines the minimum bounding box.
	 */
	public RotatedRectangle calculateOrientatedBoundingBox() {
		return this.toPolygon().minimumBoundingRectangle();
	}
}
