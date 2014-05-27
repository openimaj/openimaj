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

import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Utility methods for creating Gabor Filters
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class GaborFilters {
	private GaborFilters() {
	};

	/**
	 * Create a jet of (multiscale) Gabor filters in the frequency domain. The
	 * returned filters have the highest frequency in the centre; to apply them
	 * to an image, use
	 * {@link FourierConvolve#convolvePrepared(FImage, FImage, boolean)} with
	 * the third argument set to true.
	 * 
	 * @param width
	 *            the width of the image that will be filtered (note that the
	 *            returned filters will have twice this width to account of the
	 *            imaginary (phase) values [all of which are zero])
	 * @param height
	 *            the height of the image that will be filtered
	 * @param orientationsPerScale
	 *            the number of filter orientations for each scale (from HF to
	 *            LF)
	 * @return the jet of filters
	 */
	public static FImage[] createGaborJets(int width, int height, int... orientationsPerScale) {
		final int nscales = orientationsPerScale.length;
		final int nfilters = (int) ArrayUtils.sumValues(orientationsPerScale);

		final FImage[] filters = new FImage[nfilters];

		final double[][] param = new double[nfilters][];
		for (int i = 0, l = 0; i < nscales; i++) {
			for (int j = 0; j < orientationsPerScale[i]; j++) {
				param[l++] = new double[] {
						.35,
						.3 / Math.pow(1.85, i),
						16.0 * orientationsPerScale[i] * orientationsPerScale[i] / (32.0 * 32.0),
						Math.PI / (orientationsPerScale[i]) * j
				};
			}
		}

		final double[][] freq = new double[height][width];
		final double[][] phase = new double[height][width];

		final float hw = width / 2f;
		final float hh = height / 2f;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				final float fx = x - hw;
				final float fy = y - hh;
				freq[y][x] = Math.sqrt(fx * fx + fy * fy);
				phase[y][x] = Math.atan2(fy, fx);
			}
		}

		for (int i = 0; i < nfilters; i++) {
			filters[i] = new FImage(width * 2, height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					double tr = phase[y][x] + param[i][3];

					if (tr > Math.PI)
						tr -= 2 * Math.PI;
					else if (tr < -Math.PI)
						tr += 2 * Math.PI;

					filters[i].pixels[y][x * 2] = (float) Math.exp(-10 * param[i][0] *
							(freq[y][x] / width / param[i][1] - 1) * (freq[y][x] / width / param[i][1] - 1)
							- 2 * param[i][2] * Math.PI * tr * tr
							);
				}
			}
		}

		return filters;
	}
}
