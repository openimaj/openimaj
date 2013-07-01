package org.openimaj.ml.clustering.dbscan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;

/**
 * Test DBSCAN implementation using data generated from:
 * http://people.cs.nctu.edu.tw/~rsliang/dbscan/testdatagen.html
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestDoubleDBSCAN {

	class TestStats{
		double eps;
		int minpts;
		int ncluster;
		int noutliers;
		double mineps;
	}
	private TestStats testStats;
	private double[][] testData;
	private int[][] testClusters;
	private TestStats readTestStats(String[] data) {
		TestStats ret = new TestStats();
		int i = 0;
		ret.eps = Double.parseDouble(data[i++].split("=")[1].trim());
		ret.minpts = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.ncluster = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.noutliers = Integer.parseInt(data[i++].split("=")[1].trim());
		ret.mineps = Double.parseDouble(data[i++].split("=")[1].trim());
		return ret;
	}
	@Before
	public void loadTest() throws IOException{
		String[] data = FileUtils.readlines(TestDoubleDBSCAN.class.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		this.testStats = readTestStats(data);
		this.testData = readTestData(data);
		this.testClusters = readTestClusters(data);
	}
	private int[][] readTestClusters(String[] data) {
		int i = 0;
		for (;data[i].length()!=0; i++);
		for (i=i+1;data[i].length()!=0; i++);
		List<int[]> clusters = new ArrayList<int[]>();
		for (i=i+1;i<data.length; i++){
			clusters.add(readIntDataLine(data[i]));
		}
		return clusters.toArray(new int[clusters.size()][]);
	}
	private int[] readIntDataLine(String string) {
		String[] split = string.split(",");
		int[] arr = new int[split.length];
		int i = 0;
		for (String s : split) {
			s = s.replace("<", "").replace(">", "").trim();
			arr[i++] = Integer.parseInt(s);
		}
		return arr;
	}
	private double[][] readTestData(String[] data) {
		int i = 0;
		for (;data[i].length()!=0; i++);
		List<double[]> dataL = new ArrayList<double[]>();
		for (i=i+1;data[i].length()!=0; i++){
			dataL.add(readDataLine(data[i]));
		}
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

	@Test
	public void testDBSCAN(){
		DoubleDBSCAN dbscan = new DoubleDBSCAN(
			new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				2,
				this.testStats.eps,
				this.testStats.minpts,
				new DoubleNearestNeighboursExact.Factory()
			)
		);
		dbscan.cluster(testData);
	}

}
