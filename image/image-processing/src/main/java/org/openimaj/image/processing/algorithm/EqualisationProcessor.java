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
package org.openimaj.image.processing.algorithm;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * An {@link ImageProcessor} that performs histogram equalisation (projecting
 * the colours back into the image).
 *
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @created 31 Mar 2011
 */
public class EqualisationProcessor implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * Equalise the colours in the image. Creates a histogram that contains as
	 * many bins as colours, equalises it, then back-projects it into an image.
	 * The resulting image has equalised values between 0 and 1. It assumes the
	 * image has already been normalised such that its values are also between 0
	 * and 1.
	 *
	 * It is assumed that there are 256 discrete grey-levels.
	 *
	 * @see "http://www.generation5.org/content/2004/histogramEqualization.asp"
	 */
	@Override
	public void processImage(FImage image) {
		// This will be a histogram of all intensities
		final int[] hg = new int[256];

		// Create the histogram
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				final int i = Math.round(255 * image.pixels[r][c]);
				hg[i]++;
			}
		}

		// Create cumulative histogram
		for (int i = 1; i < 256; i++) {
			hg[i] += hg[i - 1];
		}

		// The assumption is that the max value will be 1
		final float alpha = 255f / (image.getWidth() * image.getHeight());

		// Back-project into the new image
		for (int r = 0; r < image.height; r++) {
			for (int c = 0; c < image.width; c++) {
				final int i = Math.round(255 * image.pixels[r][c]);
				image.pixels[r][c] = Math.round(hg[i] * alpha) / 255.0f;
			}
		}
	}
}
