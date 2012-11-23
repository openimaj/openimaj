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
