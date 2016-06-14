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
package org.openimaj.image.annotation.evaluation.datasets.cifar;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;

/**
 * {@link BinaryReader} for CIFAR data that converts the encoded rgb pixel
 * values into an {@link MBFImage}.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MBFImageReader implements BinaryReader<MBFImage> {
	int width;
	int height;

	/**
	 * Construct with the given image height and width. The byte arrays given to
	 * {@link #read(byte[])} must be 3 * height * width in length.
	 *
	 * @param width
	 *            the image width
	 * @param height
	 *            the image height
	 */
	public MBFImageReader(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public MBFImage read(byte[] record) {
		final MBFImage image = new MBFImage(width, height, ColourSpace.RGB);
		final float[][] r = image.getBand(0).pixels;
		final float[][] g = image.getBand(1).pixels;
		final float[][] b = image.getBand(2).pixels;
		for (int y = 0, j = 0; y < height; y++) {
			for (int x = 0; x < width; x++, j++) {
				r[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j] & 0xff];
				g[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j + height * width] & 0xff];
				b[y][x] = ImageUtilities.BYTE_TO_FLOAT_LUT[record[j + 2 * height * width] & 0xff];
			}
		}
		return image;
	}
}
