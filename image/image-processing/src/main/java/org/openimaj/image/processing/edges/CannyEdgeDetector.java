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

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.HistogramAnalyser;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FSobelX;
import org.openimaj.image.processing.convolution.FSobelY;
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
				System.out.println(i);
				return i / 64f;
			}
			cumSum += hist.values[i];
		}

		return 1f;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmp = image.process(new FGaussianConvolve(sigma));
		final FImage dx = tmp.process(new FSobelX());
		final FImage dy = tmp.process(new FSobelY());

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

		thresholdingTracker(magnitudes, image, low, high);
	}

	private void thresholdingTracker(FImage magnitude, FImage output, float low, float high) {
		output.zero();

		for (int y = 0; y < magnitude.height; y++) {
			for (int x = 0; x < magnitude.width; x++) {
				if (magnitude.pixels[y][x] >= high) {
					follow(x, y, magnitude, output, low);
				}
			}
		}
	}

	private void follow(int x, int y, FImage magnitude, FImage output, float thresh) {
		final int xstart = Math.max(0, x - 1);
		final int xstop = Math.min(x + 2, magnitude.width);
		final int ystart = Math.max(0, y - 1);
		final int ystop = Math.min(y + 2, magnitude.height);

		for (int yy = ystart; yy < ystop; yy++) {
			for (int xx = xstart; xx < xstop; xx++) {
				if (magnitude.pixels[yy][xx] >= thresh && output.pixels[yy][xx] != 1) {
					output.pixels[yy][xx] = 1;
					follow(xx, yy, magnitude, output, thresh);
				}
			}
		}
	}
}
