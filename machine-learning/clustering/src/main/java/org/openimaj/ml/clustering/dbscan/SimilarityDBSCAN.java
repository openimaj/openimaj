package org.openimaj.ml.clustering.dbscan;

import org.openimaj.ml.clustering.SimilarityClusterer;

import ch.akuhn.matrix.SparseMatrix;

/**
 * {@link DBSCAN} using a {@link SparseMatrix} of similarities
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SimilarityDBSCAN extends SparseMatrixDBSCAN implements SimilarityClusterer<DoubleDBSCANClusters>{
	
	/**
	 * @param eps
	 * @param minPts
	 */
	public SimilarityDBSCAN(double eps, int minPts) {
		super(eps, minPts);
	}

	@Override
	public DoubleDBSCANClusters cluster(SparseMatrix data) {
		return this.clusterSimilarity(data);
	}

	@Override
	public DoubleDBSCANClusters clusterSimilarity(SparseMatrix data) {
		State s = new State(data.rowCount(), new SparseMatrixRegionMode(data, false));
		return dbscan(s);
	}

}
