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
package org.openimaj.ml.clustering.dbscan;

import java.util.ArrayList;
import java.util.List;

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

	private Logger logger = Logger.getLogger(TestDoubleDBSCAN.class);
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
	public double[][] readTestData(String[] data) {
		int i = 0;
		for (;data[i].length()!=0; i++);
		List<double[]> dataL = new ArrayList<double[]>();
		for (i=i+1;data[i].length()!=0; i++){
			dataL.add(readDataLine(data[i]));
		}
		logger.debug(String.format("Loading %d data items\n",dataL.size()));
		return dataL.toArray(new double[dataL.size()][]);
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