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
package org.openimaj.knn;

import jal.objects.BinaryPredicate;
import jal.objects.Sorting;

import java.util.List;

import org.openimaj.util.array.ArrayUtils;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.pair.FloatIntPair;

/**
 * Exact (brute-force) k-nearest-neighbour implementation for objects with a
 * compatible {@link DistanceComparator}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being compared.
 */
public class ObjectNearestNeighboursExact<T> extends ObjectNearestNeighbours<T> {
	protected final List<T> pnts;

	/**
	 * Construct the {@link ObjectNearestNeighboursExact} over the provided
	 * dataset with the given distance function.
	 * <p>
	 * Note: If the distance function provides similarities rather than
	 * distances they are automatically inverted.
	 * 
	 * @param pnts
	 *            the dataset
	 * @param distance
	 *            the distance function
	 */
	public ObjectNearestNeighboursExact(final List<T> pnts, final DistanceComparator<T> distance) {
		super(distance);
		this.pnts = pnts;
	}

	@Override
	public void searchNN(final List<T> qus, int[] argmins, float[] mins) {
		final int N = qus.size();
		final float[] dsqout = new float[pnts.size()];

		for (int n = 0; n < N; ++n) {
			distanceFunc(distance, qus.get(n), pnts, dsqout);

			argmins[n] = ArrayUtils.minIndex(dsqout);

			mins[n] = dsqout[argmins[n]];
		}
	}

	@Override
	public void searchKNN(final List<T> qus, int K, int[][] argmins, float[][] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, pnts.size());

		final float[] dsqout = new float[pnts.size()];
		final int N = qus.size();

		final FloatIntPair[] knn_prs = new FloatIntPair[pnts.size()];

		for (int n = 0; n < N; ++n) {
			distanceFunc(distance, qus.get(n), pnts, dsqout);

			for (int p = 0; p < pnts.size(); ++p)
				knn_prs[p] = new FloatIntPair(dsqout[p], p);

			Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
				@Override
				public boolean apply(Object arg0, Object arg1) {
					final FloatIntPair p1 = (FloatIntPair) arg0;
					final FloatIntPair p2 = (FloatIntPair) arg1;

					if (p1.first < p2.first)
						return true;
					if (p2.first < p1.first)
						return false;
					return (p1.second < p2.second);
				}
			});

			for (int k = 0; k < K; ++k) {
				argmins[n][k] = knn_prs[k].second;
				mins[n][k] = knn_prs[k].first;
			}
		}
	}

	@Override
	public void searchNN(final T[] qus, int[] argmins, float[] mins) {
		final int N = qus.length;
		final float[] dsqout = new float[pnts.size()];

		for (int n = 0; n < N; ++n) {
			distanceFunc(distance, qus[n], pnts, dsqout);

			argmins[n] = ArrayUtils.minIndex(dsqout);

			mins[n] = dsqout[argmins[n]];
		}
	}

	@Override
	public void searchKNN(final T[] qus, int K, int[][] argmins, float[][] mins) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, pnts.size());

		final float[] dsqout = new float[pnts.size()];
		final int N = qus.length;

		final FloatIntPair[] knn_prs = new FloatIntPair[pnts.size()];

		for (int n = 0; n < N; ++n) {
			distanceFunc(distance, qus[n], pnts, dsqout);

			for (int p = 0; p < pnts.size(); ++p)
				knn_prs[p] = new FloatIntPair(dsqout[p], p);

			Sorting.partial_sort(knn_prs, 0, K, knn_prs.length, new BinaryPredicate() {
				@Override
				public boolean apply(Object arg0, Object arg1) {
					final FloatIntPair p1 = (FloatIntPair) arg0;
					final FloatIntPair p2 = (FloatIntPair) arg1;

					if (p1.first < p2.first)
						return true;
					if (p2.first < p1.first)
						return false;
					return (p1.second < p2.second);
				}
			});

			for (int k = 0; k < K; ++k) {
				argmins[n][k] = knn_prs[k].second;
				mins[n][k] = knn_prs[k].first;
			}
		}
	}

	@Override
	public int size() {
		return pnts.size();
	}
}
