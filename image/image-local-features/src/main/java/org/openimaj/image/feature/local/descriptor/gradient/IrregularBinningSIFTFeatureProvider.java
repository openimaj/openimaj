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
import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.util.array.ArrayUtils;

/**
 * Irregular binning SIFT descriptor based on this paper:
 * {@link <a href="http://www.mpi-inf.mpg.de/~hasler/download/CuiHasThoSei09igSIFT.pdf">CuiHasThoSei09igSIFT.pdf</a>}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Cui, Yan", "Hasler, Nils", "Thorm\"{a}hlen, Thorsten", "Seidel, Hans-Peter" },
		title = "Scale Invariant Feature Transform with Irregular Orientation Histogram Binning",
		year = "2009",
		booktitle = "Proceedings of the 6th International Conference on Image Analysis and Recognition",
		pages = { "258", "", "267" },
		publisher = "Springer-Verlag",
		series = "ICIAR '09",
		customData = {
				"Address", "Berlin, Heidelberg"
	})
public class IrregularBinningSIFTFeatureProvider implements GradientFeatureProvider, GradientFeatureProviderFactory {
	private final static float TWO_PI_FLOAT = (float) (Math.PI * 2);

	private final static float FULL_SIZE = 1;
	private final static float HALF_SIZE = 1 / 2;
	private final static float QUARTER_SIZE = 1 / 4;
	private final static float THREE_QUARTER_SIZE = HALF_SIZE + QUARTER_SIZE;
	private final static float THREE_EIGHTHS_SIZE = 3 / 8;
	private final static float FIVE_EIGHTHS_SIZE = 5 / 8;

	/** Number of orientation bins in the histograms */
	protected int numOriBins = 8;

	/** Threshold for the maximum allowed value in the histogram */
	protected float valueThreshold = 0.2f;

	protected float patchOrientation;

	protected float[] vec;

	/**
	 * Construct a IrregularBinningSIFTFeatureExtractor with the default
	 * parameters.
	 */
	public IrregularBinningSIFTFeatureProvider() {
		vec = new float[16 * numOriBins];
	}

	/**
	 * Construct a IrregularBinningSIFTFeatureExtractor with the default
	 * parameters.
	 * 
	 * @param numOriBins
	 *            the number of orientation bins (default 8)
	 */
	public IrregularBinningSIFTFeatureProvider(int numOriBins) {
		this.numOriBins = numOriBins;
		vec = new float[16 * numOriBins];
	}

	/**
	 * Construct a IrregularBinningSIFTFeatureExtractor with the default
	 * parameters.
	 * 
	 * @param numOriBins
	 *            the number of orientation bins (default 8)
	 * @param valueThreshold
	 *            threshold for the maximum value allowed in the histogram
	 *            (default 0.2)
	 */
	public IrregularBinningSIFTFeatureProvider(int numOriBins, float valueThreshold) {
		this.numOriBins = numOriBins;
		this.valueThreshold = valueThreshold;
		vec = new float[16 * numOriBins];
	}

	@Override
	public void addSample(float x, float y, float gradmag, float gradori) {
		// adjust the gradient angle to be relative to the patch angle
		float ori = gradori - patchOrientation;

		// adjust range to 0<=ori<2PI
		ori = ((ori %= TWO_PI_FLOAT) >= 0 ? ori : (ori + TWO_PI_FLOAT));

		if (x >= 0 && x < HALF_SIZE && y >= 0 && y < HALF_SIZE)
			assignOri(0, 0, ori, gradmag);
		if (x >= QUARTER_SIZE && x < THREE_QUARTER_SIZE && y >= 0 && y < HALF_SIZE)
			assignOri(0, 1, ori, gradmag);
		if (x >= HALF_SIZE && x < FULL_SIZE && y >= 0 && y < HALF_SIZE)
			assignOri(0, 2, ori, gradmag);

		if (x >= 0 && x < HALF_SIZE && y >= QUARTER_SIZE && y < THREE_QUARTER_SIZE)
			assignOri(0, 3, ori, gradmag);
		if (x >= HALF_SIZE && x < FULL_SIZE && y >= QUARTER_SIZE && y < THREE_QUARTER_SIZE)
			assignOri(1, 0, ori, gradmag);

		if (x >= 0 && x < HALF_SIZE && y >= HALF_SIZE && y < FULL_SIZE)
			assignOri(1, 1, ori, gradmag);
		if (x >= QUARTER_SIZE && x < THREE_QUARTER_SIZE && y >= HALF_SIZE && y < FULL_SIZE)
			assignOri(1, 2, ori, gradmag);
		if (x >= HALF_SIZE && x < FULL_SIZE && y >= HALF_SIZE && y < FULL_SIZE)
			assignOri(1, 3, ori, gradmag);

		if (x >= QUARTER_SIZE && x < HALF_SIZE && y >= QUARTER_SIZE && y < HALF_SIZE)
			assignOri(2, 0, ori, gradmag);
		if (x >= HALF_SIZE && x < THREE_QUARTER_SIZE && y >= QUARTER_SIZE && y < HALF_SIZE)
			assignOri(2, 1, ori, gradmag);

		if (x >= QUARTER_SIZE && x < HALF_SIZE && y >= HALF_SIZE && y < THREE_QUARTER_SIZE)
			assignOri(2, 2, ori, gradmag);
		if (x >= HALF_SIZE && x < THREE_QUARTER_SIZE && y >= HALF_SIZE && y < THREE_QUARTER_SIZE)
			assignOri(2, 3, ori, gradmag);

		if (x >= THREE_EIGHTHS_SIZE && x < HALF_SIZE && y >= THREE_EIGHTHS_SIZE && y < HALF_SIZE)
			assignOri(3, 0, ori, gradmag);
		if (x >= HALF_SIZE && x < FIVE_EIGHTHS_SIZE && y >= THREE_EIGHTHS_SIZE && y < HALF_SIZE)
			assignOri(3, 1, ori, gradmag);
		if (x >= THREE_EIGHTHS_SIZE && x < HALF_SIZE && y >= HALF_SIZE && y < FIVE_EIGHTHS_SIZE)
			assignOri(3, 2, ori, gradmag);
		if (x >= HALF_SIZE && x < FIVE_EIGHTHS_SIZE && y >= HALF_SIZE && y < FIVE_EIGHTHS_SIZE)
			assignOri(3, 3, ori, gradmag);
	}

	protected void assignOri(int r, int c, float orif, float mag) {
		final float oval = (float) (numOriBins * orif / (2 * Math.PI));
		final int oi = (int) ((oval >= 0.0f) ? oval : oval - 1.0f);

		final float ofrac = oval - oi;

		for (int or = 0; or < 2; or++) {
			int oindex = oi + or;
			if (oindex >= numOriBins) // Orientation wraps at 2PI.
				oindex = 0;
			final float oweight = mag * ((or == 0) ? 1.0f - ofrac : ofrac);

			vec[(4 * numOriBins * r) + (numOriBins * c) + oindex] += oweight;
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
		return new IrregularBinningSIFTFeatureProvider(numOriBins, valueThreshold);
	}

	@Override
	public float getOversamplingAmount() {
		// no need to over-sample for this feature
		return 0;
	}
}
