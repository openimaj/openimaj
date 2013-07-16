package org.openimaj.ml.clustering;

import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;


/**
 * A {@link SimilarityClusterer} clusters data that can be represented as a similarity
 * (or distance) matrix. Specifically these clusterers only need to know (in some sense) how
 * different two things are, and nothing else.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <CLUSTERTYPE>
 *            The type of cluster produced.
 */
public interface SimilarityClusterer<CLUSTERTYPE extends SimilarityClusters> {

	/**
	 * Perform clustering on the given data.
	 *
	 * @param data
	 *            the data.
	 * @param distanceMode if true the data matrix is treated as distance rather than similarity
	 *
	 * @return the generated clusters.
	 */
	public abstract CLUSTERTYPE cluster(SparseMatrix data,boolean distanceMode);
}
