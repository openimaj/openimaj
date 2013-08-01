package org.openimaj.ml.clustering.dbscan;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.List;

import org.openimaj.ml.clustering.dbscan.neighbourhood.RegionMode;
import org.openimaj.util.pair.IntDoublePair;

/**
 * Implementation of DBSCAN (http://en.wikipedia.org/wiki/DBSCAN) using
 * a
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DBSCAN {
	/**
	 *
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	public static class State{

		TIntHashSet visited = new TIntHashSet();
		TIntHashSet noise = new TIntHashSet();
		TIntHashSet addedToCluster = new TIntHashSet();
		TIntObjectHashMap<TIntList> clusters = new TIntObjectHashMap<TIntList>();
		private RegionMode<IntDoublePair> regionMode;
		private int length;
		/**
		 * @param length
		 * @param regionMode
		 */
		public State(int length, RegionMode<IntDoublePair> regionMode){
			this.regionMode = regionMode;
			this.length = length;
		}
	}

	DoubleDBSCANClusters dbscan(State state) {
		int clusterIndex = 0;
		for (int p = 0; p < state.length; p++) {
			if(state.visited.contains(p))continue;
			List<IntDoublePair> region = state.regionMode.regionQuery(p);
			if(!state.regionMode.validRegion(region)){
				state.noise.add(p);
			}
			else{
				TIntList cluster = new TIntArrayList();
				state.clusters.put(clusterIndex, cluster);
				expandCluster(p,region,cluster,state);
				clusterIndex++;
			}
		}
		final int[][] clusterMembers = new int[state.clusters.size()][];
		final int[] nEntries = new int[1];
		state.clusters.forEachEntry(new TIntObjectProcedure<TIntList>() {
			@Override
			public boolean execute(int cluster, TIntList b) {
				clusterMembers[cluster] = b.toArray();
				nEntries[0] += clusterMembers[cluster].length; 
				return true;
			}
		});
		int[] noise = state.noise.toArray();
		final DoubleDBSCANClusters dbscanClusters = new DoubleDBSCANClusters(noise, clusterMembers);
		return dbscanClusters;
	}

	private void expandCluster(int p, List<IntDoublePair> region, TIntList cluster, State state) {
		addToCluster(p,cluster,state);
		for (int regionIndex = 0; regionIndex < region.size(); regionIndex++) {
			int pprime = region.get(regionIndex).first;
			if (!state.visited.contains(pprime)){
				state.visited.add(pprime);
				List<IntDoublePair> regionPrime = state.regionMode.regionQuery(pprime);
				if(state.regionMode.validRegion(regionPrime)) 
					region.addAll(regionPrime);
			}

			addToCluster(pprime, cluster, state);
		}
	}

	private void addToCluster(int p, TIntList cluster, State state) {
		if(!state.addedToCluster.contains(p)){
			cluster.add(p);
			state.addedToCluster.add(p);
		}
	}
}