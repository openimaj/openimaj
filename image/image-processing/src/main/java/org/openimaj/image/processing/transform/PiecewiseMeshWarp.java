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
package org.openimaj.image.processing.transform;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.Image;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.renderer.ScanRasteriser;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Implementation of a piecewise warp. The warp can be piecewise affine,
 * piecewise homographic or a mixture of the two. Basically this means you can
 * warp images represented by triangles, quads or a mixture of the two.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the pixel type
 * @param <I>
 *            the actual image of the concrete subclass
 */
public class PiecewiseMeshWarp<T, I extends Image<T, I>> implements ImageProcessor<I> {
	List<Pair<Shape>> matchingRegions;
	List<Matrix> transforms = new ArrayList<Matrix>();
	Rectangle bounds;

	/**
	 * Construct the warp with a list of matching shapes (which must be either
	 * quads or triangles). The pairs map from the measured/observed space to
	 * the canonical space.
	 * 
	 * @param matchingRegions
	 *            the matching shapes
	 */
	public PiecewiseMeshWarp(List<Pair<Shape>> matchingRegions) {
		this.matchingRegions = matchingRegions;
		initTransforms();
	}

	protected final Matrix getTransform(Point2d p) {
		final int sz = matchingRegions.size();

		for (int i = 0; i < sz; i++) {
			if (matchingRegions.get(i).secondObject().isInside(p)) {
				return transforms.get(i);
			}
		}
		return null;
	}

	/**
	 * Get the shape in the observation space for a point in the canonical
	 * space.
	 * 
	 * @param p
	 *            point in the canonical space.
	 * @return the matching shape in observed space
	 */
	public Shape getMatchingShape(Point2d p) {
		for (int i = 0; i < matchingRegions.size(); i++) {
			final Pair<Shape> matching = matchingRegions.get(i);
			if (matching.secondObject().isInside(p)) {
				return matching.firstObject();
			}
		}
		return null;
	}

	/**
	 * Get the shape pair index for a point in the canonical space.
	 * 
	 * @param p
	 *            the point in canonical space.
	 * @return the index of the matching point pair
	 */
	public int getMatchingShapeIndex(Point2d p) {
		for (int i = 0; i < matchingRegions.size(); i++) {
			final Pair<Shape> matching = matchingRegions.get(i);
			if (matching.secondObject().isInside(p)) {
				return i;
			}
		}
		return -1;
	}

	protected void initTransforms() {
		bounds = new Rectangle(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);

		for (final Pair<Shape> shape : matchingRegions) {
			final Polygon p1 = shape.firstObject().asPolygon();
			final Polygon p2 = shape.secondObject().asPolygon();

			bounds.x = (float) Math.min(bounds.x, p2.minX());
			bounds.y = (float) Math.min(bounds.y, p2.minY());
			bounds.width = (float) Math.max(bounds.width, p2.maxX());
			bounds.height = (float) Math.max(bounds.height, p2.maxY());

			if (p1.nVertices() == 3) {
				transforms.add(getTransform3(polyMatchToPointsMatch(p2, p1)));
			} else if (p1.nVertices() == 4) {
				transforms.add(getTransform4(polyMatchToPointsMatch(p2, p1)));
			} else {
				throw new RuntimeException("Only polygons with 3 or 4 vertices are supported!");
			}
		}

		bounds.width -= bounds.x;
		bounds.height -= bounds.y;
	}

	protected List<Pair<Point2d>> polyMatchToPointsMatch(Polygon pa, Polygon pb) {
		final List<Pair<Point2d>> pts = new ArrayList<Pair<Point2d>>();
		for (int i = 0; i < pa.nVertices(); i++) {
			final Point2d pta = pa.getVertices().get(i);
			final Point2d ptb = pb.getVertices().get(i);

			pts.add(new Pair<Point2d>(pta, ptb));
		}
		return pts;
	}

	protected Matrix getTransform4(List<Pair<Point2d>> pts) {
		return TransformUtilities.homographyMatrixNorm(pts);
	}

	protected Matrix getTransform3(List<Pair<Point2d>> pts) {
		return TransformUtilities.affineMatrix(pts);
	}

	@Override
	public void processImage(final I image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		final I ret = image.newInstance(width, height);

		final Scan scan = new Scan(width, height, image, ret);

		for (int i = 0; i < matchingRegions.size(); i++) {
			final Polygon from = matchingRegions.get(i).secondObject().asPolygon();
			scan.tf = transforms.get(i);

			ScanRasteriser.scanFill(from.points, scan);
		}

		image.internalAssign(ret);
	}

	/**
	 * Transform the content of the input image into an output image of the
	 * given dimensions.
	 * 
	 * @param image
	 *            the input image
	 * @param width
	 *            the output width
	 * @param height
	 *            the output height
	 * @return the output image
	 */
	public I transform(final I image, int width, int height) {
		final I ret = image.newInstance(width, height);

		final Scan scan = new Scan(width, height, image, ret);

		for (int i = 0; i < matchingRegions.size(); i++) {
			final Polygon from = matchingRegions.get(i).secondObject().asPolygon();
			scan.tf = transforms.get(i);

			ScanRasteriser.scanFill(from.points, scan);
		}

		return ret;
	}

	private class Scan implements ScanLineListener {
		private final Pixel p = new Pixel();
		private final int xmin = (int) Math.max(0, bounds.x);
		private final int ymin = (int) Math.max(0, bounds.y);
		private final int xmax;
		private final int ymax;
		private final I image;
		private final I ret;
		Matrix tf;

		Scan(int width, int height, I image, I ret) {
			xmax = (int) Math.min(width, bounds.x + bounds.width);
			ymax = (int) Math.min(height, bounds.y + bounds.height);
			this.image = image;
			this.ret = ret;
		}

		@Override
		public void process(int x1, int x2, int y) {
			if (y < ymin || y > ymax)
				return;

			final int startx = Math.max(xmin, Math.min(x1, x2));
			final int stopx = Math.min(xmax, Math.max(x1, x2));

			for (int x = startx; x <= stopx; x++) {
				p.x = x;
				p.y = y;

				p.transformInplace(tf);

				ret.setPixel(x, y, image.getPixelInterp(p.x, p.y));
			}
		}
	}
}
