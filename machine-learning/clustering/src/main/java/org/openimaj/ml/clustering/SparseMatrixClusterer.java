package org.openimaj.ml.clustering;

import ch.akuhn.matrix.SparseMatrix;

/**
 * A matrix clusterer can cluster a matrix of data in some way
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <CLUSTERS> 
 */
public interface SparseMatrixClusterer<CLUSTERS extends IndexClusters> extends DataClusterer<SparseMatrix,CLUSTERS>{

}
