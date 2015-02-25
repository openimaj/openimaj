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
package org.openimaj.demos;

import gnu.trove.map.hash.TIntIntHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.iterator.TextLineIterable;

import cern.colt.Arrays;

public class ClusterTest {
	public static void main(String[] args) {
		final File f = new File("/Users/jsh2/sed/histograms-uploaded.txt");

		System.out.println("Loading data");
		final List<float[]> vectors = new ArrayList<float[]>();
		float[] mean = null;
		for (final String line : new TextLineIterable(f)) {
			final String[] parts = line.split(" ");
			final float[] vector = new float[parts.length];
			for (int i = 0; i < vector.length; i++) {
				vector[i] = Float.parseFloat(parts[i]);
			}
			vectors.add(vector);

			if (mean == null) {
				mean = vector.clone();
			} else {
				ArrayUtils.sum(mean, vector);
			}
		}

		System.out.println("Done");

		final boolean cluster = true;

		ArrayUtils.divide(mean, vectors.size());
		// System.out.println(Arrays.toString(mean));

		if (cluster) {
			final FloatKMeans km = FloatKMeans.createExact(50);
			final FloatCentroidsResult result = km.cluster(vectors.toArray(new float[vectors.size()][]));

			final TIntIntHashMap map = new TIntIntHashMap();
			for (final float[] vector : vectors) {
				final int clustid = result.defaultHardAssigner().assign(vector);
				map.adjustOrPutValue(clustid, 1, 1);
			}

			for (int i = 0; i < result.centroids.length; i++) {
				float[] r = result.centroids[i];

				r = ArrayUtils.divide(r, ArrayUtils.maxValue(r));

				System.out.println(Arrays.toString(r).replace("[", "").replace("]", ""));
			}
		}
	}
}
