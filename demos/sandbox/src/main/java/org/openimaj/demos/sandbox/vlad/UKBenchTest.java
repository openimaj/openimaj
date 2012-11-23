package org.openimaj.demos.sandbox.vlad;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatADCNearestNeighbours;
import org.openimaj.util.pair.IntObjectPair;

public class UKBenchTest {
	// public static void main(String[] args) throws IOException {
	// final VLADIndexer indexer = VLADIndexer.read(new
	// File("/Users/jsh2/vlad-indexer-ukbench-2x-nohell.dat"));
	//
	// final List<IntObjectPair<float[]>> index = new
	// ArrayList<IntObjectPair<float[]>>();
	// final List<IntObjectPair<float[]>> syncList =
	// Collections.synchronizedList(index);
	//
	// Parallel.forEach(Arrays.asList(new
	// File("/Users/jsh2/Data/ukbench/sift/").listFiles()), new
	// Operation<File>()
	// {
	//
	// @Override
	// public void perform(File f) {
	// try {
	// System.out.println(f);
	//
	// final int id = Integer.parseInt(f.getName().replace("ukbench",
	// "").replace(".jpg", ""));
	//
	// final MemoryLocalFeatureList<Keypoint> keys =
	// MemoryLocalFeatureList.read(f, Keypoint.class);
	// final MemoryLocalFeatureList<FloatKeypoint> fkeys =
	// FloatKeypoint.convert(keys);
	//
	// for (final FloatKeypoint k : fkeys) {
	// HellingerNormaliser.normalise(k.vector, 0);
	// }
	//
	// indexer.index(fkeys, id, syncList);
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	//
	// IOUtils.writeToFile(index, new
	// File("/Users/jsh2/Desktop/ukb-nohell.idx"));
	// }

	public static void main(String[] args) throws IOException {
		final VLADIndexer indexer = VLADIndexer.read(new
				File("/Users/jsh2/vlad-indexer-ukbench-2x-nohell.dat"));
		final List<IntObjectPair<float[]>> index = IOUtils.readFromFile(new
				File("/Users/jsh2/Desktop/ukb-nohell.idx"));

		Collections.sort(index, new Comparator<IntObjectPair<float[]>>() {
			@Override
			public int compare(IntObjectPair<float[]> o1, IntObjectPair<float[]> o2)
			{
				return o1.first == o2.first ? 0 : o1.first < o2.first ? -1 : 1;
			}
		});

		final List<float[]> data = IntObjectPair.getSecond(index);

		final FloatADCNearestNeighbours nn = new
				FloatADCNearestNeighbours(indexer.pq,
						data.toArray(new float[data.size()][]));
		// final FloatNearestNeighboursExact nn = new
		// FloatNearestNeighboursExact(data.toArray(new float[data.size()][]));

		final DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < 10200; i++) {
			final int start = (i / 4) * 4;
			final int stop = start + 4;

			final int[][] argmins = new int[1][4];
			final float[][] mins = new float[1][4];
			nn.searchKNN(new float[][] { index.get(i).second }, 4, argmins, mins);
			final int score = score(argmins[0], start, stop);

			stats.addValue(score);
			System.out.format("Query %d, Score: %d, Mean: %f\n", i, score,
					stats.getMean());
		}
		System.out.println("Total: " + stats.getMean());
	}

	private static int score(int[] docs, int start, int stop) {
		int score = 0;
		for (final int i : docs) {
			if (i >= start && i < stop)
				score++;
		}
		return score;
	}
}
