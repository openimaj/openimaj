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

import com.sun.xml.internal.ws.util.CompletedFuture;

import ch.akuhn.matrix.SparseMatrix;

/**
 *
 * An incremental clusterer which holds old {@link SparseMatrix} instances internally, 
 * only forgetting rows once they have been clustered and are relatively stable.
 * 
 * The criteria for row removal is cluster stability.
 * The defenition of cluster stability is maximum f1-score achieving a threshold between
 * clusters in the previous round and the current round. Once one round of stability is achieved
 * the cluster is stable and its elements are removed.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IncrementalSparseClusterer implements SparseMatrixClusterer<IndexClusters>{
	
	private SparseMatrixClusterer<? extends IndexClusters> clusterer;
	private int window;
	protected double threshold;
	private int maxwindow = -1;
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
	
	/**
	 * @param clusterer the underlying clusterer
	 * @param window 
	 * @param maxwindow 
	 */
	@SuppressWarnings("unused")
	private IncrementalSparseClusterer(SparseMatrixClusterer<? extends IndexClusters> clusterer, int window, int maxwindow) {
		this.clusterer = clusterer;
		this.window = window;
		if(maxwindow>0 ){
			if(maxwindow < window * 2)
				maxwindow = window * 2;
		}
		if(maxwindow <= 0){
			maxwindow = -1;
		}
		this.maxwindow  = maxwindow;
		this.threshold = 1.;
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
		logger.debug("First clustering!: " + seen.rowCount() + "x" + seen.columnCount());
		IndexClusters oldClusters = clusterer.cluster(seen);
		logger.debug("First clusters:\n" + oldClusters);
		List<int[]> completedClusters = new ArrayList<int[]>();
		while(seenrows < data.rowCount()){
			int nextwindow = seenrows + window;
			if(nextwindow >= data.rowCount()) nextwindow = data.rowCount();
			if(this.maxwindow > 0 && nextwindow - inactiveRows.size() > this.maxwindow){
				logger.debug(String.format("Window size (%d) without inactive (%d) = (%d), greater than maximum (%d)",nextwindow, inactiveRows.size(), nextwindow - inactiveRows.size(), this.maxwindow));
				deactiveOldItemsAsNoise(nextwindow,inactiveRows,completedClusters);
			}
			WindowedSparseMatrix wsp = new WindowedSparseMatrix(data, nextwindow, inactiveRows);
			logger.debug("Clustering: " + wsp.window.rowCount() + "x" + wsp.window.columnCount());
			IndexClusters newClusters = clusterer.cluster(wsp.window);
			wsp.correctClusters(newClusters);
			logger.debug("New clusters:\n" + newClusters);
			// if stability == 1 for any cluster, it was the same last window, we should not include those items next round
			detectInactive(oldClusters, newClusters, inactiveRows, completedClusters);
			
			oldClusters = newClusters;
			seenrows += window;
			logger.debug("Seen rows: " + seenrows);
			logger.debug("Inactive rows: " + inactiveRows.size());
		}
		for (int i = 0; i < oldClusters.clusters().length; i++) {
			int[] cluster = oldClusters.clusters()[i];
			if(cluster.length!=0) 
				completedClusters.add(cluster);
		}
		
		return new IndexClusters(completedClusters);
	}

	private void deactiveOldItemsAsNoise(int nextwindow, TIntSet inactiveRows, List<int[]> completedClusters) {
		int toDeactivate = 0;
		while(nextwindow - inactiveRows.size() > this.maxwindow){
			if(!inactiveRows.contains(toDeactivate)){
				logger.debug("Forcing the deactivation of: " + toDeactivate);
				inactiveRows.add(toDeactivate);
				completedClusters.add(new int[]{toDeactivate});
			}
			toDeactivate++;
		}
	}

	/**
	 * Given the old and new clusters, make a decision as to which rows are now inactive,
	 * and therefore which clusters are now completed
	 * @param oldClusters
	 * @param newClusters
	 * @param inactiveRows
	 * @param completedClusters
	 */
	protected void detectInactive(IndexClusters oldClusters, IndexClusters newClusters, TIntSet inactiveRows, List<int[]> completedClusters) {
		Map<Integer, IntDoublePair> stability = calculateStability(oldClusters,newClusters,inactiveRows);
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
	}

	protected Map<Integer, IntDoublePair> calculateStability(IndexClusters c1, IndexClusters c2, TIntSet inactiveRows) {
		
		Map<Integer, IntDoublePair> stability = new HashMap<Integer, IntDoublePair>();
		int[][] clusters1 = c1.clusters();
		int[][] clusters2 = c2.clusters();
		for (int i = 0; i < clusters1.length; i++) {
			if(clusters1[i].length == 0) continue;
			double maxnmi = 0;
			int maxj = -1;
			TIntArrayList cluster = new TIntArrayList(clusters1[i].length);
			for (int j = 0; j < clusters1[i].length; j++) {
				if(inactiveRows.contains(clusters1[i][j]))
					continue;
				cluster.add(clusters1[i][j]);
			}
			int[][] correct = new int[][]{cluster.toArray()};
			for (int j = 0; j < clusters2.length; j++) {
				int[][] estimated = new int[][]{clusters2[j]};
//				NMIAnalysis nmi = new NMIClusterAnalyser().analyse(correct, estimated);
				double score = 0;
				if(correct[0].length == 1 && estimated[0].length == 1){
					// BOTH 1, either they are the same or not!
					score = correct[0][0] == estimated[0][0] ? 1 : 0;
				}
				else{					
					score = new FScoreClusterAnalyser().analyse(correct, estimated).score();
				}
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
