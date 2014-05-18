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
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * {@link FImage} convolution performed in the fourier domain.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FourierConvolve implements SinglebandImageProcessor<Float, FImage> {
	private float[][] kernel;

	/**
	 * Construct the convolution operator with the given kernel
	 * 
	 * @param kernel
	 *            the kernel
	 */
	public FourierConvolve(float[][] kernel) {
		this.kernel = kernel;
	}

	/**
	 * Construct the convolution operator with the given kernel
	 * 
	 * @param kernel
	 *            the kernel
	 */
	public FourierConvolve(FImage kernel) {
		this.kernel = kernel.pixels;
	}

	@Override
	public void processImage(FImage image) {
		convolve(image, kernel, true);
	}

	/**
	 * Convolve an image with a kernel using an FFT.
	 * 
	 * @param image
	 *            The image to convolve
	 * @param kernel
	 *            The kernel
	 * @param inplace
	 *            if true, then output overwrites the input, otherwise a new
	 *            image is created.
	 * @return convolved image
	 */
	public static FImage convolve(FImage image, float[][] kernel, boolean inplace) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		final FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		final float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		final float[][] preparedKernel = FourierTransform.prepareData(kernel, rows, cols, false);
		fft.complexForward(preparedKernel);

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedKernel[y][x * 2];
				final float imKernel = preparedKernel[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				preparedImage[y][x * 2] = re;
				preparedImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		FImage out = image;
		if (!inplace)
			out = new FImage(cols, rows);

		FourierTransform.unprepareData(preparedImage, out, false);

		return out;
	}

	/**
	 * Convolve an image with a pre-prepared frequency domain filter. The filter
	 * must have the same height as the image and twice the width (to account
	 * for the imaginary components). Real and imaginary components should be
	 * interlaced across the rows.
	 * 
	 * @param image
	 *            The image to convolve
	 * @param filter
	 *            the prepared frequency domain filter
	 * @param centered
	 *            true if the prepared filter has the highest frequency in the
	 *            centre.
	 * @return convolved image
	 */
	public static FImage convolvePrepared(FImage image, FImage filter, boolean centered) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		final FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		final float[][] preparedImage =
				FourierTransform.prepareData(image.pixels, rows, cols, centered);
		fft.complexForward(preparedImage);

		final float[][] preparedKernel = filter.pixels;

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedKernel[y][x * 2];
				final float imKernel = preparedKernel[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				preparedImage[y][x * 2] = re;
				preparedImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		final FImage out = new FImage(cols, rows);
		FourierTransform.unprepareData(preparedImage, out, centered);
		return out;
	}
}
