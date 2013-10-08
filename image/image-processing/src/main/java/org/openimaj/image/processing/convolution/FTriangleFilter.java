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
import org.openimaj.image.processor.ImageProcessor;

/**
 * Image convolution with a triangular filter. Implementation is based on
 * repeated convolution with rectangular kernels, which is done efficiently
 * using a integral image style approach. Overall complexity is independent of
 * filter size and linear in the number of pixels.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class FTriangleFilter implements ImageProcessor<FImage> {
	private boolean zeropad;
	private int filterHeight;
	private int filterWidth;

	/**
	 * Construct with the given dimensions.
	 * 
	 * @param filterWidth
	 *            width of filter
	 * @param filterHeight
	 *            height of filter
	 * @param zeropad
	 *            zero-pad off the edge of the image if true; duplicate edge
	 *            value otherwise.
	 */
	public FTriangleFilter(int filterWidth, int filterHeight, boolean zeropad) {
		super();
		this.filterWidth = filterWidth;
		this.filterHeight = filterHeight;
		this.zeropad = zeropad;
	}

	/**
	 * Construct with the given dimensions. Edge effects are handled by
	 * duplicating the edge pixels.
	 * 
	 * @param filterWidth
	 *            half-width of filter
	 * @param filterHeight
	 *            half-height of filter
	 */
	public FTriangleFilter(int filterWidth, int filterHeight) {
		this(filterWidth, filterHeight, false);
	}

	@Override
	public void processImage(FImage image) {
		convolve(image, filterWidth, filterHeight, zeropad);
	}

	/**
	 * Construct a triangular kernel of the given size. The kernel will have
	 * 2*width - 1 elements.
	 * 
	 * @param width
	 *            the kernel half-width
	 * @return the triangular kernel
	 */
	public static float[] createKernel1D(int width) {
		final float[] kernel = new float[width * 2 - 1];
		final float invNorm = 1f / (width * width);

		kernel[width - 1] = width * invNorm;
		for (int i = 0; i < width - 1; i++) {
			kernel[i] = (i + 1) * invNorm;
			kernel[kernel.length - i - 1] = kernel[i];
		}
		return kernel;
	}

	static void convolve(FImage image, int filterWidth, int filterHeight, boolean zeropad) {
		convolveVertical(image, image, filterHeight, zeropad);
		convolveHorizontal(image, image, filterWidth, zeropad);
	}

	static void convolveVertical(FImage dest, FImage image, int filterSize, boolean zeropad) {
		if (image.height == 0) {
			return;
		}

		final float scale = (float) (1.0 / ((double) filterSize * (double) filterSize));
		final float[] buffer = new float[image.height + filterSize];
		final int bufferOffset = filterSize;

		for (int x = 0; x < image.width; x++) {
			// integrate backward
			buffer[bufferOffset + image.height - 1] = image.pixels[image.height - 1][x];
			int y;
			for (y = image.height - 2; y >= 0; --y) {
				buffer[bufferOffset + y] = buffer[bufferOffset + y + 1] + image.pixels[y][x];
			}
			if (zeropad) {
				for (; y >= -filterSize; y--) {
					buffer[bufferOffset + y] = buffer[bufferOffset + y + 1];
				}
			} else {
				for (; y >= -filterSize; y--) {
					buffer[bufferOffset + y] = buffer[bufferOffset + y + 1] + image.pixels[0][x];
				}
			}

			// filter forward
			for (y = -filterSize; y < image.height - filterSize; y++) {
				buffer[bufferOffset + y] = buffer[bufferOffset + y] - buffer[bufferOffset + y + filterSize];
			}
			if (!zeropad) {
				for (y = image.height - filterSize; y < image.height; ++y) {
					buffer[bufferOffset + y] =
							buffer[bufferOffset + y] - buffer[bufferOffset + image.height - 1]
									* (image.height - filterSize - y);
				}
			}

			// integrate forward
			for (y = -filterSize + 1; y < image.height; y++) {
				buffer[bufferOffset + y] += buffer[bufferOffset + y - 1];
			}

			// filter backward
			for (y = dest.height - 1; y >= 0; y--) {
				dest.pixels[y][x] = scale * (buffer[bufferOffset + y] - buffer[bufferOffset + y - filterSize]);
			}
		}
	}

	static void convolveHorizontal(FImage dest, FImage image, int filterSize, boolean zeropad) {
		if (image.width == 0) {
			return;
		}

		final float scale = (float) (1.0 / ((double) filterSize * (double) filterSize));
		final float[] buffer = new float[image.width + filterSize];
		final int bufferOffset = filterSize;

		for (int y = 0; y < image.height; y++) {
			// integrate backward
			buffer[bufferOffset + image.width - 1] = image.pixels[y][image.width - 1];
			int x;
			for (x = image.width - 2; x >= 0; --x) {
				buffer[bufferOffset + x] = buffer[bufferOffset + x + 1] + image.pixels[y][x];
			}
			if (zeropad) {
				for (; x >= -filterSize; x--) {
					buffer[bufferOffset + x] = buffer[bufferOffset + x + 1];
				}
			} else {
				for (; x >= -filterSize; x--) {
					buffer[bufferOffset + x] = buffer[bufferOffset + x + 1] + image.pixels[y][0];
				}
			}

			// filter forward
			for (x = -filterSize; x < image.width - filterSize; x++) {
				buffer[bufferOffset + x] = buffer[bufferOffset + x] - buffer[bufferOffset + x + filterSize];
			}
			if (!zeropad) {
				for (x = image.width - filterSize; x < image.width; ++x) {
					buffer[bufferOffset + x] =
							buffer[bufferOffset + x] - buffer[bufferOffset + image.width - 1]
									* (image.width - filterSize - x);
				}
			}

			// integrate forward
			for (x = -filterSize + 1; x < image.width; x++) {
				buffer[bufferOffset + x] += buffer[bufferOffset + x - 1];
			}

			// filter backward
			for (x = dest.width - 1; x >= 0; x--) {
				dest.pixels[y][x] = scale * (buffer[bufferOffset + x] - buffer[bufferOffset + x - filterSize]);
			}
		}
	}
}
