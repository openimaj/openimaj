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
import org.openimaj.image.processor.SinglebandImageProcessor;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * {@link FImage} correlation performed using an FFT.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FourierCorrelation implements SinglebandImageProcessor<Float, FImage> {
	/**
	 * The template image
	 */
	public FImage template;
	
	/**
	 * Construct the correlation operator with the given template
	 * @param template the template
	 */
	public FourierCorrelation(FImage template) {
		this.template = template;
	}
	
	@Override
	public void processImage(FImage image) {
		correlate(image, template, true);
	}

	/**
	 * Correlate an image with a kernel using an FFT.
	 * @param image The image 
	 * @param template The template to correlate with the image
	 * @param inplace if true, then output overwrites the input, otherwise a new image is created.
	 * @return correlation map
	 */
	public static FImage correlate(FImage image, FImage template, boolean inplace) {
		final int cols = image.getCols();
		final int rows = image.getRows();

		FloatFFT_2D fft = new FloatFFT_2D(rows, cols);

		float[][] preparedImage = FourierTransform.prepareData(image.pixels, rows, cols, false);
		fft.complexForward(preparedImage);

		float[][] preparedKernel = FourierTransform.prepareData(template.pixels, rows, cols, false);
		fft.complexForward(preparedKernel);

		for(int y = 0; y < rows; y++) {
			for(int x = 0; x < cols; x++) {
				float reImage = preparedImage[y][x*2];
				float imImage = preparedImage[y][1 + x*2];

				float reKernel = preparedKernel[y][x*2];
				float imKernelConj = -1 * preparedKernel[y][1 + x*2];

				float re = reImage * reKernel - imImage * imKernelConj;
				float im = reImage * imKernelConj + imImage * reKernel;

				preparedImage[y][x*2] = re;
				preparedImage[y][1 + x*2] = im;
			}
		}

		fft.complexInverse(preparedImage, true);

		FImage out = image;
		if (!inplace) 
			out = new FImage(cols, rows);

		FourierTransform.unprepareData(preparedImage, out, false);
		
		return out;
	}
}
