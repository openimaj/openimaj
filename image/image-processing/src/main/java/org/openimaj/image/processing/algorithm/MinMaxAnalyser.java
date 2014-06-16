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
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.Pixel;

/**
 * Analyser that computes for every pixel the minimum and maximum of its
 * neighbours. This is equivalent to greyscale morphological erosion and
 * dilation.
 * 
 * @see MinFilter
 * @see MaxFilter
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class MinMaxAnalyser implements ImageAnalyser<FImage> {
	private Set<Pixel> support;
	private int blockWidth = -1;
	private int blockHeight = -1;

	/**
	 * The min filtered image computed from the last call to
	 * {@link #analyseImage(FImage)}
	 */
	public FImage min;

	/**
	 * The max filtered image computed from the last call to
	 * {@link #analyseImage(FImage)}
	 */
	public FImage max;

	/**
	 * Construct with the given support region for selecting pixels to take the
	 * median from. The support mask is a set of <code>n</code> relative x, y
	 * offsets from the pixel currently being processed, and can be created
	 * using the methods or constants in the {@link FilterSupport} class.
	 * 
	 * @param support
	 *            the support coordinates
	 */
	public MinMaxAnalyser(Set<Pixel> support) {
		this.support = support;

		if (FilterSupport.isBlockSupport(support)) {
			blockWidth = FilterSupport.getSupportWidth(support);
			blockHeight = FilterSupport.getSupportHeight(support);
		}
	}

	@Override
	public void analyseImage(FImage image) {
		min = new FImage(image.width, image.height);
		max = new FImage(image.width, image.height);

		if (blockHeight >= 1 && blockWidth >= 1) {
			processBlock(image, blockWidth, blockHeight);
		} else {
			for (int y = 0; y < image.height; y++) {
				for (int x = 0; x < image.width; x++) {
					float minv = Float.MAX_VALUE;
					float maxv = -Float.MAX_VALUE;

					for (final Pixel sp : support) {
						final int xx = x + sp.x;
						final int yy = y + sp.y;

						if (xx >= 0 && xx < image.width - 1 && yy >= 0 && yy < image.height -
								1)
						{
							minv = Math.min(minv, image.pixels[yy][xx]);
							maxv = Math.max(maxv, image.pixels[yy][xx]);
						}
					}

					min.pixels[y][x] = minv;
					max.pixels[y][x] = maxv;
				}
			}
		}
	}

	/**
	 * Efficient processing of block support regions through separable passes
	 * over the data.
	 * 
	 * @param image
	 *            the image
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	private void processBlock(FImage image, int width, int height) {
		final int halfWidth = width / 2;
		final float buffer[] = new float[image.width + width];

		for (int r = 0; r < image.height; r++) {
			for (int i = 0; i < halfWidth; i++)
				buffer[i] = image.pixels[r][0];
			for (int i = 0; i < image.width; i++)
				buffer[halfWidth + i] = image.pixels[r][i];
			for (int i = 0; i < halfWidth; i++)
				buffer[halfWidth + image.width + i] = image.pixels[r][image.width - 1];

			final int l = buffer.length - width;
			for (int i = 0; i < l; i++) {
				float min = Float.MAX_VALUE;
				float max = -Float.MAX_VALUE;

				for (int j = 0; j < width; j++) {
					min = Math.min(buffer[i + j], min);
					max = Math.max(buffer[i + j], max);
				}

				this.min.pixels[r][i] = min;
				this.max.pixels[r][i] = max;
			}
		}

		final int halfHeight = height / 2;

		final float minbuffer[] = new float[min.height + height];
		final float maxbuffer[] = new float[max.height + height];

		for (int c = 0; c < min.width; c++) {
			for (int i = 0; i < halfHeight; i++) {
				minbuffer[i] = min.pixels[0][c];
				maxbuffer[i] = max.pixels[0][c];
			}
			for (int i = 0; i < min.height; i++) {
				minbuffer[halfHeight + i] = min.pixels[i][c];
				maxbuffer[halfHeight + i] = max.pixels[i][c];
			}
			for (int i = 0; i < halfHeight; i++) {
				minbuffer[halfHeight + min.height + i] = min.pixels[min.height - 1][c];
				maxbuffer[halfHeight + min.height + i] = max.pixels[max.height - 1][c];
			}

			final int l = minbuffer.length - height;
			for (int i = 0; i < l; i++) {
				float minv = Float.MAX_VALUE;
				float maxv = -Float.MAX_VALUE;

				for (int j = 0; j < height; j++) {
					minv = Math.min(minbuffer[i + j], minv);
					maxv = Math.max(maxbuffer[i + j], maxv);
				}

				minbuffer[i] = minv;
				maxbuffer[i] = maxv;
			}

			for (int r = 0; r < min.height; r++) {
				min.pixels[r][c] = minbuffer[r];
				max.pixels[r][c] = maxbuffer[r];
			}
		}
	}
}
