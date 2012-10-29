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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.combiner.AccumulatingImageCombiner;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;

import Jama.Matrix;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 *         Perform a set of matrix transforms on a set of images and construct a
 *         single image containing all the pixels (or a window of the pixels) in
 *         the projected space.
 * 
 * @param <Q>
 *            The image pixel type
 * @param <T>
 *            the image type
 */
public class ProjectionProcessor<Q, T extends Image<Q, T>>
		implements
		AccumulatingImageCombiner<T, T>
{
	protected int minc;
	protected int minr;
	protected int maxc;
	protected int maxr;
	protected boolean unset;
	protected List<Matrix> transforms;
	protected List<Matrix> transformsInverted;
	protected List<T> images;
	protected List<Shape> projectedShapes;
	protected List<Rectangle> projectedRectangles;

	protected Matrix currentMatrix = new Matrix(new double[][] { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } });

	/**
	 * Construct a projection processor starting with an identity matrix for any
	 * images processed (i.e., don't do anything)
	 */
	public ProjectionProcessor() {
		unset = true;
		this.minc = 0;
		this.minr = 0;
		this.maxc = 0;
		this.maxr = 0;

		transforms = new ArrayList<Matrix>();
		this.transformsInverted = new ArrayList<Matrix>();
		images = new ArrayList<T>();
		this.projectedShapes = new ArrayList<Shape>();
		this.projectedRectangles = new ArrayList<Rectangle>();
	}

	/**
	 * Set the matrix, any images processed from this point forward will be
	 * projected using this matrix
	 * 
	 * @param matrix
	 *            a 3x3 matrix representing a 2d transform
	 */
	public void setMatrix(Matrix matrix) {
		if (matrix.getRowDimension() == 2) {
			final int c = matrix.getColumnDimension() - 1;

			currentMatrix = new Matrix(3, 3);
			currentMatrix.setMatrix(0, 1, 0, c, matrix);
			currentMatrix.set(2, 2, 1);
		} else {
			this.currentMatrix = matrix;
		}
	}

	/**
	 * Prepare an image to be transformed using the current matrix. The bounds
	 * of the image post transform are calculated so the default
	 * {@link ProjectionProcessor#performProjection} knows what range of pixels
	 * to draw
	 * 
	 * @param image
	 *            to be transformed
	 */
	@Override
	public void accumulate(T image) {
		final Rectangle actualBounds = image.getBounds();
		final Shape transformedActualBounds = actualBounds.transform(this.currentMatrix);
		final double tminX = transformedActualBounds.minX();
		final double tmaxX = transformedActualBounds.maxX();
		final double tminY = transformedActualBounds.minY();
		final double tmaxY = transformedActualBounds.maxY();
		if (unset) {
			this.minc = (int) Math.floor(tminX);
			this.minr = (int) Math.floor(tminY);
			this.maxc = (int) Math.floor(tmaxX);
			this.maxr = (int) Math.floor(tmaxY);
			unset = false;
		}
		else {
			if (tminX < minc)
				minc = (int) Math.floor(tminX);
			if (tmaxX > maxc)
				maxc = (int) Math.floor(tmaxX);
			if (tminY < minr)
				minr = (int) Math.floor(tminY);
			if (tmaxY > maxr)
				maxr = (int) Math.floor(tmaxY);
		}
		// Expand the borders by 1 pixel so we get a nicer effect around the
		// edges
		final float padding = 1f;
		final Rectangle expandedBounds = new Rectangle(actualBounds.x - padding, actualBounds.y - padding,
				actualBounds.width + padding * 2, actualBounds.height + padding * 2);
		final Shape transformedExpandedBounds = expandedBounds.transform(this.currentMatrix);
		Matrix minv = null, m = null;
		try {
			m = this.currentMatrix.copy();
			minv = this.currentMatrix.copy().inverse();
		} catch (final Throwable e) {
			// the matrix might be singular, return
			return;
		}

		this.images.add(image);
		this.transforms.add(m);
		this.transformsInverted.add(minv);
		// this.projectedShapes.add(new
		// TriangulatedPolygon(transformedExpandedBounds));
		this.projectedShapes.add(transformedExpandedBounds);
		this.projectedRectangles.add(transformedExpandedBounds.calculateRegularBoundingBox());

		// System.out.println("added image with transform: ");
		// this.currentMatrix.print(5,5);
		// System.out.println("and the inverse:");
		// this.currentMatrix.inverse().print(5,5);
		// System.out.println("New min/max become:" + minc + "x" + minr + "/" +
		// maxc + "x" + maxr);
	}

	/**
	 * Using all the images currently processed, perform the projection on each
	 * image and draw every pixel with valid data. Pixels within the bounding
	 * box but with no data are set to black (more specifically 0, whatever that
	 * may mean for this kind of image)
	 * 
	 * @return the image containing all the pixels drawn
	 */
	public T performProjection() {
		// The most long winded way to get a black pixel EVER
		return performProjection(false, this.images.get(0).newInstance(1, 1).getPixel(0, 0));
	}

	/**
	 * Perform projection specifying the background colour (i.e. the colour of
	 * pixels with no data).
	 * 
	 * @param backgroundColour
	 *            the background colour
	 * @return projected images
	 */
	public T performProjection(Q backgroundColour) {
		final int projectionMinC = minc, projectionMaxC = maxc, projectionMinR = minr, projectionMaxR = maxr;
		return performProjection(projectionMinC, projectionMaxC, projectionMinR, projectionMaxR, backgroundColour);
	}

	/**
	 * Perform projection specifying the background colour (i.e. the colour of
	 * pixels with no data) and whether the original window size should be kept.
	 * If set to true the window of pixels drawn post projection are within the
	 * window of the first image processed.
	 * 
	 * @param keepOriginalWindow
	 *            whether to keep the original image's window
	 * @param backgroundColour
	 *            the background colour
	 * @return projected images
	 */
	public T performProjection(boolean keepOriginalWindow, Q backgroundColour) {
		int projectionMinC = minc, projectionMaxC = maxc, projectionMinR = minr, projectionMaxR = maxr;
		if (keepOriginalWindow)
		{
			projectionMinC = 0;
			projectionMinR = 0;
			projectionMaxR = images.get(0).getRows();
			projectionMaxC = images.get(0).getCols();
		}
		return performProjection(projectionMinC, projectionMaxC, projectionMinR, projectionMaxR, backgroundColour);
	}

	/**
	 * Perform projection but only request data for pixels within the windowed
	 * range provided. Specify the background colour, i.e. the value of pixels
	 * with no data post projection.
	 * 
	 * @param windowMinC
	 *            left X
	 * @param windowMaxC
	 *            right X
	 * @param windowMinR
	 *            top Y
	 * @param windowMaxR
	 *            bottom Y
	 * @return projected image within the window
	 */
	public T performProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR) {
		return performProjection(windowMinC, windowMaxC, windowMinR, windowMaxR, this.images.get(0).newInstance(1, 1)
				.getPixel(0, 0));
	}

	/**
	 * Perform projection but only request data for pixels within the windowed
	 * range provided. Specify the background colour, i.e. the value of pixels
	 * with no data post projection.
	 * 
	 * @param windowMinC
	 *            left X
	 * @param windowMaxC
	 *            right X
	 * @param windowMinR
	 *            top Y
	 * @param windowMaxR
	 *            bottom Y
	 * @param backgroundColour
	 *            background colour of pixels with no data
	 * @return projected image within the window
	 */
	public T performProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR, Q backgroundColour) {
		T output = null;
		output = images.get(0).newInstance(windowMaxC - windowMinC, windowMaxR - windowMinR);
		if (backgroundColour != null)
			output.fill(backgroundColour);

		final Shape[][] projectRectangleShapes = getCurrentShapes();

		for (int y = 0; y < output.getHeight(); y++)
		{
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (int shapeIndex = 0; shapeIndex < this.projectedShapes.size(); shapeIndex++) {
					if (backgroundColour == null || isInside(shapeIndex, projectRectangleShapes, realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						final T im = this.images.get(i);
						if (backgroundColour != null)
							output.setPixel(x, y, im.getPixelInterp(xt, yt, backgroundColour));
						else
							output.setPixel(x, y, im.getPixelInterp(xt, yt));
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * Get the current shapes as an array for efficient access, first entry for
	 * each shape is its rectangle, second entry is the shape
	 * 
	 * @return
	 */
	protected Shape[][] getCurrentShapes() {
		final Shape[][] currentShapes = new Shape[this.projectedShapes.size()][2];
		for (int i = 0; i < this.projectedShapes.size(); i++) {
			currentShapes[i][0] = this.projectedRectangles.get(i);
			currentShapes[i][1] = this.projectedShapes.get(i);
		}
		return currentShapes;
	}

	protected boolean isInside(int shapeIndex, Shape[][] projectRectangleShapes, Point2d realPoint) {
		return projectRectangleShapes[shapeIndex][0].isInside(realPoint)
				&& projectRectangleShapes[shapeIndex][1].isInside(realPoint);
	}

	/**
	 * Perform projection but only request data for pixels within the windowed
	 * range provided. Specify the background colour, i.e. the value of pixels
	 * with no data post projection.
	 * 
	 * @param windowMinC
	 *            left X
	 * @param windowMinR
	 *            top Y
	 * @param output
	 *            the target image in which to project
	 * @return projected image within the window
	 */
	public T performProjection(int windowMinC, int windowMinR, T output) {

		for (int y = 0; y < output.getHeight(); y++)
		{
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (final Shape s : this.projectedShapes) {
					if (s.calculateRegularBoundingBox().isInside(realPoint) && s.isInside(realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						final T im = this.images.get(i);
						output.setPixel(x, y, im.getPixelInterp(xt, yt, output.getPixel(x, y)));
					}
					i++;
				}
			}
		}

		return output;
	}

	/**
	 * Perform blended projection but only request data for pixels within the
	 * windowed range provided. Specify the background colour, i.e. the value of
	 * pixels with no data post projection. This blends any existing pixels to
	 * newly added pixels
	 * 
	 * @param windowMinC
	 *            left X
	 * @param windowMaxC
	 *            right X
	 * @param windowMinR
	 *            top Y
	 * @param windowMaxR
	 *            bottom Y
	 * @param backgroundColour
	 *            background colour of pixels with no data
	 * @return projected image within the window
	 */
	public T performBlendedProjection(int windowMinC, int windowMaxC, int windowMinR, int windowMaxR, Q backgroundColour)
	{
		T output = null;
		output = images.get(0).newInstance(windowMaxC - windowMinC, windowMaxR - windowMinR);
		final Map<Integer, Boolean> setMap = new HashMap<Integer, Boolean>();
		final T blendingPallet = output.newInstance(2, 1);
		for (int y = 0; y < output.getHeight(); y++)
		{
			for (int x = 0; x < output.getWidth(); x++) {
				final Point2d realPoint = new Point2dImpl(windowMinC + x, windowMinR + y);
				int i = 0;
				for (final Shape s : this.projectedShapes) {
					if (s.isInside(realPoint)) {
						final double[][] transform = this.transformsInverted.get(i).getArray();

						float xt = (float) transform[0][0] * realPoint.getX() + (float) transform[0][1]
								* realPoint.getY() + (float) transform[0][2];
						float yt = (float) transform[1][0] * realPoint.getX() + (float) transform[1][1]
								* realPoint.getY() + (float) transform[1][2];
						final float zt = (float) transform[2][0] * realPoint.getX() + (float) transform[2][1]
								* realPoint.getY() + (float) transform[2][2];

						xt /= zt;
						yt /= zt;
						Q toSet = null;
						if (backgroundColour != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, backgroundColour);
						else if (setMap.get(y * output.getWidth() + x) != null)
							toSet = this.images.get(i).getPixelInterp(xt, yt, output.getPixelInterp(x, y));
						else
							toSet = this.images.get(i).getPixelInterp(xt, yt);
						// Blend the pixel with the existing pixel
						if (setMap.get(y * output.getWidth() + x) != null) {
							blendingPallet.setPixel(1, 0, toSet);
							blendingPallet.setPixel(0, 0, output.getPixel(x, y));

							toSet = blendingPallet.getPixelInterp(0.1, 0.5);
						}
						setMap.put(y * output.getWidth() + x, true);
						output.setPixel(x, y, toSet);
					}
					i++;
				}
			}
		}
		return output;
	}

	/**
	 * @return Current matrix
	 */
	public Matrix getMatrix() {
		return this.currentMatrix;
	}

	/**
	 * Utility function, project one image with one matrix. Every valid pixel in
	 * the space the image is projected into is displayed in the final image.
	 * 
	 * @param <Q>
	 *            the image pixel type
	 * @param <T>
	 *            image type
	 * @param image
	 *            the image to project
	 * @param matrix
	 *            the matrix to project against
	 * @return projected image
	 */
	@SuppressWarnings("unchecked")
	public static <Q, T extends Image<Q, T>> T project(T image, Matrix matrix) {
		// Note: extra casts to work around compiler bug
		if ((Image<?, ?>) image instanceof FImage) {
			final FProjectionProcessor proc = new FProjectionProcessor();
			proc.setMatrix(matrix);
			((FImage) (Image<?, ?>) image).accumulateWith(proc);
			return (T) (Image<?, ?>) proc.performProjection();
		}
		if ((Image<?, ?>) image instanceof MBFImage) {
			final MBFProjectionProcessor proc = new MBFProjectionProcessor();
			proc.setMatrix(matrix);
			((MBFImage) (Image<?, ?>) image).accumulateWith(proc);
			return (T) (Image<?, ?>) proc.performProjection();
		} else {
			final ProjectionProcessor<Q, T> proc = new ProjectionProcessor<Q, T>();
			proc.setMatrix(matrix);
			image.accumulateWith(proc);
			return proc.performProjection();
		}
	}

	/**
	 * Utility function, project one image with one matrix. Every valid pixel in
	 * the space the image is projected into is displayed in the final image.
	 * 
	 * @param <Q>
	 *            the image pixel type
	 * @param <T>
	 *            image type
	 * @param image
	 *            the image to project
	 * @param matrix
	 *            the matrix to project against
	 * @param backgroundColour
	 *            The colour of pixels with no data
	 * @return projected image
	 */
	public static <Q, T extends Image<Q, T>> T project(T image, Matrix matrix, Q backgroundColour) {
		final ProjectionProcessor<Q, T> proc = new ProjectionProcessor<Q, T>();
		proc.setMatrix(matrix);
		image.accumulateWith(proc);
		return proc.performProjection(backgroundColour);
	}

	@Override
	public T combine() {
		return performProjection();
	}
}
