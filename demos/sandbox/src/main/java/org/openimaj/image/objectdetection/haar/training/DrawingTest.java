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
package org.openimaj.image.objectdetection.haar.training;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.objectdetection.haar.HaarFeature;
import org.openimaj.image.objectdetection.haar.WeightedRectangle;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

public class DrawingTest {
	static FImage loadPositive() throws IOException {
		final String base = "/Users/jsh2/Data/att_faces/s%d/%d.pgm";

		final FImage image = new FImage(400, 400);
		for (int j = 1; j <= 40; j++) {
			for (int i = 1; i <= 10; i++) {
				final File file = new File(String.format(base, j, i));

				FImage img = ImageUtilities.readF(file);
				img = img.extractCenter(50, 50);
				img = ResizeProcessor.resample(img, 400, 400);
				image.addInplace(img);
			}
		}

		return image.normalise();
	}

	public static FImage drawRects(WeightedRectangle[] rects) {
		final FImage image = new FImage(400, 400);
		return drawRects(rects, image);
	}

	public static FImage drawRects(WeightedRectangle[] rects, FImage image) {
		final int scale = 20;
		for (final WeightedRectangle r : rects) {

			final Rectangle rect = new Rectangle(scale * r.x, scale * r.y, scale * r.width, scale * r.height);

			image.drawShape(rect, 1F);
		}
		return image;
	}

	public static void main(String[] args) throws IOException {
		final List<HaarFeature> features = HaarFeatureType.generateFeatures(20, 20, HaarFeatureType.BASIC);

		final HaarFeature f = features.get(77661);

		DisplayUtilities.display(drawRects(f.rects, loadPositive()));
	}
}
