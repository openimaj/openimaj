package org.openimaj.ml.clustering.kdtree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

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
	/**
	 * @param data
	 * @return read {@link TestStats}
	 */
	public TestStats readTestStats(String[] data) {
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
	public int[][] readTestClusters(String[] data) {
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
		correctClusters(clusters);
		return clusters.toArray(new int[clusters.size()][]);
	}
	private void correctClusters(List<int[]> clusters) {
		if(this.percluster==-1) return;
		for (int i = 0; i < clusters.size(); i++) {
			int[] corrected = new int[percluster];
			int[] toCorrect = clusters.get(0);
			clusters.remove(0);
			for (int j = 0; j < corrected.length; j++) {
				corrected[j] = toCorrect[j];
			}
			clusters.add(corrected);
		}
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
	public double[][] readTestData(String[] data) {
		int[][] correct = readTestClusters(data);
		TestStats stats = readTestStats(data);
		Set<Integer> exist = existing(correct);
		
		int i = 0;
		for (;data[i].length()!=0; i++);
		List<double[]> dataL = new ArrayList<double[]>();
		int start = i+1;
		for (i=start;data[i].length()!=0; i++){
			if(exist.contains(i-start) || (i - start < stats.noutliers && this.outliers)){				
				dataL.add(readDataLine(data[i]));
			}
			else{
				dataL.add(null);
			}
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
}