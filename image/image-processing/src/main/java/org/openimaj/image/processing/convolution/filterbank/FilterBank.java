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
import org.openimaj.image.processing.convolution.FConvolution;
import org.openimaj.image.processor.ImageProcessor;

/**
 * A FilterBank is a set of convolution filters which can be
 * applied to an image. The filterbank allows a response vector
 * of the filter at each pixel in the image to be generated.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public abstract class FilterBank implements ImageProcessor<FImage> {
	protected FConvolution [] filters;
	protected FImage[] responses;
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void processImage(FImage image) {
		responses = new FImage[filters.length];
		
		for (int i=0; i<filters.length; i++) {
			responses[i] = image.process(filters[i]);
		}
	}

	/**
	 * Get the response images for the image processed with
	 * {@link #processImage(FImage)}.
	 * @return the filter responses.
	 */
	public FImage[] getResponseImages() {
		return responses;
	}
	
	/**
	 * Get the response vector for a given pixel.
	 * 
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 * @return the response vector
	 */
	public float[] getResponse(int x, int y) {
		float[] response = new float[responses.length];
		
		for (int i=0; i<response.length; i++)
			response[i] = responses[i].getPixelNative(x, y);
		
		return response;
	}
	
	/**
	 * Get the response vector for a given pixel as a {@link FloatFV}.
	 * 
	 * @param x the x-ordinate
	 * @param y the y-ordinate
	 * @return the response vector
	 */
	public FloatFV getResponseFV(int x, int y) {
		return new FloatFV(getResponse(x, y));
	}
	
	/**
	 * Create an image to visualise the filters in the bank.
	 * Assumes that all the filters are the same size. Filters
	 * are normalised and displayed in a grid.
	 * 
	 * @param numFiltersX number of filters to display per row
	 * @return a visualisation of the filters
	 */
	public FImage renderFilters(int numFiltersX) {
		int border = 4;
		int numFiltersY = (int) Math.ceil((double)filters.length / numFiltersX);
		int w = (border + filters[0].kernel.width);
		int width = w * (numFiltersX) + border;
		int h = (border + filters[0].kernel.height);
		int height = h * (numFiltersY) + border;
		
		FImage image = new FImage(width, height);
		image.fill(1f);
		
		int count = 0;
		for (int j=0; j<numFiltersY; j++)
			for (int i=0; i<numFiltersX && count<filters.length; i++)
				image.drawImage(filters[count++].kernel.clone().normalise(), w*i + border, h*j + border);
		
		return image;
	}
}
