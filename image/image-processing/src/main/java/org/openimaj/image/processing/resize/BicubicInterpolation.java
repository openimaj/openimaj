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
package org.openimaj.image.processing.resize;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.ImageInterpolation;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Bi-cubic interpolation to resize images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class BicubicInterpolation implements SinglebandImageProcessor<Float, FImage> {
	protected int width;
	protected int height;
	protected float scale;

	/**
	 * Construct a new bicubic interpolator. The parameters describe the width
	 * and height of the generated image, together with the scale that goes FROM
	 * the NEW image TO the ORIGINAL.
	 * 
	 * @param width
	 *            new width.
	 * @param height
	 *            new height.
	 * @param scale
	 *            scaling from new size to original.
	 */
	public BicubicInterpolation(int width, int height, float scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	@Override
	public void processImage(FImage image) {
		final FImage newimage = image.newInstance(width, height);

		final float[][] working = new float[4][4];

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				newimage.pixels[y][x] = ImageInterpolation.InterpolationType.BICUBIC.interpolate(x * scale, y * scale,
						image, working);

		image.internalAssign(newimage);
	}
}
