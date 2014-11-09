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
package org.openimaj.image.processing.edges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.pixel.util.LineIterators;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Implementation of the Stroke Width Transform.
 * <p>
 * The Stroke Width Transform detects strokes and their respective widths from
 * an image. This implementation contains a number of enhancements to improve
 * the quality of the detected strokes, based on ideas from <a
 * href="http://libccv.org/lib/ccv-swt/">LibCCV</a> implementation:
 * <ul>
 * <li>We search around the stroke in a small window for endpoints.</li>
 * <li>We search around the endpoint in a small window for matching gradients.</li>
 * <li>In addition to the stroke along the gradient, we also stroke at +/-45
 * degrees from this.</li>
 * </ul>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Epshtein, B.", "Ofek, E.", "Wexler, Y." },
		title = "Detecting text in natural scenes with stroke width transform",
		year = "2010",
		booktitle = "Computer Vision and Pattern Recognition (CVPR), 2010 IEEE Conference on",
		pages = { "2963", "2970" },
		customData = {
				"keywords",
				"image processing;text analysis;image operator;image pixel;natural images;natural scenes;stroke width transform;text detection;Colored noise;Computer vision;Engines;Filter bank;Geometry;Image segmentation;Layout;Optical character recognition software;Pixel;Robustness",
				"doi", "10.1109/CVPR.2010.5540041",
				"ISSN", "1063-6919"
		})
public class StrokeWidthTransform implements SinglebandImageProcessor<Float, FImage> {
	private final static int[][] edgeSearchRegion = { { 0, 0 }, { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
	private final static int[][] gradSearchRegion = {
			{ 0, 0 }, { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { 1, -1 }, { -1, 1 }, { 1, 1 } };

	private final CannyEdgeDetector canny;
	private boolean direction;
	private int maxStrokeWidth = 70;

	/**
	 * Construct the SWT with the given Canny edge detector.
	 * 
	 * @param direction
	 *            direction of the SWT; true for dark on light, false for light
	 *            on dark.
	 * @param canny
	 *            the canny edge detector
	 */
	public StrokeWidthTransform(boolean direction, CannyEdgeDetector canny) {
		this.direction = direction;
		this.canny = canny;
	}

	/**
	 * Construct the SWT with the given sigma for smoothing in the Canny phase
	 * The Canny thresholds are chosen automatically.
	 * 
	 * @param direction
	 *            direction of the SWT; true for dark on light, false for light
	 *            on dark.
	 * @param sigma
	 *            the amount of initial blurring
	 */
	public StrokeWidthTransform(boolean direction, float sigma) {
		this.direction = direction;
		this.canny = new CannyEdgeDetector(sigma);
	}

	/**
	 * Construct with all Canny parameters set manually.
	 * 
	 * @param direction
	 *            direction of the SWT; true for dark on light, false for light
	 *            on dark.
	 * 
	 * @param lowThresh
	 *            lower hysteresis threshold.
	 * @param highThresh
	 *            upper hysteresis threshold.
	 * @param sigma
	 *            the amount of initial blurring.
	 */
	public StrokeWidthTransform(boolean direction, float lowThresh, float highThresh, float sigma) {
		this.direction = direction;
		this.canny = new CannyEdgeDetector(lowThresh, highThresh, sigma);
	}

	/**
	 * Get the maximum stroke width
	 * 
	 * @return the maximum stroke width
	 */
	public int getMaxStrokeWidth() {
		return maxStrokeWidth;
	}

	/**
	 * Set the maximum stroke width
	 * 
	 * @param maxStrokeWidth
	 *            the maximum stroke width
	 */
	public void setMaxStrokeWidth(int maxStrokeWidth) {
		this.maxStrokeWidth = maxStrokeWidth;
	}

	@Override
	public void processImage(FImage image) {
		final FSobel grads = new FSobel(canny.sigma);

		final FImage edges = image.clone();
		canny.processImage(edges, grads);

		image.fill(Float.POSITIVE_INFINITY);

		final List<List<Pixel>> rays = generateRays(edges, grads.dx, grads.dy, direction, image);
		medianFilter(image, rays);
	}

	private List<List<Pixel>> generateRays(FImage edges, FImage dx, FImage dy, boolean detectDark, FImage output) {
		final List<List<Pixel>> rays = new ArrayList<List<Pixel>>();

		final float gradDirection = detectDark ? -1 : 1;

		for (int y = 0; y < output.height; y++) {
			for (int x = 0; x < output.width; x++) {
				if (edges.pixels[y][x] > 0) {
					traceRay(edges, dx, dy, detectDark, output, gradDirection, x, y, rays, 1, 0, 0, 1);
					traceRay(edges, dx, dy, detectDark, output, gradDirection, x, y, rays, 1, 1, -1, 1);
					traceRay(edges, dx, dy, detectDark, output, gradDirection, x, y, rays, 1, -1, 1, 1);
				}
			}
		}
		return rays;
	}

	private void traceRay(FImage edges, FImage dx, FImage dy, boolean
			detectDark, FImage output, float gradDirection,
			int x, int y, List<List<Pixel>> rays, int xx, int xy, int yx, int yy)
	{
		final float gradX = (xx * dx.pixels[y][x] + xy * dy.pixels[y][x]) * gradDirection;
		final float gradY = (yy * dy.pixels[y][x] + yx * dx.pixels[y][x]) * gradDirection;

		final Iterator<Pixel> iterator = LineIterators.bresenham(x, y, gradX, gradY);
		final Pixel start = iterator.next().clone(); // start of ray

		for (int j = 0; j < maxStrokeWidth; j++) {
			final Pixel current = iterator.next();

			// check bounds
			if (current.x < 1 || current.x >= output.width - 1 || current.y < 1 || current.y >= output.height - 1) {
				break;
			}

			if (Math.abs(current.x - start.x) < 2 && Math.abs(current.y - start.y) < 2)
				continue;

			Pixel end = null;

			// search over the around the current pixel region for an edge
			for (int i = 0; i < edgeSearchRegion.length; i++) {
				final int currentX = current.x + edgeSearchRegion[i][0];
				final int currentY = current.y + edgeSearchRegion[i][1];

				if (edges.pixels[currentY][currentX] > 0) {
					end = new Pixel(currentX, currentY);
					break;
				}
			}

			if (end != null) {
				// edge found; now search for matching gradient in surrounding
				// area
				boolean found = false;

				final float startGradX = dx.pixels[start.y][start.x];
				final float startGradY = dy.pixels[start.y][start.x];

				for (int i = 0; i < gradSearchRegion.length; i++) {
					final int currentX = end.x + gradSearchRegion[i][0];
					final int currentY = end.y + gradSearchRegion[i][1];

					final float currentGradX = dx.pixels[currentY][currentX];
					final float currentGradY = dy.pixels[currentY][currentX];

					// final float currentMag = (float) Math.sqrt((currentGradX
					// * currentGradX)
					// + (currentGradY * currentGradY))
					// * gradDirection;
					//
					// currentGradX = currentGradX / currentMag;
					// currentGradY = currentGradY / currentMag;
					//
					// if (Math.acos(gradX * -currentGradX + gradY *
					// -currentGradY) < Math.PI / 6.0) {
					// found = true;
					// break;
					// }
					final float tn = startGradY * currentGradX - startGradX * currentGradY;
					final float td = startGradX * currentGradX + startGradY * currentGradY;
					if (tn * 7 < -td * 4 && tn * 7 > td * 4)
					{
						found = true;
						break;
					}
				}

				// if we found an opposite grad, then we fill in the ray using
				// the supercover to select all pixels that the ray passes
				// through
				if (found) {
					final float length = (float) Line2d.distance(start, end);
					final List<Pixel> ray = LineIterators.supercoverAsList(start, end);
					for (final Pixel p : ray) {
						output.pixels[p.y][p.x] = Math.min(length, output.pixels[p.y][p.x]);
					}

					rays.add(ray);
				}
				break;
			}
		}
	}

	private void medianFilter(FImage output, List<List<Pixel>> rays) {
		if (rays.size() == 0)
			return;

		Collections.sort(rays, new Comparator<List<Pixel>>() {
			@Override
			public int compare(List<Pixel> o1, List<Pixel> o2) {
				return o1.size() - o2.size();
			}
		});

		final float[] working = new float[rays.get(rays.size() - 1).size()];

		for (final List<Pixel> ray : rays) {
			final int length = ray.size();
			for (int i = 0; i < length; i++) {
				final Pixel pixel = ray.get(i);
				working[i] = output.pixels[pixel.y][pixel.x];
			}

			final float median = FloatArrayStatsUtils.median(working, 0, length);
			for (int i = 0; i < length; i++) {
				final Pixel pixel = ray.get(i);

				if (output.pixels[pixel.y][pixel.x] > median)
					output.pixels[pixel.y][pixel.x] = median;
			}
		}
	}

	/**
	 * Normalise the output image of the {@link StrokeWidthTransform} for
	 * display. This replaces all {@link Float#POSITIVE_INFINITY} pixels with a
	 * value of 1, and min-max normalises all the valid stroke pixels to be
	 * between 0 and 1.
	 * 
	 * @param input
	 *            the image to normalise
	 * @return the normalised image
	 */
	public static FImage normaliseImage(FImage input) {
		final FImage output = input.clone();

		float maxVal = 0;
		float minVal = Float.MAX_VALUE;
		for (int row = 0; row < input.height; row++) {
			for (int col = 0; col < input.width; col++) {
				final float val = input.pixels[row][col];
				if (val != Float.POSITIVE_INFINITY) {
					maxVal = Math.max(val, maxVal);
					minVal = Math.min(val, minVal);
				}
			}
		}

		final float difference = maxVal - minVal;
		for (int row = 0; row < input.height; row++) {
			for (int col = 0; col < input.width; col++) {
				final float val = input.pixels[row][col];
				if (val == Float.POSITIVE_INFINITY) {
					output.pixels[row][col] = 1;
				} else {
					output.pixels[row][col] = (val - minVal) / difference;
				}
			}
		}
		return output;
	}

	/**
	 * Get the direction of the SWT; true for dark on light, false for light
	 * 
	 * @return the direction
	 */
	public boolean getDirection() {
		return direction;
	}

	/**
	 * Set the direction of the SWT; true for dark on light, false for light
	 * 
	 * @param direction
	 *            the direction to set
	 */
	public void setDirection(boolean direction) {
		this.direction = direction;
	}

}
