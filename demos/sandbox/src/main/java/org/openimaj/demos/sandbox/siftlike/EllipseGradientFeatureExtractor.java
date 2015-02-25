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
package org.openimaj.demos.sandbox.siftlike;

import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProvider;
import org.openimaj.image.feature.local.descriptor.gradient.GradientFeatureProviderFactory;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.extraction.GradientScaleSpaceImageExtractorProperties;
import org.openimaj.image.processing.convolution.FImageGradients;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;

import Jama.Matrix;

public class EllipseGradientFeatureExtractor {
	GradientFeatureProviderFactory factory;
	int patchSize = 100;

	public EllipseGradientFeatureExtractor() {
		this.factory = new SIFTFeatureProvider();
	}

	public EllipseGradientFeatureExtractor(GradientFeatureProviderFactory factory) {
		this.factory = factory;
	}

	public OrientedFeatureVector[] extract(FImage image, Ellipse ellipse) {
		final Matrix tf = ellipse.transformMatrix();
		final FImage patch = new FImage(patchSize, patchSize);
		final float halfSize = patchSize / 2;

		// Sample the ellipse content into a rectified image
		for (int y = 0; y < patchSize; y++) {
			for (int x = 0; x < patchSize; x++) {
				final Point2dImpl pt = new Point2dImpl((x - halfSize) / halfSize, (y - halfSize) / halfSize);
				final Point2dImpl tpt = pt.transform(tf);
				patch.pixels[y][x] = image.getPixelInterpNative(tpt.x, tpt.y, 0);
			}
		}

		// now find grad mags and oris
		final FImageGradients gmo = FImageGradients.getGradientMagnitudesAndOrientations(patch);

		final GradientScaleSpaceImageExtractorProperties<FImage> props = new GradientScaleSpaceImageExtractorProperties<FImage>();
		props.image = patch;
		props.magnitude = gmo.magnitudes;
		props.orientation = gmo.orientations;
		props.x = patch.width / 2;
		props.y = patch.height / 2;
		props.scale = patch.height / 2 / 3; // ???

		final DominantOrientationExtractor doe = new DominantOrientationExtractor();
		final float[] oris = doe.extractFeatureRaw(props);

		final MBFImage p2 = patch.toRGB();
		for (final float o : oris) {
			p2.drawLine(p2.getWidth() / 2, p2.getHeight() / 2, o, 20, RGBColour.RED);
		}
		DisplayUtilities.display(p2);

		final OrientedFeatureVector[] vectors = new OrientedFeatureVector[oris.length];
		for (int i = 0; i < oris.length; i++) {
			final float ori = oris[i];
			final GradientFeatureProvider provider = factory.newProvider();

			// and construct the feature and sampling every pixel in the patch
			// note: the descriptor is actually computed over a sub-patch; there
			// is
			// a border that is used for oversampling and avoiding edge effects.
			final float overSample = provider.getOversamplingAmount();
			for (int y = 0; y < patchSize; y++) {
				final float yy = (y * (2 * overSample + 1) / patchSize) - overSample;

				for (int x = 0; x < patchSize; x++) {
					final float xx = (x * (2 * overSample + 1) / patchSize) - overSample;

					final float gradmag = gmo.magnitudes.pixels[y][x];
					final float gradori = gmo.orientations.pixels[y][x];
					provider.addSample(xx, yy, gradmag, gradori - ori);
				}
			}

			vectors[i] = provider.getFeatureVector();
		}

		return vectors;
	}
}
