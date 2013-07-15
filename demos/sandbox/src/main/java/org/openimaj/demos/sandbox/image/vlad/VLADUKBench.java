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
package org.openimaj.demos.sandbox.image.vlad;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.openimaj.feature.FloatFVComparison;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.indexing.vlad.VLADIndexerData;
import org.openimaj.util.function.Operation;
import org.openimaj.util.pair.FloatIntPair;
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

		final VLADIndexerData indexer = VLADIndexerData.read(new File("/Users/jsh2/vlad-indexer-ukbench-2x.dat"));

		final float[][] vlads = new float[10200][];
		Parallel.forIndex(0, 10200, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				try {
					System.out.println("Loading " + i);
					final File file = new File(String.format("/Users/jsh2/Data/ukbench/sift/ukbench%05d.jpg", i));
					final List<Keypoint> keys = MemoryLocalFeatureList.read(file, Keypoint.class);

					vlads[i] = indexer.extractPcaVlad(keys);
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
