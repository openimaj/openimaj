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

import java.util.List;

import org.openimaj.image.analysis.algorithm.SummedSqTiltAreaTable;
import org.openimaj.image.objectdetection.haar.HaarFeature;
import org.openimaj.util.array.ArrayUtils;

public class BasicTrainingData implements HaarTrainingData {
	SummedSqTiltAreaTable[] sats;
	boolean[] classes;
	HaarFeature[] features;

	public BasicTrainingData(List<SummedSqTiltAreaTable> positive, List<SummedSqTiltAreaTable> negative,
			List<HaarFeature> features)
	{
		sats = new SummedSqTiltAreaTable[positive.size() + negative.size()];
		classes = new boolean[sats.length];

		int count = 0;
		for (final SummedSqTiltAreaTable t : positive) {
			sats[count] = t;
			classes[count] = true;
			++count;
		}

		for (final SummedSqTiltAreaTable t : negative) {
			sats[count] = t;
			classes[count] = false;
			++count;
		}

		this.features = features.toArray(new HaarFeature[features.size()]);
	}

	@Override
	public float[] getResponses(int dimension) {
		final float[] response = new float[sats.length];

		for (int i = 0; i < sats.length; i++) {
			final float wvNorm = computeWindowVarianceNorm(sats[i]);

			response[i] = features[dimension].computeResponse(sats[i], 0, 0) / wvNorm;
		}

		return response;
	}

	@Override
	public boolean[] getClasses() {
		return classes;
	}

	@Override
	public int numInstances() {
		return classes.length;
	}

	@Override
	public int numFeatures() {
		return features.length;
	}

	float computeWindowVarianceNorm(SummedSqTiltAreaTable sat) {
		final int w = sat.sum.width - 1 - 2;
		final int h = sat.sum.height - 1 - 2;

		final int x = 1; // shift by 1 scaled px to centre box
		final int y = 1;

		final float sum = sat.sum.pixels[y + h][x + w] + sat.sum.pixels[y][x] -
				sat.sum.pixels[y + h][x] - sat.sum.pixels[y][x + w];
		final float sqSum = sat.sqSum.pixels[y + w][x + w] + sat.sqSum.pixels[y][x] -
				sat.sqSum.pixels[y + w][x] - sat.sqSum.pixels[y][x + w];

		final float cachedInvArea = 1.0f / (w * h);
		final float mean = sum * cachedInvArea;
		float wvNorm = sqSum * cachedInvArea - mean * mean;
		wvNorm = (float) ((wvNorm >= 0) ? Math.sqrt(wvNorm) : 1);

		return wvNorm;
	}

	@Override
	public float[] getInstanceFeature(int idx) {
		final float[] feature = new float[features.length];
		final SummedSqTiltAreaTable sat = sats[idx];

		final float wvNorm = computeWindowVarianceNorm(sat);

		for (int i = 0; i < features.length; i++) {
			feature[i] = features[i].computeResponse(sat, 0, 0) / wvNorm;
		}

		return feature;
	}

	@Override
	public int[] getSortedIndices(int d) {
		return ArrayUtils.indexSort(getResponses(d));
	}

	@Override
	public HaarFeature getFeature(int dimension) {
		return features[dimension];
	}
}
