package org.openimaj.ml.clustering.dbscan;

import java.util.Arrays;

import org.openimaj.ml.clustering.IndexClusters;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DBSCANClusters extends IndexClusters {
	/**
	 * Indexes of noise elements
	 */
	private int[] noise;
	
	/**
	 * @param noise
	 * @param clusters
	 */
	public DBSCANClusters(int[] noise, int[][] clusters) {
		super(clusters);
		this.noise = noise;
	}
	
	/**
	 * @param noise
	 * @param clusters
	 * @param nEntries 
	 */
	public DBSCANClusters(int[] noise, int[][] clusters, int nEntries) {
		super(clusters,nEntries);
		this.noise = noise;
	}
	
	@Override
	public String toString() {
		String str = String.format("%s", Arrays.toString(this.noise));
		return super.toString() + "\n" + str;
	}
	
	/**
	 * @return the data indexes assigned to noise
	 */
	public int[] getNoise(){
		return this.noise;
	}
	
}
