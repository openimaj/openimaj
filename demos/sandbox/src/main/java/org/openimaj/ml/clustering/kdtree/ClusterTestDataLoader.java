package org.openimaj.ml.clustering.kdtree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openimaj.ml.clustering.kdtree.ClusterTestDataLoader.TestStats;

/**
 * Load clusters from http://people.cs.nctu.edu.tw/~rsliang/dbscan/testdatagen.html
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ClusterTestDataLoader{
	/**
	 * Test details
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static class TestStats{
		/**
		 * EPS variable
		 */
		public double eps;
		/**
		 * minpts variable
		 */
		public int minpts;
		/**
		 * nclusters variable
		 */
		public int ncluster;
		/**
		 * noutliers variable
		 */
		public int noutliers;
		/**
		 * mineps variable
		 */
		public double mineps;
	}
	private int percluster = -1;
	private boolean outliers = true;
	
	
	/**
	 * 
	 */
	public ClusterTestDataLoader() {
		this.percluster = -1;
	}
	
	/**
	 * @param percluster 
	 * @param outliers 
	 * 
	 */
	public ClusterTestDataLoader(int percluster, boolean outliers) {
		this.percluster = percluster;
		this.outliers = outliers;
	}

	private Logger logger = Logger.getLogger(ClusterTestDataLoader.class);
	private TestStats testStats;
	private int[][] testClusters;
	private double[][] testData;
	/**
	 * @param data
	 * @return read {@link TestStats}
	 */
	private TestStats readTestStats(String[] data) {
		ClusterTestDataLoader.TestStats ret = new TestStats();
		int i = 0;
		ret.eps = Double.parseDouble(data[i++].split("=")[1].trim());
		ret.minpts = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.ncluster = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.noutliers = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.mineps = Double.parseDouble(data[i++].split("=")[1].trim());
		return ret;
	}


	/**
	 * @param data
	 * @return read the correct clusters
	 */
	private int[][] readTestClusters(String[] data) {
		int i = 0;
		for (;data[i].length()!=0; i++);
		for (i=i+1;data[i].length()!=0; i++);
		List<int[]> clusters = new ArrayList<int[]>();
		int count = 0;
		for (i=i+1;i<data.length; i++){
			int[] readIntDataLine = readIntDataLine(data[i]);
			clusters.add(readIntDataLine);
			count += readIntDataLine.length;
		}
		logger .debug(String.format("Loading %d items in %d clusters\n",count,clusters.size()));
		return clusters.toArray(new int[clusters.size()][]);
	}
	

	/**
	 * @param string
	 * @return read
	 */
	public int[] readIntDataLine(String string) {
		String[] split = string.split(",");
		int[] arr = new int[split.length-1];
		int i = 0;

		for (String s : split) {
			if(s.contains("<"))continue; // skip the first, it is the cluster index
			s = s.replace(">", "").trim();
			arr[i++] = Integer.parseInt(s)-1;

		}
		return arr;
	}
	/**
	 * @param data
	 * @return read the test data
	 */
	private double[][] readTestData(String[] data) {
		
		int i = 0;
		for (;data[i].length()!=0; i++);
		List<double[]> dataL = new ArrayList<double[]>();
		int start = i+1;
		for (i=start;data[i].length()!=0; i++){
			dataL.add(readDataLine(data[i]));
		}
		logger.debug(String.format("Loading %d data items\n",dataL.size()));
		return dataL.toArray(new double[dataL.size()][]);
	}
	private Set<Integer> existing(int[][] correct) {
		Set<Integer> exist = new HashSet<Integer>();
		for (int[] is : correct) {
			for (int i : is) {
				exist.add(i);
			}
		}
		return exist;
	}

	private double[] readDataLine(String string) {
		String[] split = string.split(" ");
		double[] arr = new double[]{
				Double.parseDouble(split[1]),
				Double.parseDouble(split[2])
		};
		return arr;
	}

	public void prepare(String[] data) {
		this.testStats = this.readTestStats(data);
		this.testClusters = this.readTestClusters(data);
		this.testData = this.readTestData(data);
		correctClusters();
	}

	private void correctClusters() {
		
		if(this.percluster != -1){
			double[][] correctedData = null;
			int[][] correctedClusters = new int[this.testClusters.length][this.percluster];	
			int seen ;
			if(this.outliers){
				seen = this.testStats.noutliers;
				correctedData= new double[this.percluster * this.testClusters.length + seen][];
				for (int i = 0; i < seen; i++) {
					correctedData[i] = this.testData[i];
				}
				
			}
			else{
				seen = 0;
				correctedData = new double[this.percluster * this.testClusters.length][];
			}
			for (int i = 0; i < this.testClusters.length; i++) {
				int[] clust = this.testClusters[i];
				for (int j = 0; j < this.percluster; j++) {
					int d = clust[j];
					correctedData[seen] = this.testData[d];
					correctedClusters[i][j] = seen;
					seen++;
				}
			}
			
			this.testClusters = correctedClusters;
			this.testData = correctedData;
		}
	}

	public TestStats getTestStats() {
		return this.testStats;
	}

	public double[][] getTestData() {
		return this.testData;
	}

	public int[][] getTestClusters() {
		return this.testClusters;
	}
}