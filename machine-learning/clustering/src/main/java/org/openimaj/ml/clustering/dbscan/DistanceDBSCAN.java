package org.openimaj.ml.clustering.dbscan;

import org.openimaj.ml.clustering.DistanceClusterer;

import ch.akuhn.matrix.SparseMatrix;

/**
 * {@link DBSCAN} using a {@link SparseMatrix} of distances
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DistanceDBSCAN extends SparseMatrixDBSCAN implements DistanceClusterer<DoubleDBSCANClusters>{
	
	/**
	 * @param eps
	 * @param minPts
	 */
	public DistanceDBSCAN(double eps, int minPts) {
		super(eps, minPts);
	}

	@Override
	public DoubleDBSCANClusters cluster(SparseMatrix data) {
		return this.clusterDistance(data);
	}

	@Override
	public DoubleDBSCANClusters clusterDistance(SparseMatrix data) {
		State s = new State(data.rowCount(), new SparseMatrixRegionMode(data, true));
		return dbscan(s);
	}

	

}
