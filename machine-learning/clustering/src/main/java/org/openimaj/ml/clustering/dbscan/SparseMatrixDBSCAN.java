package org.openimaj.ml.clustering.dbscan;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.ml.clustering.SparseMatrixClusterer;
import org.openimaj.ml.clustering.dbscan.neighbourhood.RegionMode;
import org.openimaj.util.pair.IntDoublePair;

import ch.akuhn.matrix.SparseMatrix;
import ch.akuhn.matrix.Vector;
import ch.akuhn.matrix.Vector.Entry;

/**
 * Implementation of DBSCAN (http://en.wikipedia.org/wiki/DBSCAN) using
 * a
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class SparseMatrixDBSCAN extends DBSCAN implements SparseMatrixClusterer<DoubleDBSCANClusters>{

	private double eps;
	private int minPts;

	/**
	 * Perform a DBScane with this configuration
	 * @param eps 
	 * @param minPts
	 */
	public SparseMatrixDBSCAN(double eps, int minPts) {
		this.eps = eps;
		this.minPts = minPts;
	}
	
	class SparseMatrixRegionMode implements RegionMode<IntDoublePair>{
		private SparseMatrix mat;
		private boolean distanceMode;
		public SparseMatrixRegionMode(SparseMatrix mat, boolean distanceMode) {
			this.mat = mat;
			this.distanceMode = distanceMode;
		}
		@Override
		public List<IntDoublePair> regionQuery(int index) {
			Vector vec = mat.row(index);
			List<IntDoublePair> ret = new ArrayList<IntDoublePair>();
			if(distanceMode){
				ret.add(IntDoublePair.pair(index, 0));
				for (Entry ent: vec.entries()) {
					double v= ent.value;
					if(v<eps)
						ret.add(IntDoublePair.pair(ent.index, v));
					else break;
				}
			}
			else{
				ret.add(IntDoublePair.pair(index, eps * 2)); // HACK
				for (Entry ent: vec.entries()) {
					if(ent.value>eps)
						ret.add(IntDoublePair.pair(ent.index, ent.value));
				}
			}
			return ret;
		}
		@Override
		public boolean validRegion(List<IntDoublePair> region) {
			return region.size() >= minPts;
		}

	}

	@Override
	public int[][] rawcluster(SparseMatrix data) {
		return this.cluster(data).clusters();
	}
	
	/**
	 * @return the eps of this dbscan
	 */
	public double getEps() {
		return this.eps;
	}

}