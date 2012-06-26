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
import org.openimaj.image.processor.SinglebandKernelProcessor;

/**
 * Apply the sobel operator to an image. This is achieved using a kernel convolution in the X and Y. 
 * The kernels are normalised 3x3 first derivatives of a gaussian of sigma 1.0f
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FSobelMagnitude implements SinglebandKernelProcessor<Float, FImage> {

	/**
	 * The 3x3 derivative of a gaussian of sigma 1 in the x direction
	 */
	public static final FImage KERNEL_X = new FImage(new float[][] {
			{1,0,-1},
			{2,0,-2},
			{1,0,-1}	
	});

	/**
	 * The 3x3 derivative of a gaussian of sigma 1 in the x direction
	 */
	public static final FImage KERNEL_Y = new FImage(new float[][] {
			{ 1, 2, 1},
			{ 0, 0, 0},
			{-1,-2,-1}
	});

	@Override
	public int getKernelHeight() {
		return 3;
	}

	@Override
	public int getKernelWidth() {
		return 3;
	}

	@Override
	public Float processKernel(FImage patch) {
		float sumx=0, sumy=0;

		for (int r=0; r<3; r++) {
			for (int c=0; c<3; c++) {
				sumx += (KERNEL_X.pixels[2-r][2-c] * patch.pixels[r][c]);
				sumy += (KERNEL_Y.pixels[2-r][2-c] * patch.pixels[r][c]);
			}
		}

		return (float)Math.sqrt((sumx*sumx) + (sumy*sumy));
	}	
}
