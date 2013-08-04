package org.openimaj.ml.clustering.random;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SparseMatrixClusterer;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Given a similarity or distance matrix, this clusterer randomly
 * selects a number of clusters and randomly assigned each row to
 * each cluster.
 * 
 * The number of clusters is a random number from  0 to {@link SparseMatrix#rowCount()}
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RandomClusterer implements SparseMatrixClusterer<IndexClusters>{

	private Random random;
	private int forceClusters = -1;

	/**
	 * unseeded random
	 */
	public RandomClusterer() {
		this.random = new Random();
	}
	
	/**
	 * seeded random
	 * @param seed
	 */
	public RandomClusterer(long seed) {
		
		this.random = new Random(seed);
	}
	
	/**
	 * seeded random
	 * @param nclusters
	 */
	public RandomClusterer(int nclusters) {
		this();
		this.forceClusters = nclusters;
	}
	
	/**
	 * seeded random
	 * @param nclusters
	 * @param seed random seed
	 */
	public RandomClusterer(int nclusters, long seed) {
		this(seed);
		this.forceClusters = nclusters;
	}
	@Override
	public IndexClusters cluster(SparseMatrix data) {
		int nClusters = 0;
		if(this.forceClusters > 0)
			nClusters = this.forceClusters;
		else
			nClusters = this.random.nextInt(data.rowCount());
		Map<Integer,TIntArrayList> clusters = new HashMap<Integer, TIntArrayList>();
		for (int i = 0; i < data.rowCount(); i++) {
			int cluster = this.random.nextInt(nClusters);
			TIntArrayList l = clusters.get(cluster);
			if(l == null){
				clusters.put(cluster, l = new TIntArrayList());
			}
			l.add(i);
		}
		int[][] outClusters = new int[clusters.size()][];
		int i = 0;
		for (Entry<Integer, TIntArrayList> is : clusters.entrySet()) {
			outClusters[i++] = is.getValue().toArray();
		}
		return new IndexClusters(outClusters, data.rowCount());
	}

	@Override
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}

}
