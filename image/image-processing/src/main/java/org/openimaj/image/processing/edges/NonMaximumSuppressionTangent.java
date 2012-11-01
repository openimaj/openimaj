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
package org.openimaj.image.processing.edges;

import org.openimaj.image.FImage;
import org.openimaj.image.combiner.ImageCombiner;

/**
 * Non-maximum suppression using X and Y gradient images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class NonMaximumSuppressionTangent implements ImageCombiner<FImage, FImage, FImage> {

	/**
	 * Perform non-maximum suppression.
	 * 
	 * @param dxImage
	 *            x-gradients
	 * @param dyImage
	 *            y-gradients
	 * @return non-maximum suppressed magnitude image.
	 */
	public static FImage computeSuppressed(FImage dxImage, FImage dyImage) {
		return computeSuppressed(dxImage, dyImage, null);
	}

	/**
	 * Perform non-maximum suppression.
	 * 
	 * @param dxImage
	 *            x-gradients
	 * @param dyImage
	 *            y-gradients
	 * @param magsOut
	 *            an image with the same dimensions as dxImage and dyImage for
	 *            holding the magnitudes before non-maximum suppression. May be
	 *            <code>null</code>.
	 * @return non-maximum suppressed magnitude image.
	 */
	public static FImage computeSuppressed(FImage dxImage, FImage dyImage, FImage magsOut) {
		final float[][] diffx = dxImage.pixels;
		final float[][] diffy = dyImage.pixels;
		final int width = dxImage.width;
		final int height = dxImage.height;

		final float[][] mag = magsOut == null ? new float[height][width] : magsOut.pixels;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				mag[y][x] = (float) Math.sqrt(diffx[y][x] * diffx[y][x] + diffy[y][x] * diffy[y][x]);

		final FImage outimg = new FImage(width, height);
		final float[][] output = outimg.pixels;

		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				int dx, dy;

				if (diffx[y][x] > 0)
					dx = 1;
				else
					dx = -1;

				if (diffy[y][x] > 0)
					dy = -1;
				else
					dy = 1;

				float a1, a2, b1, b2, A, B, point, val;
				if (Math.abs(diffx[y][x]) > Math.abs(diffy[y][x]))
				{
					a1 = mag[y][x + dx];
					a2 = mag[y - dy][x + dx];
					b1 = mag[y][x - dx];
					b2 = mag[y + dy][x - dx];
					A = (Math.abs(diffx[y][x]) - Math.abs(diffy[y][x])) * a1 + Math.abs(diffy[y][x]) * a2;
					B = (Math.abs(diffx[y][x]) - Math.abs(diffy[y][x])) * b1 + Math.abs(diffy[y][x]) * b2;
					point = mag[y][x] * Math.abs(diffx[y][x]);
					if (point >= A && point > B) {
						val = Math.abs(diffx[y][x]);
						output[y][x] = val;
					}
					else {
						val = 0;
						output[y][x] = val;
					}
				}
				else
				{
					a1 = mag[y - dy][x];
					a2 = mag[y - dy][x + dx];
					b1 = mag[y + dy][x];
					b2 = mag[y + dy][x - dx];
					A = (Math.abs(diffy[y][x]) - Math.abs(diffx[y][x])) * a1 + Math.abs(diffx[y][x]) * a2;
					B = (Math.abs(diffy[y][x]) - Math.abs(diffx[y][x])) * b1 + Math.abs(diffx[y][x]) * b2;
					point = mag[y][x] * Math.abs(diffy[y][x]);
					if (point >= A && point > B) {
						val = Math.abs(diffy[y][x]);
						output[y][x] = val;
					}
					else {
						val = 0;
						output[y][x] = val;
					}
				}
			}
		}

		return outimg;
	}

	/**
	 * Perform non-maximum suppression.
	 * 
	 * @param dxImage
	 *            x-gradients
	 * @param dyImage
	 *            y-gradients
	 * @return non-maximum suppressed magnitude image.
	 * 
	 * @see org.openimaj.image.combiner.ImageCombiner#combine(org.openimaj.image.Image,
	 *      org.openimaj.image.Image)
	 */
	@Override
	public FImage combine(FImage dxImage, FImage dyImage) {
		return computeSuppressed(dxImage, dyImage);
	}
}
