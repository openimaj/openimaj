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
package org.openimaj.image.processing.effects;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.SummedAreaTable;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.line.Line2d;

/**
 * Class to produce a Diorama or "Minature Faking" effect. The effect is
 * achieved by blurring the image with a kernel that increases size with
 * distance from a tilt-axis. In this implementation we use a
 * {@link SummedAreaTable} to efficiently compute box-blurs.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class DioramaEffect implements SinglebandImageProcessor<Float, FImage> {
	Line2d axis;

	/**
	 * Construct with the given tilt axis
	 * 
	 * @param axis
	 */
	public DioramaEffect(Line2d axis) {
		this.axis = axis;
	}

	/**
	 * Get the current tilt axis
	 * 
	 * @return the tilt axis
	 */
	public Line2d getAxis() {
		return axis;
	}

	/**
	 * Set the current tilt axis
	 * 
	 * @param axis
	 *            the tilt axis
	 */
	public void setAxis(Line2d axis) {
		this.axis = axis;
	}

	@Override
	public void processImage(FImage image) {
		render(image, new SummedAreaTable(image),
				(int) axis.getBeginPoint().getX(),
				(int) axis.getBeginPoint().getY(),
				(int) axis.getEndPoint().getX(),
				(int) axis.getEndPoint().getY());
	}

	private void render(final FImage image, final SummedAreaTable sat, final int x1, final int y1, final int x2,
			final int y2)
	{
		final int w = image.width;
		final int h = image.height;
		final double s = (w + h) * 2.0;

		final int dx = x2 - x1;
		final int dy = y2 - y1;

		final float[][] pixels = image.pixels;

		for (int y = 0; y < h; ++y)
		{
			final double yt = y - y1;
			for (int x = 0; x < w; ++x)
			{
				final double xt = x - x1;

				final double r = (dx * xt + dy * yt) / s;
				final int ri = r < 0 ? (int) -r : (int) r;

				final int yMin = Math.max(0, y - ri);
				final int yMax = Math.min(h, y + ri);
				final int bh = yMax - yMin;

				if (bh == 0)
					continue;

				final int xMin = Math.max(0, x - ri);
				final int xMax = Math.min(w, x + ri);
				final float scale = 1.0f / (xMax - xMin) / bh;

				pixels[y][x] = sat.calculateArea(xMin, yMin, xMax, yMax) * scale;
			}
		}
	}
}
