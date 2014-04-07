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
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Image processor for FImage capable of performing convolutions with Gaussians.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FGaussianConvolve implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * The default number of sigmas at which the Gaussian function is truncated
	 * when building a kernel
	 */
	public static final float DEFAULT_GAUSS_TRUNCATE = 4.0f;

	protected float[] kernel;

	/**
	 * Construct an {@link FGaussianConvolve} with a Gaussian of standard
	 * deviation sigma.
	 * 
	 * @param sigma
	 *            Gaussian kernel standard deviation
	 */
	public FGaussianConvolve(float sigma) {
		this(sigma, DEFAULT_GAUSS_TRUNCATE);
	}

	/**
	 * Construct an {@link FGaussianConvolve} with a Gaussian of standard
	 * deviation sigma. The truncate parameter defines how many sigmas wide the
	 * kernel is.
	 * 
	 * @param sigma
	 * @param truncate
	 */
	public FGaussianConvolve(float sigma, float truncate) {
		kernel = makeKernel(sigma, truncate);
	}

	/**
	 * Construct a zero-mean Gaussian with the specified standard deviation.
	 * 
	 * @param sigma
	 *            the standard deviation of the Gaussian
	 * @return an array representing a Gaussian function
	 */
	public static float[] makeKernel(float sigma) {
		return makeKernel(sigma, DEFAULT_GAUSS_TRUNCATE);
	}

	/**
	 * Construct a zero-mean Gaussian with the specified standard deviation.
	 * 
	 * @param sigma
	 *            the standard deviation of the Gaussian
	 * @param truncate
	 *            the number of sigmas from the centre at which to truncate the
	 *            Gaussian
	 * @return an array representing a Gaussian function
	 */
	public static float[] makeKernel(float sigma, float truncate) {
		if (sigma == 0)
			return new float[] { 1f };
		// The kernel is truncated at truncate sigmas from center.
		int ksize = (int) (2.0f * truncate * sigma + 1.0f);
		// ksize = Math.max(1, ksize); // size must be at least 3
		if (ksize % 2 == 0)
			ksize++; // size must be odd

		final float[] kernel = new float[ksize];

		// build kernel
		float sum = 0.0f;
		for (int i = 0; i < ksize; i++) {
			final float x = i - ksize / 2;
			kernel[i] = (float) Math.exp(-x * x / (2.0 * sigma * sigma));
			sum += kernel[i];
		}

		// normalise area to 1
		for (int i = 0; i < ksize; i++) {
			kernel[i] /= sum;
		}

		return kernel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void processImage(FImage image) {
		FImageConvolveSeparable.convolveHorizontal(image, kernel);
		FImageConvolveSeparable.convolveVertical(image, kernel);
	}
}
