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
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Adaptive local thresholding using the Gaussian weighted sum of the patch and
 * an offset.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class AdaptiveLocalThresholdGaussian implements SinglebandImageProcessor<Float, FImage> {
	private float offset;
	private float sigma;

	/**
	 * Construct the thresholding operator with the given Gaussian standard
	 * deviation, sigma, and offset
	 * 
	 * @param sigma
	 *            Gaussian kernel standard deviation
	 * @param offset
	 *            offset from the patch mean at which the threshold occurs
	 */
	public AdaptiveLocalThresholdGaussian(float sigma, float offset) {
		this.sigma = sigma;
		this.offset = offset;
	}

	@Override
	public void processImage(FImage image) {
		final FImage tmp = image.process(new FGaussianConvolve(sigma));

		final float[][] tpix = tmp.pixels;
		final float[][] ipix = image.pixels;
		for (int y = 0; y < image.height; y++)
			for (int x = 0; x < image.width; x++)
				tpix[y][x] = ipix[y][x] < (tpix[y][x] - offset) ? 0f : 1f;

		image.internalAssign(tmp);
	}
}
