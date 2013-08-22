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
public class IncrementalSparseClusterer<CLUSTER extends IndexClusters> implements SparseMatrixClusterer<IndexClusters>{
	
	private SparseMatrixClusterer<CLUSTER> clusterer;
	private int window;
	private double threshold;
	private final static Logger logger = Logger.getLogger(IncrementalSparseClusterer.class);
	

	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 */
	public IncrementalSparseClusterer(SparseMatrixClusterer<CLUSTER> clusterer, int window) {
		this.clusterer = clusterer;
		this.window = window;
		this.threshold = 1.;
	}
	
	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 * @param threshold 
	 */
	public IncrementalSparseClusterer(SparseMatrixClusterer<CLUSTER> clusterer, int window, double threshold) {
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
			Map<Integer, Double> stability = mergeAndDeactivate(oldClusters,newClusters,inactiveRows);
			for (Entry<Integer, Double> e : stability.entrySet()) {
				if(e.getValue() >= threshold){
					int[] completedCluster = oldClusters.clusters()[e.getKey()];
					inactiveRows.addAll(completedCluster);
					completedClusters.add(completedCluster);
				}
			}
			
			seenrows += window;
			oldClusters = newClusters;
			logger.debug("Seen rows: " + seenrows);
			logger.debug("Inactive rows: " + inactiveRows.size());
		}
		for (int i = 0; i < oldClusters.clusters().length; i++) {
			completedClusters.add(oldClusters.clusters()[i]);
		}
		
		return new IndexClusters(completedClusters);
	}

	private Map<Integer, Double> mergeAndDeactivate(IndexClusters c1, IndexClusters c2, TIntSet inactiveRows) {
		
		Map<Integer, Double> stability = new HashMap<Integer, Double>();
		int[][] clusters1 = c1.clusters();
		int[][] clusters2 = c2.clusters();
		for (int i = 0; i < clusters1.length; i++) {
			double maxnmi = 0;
			int[][] correct = new int[][]{clusters1[i]};
			for (int j = 0; j < clusters2.length; j++) {
				int[][] estimated = new int[][]{clusters2[j]};
//				NMIAnalysis nmi = new NMIClusterAnalyser().analyse(correct, estimated);
//				double score = nmi.nmi;
				double score = new FScoreClusterAnalyser().analyse(correct, estimated).score();
				if(!Double.isNaN(score))
					maxnmi = Math.max(maxnmi, score);
			}
			stability.put(i, maxnmi);
		}
		logger.debug(String.format("The stability is:\n%s",stability));
		return stability;
	}

	@Override
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}

	
}
