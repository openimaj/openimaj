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

import edu.emory.mathcs.jtransforms.fft.FloatFFT_2D;

/**
 * From the matlab implementation of DISCGAUSSFFT which uses an FFT to apply a gaussian kernel.
 * The matlab docs:
 * 
% DISCGAUSSFFT(pic, sigma2) -- Convolves an image by the
% (separable) discrete analogue of the Gaussian kernel by
% performing the convolution in the Fourier domain.
% The parameter SIGMA2 is the variance of the kernel.

% Reference: Lindeberg "Scale-space theory in computer vision", Kluwer, 1994.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FDiscGausConvolve implements SinglebandImageProcessor<Float, FImage> {
	private float sigma2;

	/**
	 * Construct with given variance
	 * @param sigma2 variance of the kernel
	 */
	public FDiscGausConvolve(float sigma2) {
		this.sigma2 = sigma2;
//		this.fft = new FastFourierTransformer();
	}

	@Override
	public void processImage(FImage image) {
		int cs = image.getCols();
		int rs = image.getRows();
		FloatFFT_2D fft = new FloatFFT_2D(rs,cs);
		float[][] prepared = new float[rs][cs*2];
		for(int r = 0; r < rs ; r++){
			for(int c = 0; c < cs; c++){
				prepared[r][c*2] = image.pixels[r][c];
				prepared[r][1 + c*2] = 0;
			}
		}
		fft.complexForward(prepared);
		for(int y = 0; y < rs; y++){
			for(int x = 0; x < cs; x++){
				double xcos = Math.cos(2 * Math.PI * ((float)x/cs));
				double ycos = Math.cos(2 * Math.PI * ((float)y/rs));
				float multiply = (float) Math.exp(sigma2 * (xcos + ycos - 2));
				prepared[y][x*2] = prepared[y][x*2] * multiply;
				prepared[y][1 + x*2] = prepared[y][1 + x*2] * multiply;
			}
		}
		fft.complexInverse(prepared, true);
		for(int r = 0; r < rs ; r++){
			for(int c = 0; c < cs; c++){
				image.pixels[r][c] = prepared[r][c*2];
			}
		}
	}
}
