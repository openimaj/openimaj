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
