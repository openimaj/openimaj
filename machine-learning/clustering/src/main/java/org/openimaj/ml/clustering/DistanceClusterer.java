package org.openimaj.ml.clustering;

import ch.akuhn.matrix.SparseMatrix;


/**
 * A {@link DistanceClusterer} clusters data that can be represented as a distance
 * matrix. Specifically these clusterers only need to know (in some sense) how similar two 
 * things are and nothing else
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <CLUSTERS> 
 *
 */
public interface DistanceClusterer<CLUSTERS extends IndexClusters> extends SparseMatrixClusterer<CLUSTERS> {
	/**
	 * @param dist the distance matrix
	 * @return the clusters
	 */
	public CLUSTERS clusterDistance(SparseMatrix dist);
}
