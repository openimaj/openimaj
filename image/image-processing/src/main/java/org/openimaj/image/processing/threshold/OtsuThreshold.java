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

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Otsu's adaptive thresholding algorithm. 
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Otsu's_method">http://en.wikipedia.org/wiki/Otsu&apos;s_method</a>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class OtsuThreshold implements ImageProcessor<FImage> {
	private static final int NUM_BINS = 256;

	protected int[] makeHistogram(FImage fimg) {
		int [] histData = new int[NUM_BINS];

		// Calculate histogram
		for (int r=0; r<fimg.height; r++) {
			for (int c=0; c<fimg.width; c++) {
				int h = (int) (fimg.pixels[r][c] * (NUM_BINS - 1));
				histData[h]++;
			}
		}

		return histData;
	}

	/**
	 * Estimate the threshold for the given image.
	 * @param img the image
	 * @return the estimated threshold
	 */
	public float calculateThreshold(FImage img) {
		int [] histData = makeHistogram(img);

		// Total number of pixels
		int total = img.getWidth() * img.getHeight();

		float sum = 0;
		for (int t=0; t<NUM_BINS; t++) sum += t * histData[t];

		float sumB = 0;
		int wB = 0;
		int wF = 0;

		float varMax = 0;
		float threshold = 0;

		for (int t=0; t<NUM_BINS; t++) {
			wB += histData[t];               // Weight Background
			if (wB == 0) continue;

			wF = total - wB;                 // Weight Foreground
			if (wF == 0) break;

			sumB += (t * histData[t]);

			float mB = sumB / wB;            // Mean Background
			float mF = (sum - sumB) / wF;    // Mean Foreground

			// Calculate Between Class Variance
			float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}

		return threshold / (NUM_BINS - 1);
	}

	@Override
	public void processImage(FImage image) {
		float threshold = calculateThreshold(image);

		image.threshold(threshold);
	}
}
