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

/**
 * An {@link ImageProcessor} that computes the mean of the image's pixels
 * and subtracts the mean from all pixels.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class MeanCenter implements ImageProcessor<FImage> {

	@Override
	public void processImage(FImage image) {
		final int width = image.width;
		final int height = image.height;
		final float[][] data = image.pixels;
		
		image.subtractInline(patchMean(data,0,0,width,height));
	}
	
	/**
	 * same as {@link #patchMean(float[][], int, int)} but the width and height are estimated from data[0].length and data.length
	 * @param data
	 * @return the patch mean
	 */
	public static float patchMean(final float[][] data){
		return patchMean(data,0,0,data.length > 0 && data[0]!=null ? data[0].length:0,data.length);
	}
	/**
	 * given a float array, find the mean pixel value
	 * @param data patch
	 * @param x the location of the subpatch
	 * @param y the location of the subpatch
	 * @param width patch dims
	 * @param height patch dims
	 * @return the patch mean
	 */
	public static float patchMean(final float[][] data, int x, int y, int width, int height){
		float accum = 0;
		int endX = width + x;
		int endY = height + y;
		for (int yy=y; y<endY; yy++) {
			for (int xx=x; x<endX; xx++) {
				accum += data[yy][xx];
			}
		}
		
		float mean = accum / (float)(width * height);
		return mean;
	}
}
