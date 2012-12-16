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
