package org.openimaj.ml.clustering.dbscan;

import java.util.Arrays;

import org.openimaj.knn.DoubleNearestNeighbours;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DBSCANClusters {
	/**
	 * The members of a cluster
	 */
	int[][] clusterMembers;
	/**
	 * Indexes of noise elements
	 */
	int[] noise;
	/**
	 * The configuration that created this DBSCAN cluster
	 */
	DBSCANConfiguration<DoubleNearestNeighbours, double[]> conf;


	@Override
	public String toString() {
		int[][] clusters = clusterMembers;
		int i = 0;
		String str = "";
		for (int[] member : clusters) {
			str += String.format("%d %s\n",i++,Arrays.toString(member));
		}
		str+=String.format("%s", Arrays.toString(this.noise));
		return str;
	}
	
	/**
	 * @return the clusters and the indexes of data assigned
	 */
	public int[][] getClusterMembers(){
		return this.clusterMembers;
	}
	
	/**
	 * @return the data indexes assigned to noise
	 */
	public int[] getNoise(){
		return this.noise;
	}
	
}
