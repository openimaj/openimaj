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
package org.openimaj.workinprogress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.feature.SparseFloatFV;
import org.openimaj.feature.SparseFloatFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.ml.clustering.FeatureVectorCentroidsResult;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.assignment.hard.ExactFeatureVectorAssigner;
import org.openimaj.ml.clustering.kmeans.FeatureVectorKMeans;
import org.openimaj.util.array.ArrayUtils;

import cern.colt.Arrays;

public class Cluster {
	public static void main(String[] args) throws Exception {
		final File dir = new File("/Users/jon/Work/lmlk/trunk/bbc/subtitle-analyser/data_to_cluster");
		final List<String> vocab = new ArrayList<String>();
		final List<String> names = new ArrayList<String>();
		final List<SparseFloatFV> features = new ArrayList<SparseFloatFV>();

		System.err.println("Loading data");
		for (final File f : dir.listFiles()) {
			if (f.getName().startsWith("TR")) {
				final SparseFloatFV fv = loadVector(f, vocab);

				names.add(f.getName());
				features.add(fv);
			}
		}

		System.err.println("Setting lengths");
		for (final SparseFloatFV fv : features)
			fv.values.setLength(vocab.size());

		final FeatureVectorKMeans<SparseFloatFV> fkm = FeatureVectorKMeans.createExact(120,
				SparseFloatFVComparison.CORRELATION, 100);
		fkm.getConfiguration().setBlockSize(500);

		final SparseFloatFV[] data = features.toArray(new SparseFloatFV[features.size()]);
		final FeatureVectorCentroidsResult<SparseFloatFV> clusters = fkm.cluster(data);

		final ExactFeatureVectorAssigner<SparseFloatFV> eoa = new ExactFeatureVectorAssigner<SparseFloatFV>(clusters,
				SparseFloatFVComparison.CORRELATION);
		final int[][] assignments = new IndexClusters(eoa.assign(data)).clusters();

		System.out.print("[");
		for (int i = 0; i < assignments.length; i++) {
			System.out.print("{");

			System.out.print("\"name\":\"cluster" + i + "\",");

			final int[] a = assignments[i];
			final String[] items = new String[a.length];
			for (int j = 0; j < a.length; j++)
				items[j] = "\"" + names.get(a[j]) + "\"";
			System.out.print("\"items\":" + Arrays.toString(items) + ",");

			final double[] centroid = clusters.centroids[i].asDoubleVector();
			final int[] indexes = ArrayUtils.indexSort(centroid);
			System.out.print("\"labels\":[");
			for (int j = 0; j < 25; j++) {
				final int idx = indexes[indexes.length - 1 - j];
				final String tag = vocab.get(idx);
				final double score = centroid[idx];
				System.out.print("{\"tag\":\"" + tag + "\",\"weight\":" + score + "}");

				final double nextscore = centroid[indexes[indexes.length - 1 - (j + 1)]];

				if (nextscore == 0)
					break;

				if (j < 25 - 1)
					System.out.print(",");
			}

			System.out.print("]}");

			if (i < assignments.length - 1)
				System.out.print(",\n");
		}

		System.out.print("]");
	}

	private static SparseFloatFV loadVector(File f, List<String> vocab) throws IOException {
		final String str = FileUtils.readall(f);

		final String[] terms = str.split(",\\s*");
		final SparseFloatFV fv = new SparseFloatFV(vocab.size());
		for (String term : terms) {
			term = term.trim();
			if (term.length() < 1)
				continue;

			int idx = vocab.indexOf(term);
			if (idx == -1) {
				idx = vocab.size();
				vocab.add(term);
				fv.values.setLength(idx + 1);
				fv.values.set(idx, 1);
			} else {
				fv.values.increment(idx, 1);
			}
		}

		return fv;
	}
}
