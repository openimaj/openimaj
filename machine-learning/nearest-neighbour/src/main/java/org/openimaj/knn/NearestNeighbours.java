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

import java.util.List;

/**
 * Interface for k-nearest-neighbour calculations with some data.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <DATA>
 *            The type of data
 * @param <DISTANCES>
 *            The type of distances measured (usually an array type)
 * @param <PAIR_TYPE>
 *            The type of distance-index pair returned by the search methods
 */
public interface NearestNeighbours<DATA, DISTANCES, PAIR_TYPE> {

	/**
	 * Search for the nearest neighbour to each of the N queries, and return the
	 * index of each nearest neighbour and the respective distance.
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * <p>
	 * <b>If a nearest-neighbour cannot be determined, it will have an index
	 * value of -1</b>
	 *
	 * @param qus
	 *            An array of N query vectors
	 * @param indices
	 *            The return N-dimensional array for holding the indices of the
	 *            nearest neighbour of each respective query.
	 * @param distances
	 *            The return N-dimensional array for holding the distances of
	 *            the nearest neighbour to each respective query.
	 */
	public abstract void searchNN(final DATA[] qus, int[] indices, DISTANCES distances);

	/**
	 * Search for the K nearest neighbours to each of the N queries, and return
	 * the indices of each nearest neighbour and their respective distances.
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * <p>
	 * <b>If a k-th nearest-neighbour cannot be determined, it will have an
	 * index value of -1</b>
	 *
	 * @param qus
	 *            An array of N query vectors
	 * @param K
	 *            the number of neighbours to find
	 * @param indices
	 *            The return N*K-dimensional array for holding the indices of
	 *            the K nearest neighbours of each respective query.
	 * @param distances
	 *            The return N*K-dimensional array for holding the distances of
	 *            the nearest neighbours of each respective query.
	 */
	public abstract void searchKNN(final DATA[] qus, int K, int[][] indices, DISTANCES[] distances);

	/**
	 * Search for the nearest neighbour to each of the N queries, and return the
	 * index of each nearest neighbour and the respective distance.
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * <p>
	 * <b>If a nearest-neighbour cannot be determined, it will have an index
	 * value of -1</b>
	 *
	 * @param qus
	 *            An array of N query vectors
	 * @param indices
	 *            The return N-dimensional array for holding the indices of the
	 *            nearest neighbour of each respective query.
	 * @param distances
	 *            The return N-dimensional array for holding the distances of
	 *            the nearest neighbour to each respective query.
	 */
	public abstract void searchNN(final List<DATA> qus, int[] indices, DISTANCES distances);

	/**
	 * Search for the K nearest neighbours to each of the N queries, and return
	 * the indices of each nearest neighbour and their respective distances.
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * <p>
	 * <b>If a k-th nearest-neighbour cannot be determined, it will have an
	 * index value of -1</b>
	 *
	 * @param qus
	 *            An array of N query vectors
	 * @param K
	 *            the number of neighbours to find
	 * @param indices
	 *            The return N*K-dimensional array for holding the indices of
	 *            the K nearest neighbours of each respective query.
	 * @param distances
	 *            The return N*K-dimensional array for holding the distances of
	 *            the nearest neighbours of each respective query.
	 */
	public abstract void searchKNN(final List<DATA> qus, int K, int[][] indices, DISTANCES[] distances);

	/**
	 * Search for the K nearest neighbours to the given query and return an
	 * ordered list of pairs containing the distance and index of each
	 * neighbour.
	 * <p>
	 * If k neighbours cannot be determined, then the resultant list might have
	 * fewer than k elements.
	 *
	 * @param query
	 *            the query vector
	 * @param K
	 *            the number of neighbours to search for
	 * @return the top K nearest neighbours ordered by increasing distance
	 */
	public abstract List<PAIR_TYPE> searchKNN(DATA query, int K);

	/**
	 * Search for the nearest neighbour to the given query and return a pair
	 * containing the distance and index of that neighbour.
	 * <p>
	 * If the nearest-neighbour cannot be determined <code>null</code> will be
	 * returned.
	 *
	 * @param query
	 *            the query vector
	 * @return the distance and index of the nearest neighbour
	 */
	public abstract PAIR_TYPE searchNN(final DATA query);

	/**
	 * Get the size of the dataset
	 *
	 * @return the dataset size
	 */
	public abstract int size();
}
