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
package org.openimaj.ml.clustering.incremental;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SparseMatrixClusterer;
import org.openimaj.util.pair.IntDoublePair;

/**
 *
 * An {@link IncrementalSparseClusterer} which also has a notion of a lifetime.
 * A lifetime is the number of windows which a given item is allowed to live before it
 * must be clustered. 
 * 
 * By default this value is 3, meaning if 3 windows pass and a given row was not in a valid cluster,
 * it is classified as noise and removed from clustering. More specifically it is added to a cluster
 * of its own in isolation
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IncrementalLifetimeSparseClusterer extends IncrementalSparseClusterer{
	
	

	private int lifetime;
	private TIntIntHashMap seenCount;

	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 */
	public IncrementalLifetimeSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window) {
		super(clusterer, window);
		this.lifetime = 3;
		this.seenCount = new TIntIntHashMap();
	}
	
	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 * @param lifetime 
	 */
	public IncrementalLifetimeSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window, int lifetime) {
		super(clusterer, window);
		this.lifetime = lifetime;
		this.seenCount = new TIntIntHashMap();
	}
	
	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 * @param threshold 
	 * @param lifetime 
	 */
	public IncrementalLifetimeSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window, double threshold, int lifetime) {
		super(clusterer, window, threshold);
		this.lifetime = lifetime;
		this.seenCount = new TIntIntHashMap();
	}
	
	@Override
	protected void detectInactive(IndexClusters oldClusters, IndexClusters newClusters, TIntSet inactiveRows, List<int[]> completedClusters) {
		Map<Integer, IntDoublePair> stability = calculateStability(oldClusters,newClusters, inactiveRows);
		for (Entry<Integer, IntDoublePair> e : stability.entrySet()) {
			int[] completedCluster = oldClusters.clusters()[e.getKey()];
			if(e.getValue().second >= threshold){
				for (int i = 0; i < completedCluster.length; i++) {
					int index = completedCluster[i];
					inactiveRows.add(index);
					this.seenCount.remove(index);
				}
				completedClusters.add(completedCluster);
			}
			else{
				for (int i = 0; i < completedCluster.length; i++) {
					int index = completedCluster[i];
					int newCount = this.seenCount.adjustOrPutValue(index, 1, 1);
					if(newCount>= this.lifetime){
						this.seenCount.remove(index);
						inactiveRows.add(index);
						completedClusters.add(new int[]{index});
					}
				}
			}
		}
	}
	

	
}
