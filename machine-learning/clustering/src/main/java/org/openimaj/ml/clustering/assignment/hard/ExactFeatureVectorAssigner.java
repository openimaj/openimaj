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
package org.openimaj.ml.clustering.assignment.hard;

import java.lang.reflect.Array;
import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.knn.ObjectNearestNeighboursExact;
import org.openimaj.ml.clustering.CentroidsProvider;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.comparator.DistanceComparator;
import org.openimaj.util.pair.IntFloatPair;

/**
 * A {@link HardAssigner} that assigns points to the closest cluster based on
 * the distance to the centroid.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            Type of features
 */
public class ExactFeatureVectorAssigner<T extends FeatureVector> implements HardAssigner<T, float[], IntFloatPair> {
	protected ObjectNearestNeighboursExact<T> nn;
	protected int ndims;
	protected Class<T> clz;

	/**
	 * Construct the assigner using the given cluster data and distance
	 * function.
	 *
	 * @param provider
	 *            the cluster data provider
	 * @param comparison
	 *            the distance function
	 */
	@SuppressWarnings("unchecked")
	public ExactFeatureVectorAssigner(CentroidsProvider<T> provider, DistanceComparator<? super T> comparison) {
		final T[] centroids = provider.getCentroids();
		nn = new ObjectNearestNeighboursExact<T>(centroids, comparison);
		this.ndims = centroids[0].length();
		this.clz = (Class<T>) centroids.getClass().getComponentType();
	}

	/**
	 * Construct the assigner using the given cluster data and distance
	 * function.
	 *
	 * @param data
	 *            the cluster data
	 * @param comparison
	 *            the distance function
	 */
	@SuppressWarnings("unchecked")
	public ExactFeatureVectorAssigner(T[] data, DistanceComparator<? super T> comparison) {
		nn = new ObjectNearestNeighboursExact<T>(data, comparison);
		this.ndims = data[0].length();
		this.clz = (Class<T>) data.getClass().getComponentType();
	}

	/**
	 * Construct the assigner using the given cluster data and distance
	 * function.
	 *
	 * @param data
	 *            the cluster data
	 * @param comparison
	 *            the distance function
	 */
	@SuppressWarnings("unchecked")
	public ExactFeatureVectorAssigner(List<T> data, DistanceComparator<? super T> comparison) {
		nn = new ObjectNearestNeighboursExact<T>(data, comparison);
		this.ndims = data.get(0).length();
		this.clz = (Class<T>) data.get(0).getClass();
	}

	@Override
	public int[] assign(T[] data) {
		final int[] argmins = new int[data.length];
		final float[] mins = new float[data.length];

		nn.searchNN(data, argmins, mins);

		return argmins;
	}

	@Override
	public int assign(T data) {
		@SuppressWarnings("unchecked")
		final T[] arr = (T[]) Array.newInstance(clz, 1);
		arr[0] = data;
		return assign(arr)[0];
	}

	@Override
	public void assignDistance(T[] data, int[] indices, float[] distances) {
		nn.searchNN(data, indices, distances);
	}

	@Override
	public IntFloatPair assignDistance(T data) {
		final int[] index = new int[1];
		final float[] distance = new float[1];
		@SuppressWarnings("unchecked")
		final T[] arr = (T[]) Array.newInstance(clz, 1);
		arr[0] = data;

		nn.searchNN(arr, index, distance);

		return new IntFloatPair(index[0], distance[0]);
	}

	@Override
	public int size() {
		return nn.size();
	}

	@Override
	public int numDimensions() {
		return ndims;
	}

	/**
	 * Get the underlying nearest-neighbour implementation.
	 *
	 * @return the underlying nearest-neighbour implementation.
	 */
	public ObjectNearestNeighboursExact<T> getNN() {
		return this.nn;
	}
}
