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
package org.openimaj.math.geometry.transforms.estimation.sampling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.util.CollectionSampler;
import org.openimaj.util.pair.IndependentPair;

/**
 * Implementation of the bucketing sampling strategy proposed by Zhang et al to
 * try and ensure a good spatial distribution of point-pairs for estimation of
 * geometric transforms and the fundamental matrix.
 * <p>
 * Works by splitting the space of first image points into a number of buckets.
 * When selecting the sample, a bucket is chosen at random, with the probability
 * of the bucket being picked weighted towards buckets with more points. Then a
 * point pair is picked using a uniform random from within the bucket.
 * Additionally, the algorithm attempts to pick unique buckets for each point by
 * attempting to discount previously selected buckets if possible.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Zhengyou Zhang", "Rachid Deriche", "Olivier Faugeras", "Quang-Tuan Luong" },
		title = "A robust technique for matching two uncalibrated images through the recovery of the unknown epipolar geometry ",
		year = "1995",
		journal = "Artificial Intelligence ",
		pages = { "87 ", " 119" },
		url = "http://www.sciencedirect.com/science/article/pii/0004370295000224",
		note = "Special Volume on Computer Vision ",
		number = "1--2",
		volume = "78",
		customData = {
				"issn", "0004-3702",
				"doi", "http://dx.doi.org/10.1016/0004-3702(95)00022-4",
				"keywords", "Correlation "
		})
public class BucketingSampler2d implements CollectionSampler<IndependentPair<Point2d, Point2d>> {
	private class Bucket {
		private List<IndependentPair<Point2d, Point2d>> buckets = new ArrayList<IndependentPair<Point2d, Point2d>>();
		private double interval;
	}

	/**
	 * Default number of buckets per dimension
	 */
	public final static int DEFAULT_N_BUCKETS_PER_DIM = 8;

	/**
	 * Maximum allowed number of trials in picking a bucket that has not been
	 * previously picked.
	 */
	public static int NUM_TRIALS = 100;

	private Random rng;
	private Bucket[] bucketList;

	private int nBucketsX;

	private int nBucketsY;

	/**
	 * Construct the sampler with the default number of buckets in the x and y
	 * dimensions (8).
	 */
	public BucketingSampler2d() {
		this(DEFAULT_N_BUCKETS_PER_DIM, DEFAULT_N_BUCKETS_PER_DIM);
	}

	/**
	 * Construct the sampler with the given number of buckets in each dimension.
	 * 
	 * @param nBucketsX
	 *            number of buckets in the x dimension
	 * @param nBucketsY
	 *            number of buckets in the y dimension
	 */
	public BucketingSampler2d(int nBucketsX, int nBucketsY) {
		this.nBucketsX = nBucketsX;
		this.nBucketsY = nBucketsY;
		this.rng = new Random();
	}

	@Override
	public void setCollection(Collection<? extends IndependentPair<Point2d, Point2d>> collection) {
		// find max, max
		float minx = Float.MAX_VALUE;
		float maxx = -Float.MAX_VALUE;
		float miny = Float.MAX_VALUE;
		float maxy = -Float.MAX_VALUE;

		for (final IndependentPair<Point2d, Point2d> pair : collection) {
			final Point2d first = pair.firstObject();
			final float x = first.getX();
			final float y = first.getY();

			if (x < minx)
				minx = x;
			if (x > maxx)
				maxx = x;
			if (y < miny)
				miny = y;
			if (y > maxy)
				maxy = y;
		}

		minx -= 0.001;
		maxx += 0.001;
		miny -= 0.001;
		maxy += 0.001;

		// reset buckets
		final Bucket[][] buckets = new Bucket[nBucketsY][nBucketsX];

		// build buckets
		final double bucketWidth = (maxx - minx) / (double) (buckets[0].length);
		final double bucketHeight = (maxy - miny) / (double) (buckets.length);
		int numNonEmptyBuckets = 0;

		for (final IndependentPair<Point2d, Point2d> pair : collection) {
			final Point2d first = pair.firstObject();
			final float x = first.getX();
			final float y = first.getY();

			final int bx = (int) ((x - minx) / bucketWidth);
			final int by = (int) ((y - miny) / bucketHeight);

			if (buckets[by][bx] == null) {
				buckets[by][bx] = new Bucket();
				numNonEmptyBuckets++;
			}

			buckets[by][bx].buckets.add(pair);
		}

		// compute intervals and assign buckets to the list
		bucketList = new Bucket[numNonEmptyBuckets];
		for (int y = 0, i = 0; y < buckets.length; y++) {
			for (int x = 0; x < buckets.length; x++) {
				if (buckets[y][x] != null) {
					buckets[y][x].interval = (double) buckets[y][x].buckets.size() / (double) collection.size();
					bucketList[i++] = buckets[y][x];
				}
			}
		}
	}

	@Override
	public List<IndependentPair<Point2d, Point2d>> sample(int nItems) {
		final List<IndependentPair<Point2d, Point2d>> sample =
				new ArrayList<IndependentPair<Point2d, Point2d>>(nItems);
		final boolean[] selected = new boolean[bucketList.length];
		int nSelectedBuckets = 0;

		for (int i = 0; i < nItems; i++) {
			// attempt to pick a bucket that hasn't been picked already
			int selectedBucketIdx = 0;
			for (int j = 0; j < NUM_TRIALS; j++) {
				final double r = rng.nextDouble();
				double sum = 0;
				selectedBucketIdx = -1;

				do {
					sum += bucketList[++selectedBucketIdx].interval;
				} while (sum < r);

				if (!selected[j] || nSelectedBuckets >= selected.length) {
					nSelectedBuckets++;
					break;
				}
			}

			// now pick a value from that bucket
			selected[selectedBucketIdx] = true;
			final int selectedPairIdx = rng.nextInt(bucketList[selectedBucketIdx].buckets.size());
			sample.add(bucketList[selectedBucketIdx].buckets.get(selectedPairIdx));
		}

		return sample;
	}
}
