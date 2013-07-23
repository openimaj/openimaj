package org.openimaj.ml.clustering.dbscan;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.data.DataSource;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.ml.clustering.SimilarityClusterer;
import org.openimaj.ml.clustering.SpatialClusterer;
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
public class DoubleDBSCAN implements SpatialClusterer<DoubleDBSCANClusters, double[]>, SimilarityClusterer<DoubleDBSCANClusters>{

	private DBSCANConfiguration<DoubleNearestNeighbours, double[]> conf;

	/**
	 * Perform a DBScane with this configuration
	 * @param dbsConf
	 */
	public DoubleDBSCAN(DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf) {
		this.conf = dbsConf;
	}
	class NNRegionMode implements RegionMode<IntDoublePair>{
		double[][] data;
		DoubleNearestNeighbours nn;
		public NNRegionMode(double[][] data) {
			this.data = data;
			this.nn = DoubleDBSCAN.this.conf.factory.create(data);
		}
		@Override
		public List<IntDoublePair> regionQuery(int index) {
			List<IntDoublePair> res = nn.searchKNN(data[index], data.length);
			List<IntDoublePair> ret = new ArrayList<IntDoublePair>();
			for (IntDoublePair intFloatPair : res) {
				if(intFloatPair.second<DoubleDBSCAN.this.conf.eps)ret.add(intFloatPair);
				else break;
			}
			return ret;
		}

	}
	class SimilarityRegionMode implements RegionMode<IntDoublePair>{
		private SparseMatrix mat;
		private boolean distanceMode;
		public SimilarityRegionMode(SparseMatrix mat, boolean distanceMode) {
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
					if(v<DoubleDBSCAN.this.conf.eps)
						ret.add(IntDoublePair.pair(ent.index, v));
					else break;
				}
			}
			else{
				ret.add(IntDoublePair.pair(index, DoubleDBSCAN.this.conf.eps * 2)); // HACK
				for (Entry ent: vec.entries()) {
					if(ent.value>DoubleDBSCAN.this.conf.eps)
						ret.add(IntDoublePair.pair(ent.index, ent.value));
				}
			}
			return ret;
		}

	}
	private class State{

		TIntHashSet visited = new TIntHashSet();
		TIntHashSet noise = new TIntHashSet();
		TIntHashSet addedToCluster = new TIntHashSet();
		TIntObjectHashMap<TIntList> clusters = new TIntObjectHashMap<TIntList>();
		private RegionMode<IntDoublePair> regionMode;
		private int length;
		State(int length, RegionMode<IntDoublePair> regionMode){
			this.regionMode = regionMode;
			this.length = length;
		}
	}

	@Override
	public DoubleDBSCANClusters cluster(double[][] data) {
		State state = new State(data.length,new NNRegionMode(data));
		return dbscan(state);
	}

	@Override
	public DoubleDBSCANClusters cluster(DataSource<double[]> data) {
		double[][] allData = new double[data.numRows()][];
		Iterator<double[]> iterator = data.iterator();
		for (int i = 0; i < allData.length; i++) {
			allData[i] = iterator.next();
		}
		return this.cluster(allData);
	}

	@Override
	public DoubleDBSCANClusters cluster(SparseMatrix data,boolean distanceMode) {
		State s = new State(data.rowCount(), new SimilarityRegionMode(data, distanceMode));
		return dbscan(s);
	}

	private DoubleDBSCANClusters dbscan(State state) {
		int clusterIndex = 0;
		for (int p = 0; p < state.length; p++) {
			if(state.visited.contains(p))continue;
			List<IntDoublePair> region = state.regionMode.regionQuery(p);
			if(region.size() < conf.minPts){
				state.noise.add(p);
			}
			else{
				TIntList cluster = new TIntArrayList();
				state.clusters.put(clusterIndex, cluster);
				expandCluster(p,region,cluster,state);
				clusterIndex++;
			}
		}
		final DoubleDBSCANClusters dbscanClusters = new DoubleDBSCANClusters();
		dbscanClusters.clusterMembers = new int[state.clusters.size()][];
		state.clusters.forEachEntry(new TIntObjectProcedure<TIntList>() {
			@Override
			public boolean execute(int cluster, TIntList b) {
				dbscanClusters.clusterMembers[cluster] = b.toArray();
				return true;
			}
		});
		dbscanClusters.conf = this.conf;
		dbscanClusters.noise = state.noise.toArray();
		return dbscanClusters;
	}

	private void expandCluster(int p, List<IntDoublePair> region, TIntList cluster, State state) {
		addToCluster(p,cluster,state);
		for (int regionIndex = 0; regionIndex < region.size(); regionIndex++) {
			int pprime = region.get(regionIndex).first;
			if (!state.visited.contains(pprime)){
				state.visited.add(pprime);
				List<IntDoublePair> regionPrime = state.regionMode.regionQuery(pprime);
				if(regionPrime.size()>= this.conf.minPts) region.addAll(regionPrime);
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

	/**
	 * @return the config of this {@link DoubleDBSCAN}
	 */
	public DBSCANConfiguration<DoubleNearestNeighbours, double[]>  getConfig() {
		return this.conf;
	}


}