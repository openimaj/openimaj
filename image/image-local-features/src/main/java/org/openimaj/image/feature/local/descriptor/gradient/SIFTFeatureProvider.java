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
package org.openimaj.image.feature.local.descriptor.gradient;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.util.array.ArrayUtils;

/**
 * An extractor for SIFT features. SIFT features are basically multiple edge
 * orientation histograms constructed over a spatial grid. Samples added to the
 * SIFT histogram are weighted with a Gaussian based on the distance from the
 * centre of the sampling window. Samples are also blurred across histogram bins
 * in both the spatial and orientation directions.
 * 
 * Based on Section 6 of Lowe's IJCV paper
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@References(references = {
		@Reference(
				type = ReferenceType.Article,
				author = { "David Lowe" },
				title = "Distinctive image features from scale-invariant keypoints",
				year = "2004",
				journal = "IJCV",
				pages = { "91", "110" },
				month = "January",
				number = "2",
				volume = "60"),
		@Reference(
				type = ReferenceType.Inproceedings,
				author = { "David Lowe" },
				title = "Object recognition from local scale-invariant features",
				year = "1999",
				booktitle = "Proc. of the International Conference on Computer Vision {ICCV}",
				pages = { "1150", "1157" }
		)
})
public class SIFTFeatureProvider implements GradientFeatureProvider, GradientFeatureProviderFactory {
	private final static float TWO_PI_FLOAT = (float) (Math.PI * 2);

	/** Number of orientation bins in the histograms */
	protected int numOriBins = 8;

	/** Number of spatial bins for the x and y directions in the histograms */
	protected int numSpatialBins = 4;

	/** Threshold for the maximum allowed value in the histogram */
	protected float valueThreshold = 0.2f;

	/**
	 * 2 times the weighting Gaussian squared (normalised to the patch size in
	 * terms of spatial bins)
	 */
	protected float sigmaSq2 = 0.5f;

	protected float gaussianSigma = 1;

	protected float[] vec;

	protected float patchOrientation;

	/**
	 * Construct a {@link SIFTFeatureProvider} with the default parameters.
	 */
	public SIFTFeatureProvider() {
		this.vec = new float[numSpatialBins * numSpatialBins * numOriBins];
	}

	/**
	 * Construct a {@link SIFTFeatureProvider} with the provided options.
	 * 
	 * @param numOriBins
	 *            the number of orientation bins (default 8)
	 * @param numSpatialBins
	 *            the number of spatial bins in each direction (default 4)
	 */
	public SIFTFeatureProvider(int numOriBins, int numSpatialBins) {
		this(numOriBins, numSpatialBins, 0.2f, 1.0f);
	}

	/**
	 * Construct a {@link SIFTFeatureProvider} with the provided options.
	 * 
	 * @param numOriBins
	 *            the number of orientation bins (default 8)
	 * @param numSpatialBins
	 *            the number of spatial bins in each direction (default 4)
	 * @param valueThreshold
	 *            threshold for the maximum value allowed in the histogram
	 *            (default 0.2)
	 * @param gaussianSigma
	 *            the width of the Gaussian used for weighting samples, relative
	 *            to the half-width of the sampling window (default 1.0).
	 */
	public SIFTFeatureProvider(int numOriBins, int numSpatialBins, float valueThreshold, float gaussianSigma) {
		this.numOriBins = numOriBins;
		this.numSpatialBins = numSpatialBins;
		this.valueThreshold = valueThreshold;
		this.gaussianSigma = gaussianSigma;
		this.vec = new float[numSpatialBins * numSpatialBins * numOriBins];

		// calculate the variance of the Gaussian weighting
		final float sigma = gaussianSigma / (0.5f * numSpatialBins); // indexSigma
																		// is
																		// proportional
																		// to
																		// half
																		// the
																		// index
																		// size
		sigmaSq2 = 2 * sigma * sigma;
	}

	@Override
	public void addSample(float x, float y, float gradmag, float gradori) {
		// calculate weight based on Gaussian at the centre:
		final float dx = 0.5f - x;
		final float dy = 0.5f - y;
		final float weight = (float) Math.exp(-(dx * dx + dy * dy) / sigmaSq2);

		// weight the magnitude
		final float wmag = weight * gradmag;

		// adjust the gradient angle to be relative to the patch angle
		float ori = gradori - patchOrientation;

		// adjust range to 0<=ori<2PI
		ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

		// now add the sample to the correct bins
		interpolateSample(x, y, wmag, ori);
	}

	/**
	 * Spread the sample around the closest bins in the histogram. If there are
	 * 4 spatial bins in each direction, then a sample at 0.25 would get added
	 * equally to bins [0] and [1] in the x-direction.
	 * 
	 * @param x
	 *            the normalised x-coordinate
	 * @param y
	 *            the normalised y-coordinate
	 * @param magnitude
	 *            the magnitude of the sample
	 * @param orientation
	 *            the angle of the sample
	 */
	protected void interpolateSample(float x, float y, float magnitude, float orientation) {
		final float px = numSpatialBins * x - 0.5f; // px is now
													// 0.5<=px<=indexSize-0.5
		final float py = numSpatialBins * y - 0.5f; // py is now
													// 0.5<=py<=indexSize-0.5
		final float po = numOriBins * orientation / TWO_PI_FLOAT; // po is now
																	// 0<=po<oriSize

		// the integer parts - corresponding to the left (or equivalent) bin
		// that the sample falls in
		final int xi = (int) Math.floor(px);
		final int yi = (int) Math.floor(py);
		final int oi = (int) Math.floor(po);

		// the fractional parts - corresponding to how much goes in the right
		// bin
		// 1-xf goes in the left bin
		final float xf = px - xi;
		final float yf = py - yi;
		final float of = po - oi;

		// now spread the sample around a 2x2x2 cube (left bin, right bin each
		// each dim
		// + combinations)
		for (int yy = 0; yy < 2; yy++) {
			final int yindex = yi + yy;

			if (yindex >= 0 && yindex < numSpatialBins) {
				final float yweight = magnitude * ((yy == 0) ? 1.0f - yf : yf);

				for (int xx = 0; xx < 2; xx++) {
					final int xindex = xi + xx;

					if (xindex >= 0 && xindex < numSpatialBins) {
						final float xweight = yweight * ((xx == 0) ? 1.0f - xf : xf);

						for (int oo = 0; oo < 2; oo++) {
							int oindex = oi + oo;

							if (oindex >= numOriBins)
								oindex = 0; // Orientation wraps at 2PI.

							final float oweight = xweight * ((oo == 0) ? 1.0f - of : of);

							vec[(numSpatialBins * numOriBins * yindex) + (numOriBins * xindex) + oindex] += oweight;
						}
					}
				}
			}
		}
	}

	@Override
	public OrientedFeatureVector getFeatureVector() {
		ArrayUtils.normalise(vec);

		boolean changed = false;
		for (int i = 0; i < vec.length; i++) {
			if (vec[i] > valueThreshold) {
				vec[i] = valueThreshold;
				changed = true;
			}
		}

		if (changed)
			ArrayUtils.normalise(vec);

		// Construct the actual feature vector
		final OrientedFeatureVector fv = new OrientedFeatureVector(vec.length, patchOrientation);
		for (int i = 0; i < vec.length; i++) {
			final int intval = (int) (512.0 * vec[i]);

			fv.values[i] = (byte) (Math.min(255, intval) - 128);
		}

		return fv;
	}

	@Override
	public void setPatchOrientation(float patchOrientation) {
		this.patchOrientation = patchOrientation;
	}

	@Override
	public GradientFeatureProvider newProvider() {
		return new SIFTFeatureProvider(numOriBins, numSpatialBins, valueThreshold, gaussianSigma);
	}

	@Override
	public float getOversamplingAmount() {
		// in order to ensure a smooth interpolation,
		// half a bin's width needs to be added to the
		// sampling region all the way around the
		// sampling square (so edge bins get partial
		// contributions from outside the square)
		return 1.0f / numSpatialBins / 2;
	}
}
