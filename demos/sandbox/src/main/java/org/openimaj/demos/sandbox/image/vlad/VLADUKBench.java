package org.openimaj.demos.sandbox.image.vlad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.aggregate.VLAD;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.hard.ExactByteAssigner;
import org.openimaj.util.pair.DoubleIntPair;
import org.openimaj.util.queue.BoundedPriorityQueue;

public class VLADUKBench {
	public static void main(String[] args) throws IOException {
		final ByteCentroidsResult centroids = IOUtils.read(new File("/Users/jsh2/Desktop/ukbench64.voc"),
				ByteCentroidsResult.class);

		final ExactByteAssigner assigner = new ExactByteAssigner(centroids);
		final VLAD<byte[]> vlad = new VLAD<byte[]>(assigner, centroids.centroids, true);

		final List<MultidimensionalFloatFV> vlads = new ArrayList<MultidimensionalFloatFV>();
		for (int i = 0; i < 10200; i++) {
			System.out.println("Loading " + i);
			final File file = new File(String.format("/Users/jsh2/Data/ukbench/sift/ukbench%05d.jpg", i));
			final List<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);

			vlads.add(vlad.aggregate(keys));
		}

		final DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < 10200; i++) {
			final int start = (i / 4) * 4;
			final int stop = start + 4;
			final int score = score(search(vlads.get(i), vlads), start, stop);
			stats.addValue(score);
			System.out.format("Query %d, Score: %d, Mean: %f\n", i, score, stats.getMean());
		}
		System.out.println("Total: " + stats.getMean());
	}

	private static int score(BoundedPriorityQueue<DoubleIntPair> search, int start, int stop) {
		int score = 0;
		for (final DoubleIntPair i : search) {
			if (i.second >= start && i.second < stop)
				score++;
		}
		return score;
	}

	private static BoundedPriorityQueue<DoubleIntPair> search(MultidimensionalFloatFV query,
			List<MultidimensionalFloatFV> vlads)
	{
		final BoundedPriorityQueue<DoubleIntPair> queue = new BoundedPriorityQueue<DoubleIntPair>(4,
				new Comparator<DoubleIntPair>() {

					@Override
					public int compare(DoubleIntPair o1, DoubleIntPair o2) {
						return Double.compare(o1.first, o2.first);
					}
				});
		for (int i = 0; i < vlads.size(); i++) {
			final MultidimensionalFloatFV that = vlads.get(i);
			final double distance = that.compare(query, FloatFVComparison.EUCLIDEAN);

			queue.add(new DoubleIntPair(distance, i));
		}
		return queue;
	}
}
