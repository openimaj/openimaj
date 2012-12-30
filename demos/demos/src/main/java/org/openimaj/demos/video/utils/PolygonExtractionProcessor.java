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
package org.openimaj.demos.video.utils;

import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.renderer.ScanRasteriser;
import org.openimaj.image.renderer.ScanRasteriser.ScanLineListener;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Extract a polygon from an image. The output image will just contain the
 * polygon. The polygon is rasterised using a {@link ScanRasteriser}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The pixel type
 * @param <S>
 *            The {@link Image} type
 */
public class PolygonExtractionProcessor<T, S extends Image<T, S>> implements SinglebandImageProcessor<T, S> {
	private Polygon polygon;
	private T background;

	/**
	 * Construct with the given polygon and background colour
	 * 
	 * @param p
	 * @param colour
	 */
	public PolygonExtractionProcessor(Polygon p, T colour) {
		this.polygon = p;
		this.background = colour;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
	 * .image.Image)
	 */
	@Override
	public void processImage(final S image) {
		final Polygon p = this.polygon;
		final Rectangle r = p.calculateRegularBoundingBox();

		final int dx = (int) r.x;
		final int dy = (int) r.y;

		final S output = image.newInstance((int) r.width, (int) r.height);
		output.fill(background);

		ScanRasteriser.scanFill(p.getVertices(), new ScanLineListener() {
			@Override
			public void process(int x1, int x2, int y) {
				for (int x = x1; x <= x2; x++) {
					output.setPixel(x - dx, y - dy, image.getPixel(x, y));
				}
			}
		});

		image.internalAssign(output);
	}
}
