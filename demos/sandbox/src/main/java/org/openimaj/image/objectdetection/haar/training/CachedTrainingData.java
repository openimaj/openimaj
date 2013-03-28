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
import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

public class CachedTrainingData implements HaarTrainingData {
	float[][] responses;
	boolean[] classes;
	int[][] sortedIndices;
	List<HaarFeature> features;
	int width, height;

	float computeWindowVarianceNorm(SummedSqTiltAreaTable sat) {
		final int w = width - 2;
		final int h = height - 2;

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

	public CachedTrainingData(final List<SummedSqTiltAreaTable> positive, final List<SummedSqTiltAreaTable> negative,
			final List<HaarFeature> features)
	{
		this.width = positive.get(0).sum.width - 1;
		this.height = positive.get(0).sum.height - 1;

		this.features = features;
		final int nfeatures = features.size();

		classes = new boolean[positive.size() + negative.size()];
		responses = new float[nfeatures][classes.length];
		sortedIndices = new int[nfeatures][];
		// for (int f = 0; f < nfeatures; f++) {

		Parallel.forIndex(0, nfeatures, 1, new Operation<Integer>() {

			@Override
			public void perform(Integer f) {
				final HaarFeature feature = features.get(f);
				int count = 0;

				for (final SummedSqTiltAreaTable t : positive) {
					final float wvNorm = computeWindowVarianceNorm(t);
					responses[f][count] = feature.computeResponse(t, 0, 0) / wvNorm;
					classes[count] = true;
					++count;
				}

				for (final SummedSqTiltAreaTable t : negative) {
					final float wvNorm = computeWindowVarianceNorm(t);
					responses[f][count] = feature.computeResponse(t, 0, 0) / wvNorm;
					classes[count] = false;
					++count;
				}

				sortedIndices[f] = ArrayUtils.indexSort(responses[f]);
			}
		});
	}

	@Override
	public float[] getResponses(int dimension) {
		return responses[dimension];
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
		return responses.length;
	}

	@Override
	public float[] getInstanceFeature(int idx) {
		final float[] feature = new float[responses.length];

		for (int i = 0; i < feature.length; i++) {
			feature[i] = responses[i][idx];
		}

		return feature;
	}

	@Override
	public int[] getSortedIndices(int d) {
		return sortedIndices[d];
	}

	@Override
	public HaarFeature getFeature(int dimension) {
		return features.get(dimension);
	}
}
