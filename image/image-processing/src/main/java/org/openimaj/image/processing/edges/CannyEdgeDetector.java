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
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FSobelX;
import org.openimaj.image.processing.convolution.FSobelY;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Performs a canny edge detector which all the standard canny perameters.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CannyEdgeDetector implements SinglebandImageProcessor<Float,FImage> {
	float threshold = 128f / 255f;
	float hyst_threshold_1 = 50f / 255f;
//	float hyst_threshold_1 = 10f / 255f;
	float hyst_threshold_2 = 230f / 255f;
//	float hyst_threshold_2 = 50f / 255f;
	float sigma = 1;
	
	@Override
	public void processImage(FImage image) {
		FImage tmp = image.process(new FGaussianConvolve(sigma));		
		
		FImage dx = tmp.process(new FSobelX());
		FImage dy = tmp.process(new FSobelY());
		
		FImage magnitudes = NonMaximumSuppressionTangent.computeSuppressed(dx, dy);
		thresholding_tracker(hyst_threshold_1, hyst_threshold_2, magnitudes, image);
		image.threshold(threshold);
	}
 
	private void thresholding_tracker(float threshMin, float threshMax, FImage magnitude, FImage output) {
		output.zero();
 
		for (int i1 = 0; i1 < magnitude.height; i1++) {
			for (int l = 0; l < magnitude.width; l++) {
				if (magnitude.pixels[i1][l] >= threshMin) {
					follow(l, i1, threshMax, magnitude, output);
				}
			}
		}
	}
 
	private boolean follow(int i, int j, float threshMax, FImage magnitude, FImage output) {
		int j1 = i + 1;
		int k1 = i - 1;
		int l1 = j + 1;
		int i2 = j - 1;
		
		if (l1 >= magnitude.height) l1 = magnitude.height - 1;
		if (i2 < 0) i2 = 0;
		if (j1 >= magnitude.width) j1 = magnitude.width - 1;
		if (k1 < 0) k1 = 0;
		
		if (output.pixels[j][i] == 0) {
			output.pixels[j][i] = magnitude.pixels[j][i];
			boolean flag = false;
			int l = k1;
			do {
				if (l > j1) break;
				int i1 = i2;
				do {
					if (i1 > l1) break;
					
					if ((i1 != j || l != i)
						&& magnitude.pixels[i1][l] >= threshMax
						&& follow(l, i1, threshMax, magnitude, output)) {
						flag = true;
						break;
					}
					i1++;
				} while (true);
				if (!flag)
					break;
				l++;
			}
			while (true);
			return true;
		} else {
			return false;
		}
	}
}
