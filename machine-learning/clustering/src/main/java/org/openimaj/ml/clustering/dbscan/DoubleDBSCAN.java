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
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.util.pair.IntDoublePair;

/**
 * Implementation of DBSCAN (http://en.wikipedia.org/wiki/DBSCAN) using
 * a
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleDBSCAN implements SpatialClusterer<DoubleDBSCANClusters, double[]>{

	private DBSCANConfiguration<DoubleNearestNeighbours, double[]> conf;

	/**
	 * Perform a DBScane with this configuration
	 * @param dbsConf
	 */
	public DoubleDBSCAN(DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf) {
		this.conf = dbsConf;
	}

	private class State{
		DoubleNearestNeighbours nn;
		TIntHashSet visited = new TIntHashSet();
		TIntHashSet noise = new TIntHashSet();
		TIntHashSet addedToCluster = new TIntHashSet();
		TIntObjectHashMap<TIntList> clusters = new TIntObjectHashMap<TIntList>();
		int dataLen;
		State(double[][] data){
			nn = DoubleDBSCAN.this.conf.getNearestNeighbourFactory().create(data);
			dataLen = data.length;
		}
	}

	@Override
	public DoubleDBSCANClusters cluster(double[][] data) {
		int clusterIndex = 0;
		State state = new State(data);
		for (int p = 0; p < data.length; p++) {
			if(state.visited.contains(p))continue;
			double[] point = data[p];
			List<IntDoublePair> region = regionQuery(point,state);
			if(region.size() < conf.minPts){
				state.noise.add(p);
			}
			else{
				TIntList cluster = new TIntArrayList();
				state.clusters.put(clusterIndex, cluster);
				expandCluster(data,p,region,cluster,state);
				clusterIndex++;
			}
		}
		final DoubleDBSCANClusters dbscanClusters = new DoubleDBSCANClusters();
		dbscanClusters.clusterMembers = new int[state.clusters.size()][];
		dbscanClusters.data = data;
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

	private void expandCluster(double[][] data, int p, List<IntDoublePair> region, TIntList cluster, State state) {
		addToCluster(p,cluster,state);
		for (int regionIndex = 0; regionIndex < region.size(); regionIndex++) {
			int pprime = region.get(regionIndex).first;
			if (!state.visited.contains(pprime)){
				state.visited.add(pprime);
				List<IntDoublePair> regionPrime = regionQuery(data[pprime], state);
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

	private List<IntDoublePair> regionQuery(double[] data, State state) {
		List<IntDoublePair> res = state.nn.searchKNN(data, state.dataLen);
		List<IntDoublePair> ret = new ArrayList<IntDoublePair>();
		for (IntDoublePair intFloatPair : res) {
			if(intFloatPair.second<this.conf.eps)ret.add(intFloatPair);
			else break;
		}
		return ret;
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


}