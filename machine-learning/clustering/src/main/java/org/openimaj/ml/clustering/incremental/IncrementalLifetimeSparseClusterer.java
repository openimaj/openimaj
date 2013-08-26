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
