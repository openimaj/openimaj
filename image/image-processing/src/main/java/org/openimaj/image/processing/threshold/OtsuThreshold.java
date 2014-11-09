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
package org.openimaj.image.processing.threshold;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.FloatFloatPair;

/**
 * Otsu's adaptive thresholding algorithm.
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Otsu's_method">http://en.wikipedia.org/wiki/Otsu&apos;s_method</a>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Nobuyuki Otsu" },
		title = "A Threshold Selection Method from Gray-Level Histograms",
		year = "1979",
		journal = "Systems, Man and Cybernetics, IEEE Transactions on",
		pages = { "62", "66" },
		number = "1",
		volume = "9",
		customData = {
				"keywords",
				"Displays;Gaussian distribution;Histograms;Least squares approximation;Marine vehicles;Q measurement;Radar tracking;Sea measurements;Surveillance;Target tracking",
				"doi", "10.1109/TSMC.1979.4310076",
				"ISSN", "0018-9472"
		})
public class OtsuThreshold implements ImageProcessor<FImage> {
	private static final int DEFAULT_NUM_BINS = 256;
	int numBins = DEFAULT_NUM_BINS;

	/**
	 * Default constructor
	 */
	public OtsuThreshold() {

	}

	/**
	 * Construct with the given number of histogram bins
	 * 
	 * @param numBins
	 *            the number of histogram bins
	 */
	public OtsuThreshold(int numBins) {
		this.numBins = numBins;
	}

	protected static int[] makeHistogram(FImage fimg, int numBins) {
		final int[] histData = new int[numBins];

		// Calculate histogram
		for (int r = 0; r < fimg.height; r++) {
			for (int c = 0; c < fimg.width; c++) {
				final int h = (int) (fimg.pixels[r][c] * (numBins - 1));
				histData[h]++;
			}
		}

		return histData;
	}

	protected static int[] makeHistogram(float[] data, int numBins, float min, float max) {
		final int[] histData = new int[numBins];

		// Calculate histogram
		for (int c = 0; c < data.length; c++) {
			final float d = (data[c] - min) / (max - min);
			final int h = (int) (d * (numBins - 1));
			histData[h]++;
		}

		return histData;
	}

	/**
	 * Estimate the threshold for the given image.
	 * 
	 * @param img
	 *            the image
	 * @param numBins
	 *            the number of histogram bins
	 * @return the estimated threshold
	 */
	public static float calculateThreshold(FImage img, int numBins) {
		final int[] histData = makeHistogram(img, numBins);

		// Total number of pixels
		final int total = img.getWidth() * img.getHeight();

		return computeThresholdFromHistogram(histData, total);
	}

	/**
	 * Estimate the threshold for the given data.
	 * <p>
	 * Internally, the data will be min-max normalised before the histogram is
	 * built, and the specified number of bins will cover the entire
	 * <code>max-min</code> range. The returned threshold will have
	 * <code>min</code> added to it to return it to the original range.
	 * 
	 * @param data
	 *            the data
	 * @param numBins
	 *            the number of histogram bins
	 * @return the estimated threshold
	 */
	public static float calculateThreshold(float[] data, int numBins) {
		final float min = ArrayUtils.minValue(data);
		final float max = ArrayUtils.maxValue(data);
		final int[] histData = makeHistogram(data, numBins, min, max);

		return computeThresholdFromHistogram(histData, data.length) + min;
	}

	/**
	 * Estimate the threshold and inter-class variance for the given data.
	 * <p>
	 * Internally, the data will be min-max normalised before the histogram is
	 * built, and the specified number of bins will cover the entire
	 * <code>max-min</code> range. The returned threshold will have
	 * <code>min</code> added to it to return it to the original range.
	 * 
	 * @param data
	 *            the data
	 * @param numBins
	 *            the number of histogram bins
	 * @return the estimated threshold and variance
	 */
	public static FloatFloatPair calculateThresholdAndVariance(float[] data, int numBins) {
		final float min = ArrayUtils.minValue(data);
		final float max = ArrayUtils.maxValue(data);
		final int[] histData = makeHistogram(data, numBins, min, max);

		final FloatFloatPair result = computeThresholdAndVarianceFromHistogram(histData, data.length);
		result.first += min;
		return result;
	}

	/**
	 * Estimate the threshold for the given histogram.
	 * 
	 * @param histData
	 *            the histogram
	 * @param total
	 *            the total number of items in the histogram
	 * @return the estimated threshold
	 */
	public static float computeThresholdFromHistogram(int[] histData, int total) {
		return computeThresholdAndVarianceFromHistogram(histData, total).first;
	}

	/**
	 * Estimate the threshold and inter-class variance for the given histogram.
	 * 
	 * @param histData
	 *            the histogram
	 * @param total
	 *            the total number of items in the histogram
	 * @return the estimated threshold and variance
	 */
	public static FloatFloatPair computeThresholdAndVarianceFromHistogram(int[] histData, int total) {
		final int numBins = histData.length;
		float sum = 0;
		for (int t = 0; t < numBins; t++)
			sum += t * histData[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		float threshold = 0;

		for (int t = 0; t < numBins; t++) {
			wB += histData[t]; // Weight Background
			if (wB == 0)
				continue;

			wF = total - wB; // Weight Foreground
			if (wF == 0)
				break;

			sumB += (t * histData[t]);

			final float mB = sumB / wB; // Mean Background
			final float mF = (sum - sumB) / wF; // Mean Foreground

			// Calculate Between Class Variance
			final float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}

		return new FloatFloatPair(threshold / (numBins - 1), varMax / total / total);
	}

	@Override
	public void processImage(FImage image) {
		final float threshold = calculateThreshold(image, numBins);

		image.threshold(threshold);
	}
}
