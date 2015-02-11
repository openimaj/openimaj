/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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