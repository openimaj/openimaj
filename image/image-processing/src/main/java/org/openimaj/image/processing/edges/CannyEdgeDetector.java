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

import java.util.ArrayDeque;
import java.util.Deque;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.histogram.HistogramAnalyser;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processing.convolution.FSobel;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Canny edge detector. Performs the following steps:
 * <ol>
 * <li>Gaussian blur with std.dev. sigma</li>
 * <li>Horizontal and vertical edge detection with Sobel operators</li>
 * <li>Non-maximum suppression</li>
 * <li>Hysteresis thresholding</li>
 * </ol>
 * 
 * The upper and lower thresholds for the hysteresis thresholding can be
 * specified manually or automatically chosen based on the histogram of the edge
 * magnitudes.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class CannyEdgeDetector implements SinglebandImageProcessor<Float, FImage> {
	static final float threshRatio = 0.4f;

	float lowThresh = -1;
	float highThresh = -1;
	float sigma = 1;

	/**
	 * Default constructor. Sigma is set to 1.0, and the thresholds are chosen
	 * automatically.
	 */
	public CannyEdgeDetector() {
	}

	/**
	 * Construct with the give sigma. The thresholds are chosen automatically.
	 * 
	 * @param sigma
	 *            the amount of initial blurring
	 */
	public CannyEdgeDetector(float sigma) {
		this.sigma = sigma;
	}

	/**
	 * Construct with all parameters set manually.
	 * 
	 * @param lowThresh
	 *            lower hysteresis threshold.
	 * @param highThresh
	 *            upper hysteresis threshold.
	 * @param sigma
	 *            the amount of initial blurring.
	 */
	public CannyEdgeDetector(float lowThresh, float highThresh, float sigma) {
		if (lowThresh < 0 || lowThresh > 1)
			throw new IllegalArgumentException("Low threshold must be between 0 and 1");
		if (highThresh < 0 || highThresh > 1)
			throw new IllegalArgumentException("High threshold must be between 0 and 1");
		if (highThresh < lowThresh)
			throw new IllegalArgumentException("High threshold must be bigger than the lower threshold");
		if (sigma < 0)
			throw new IllegalArgumentException("Sigma must be > 0");

		this.lowThresh = lowThresh;
		this.highThresh = highThresh;
		this.sigma = sigma;
	}

	float computeHighThreshold(FImage magnitudes) {
		final Histogram hist = HistogramAnalyser.getHistogram(magnitudes, 64);

		float cumSum = 0;
		for (int i = 0; i < 64; i++) {
			if (cumSum > 0.7 * magnitudes.width * magnitudes.height) {
				return i / 64f;
			}
			cumSum += hist.values[i];
		}

		return 1f;
	}

	@Override
	public void processImage(FImage image) {
		processImage(image, new FSobel(sigma));
	}

	/**
	 * Apply non-max suppression and hysteresis thresholding based using the
	 * given {@link FSobel} analyser to generate the gradients. The gradient
	 * maps held by the {@link FSobel} object will be set to the gradients of
	 * the input image after this method returns.
	 * 
	 * @param image
	 *            the image to process (and write the result to)
	 * @param sobel
	 *            the computed gradients
	 */
	public void processImage(FImage image, FSobel sobel) {
		image.analyseWith(sobel);
		processImage(image, sobel.dx, sobel.dy);
	}

	/**
	 * Apply non-max suppression and hysteresis thresholding based on the given
	 * (Sobel) gradient maps and write the result to the given output image.
	 * 
	 * @param output
	 *            the output image
	 * @param dx
	 *            the x gradients
	 * @param dy
	 *            the y gradients
	 */
	public void processImage(FImage output, FImage dx, FImage dy) {
		// tmpMags will hold the magnitudes BEFORE suppression
		final FImage tmpMags = new FImage(dx.width, dx.height);
		// magnitudes holds the suppressed magnitude image
		final FImage magnitudes = NonMaximumSuppressionTangent.computeSuppressed(dx, dy, tmpMags);
		magnitudes.normalise();

		float low = this.lowThresh;
		float high = this.highThresh;
		if (high < 0) {
			// if high has not been set we use a similar approach to matlab to
			// estimate the thresholds
			high = computeHighThreshold(tmpMags);
			low = threshRatio * high;
		}

		thresholdingTracker(magnitudes, output, low, high);
	}

	// private void thresholdingTracker(FImage magnitude, FImage output, float
	// low, float high) {
	// output.zero();
	//
	// for (int y = 0; y < magnitude.height; y++) {
	// for (int x = 0; x < magnitude.width; x++) {
	// if (magnitude.pixels[y][x] >= high) {
	// follow(x, y, magnitude, output, low);
	// }
	// }
	// }
	// }
	//
	// private void follow(int x, int y, FImage magnitude, FImage output, float
	// thresh) {
	// final int xstart = Math.max(0, x - 1);
	// final int xstop = Math.min(x + 2, magnitude.width);
	// final int ystart = Math.max(0, y - 1);
	// final int ystop = Math.min(y + 2, magnitude.height);
	//
	// for (int yy = ystart; yy < ystop; yy++) {
	// for (int xx = xstart; xx < xstop; xx++) {
	// if (magnitude.pixels[yy][xx] >= thresh && output.pixels[yy][xx] != 1) {
	// output.pixels[yy][xx] = 1;
	// follow(xx, yy, magnitude, output, thresh);
	// }
	// }
	// }
	// }

	private void thresholdingTracker(FImage magnitude, FImage output, float low, float high) {
		output.zero();

		final Deque<Pixel> candidates = new ArrayDeque<Pixel>();
		for (int y = 0; y < magnitude.height; y++) {
			for (int x = 0; x < magnitude.width; x++) {
				if (magnitude.pixels[y][x] >= high && output.pixels[y][x] != 1) {
					candidates.add(new Pixel(x, y));

					while (!candidates.isEmpty()) {
						final Pixel current = candidates.pollFirst();

						if (current.x < 0 || current.x > magnitude.width || current.y < 0 || current.y > magnitude.height)
							continue;

						if (output.pixels[current.y][current.x] == 1)
							continue;

						if (magnitude.pixels[current.y][current.x] < low)
							continue;

						output.pixels[current.y][current.x] = 1;

						candidates.add(new Pixel(x - 1, y - 1));
						candidates.add(new Pixel(x, y - 1));
						candidates.add(new Pixel(x + 1, y - 1));
						candidates.add(new Pixel(x - 1, y));
						candidates.add(new Pixel(x + 1, y));
						candidates.add(new Pixel(x - 1, y + 1));
						candidates.add(new Pixel(x, y + 1));
						candidates.add(new Pixel(x + 1, y + 1));
					}
				}
			}
		}
	}
}
