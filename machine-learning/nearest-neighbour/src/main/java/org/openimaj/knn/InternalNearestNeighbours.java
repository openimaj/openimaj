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

/**
 * Interface for K-nearest-neighbour implementations that are able to search
 * directly using an indexed item of their own internal data as the query.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DISTANCES>
 *            The type of distances measured (usually an array type)
 */
public interface InternalNearestNeighbours<DISTANCES> {
	/**
	 * Search for the nearest neighbour to each of the N queries (given by their
	 * index in this nearest neighbours object), and return the index of each
	 * nearest neighbour and the respective distance.
	 * <p>
	 * <strong>This method should not return the same index as the query (i.e.
	 * technically it should find the second-nearest-neighbour)</strong>
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * 
	 * @param qus
	 *            An array of N query vectors
	 * @param nnIndices
	 *            The return N-dimensional array for holding the indices of the
	 *            nearest neighbour of each respective query.
	 * @param nnDistances
	 *            The return N-dimensional array for holding the distances of
	 *            the nearest neighbour to each respective query.
	 */
	public abstract void searchNN(final int[] qus, int[] nnIndices, DISTANCES nnDistances);

	/**
	 * Search for the K nearest neighbours to each of the N queries, and return
	 * the indices of each nearest neighbour and their respective distances.
	 * <p>
	 * <strong>This method should not return the same index as the query (i.e.
	 * technically it should find the second-nearest-neighbour as the first
	 * returned value)</strong>
	 * <p>
	 * For efficiency, to use this method, you need to pre-construct the arrays
	 * for storing the results outside of the method and pass them in as
	 * arguments.
	 * 
	 * @param qus
	 *            An array of N query indices
	 * @param K
	 *            the number of neighbours to find
	 * @param nnIndices
	 *            The return N*K-dimensional array for holding the indices of
	 *            the K nearest neighbours of each respective query.
	 * @param nnDistances
	 *            The return N*K-dimensional array for holding the distances of
	 *            the nearest neighbours of each respective query.
	 */
	public abstract void searchKNN(final int[] qus, int K, int[][] nnIndices, DISTANCES[] nnDistances);
}
