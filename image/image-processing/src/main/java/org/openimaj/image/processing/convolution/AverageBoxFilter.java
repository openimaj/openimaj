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
import org.openimaj.image.analysis.algorithm.SummedAreaTable;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * A rectangular averaging convolution operator (often known as a Box filter).
 * For efficiency, this is implemented using a {@link SummedAreaTable} rather
 * than through an actual convolution.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class AverageBoxFilter implements SinglebandImageProcessor<Float, FImage> {
	private int width;
	private int height;

	/**
	 * Construct the averaging operator with a kernel of the given dimensions.
	 * 
	 * @param width
	 *            width of the kernel
	 * @param height
	 *            height of the kernel
	 */
	public AverageBoxFilter(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Construct the averaging operator with a square kernel of the given
	 * dimension.
	 * 
	 * @param dim
	 *            The width and height of the box
	 */
	public AverageBoxFilter(int dim) {
		this(dim, dim);
	}

	@Override
	public void processImage(FImage image) {
		// shortcut trivial case
		if (this.height == 1 && this.width == 1)
			return;

		final SummedAreaTable sat = new SummedAreaTable();
		sat.analyseImage(image);

		final int hw = width / 2;
		final int hh = height / 2;

		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				final int sx = Math.max(0, x - hw);
				final int sy = Math.max(0, y - hh);
				final int ex = Math.min(image.width, x + hw + 1);
				final int ey = Math.min(image.height, y + hh + 1);

				final int area = (ex - sx) * (ey - sy);
				final float mean = sat.calculateArea(sx, sy, ex, ey) / area;
				image.pixels[y][x] = mean;
			}
		}
	}
}
