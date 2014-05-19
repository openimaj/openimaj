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
package org.openimaj.image.processing.convolution;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Fast approximate Gaussian smoothing using repeated fast box filtering.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Kovesi, P." },
		title = "Fast Almost-Gaussian Filtering",
		year = "2010",
		booktitle = "Digital Image Computing: Techniques and Applications (DICTA), 2010 International Conference on",
		pages = { "121", "125" },
		month = "Dec",
		customData = {
				"keywords", "Gaussian processes;approximation theory;band-pass filters;image processing;Gaussian bandpass filters;fast almost-Gaussian filtering;image averaging;integral images;log-Gabor filters;separable moving average filters;summed area tables;symmetric transfer function;Approximation methods;Bandwidth;Computer vision;Frequency domain analysis;Laplace equations;Pixel;Transfer functions;Difference of Gaussian filtering;Gaussian smoothing",
				"doi", "10.1109/DICTA.2010.30"
		})
public class FFastGaussianConvolve implements SinglebandImageProcessor<Float, FImage> {
	private final int n;
	private final int m;
	private SinglebandImageProcessor<Float, FImage> wlBox;
	private SinglebandImageProcessor<Float, FImage> wuBox;

	/**
	 * Construct an {@link FFastGaussianConvolve} to approximate blurring with a
	 * Gaussian of standard deviation sigma.
	 * 
	 * @param sigma
	 *            Standard deviation of the approximated Gaussian
	 * @param n
	 *            Number of filtering iterations to perform (usually between 3
	 *            and 6)
	 */
	public FFastGaussianConvolve(float sigma, int n) {
		if (sigma < 1.8) {
			// std.devs of less than 1.8 are not well approximated.
			this.m = 1;
			this.n = 1;
			this.wlBox = new FGaussianConvolve(sigma);
		} else {
			final float ss = sigma * sigma;
			final double wIdeal = Math.sqrt((12.0 * ss / n) + 1.0);
			final int wl = (((int) wIdeal) % 2 == 0) ? (int) wIdeal - 1 : (int) wIdeal;
			final int wu = wl + 2;

			this.n = n;
			this.m = Math.round((12 * ss - n * wl * wl - 4 * n * wl - 3 * n) / (-4 * wl - 4));

			this.wlBox = new AverageBoxFilter(wl);
			this.wuBox = new AverageBoxFilter(wu);
		}
	}

	@Override
	public void processImage(FImage image) {
		for (int i = 0; i < m; i++)
			wlBox.processImage(image);
		for (int i = 0; i < n - m; i++)
			wuBox.processImage(image);
	}
}
