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
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}
	
	/**
	 * @return the eps of this dbscan
	 */
	public double getEps() {
		return this.eps;
	}

	/**
	 * @param eps the new eps
	 */
	public void setEps(double eps) {
		this.eps = eps;
	}
	
	@Override
	public String toString() {
		return String.format("%s: eps=%2.2f, minpts=%d",this.getClass().getSimpleName(),eps,minPts);
	}

}