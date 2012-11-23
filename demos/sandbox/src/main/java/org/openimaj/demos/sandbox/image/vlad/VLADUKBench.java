package org.openimaj.demos.sandbox.image.vlad;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.demos.sandbox.vlad.VLADIndexer;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.MultidimensionalFloatFV;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.feature.normalisation.HellingerNormaliser;
import org.openimaj.image.feature.local.keypoints.FloatKeypoint;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.pair.FloatIntPair;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openimaj.util.queue.BoundedPriorityQueue;

public class VLADUKBench {
	public static void main(String[] args) throws IOException {
		// final ByteCentroidsResult centroids = IOUtils.read(new
		// File("/Users/jsh2/Desktop/ukbench64.voc"),
		// ByteCentroidsResult.class);
		//
		// final ExactByteAssigner assigner = new ExactByteAssigner(centroids);
		// final VLAD<byte[]> vlad = new VLAD<byte[]>(assigner,
		// centroids.centroids, true);

		final VLADIndexer indexer = VLADIndexer.read(new File("/Users/jsh2/vlad-indexer-ukbench-2x.dat"));

		final float[][] vlads = new float[10200][];
		Parallel.forIndex(0, 10200, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				try {
					System.out.println("Loading " + i);
					final File file = new File(String.format("/Users/jsh2/Data/ukbench/sift/ukbench%05d.jpg", i));
					final List<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);

					final MemoryLocalFeatureList<FloatKeypoint> fkeys = FloatKeypoint.convert(keys);

					for (final FloatKeypoint k : fkeys) {
						HellingerNormaliser.normalise(k.vector, 0);
					}

					final MultidimensionalFloatFV mfv = indexer.vlad.aggregate(fkeys);
					// final DoubleFV fv = mfv == null ? new DoubleFV(64 * 128)
					// : mfv.normaliseFV(2);

					final DoubleFV fv = indexer.pca.project(mfv).normaliseFV(2);
					vlads[i] = ArrayUtils.doubleToFloat(fv.values);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});

		final DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < 10200; i++) {
			final int start = (i / 4) * 4;
			final int stop = start + 4;
			final int score = score(search(vlads[i], vlads), start, stop);
			stats.addValue(score);
			System.out.format("Query %d, Score: %d, Mean: %f\n", i, score, stats.getMean());
		}
		System.out.println("Total: " + stats.getMean());
	}

	private static int score(BoundedPriorityQueue<FloatIntPair> search, int start, int stop) {
		int score = 0;
		for (final FloatIntPair i : search) {
			if (i.second >= start && i.second < stop)
				score++;
		}
		return score;
	}

	private static BoundedPriorityQueue<FloatIntPair> search(float[] query,
			float[][] vlads)
	{
		final BoundedPriorityQueue<FloatIntPair> queue = new BoundedPriorityQueue<FloatIntPair>(4,
				new Comparator<FloatIntPair>() {

					@Override
					public int compare(FloatIntPair o1, FloatIntPair o2) {
						return Float.compare(o1.first, o2.first);
					}
				});
		for (int i = 0; i < vlads.length; i++) {
			final float[] that = vlads[i];
			final float distance = (float) FloatFVComparison.EUCLIDEAN.compare(query, that);

			queue.add(new FloatIntPair(distance, i));
		}
		return queue;
	}
}
