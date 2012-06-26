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

import static java.lang.Math.PI;
import static java.lang.Math.exp;

import org.openimaj.image.FImage;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * 2D Laplacian of Gaussian filter
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class LaplacianOfGaussian2D extends FConvolution {
	/**
	 * Construct with given kernel size and variance.
	 * @param width kernel width
	 * @param height kernel height
	 * @param sigma variance
	 */
	public LaplacianOfGaussian2D(int width, int height, float sigma) {
		super(createKernelImage(width, height, sigma));
	}
	
	/**
	 * Construct with given kernel size and variance.
	 * @param size kernel width/height
	 * @param sigma variance
	 */
	public LaplacianOfGaussian2D(int size, float sigma) {
		super(createKernelImage(size, size, sigma));
	}

	/**
	 * Create a kernel image with given kernel size and variance.
	 * @param size image height/width.
	 * @param sigma variance.
	 * @return new kernel image.
	 */
	public static FImage createKernelImage(int size, float sigma) {
		return createKernelImage(size, size, sigma);
	}
	
	/**
	 * Create a kernel image with given kernel size and variance.
	 * @param width image width.
	 * @param height image height.
	 * @param sigma variance.
	 * @return new kernel image.
	 */
	public static FImage createKernelImage(int width, int height, float sigma) {
		FImage f = new FImage(width, height);
		int hw = (width-1)/2;
		int hh = (height-1)/2;
		float sigmasq = sigma * sigma;
		float sigma4 = sigmasq*sigmasq;
		
		for (int y=-hh, j=0; y<hh; y++, j++) {
			for (int x=-hw, i=0; x<hw; x++, i++) {
				int radsqrd = x*x + y*y;
				f.pixels[j][i] = (float) (-1 / (PI*sigma4)*(1-radsqrd/(2*sigmasq))*exp(-radsqrd/(2*sigmasq)));	   
			}
		}
		return f.subtractInplace(FloatArrayStatsUtils.mean(f.pixels));
	}
}
