package org.openimaj.ml.clustering.incremental;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.openimaj.experiment.evaluation.cluster.analyser.FScoreClusterAnalyser;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SparseMatrixClusterer;
import org.openimaj.util.pair.IntDoublePair;

import ch.akuhn.matrix.SparseMatrix;

/**
 *
 * An incremental clusterer which holds old {@link SparseMatrix} instances internally, only forgetting rows
 * once they have been clustered and are relatively stable
 * 
 * @param <CLUSTER>
 *  
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IncrementalSparseClusterer implements SparseMatrixClusterer<IndexClusters>{
	
	private SparseMatrixClusterer<? extends IndexClusters> clusterer;
	private int window;
	private double threshold;
	private final static Logger logger = Logger.getLogger(IncrementalSparseClusterer.class);
	

	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 */
	public IncrementalSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window) {
		this.clusterer = clusterer;
		this.window = window;
		this.threshold = 1.;
	}
	
	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 * @param threshold 
	 */
	public IncrementalSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window, double threshold) {
		this.clusterer = clusterer;
		this.window = window;
		this.threshold = threshold;
	}
	
	class WindowedSparseMatrix{
		SparseMatrix window;
		Map<Integer,Integer> indexCorrection;
		
		public WindowedSparseMatrix(SparseMatrix sm, int nextwindow, TIntSet inactive) {
			TIntArrayList active = new TIntArrayList(nextwindow);
			indexCorrection = new HashMap<Integer, Integer>();
			for (int i = 0; i < nextwindow; i++) {
				if(!inactive.contains(i)){
					indexCorrection.put(active.size(), i);
					active.add(i);
				}
			}
			window = MatlibMatrixUtils.subMatrix(sm, active, active);
		}
		
		public void correctClusters(IndexClusters clstrs){
			int[][] clusters = clstrs.clusters();
			for (int i = 0; i < clusters.length; i++) {
				int[] cluster = clusters[i];
				for (int j = 0; j < cluster.length; j++) {
					cluster[j] = indexCorrection.get(cluster[j]);
				}
			}
		}
	}

	@Override
	public IndexClusters cluster(SparseMatrix data) {
		if(window >= data.rowCount()) window = data.rowCount();
		SparseMatrix seen = MatlibMatrixUtils.subMatrix(data, 0, window, 0, window);
		int seenrows = window;
		TIntSet inactiveRows = new TIntHashSet(window);
		
		IndexClusters oldClusters = clusterer.cluster(seen);
		List<int[]> completedClusters = new ArrayList<int[]>();
		while(seenrows < data.rowCount()){
			int nextwindow = seenrows + window;
			if(nextwindow >= data.rowCount()) nextwindow = data.rowCount();
			WindowedSparseMatrix wsp = new WindowedSparseMatrix(data, nextwindow, inactiveRows);
			logger.debug("Clustering: " + wsp.window.rowCount() + "x" + wsp.window.columnCount());
			IndexClusters newClusters = clusterer.cluster(wsp.window);
			wsp.correctClusters(newClusters);
			logger.debug("New clusters:\n" + newClusters);
			// if stability == 1 for any cluster, it was the same last window, we should not include those items next round
			Map<Integer, IntDoublePair> stability = mergeAndDeactivate(oldClusters,newClusters,inactiveRows);
			for (Entry<Integer, IntDoublePair> e : stability.entrySet()) {
				if(e.getValue().second >= threshold){
					int[] completedCluster = oldClusters.clusters()[e.getKey()];
					inactiveRows.addAll(completedCluster);
					completedClusters.add(completedCluster);
					if(threshold == 1){
						newClusters.clusters()[e.getValue().first] = new int[0];
					}
				}
			}
			
			seenrows += window;
			oldClusters = newClusters;
			logger.debug("Seen rows: " + seenrows);
			logger.debug("Inactive rows: " + inactiveRows.size());
		}
		for (int i = 0; i < oldClusters.clusters().length; i++) {
			int[] cluster = oldClusters.clusters()[i];
			if(cluster.length!=0) completedClusters.add(cluster);
		}
		
		return new IndexClusters(completedClusters);
	}

	private Map<Integer, IntDoublePair> mergeAndDeactivate(IndexClusters c1, IndexClusters c2, TIntSet inactiveRows) {
		
		Map<Integer, IntDoublePair> stability = new HashMap<Integer, IntDoublePair>();
		int[][] clusters1 = c1.clusters();
		int[][] clusters2 = c2.clusters();
		for (int i = 0; i < clusters1.length; i++) {
			if(clusters1[i].length == 0) continue;
			double maxnmi = 0;
			int maxj = -1;
			int[][] correct = new int[][]{clusters1[i]};
			for (int j = 0; j < clusters2.length; j++) {
				int[][] estimated = new int[][]{clusters2[j]};
//				NMIAnalysis nmi = new NMIClusterAnalyser().analyse(correct, estimated);
//				double score = nmi.nmi;
				double score = new FScoreClusterAnalyser().analyse(correct, estimated).score();
				if(!Double.isNaN(score))
				{
					if(score > maxnmi){
						maxnmi = score;
						maxj = j;
					}
				}
			}
			stability.put(i, IntDoublePair.pair(maxj, maxnmi));
		}
		logger.debug(String.format("The stability is:\n%s",stability));
		return stability;
	}

	@Override
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}

	
}
