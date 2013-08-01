package org.openimaj.ml.clustering;

import java.util.List;

import ch.akuhn.matrix.SparseMatrix;

/**
 * A {@link MultiviewSimilarityClusterer} clusters data that can be represented as multiple
 * similarity matricies. 
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <CLUSTERS> 
 */
public interface MultiviewSimilarityClusterer<CLUSTERS extends IndexClusters> extends Clusterer<List<SparseMatrix>,CLUSTERS> {

}
