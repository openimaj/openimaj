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

public class CachedTrainingData implements HaarTrainingData {
	float[][] responses;
	boolean[] classes;

	public CachedTrainingData(List<SummedSqTiltAreaTable> positive, List<SummedSqTiltAreaTable> negative,
			List<HaarFeature> features)
	{
		final int nfeatures = features.size();

		classes = new boolean[positive.size() + negative.size()];
		responses = new float[nfeatures][classes.length];

		for (int f = 0; f < nfeatures; f++) {
			final HaarFeature feature = features.get(f);
			int count = 0;

			for (final SummedSqTiltAreaTable t : positive) {
				responses[f][count] = feature.computeResponse(t, 0, 0);
				classes[count] = true;
				++count;
			}

			for (final SummedSqTiltAreaTable t : negative) {
				responses[f][count] = feature.computeResponse(t, 0, 0);
				classes[count] = false;
				++count;
			}
		}
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
}
