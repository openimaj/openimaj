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

import java.util.Set;

import org.openimaj.image.FImage;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Max filter; replaces each pixel with the maximum of its neighbours. This is
 * equivalent to greyscale morphological dilation.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MaxFilter implements SinglebandImageProcessor<Float, FImage> {
	private Set<Pixel> support;
	private int blockWidth;
	private int blockHeight;

	/**
	 * Construct with the given support region for selecting pixels to take the
	 * median from. The support mask is a set of <code>n</code> relative x, y
	 * offsets from the pixel currently being processed, and can be created
	 * using the methods or constants in the {@link FilterSupport} class.
	 * 
	 * @param support
	 *            the support coordinates
	 */
	public MaxFilter(Set<Pixel> support) {
		this.support = support;

		if (FilterSupport.isBlockSupport(support)) {
			blockWidth = FilterSupport.getSupportWidth(support);
			blockHeight = FilterSupport.getSupportHeight(support);
		}
	}

	@Override
	public void processImage(FImage image) {
		if (blockWidth >= 1 && blockHeight >= 1) {
			maxHorizontalSym(image, blockWidth);
			maxVerticalSym(image, blockWidth);
		} else {
			final FImage tmpImage = new FImage(image.width, image.height);

			for (int y = 0; y < image.height; y++) {
				for (int x = 0; x < image.width; x++) {
					float max = -Float.MAX_VALUE;

					for (final Pixel sp : support) {
						final int xx = x + sp.x;
						final int yy = y + sp.y;

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height - 1) {
							max = Math.max(max, image.pixels[yy][xx]);
						}
					}

					tmpImage.pixels[y][x] = max;
				}
			}
			image.internalAssign(tmpImage);
		}
	}

	private static void maxHorizontalSym(FImage image, int width) {
		final int halfsize = width / 2;
		final float buffer[] = new float[image.width + width];

		for (int r = 0; r < image.height; r++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[r][0];
			for (int i = 0; i < image.width; i++)
				buffer[halfsize + i] = image.pixels[r][i];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.width + i] = image.pixels[r][image.width - 1];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++)
					max = Math.max(buffer[i + j], max);

				image.pixels[r][i] = max;
			}
		}
	}

	private static void maxVerticalSym(FImage image, int width) {
		final int halfsize = width / 2;

		final float buffer[] = new float[image.height + width];

		for (int c = 0; c < image.width; c++) {
			for (int i = 0; i < halfsize; i++)
				buffer[i] = image.pixels[0][c];
			for (int i = 0; i < image.height; i++)
				buffer[halfsize + i] = image.pixels[i][c];
			for (int i = 0; i < halfsize; i++)
				buffer[halfsize + image.height + i] = image.pixels[image.height - 1][c];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++)
					max = Math.max(buffer[i + j], max);

				buffer[i] = max;
			}

			for (int r = 0; r < image.height; r++)
				image.pixels[r][c] = buffer[r];
		}
	}

	/**
	 * Apply the filter some number of times to an image with the default 3x3
	 * block support
	 * 
	 * @param img
	 *            the image
	 * @param times
	 *            the number of times to apply the dilation
	 */
	public static void filter(FImage img, int times) {
		final MaxFilter d = new MaxFilter(FilterSupport.BLOCK_3x3);
		for (int i = 0; i < times; i++)
			img.processInplace(d);
	}
}
