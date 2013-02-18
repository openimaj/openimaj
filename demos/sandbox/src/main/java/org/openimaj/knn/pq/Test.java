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
package org.openimaj.knn.pq;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.feature.local.FloatLocalFeatureAdaptor;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.KeypointLocation;
import org.openimaj.knn.FloatNearestNeighboursExact;

public class Test {
	public static void main(String[] args) throws IOException {
		final MemoryLocalFeatureList<Keypoint> keys = MemoryLocalFeatureList.read(new File(
				"/Users/jsh2/Data/ukbench/sift/ukbench00000.jpg"), Keypoint.class);
		final List<FloatLocalFeatureAdaptor<KeypointLocation>> fkeys = FloatLocalFeatureAdaptor.wrap(keys);

		final float[][] data = new float[Math.min(fkeys.size(), 1000)][];

		for (int i = 0; i < data.length; i++) {
			data[i] = fkeys.get(i).getFeatureVector().values;
		}

		System.err.format("%d features loaded\n", data.length);

		final FloatProductQuantiser pq = FloatProductQuantiserUtilities.train(data, 8, 100);

		final float[][][] centroids = new float[pq.assigners.length][][];
		for (int i = 0; i < pq.assigners.length; i++)
			centroids[i] = ((FloatNearestNeighboursExact) pq.assigners[i]).getPoints();

		System.out.format("%s\t%s\t%s\n", "Exact", "ADC", "SDC");
		for (int i = 0; i < data.length; i++) {
			final float[][] testPoint = { data[i] };

			final FloatNearestNeighboursExact nne = new FloatNearestNeighboursExact(testPoint);
			final FloatADCNearestNeighbours adc = new FloatADCNearestNeighbours(pq, testPoint);
			final FloatSDCNearestNeighbours sdc = new FloatSDCNearestNeighbours(pq, centroids, testPoint);

			for (int j = 0; j < 10; j++) {
				if (j == i)
					continue;

				final float[][] qu = { data[j] };
				final int[] indexes = { 0 };
				final float[] distances = { 0 };

				nne.searchNN(qu, indexes, distances);
				final float nneDist = distances[0];

				adc.searchNN(qu, indexes, distances);
				final float adcDist = distances[0];

				sdc.searchNN(qu, indexes, distances);
				final float sdcDist = distances[0];

				System.out.format("%2.4f\t%2.4f\t%2.4f\n", nneDist, adcDist, sdcDist);
			}
		}
	}
}
