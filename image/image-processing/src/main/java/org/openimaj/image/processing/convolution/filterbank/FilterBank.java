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
package org.openimaj.image.processing.convolution.filterbank;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.convolution.FConvolution;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * A FilterBank is a set of convolution filters which can be applied to an
 * image. The filterbank allows a response vector of the filter at each pixel in
 * the image to be generated. Convolution is performed in the fourier domain for
 * efficiency (the fft's of the filters are cached, and the fft of the image
 * only has to be performed once for all convolutions)
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public abstract class FilterBank implements ImageAnalyser<FImage> {
	private FConvolution[] filters;
	protected FImage[] responses;

	private FloatFFT_2D fft;
	private float[][][] preparedFilters;
	private float[][] tmpImage;
	private int paddingX;
	private int paddingY;

	protected FilterBank(FConvolution[] filters) {
		this.filters = filters;

		int maxWidth = 0;
		int maxHeight = 0;
		for (int i = 0; i < filters.length; i++) {
			maxWidth = Math.max(maxWidth, filters[i].kernel.width);
			maxHeight = Math.max(maxHeight, filters[i].kernel.height);
		}
		this.paddingX = (int) Math.ceil(maxWidth / 2);
		this.paddingY = (int) Math.ceil(maxHeight / 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openimaj.image.processor.ImageAnalyser#analyseImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void analyseImage(FImage in) {
		responses = new FImage[filters.length];

		final FImage image = in.padding(paddingX, paddingY);
		final int cols = image.getCols();
		final int rows = image.getRows();

		if (fft == null || preparedFilters == null || preparedFilters[0].length != rows
				|| preparedFilters[0][0].length != 2 * cols)
		{
			fft = new FloatFFT_2D(rows, cols);
			preparedFilters = new float[filters.length][][];
			tmpImage = new float[rows][cols * 2];

			for (int i = 0; i < preparedFilters.length; i++) {
				final float[][] preparedKernel = FourierTransform.prepareData(filters[i].kernel, rows, cols, false);
				fft.complexForward(preparedKernel);
				preparedFilters[i] = preparedKernel;
			}
		}

		final float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		for (int i = 0; i < preparedFilters.length; i++) {
			responses[i] = convolve(cols, rows, preparedImage, preparedFilters[i]);
			responses[i] = responses[i].extractROI(2 * paddingX, 2 * paddingY,
					responses[i].width - 2 * paddingX,
					responses[i].height - 2 * paddingY);
		}
	}

	private FImage
			convolve(final int cols, final int rows, final float[][] preparedImage, final float[][] preparedFilter)
	{
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				final float reImage = preparedImage[y][x * 2];
				final float imImage = preparedImage[y][1 + x * 2];

				final float reKernel = preparedFilter[y][x * 2];
				final float imKernel = preparedFilter[y][1 + x * 2];

				final float re = reImage * reKernel - imImage * imKernel;
				final float im = reImage * imKernel + imImage * reKernel;

				tmpImage[y][x * 2] = re;
				tmpImage[y][1 + x * 2] = im;
			}
		}

		fft.complexInverse(tmpImage, true);

		final FImage out = new FImage(cols, rows);
		FourierTransform.unprepareData(tmpImage, out, false);
		return out;
	}

	/**
	 * Get the response images for the image analysed with
	 * {@link #analyseImage(FImage)}.
	 * 
	 * @return the filter responses.
	 */
	public FImage[] getResponseImages() {
		return responses;
	}

	/**
	 * Get the response vector for a given pixel.
	 * 
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @return the response vector
	 */
	public float[] getResponse(int x, int y) {
		final float[] response = new float[responses.length];

		for (int i = 0; i < response.length; i++)
			response[i] = responses[i].getPixelNative(x, y);

		return response;
	}

	/**
	 * Get the response vector for a given pixel as a {@link FloatFV}.
	 * 
	 * @param x
	 *            the x-ordinate
	 * @param y
	 *            the y-ordinate
	 * @return the response vector
	 */
	public FloatFV getResponseFV(int x, int y) {
		return new FloatFV(getResponse(x, y));
	}

	/**
	 * Create an image to visualise the filters in the bank. Assumes that all
	 * the filters are the same size. Filters are normalised and displayed in a
	 * grid.
	 * 
	 * @param numFiltersX
	 *            number of filters to display per row
	 * @return a visualisation of the filters
	 */
	public FImage renderFilters(int numFiltersX) {
		final int border = 4;
		final int numFiltersY = (int) Math.ceil((double) filters.length / numFiltersX);
		final int w = (border + filters[0].kernel.width);
		final int width = w * (numFiltersX) + border;
		final int h = (border + filters[0].kernel.height);
		final int height = h * (numFiltersY) + border;

		final FImage image = new FImage(width, height);
		image.fill(1f);

		int count = 0;
		for (int j = 0; j < numFiltersY; j++)
			for (int i = 0; i < numFiltersX && count < filters.length; i++)
				image.drawImage(filters[count++].kernel.clone().normalise(), w * i + border, h * j + border);

		return image;
	}

	/**
	 * Build an array of responses for every pixel. The response for each pixel
	 * is added in scan order (left-right, top-bottom).
	 * 
	 * @return the responses for each pixel.
	 */
	public float[][] getResponses() {
		final int width = this.responses[0].width;
		final int height = this.responses[0].height;

		final float[][] resp = new float[width * height][this.responses.length];

		for (int i = 0; i < responses.length; i++) {
			for (int y = 0; y < responses[0].height; y++) {
				for (int x = 0; x < responses[0].width; x++) {
					resp[x + width * y][i] = responses[i].pixels[y][x];
				}
			}
		}

		return resp;
	}
}
