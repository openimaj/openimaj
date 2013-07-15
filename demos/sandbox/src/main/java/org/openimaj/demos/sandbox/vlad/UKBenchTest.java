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
package org.openimaj.demos.sandbox.vlad;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.indexing.vlad.VLADIndexerData;
import org.openimaj.io.IOUtils;
import org.openimaj.knn.pq.FloatADCNearestNeighbours;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.IntObjectPair;
import org.openimaj.util.parallel.Parallel;

public class UKBenchTest {
	public static void index() throws IOException {
		final VLADIndexerData indexer = VLADIndexerData.read(new File("/Users/jsh2/vlad-indexer-ukbench-2x-nohell.dat"));

		final List<IntObjectPair<float[]>> index = new ArrayList<IntObjectPair<float[]>>();
		final List<IntObjectPair<float[]>> syncList = Collections.synchronizedList(index);

		Parallel.forEach(Arrays.asList(new File("/Users/jsh2/Data/ukbench/sift/").listFiles()), new
				Operation<File>()
				{

					@Override
					public void perform(File f) {
						try {
							System.out.println(f);

							final int id = Integer.parseInt(f.getName().replace("ukbench",
									"").replace(".jpg", ""));

							final MemoryLocalFeatureList<Keypoint> keys =
									MemoryLocalFeatureList.read(f, Keypoint.class);

							syncList.add(new IntObjectPair<float[]>(id, indexer.extractPcaVlad(keys)));
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				});

		IOUtils.writeToFile(index, new
				File("/Users/jsh2/Desktop/ukb-nohell.idx"));
	}

	public static void search() throws IOException {
		final VLADIndexerData indexer = VLADIndexerData.read(new
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
				FloatADCNearestNeighbours(indexer.getProductQuantiser(),
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
