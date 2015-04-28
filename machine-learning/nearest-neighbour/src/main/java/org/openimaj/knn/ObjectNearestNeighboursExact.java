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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.pair.IntFloatPair;
import org.openimaj.util.queue.BoundedPriorityQueue;

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
public class ObjectNearestNeighboursExact<T> extends ObjectNearestNeighbours<T>
		implements
		IncrementalNearestNeighbours<T, float[], IntFloatPair>
{
	/**
	 * {@link NearestNeighboursFactory} for producing
	 * {@link ObjectNearestNeighboursExact}s.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 * @param <T>
	 *            Type of object being compared.
	 */
	public static final class Factory<T> implements NearestNeighboursFactory<ObjectNearestNeighboursExact<T>, T> {
		private final DistanceComparator<? super T> distance;

		/**
		 * Construct the factory with the given distance function for the
		 * produced ObjectNearestNeighbours instances.
		 *
		 * @param distance
		 *            the distance function
		 */
		public Factory(DistanceComparator<? super T> distance) {
			this.distance = distance;
		}

		@Override
		public ObjectNearestNeighboursExact<T> create(T[] data) {
			return new ObjectNearestNeighboursExact<T>(data, distance);
		}
	}

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
	public ObjectNearestNeighboursExact(final List<T> pnts, final DistanceComparator<? super T> distance) {
		super(distance);
		this.pnts = pnts;
	}

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
	public ObjectNearestNeighboursExact(final T[] pnts, final DistanceComparator<? super T> distance) {
		super(distance);
		this.pnts = Arrays.asList(pnts);
	}

	/**
	 * Construct any empty {@link ObjectNearestNeighboursExact} with the given
	 * distance function.
	 * <p>
	 * Note: If the distance function provides similarities rather than
	 * distances they are automatically inverted.
	 *
	 * @param distance
	 *            the distance function
	 */
	public ObjectNearestNeighboursExact(final DistanceComparator<T> distance) {
		super(distance);
		this.pnts = new ArrayList<T>();
	}

	@Override
	public void searchNN(final T[] qus, int[] indices, float[] distances) {
		final int N = qus.length;

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(1, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(2);
		list.add(new IntFloatPair());
		list.add(new IntFloatPair());

		for (int n = 0; n < N; ++n) {
			final List<IntFloatPair> result = search(qus[n], queue, list);

			final IntFloatPair p = result.get(0);
			indices[n] = p.first;
			distances[n] = p.second;
		}
	}

	@Override
	public void searchKNN(final T[] qus, int K, int[][] indices, float[][] distances) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, pnts.size());

		final int N = qus.length;

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(K, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(K + 1);
		for (int i = 0; i < K + 1; i++) {
			list.add(new IntFloatPair());
		}

		// search on each query
		for (int n = 0; n < N; ++n) {
			final List<IntFloatPair> result = search(qus[n], queue, list);

			for (int k = 0; k < K; ++k) {
				final IntFloatPair p = result.get(k);
				indices[n][k] = p.first;
				distances[n][k] = p.second;
			}
		}
	}

	@Override
	public void searchNN(final List<T> qus, int[] indices, float[] distances) {
		final int N = qus.size();

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(1, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(2);
		list.add(new IntFloatPair());
		list.add(new IntFloatPair());

		for (int n = 0; n < N; ++n) {
			final List<IntFloatPair> result = search(qus.get(n), queue, list);

			final IntFloatPair p = result.get(0);
			indices[n] = p.first;
			distances[n] = p.second;
		}
	}

	@Override
	public void searchKNN(final List<T> qus, int K, int[][] indices, float[][] distances) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, pnts.size());

		final int N = qus.size();

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(K, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(K + 1);
		for (int i = 0; i < K + 1; i++) {
			list.add(new IntFloatPair());
		}

		// search on each query
		for (int n = 0; n < N; ++n) {
			final List<IntFloatPair> result = search(qus.get(n), queue, list);

			for (int k = 0; k < K; ++k) {
				final IntFloatPair p = result.get(k);
				indices[n][k] = p.first;
				distances[n][k] = p.second;
			}
		}
	}

	@Override
	public List<IntFloatPair> searchKNN(T query, int K) {
		// Fix for when the user asks for too many points.
		K = Math.min(K, pnts.size());

		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(K, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(K + 1);
		for (int i = 0; i < K + 1; i++) {
			list.add(new IntFloatPair());
		}

		// search
		return search(query, queue, list);
	}

	@Override
	public IntFloatPair searchNN(final T query) {
		final BoundedPriorityQueue<IntFloatPair> queue =
				new BoundedPriorityQueue<IntFloatPair>(1, IntFloatPair.SECOND_ITEM_ASCENDING_COMPARATOR);

		// prepare working data
		final List<IntFloatPair> list = new ArrayList<IntFloatPair>(2);
		list.add(new IntFloatPair());
		list.add(new IntFloatPair());

		return search(query, queue, list).get(0);
	}

	private List<IntFloatPair> search(T query, BoundedPriorityQueue<IntFloatPair> queue, List<IntFloatPair> results)
	{
		IntFloatPair wp = null;

		// reset all values in the queue to MAX, -1
		for (final IntFloatPair p : results) {
			p.second = Float.MAX_VALUE;
			p.first = -1;
			wp = queue.offerItem(p);
		}

		// perform the search
		final int size = this.pnts.size();
		for (int i = 0; i < size; i++) {
			wp.second = ObjectNearestNeighbours.distanceFunc(distance, query, pnts.get(i));
			wp.first = i;
			wp = queue.offerItem(wp);
		}

		return queue.toOrderedListDestructive();
	}

	@Override
	public int size() {
		return this.pnts.size();
	}

	@Override
	public int[] addAll(final List<T> d) {
		final int[] indexes = new int[d.size()];

		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = this.add(d.get(i));
		}

		return indexes;
	}

	@Override
	public int add(final T o) {
		final int ret = this.pnts.size();
		this.pnts.add(o);
		return ret;
	}
}
