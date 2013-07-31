package org.openimaj.ml.clustering;

import java.util.List;

import ch.akuhn.matrix.SparseMatrix;

/**
 * A {@link MultiviewSimilarityClusterer} clusters data that can be represented as multiple
 * similarity matricies. 
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <CLUSTERTYPE>
 *            The type of cluster produced.
 */
public interface MultiviewSimilarityClusterer<CLUSTERTYPE extends SimilarityClusters> {

	/**
	 * Perform clustering on the given data. Note that this matrix is assumed to be square and symmetric.
	 * Furthermore the diagonal elements should always taken as being "exactly similar" i.e. 0 for distance
	 * and ... identical for similarity
	 *
	 * @param data
	 *            the data.
	 * @param distanceMode 
	 * 			  if true the data matrix is treated as distance rather than similarity
	 *
	 * @return the generated clusters.
	 */
	public abstract CLUSTERTYPE cluster(List<SparseMatrix> data,boolean distanceMode);
}
