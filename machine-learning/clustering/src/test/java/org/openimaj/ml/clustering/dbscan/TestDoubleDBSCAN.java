package org.openimaj.ml.clustering.dbscan;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader.TestStats;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Test DBSCAN implementation using data generated from:
 * http://people.cs.nctu.edu.tw/~rsliang/dbscan/testdatagen.html
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestDoubleDBSCAN {
	private TestStats testStats;
	private double[][] testData;
	private int[][] testClusters;
	/**
	 * @throws IOException
	 */
	@Before
	public void loadTest() throws IOException{
		String[] data = FileUtils.readlines(TestDoubleDBSCAN.class.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
	}

	/**
	 *
	 */
	@Test
	public void testDBSCAN(){
		DoubleDBSCAN dbscan = new DoubleDBSCAN(
			new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				this.testStats.eps,
				this.testStats.minpts,
				new DoubleNearestNeighboursExact.Factory()
			)
		);
		DoubleDBSCANClusters res = dbscan.cluster(testData);
		for (int i = 0; i < res.noise.length; i++) {
			assertTrue(res.noise[i] < this.testStats.noutliers);
		}
		assertTrue(res.noise.length == this.testStats.noutliers);
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusterMembers[i])));
		}
	}
	/**
	 *
	 */
	@Test
	public void testSimDBSCAN(){
		DoubleDBSCAN dbscan = new DoubleDBSCAN(
			new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				this.testStats.eps,
				this.testStats.minpts,
				new DoubleNearestNeighboursExact.Factory()
			)
		);
		SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = DoubleNearestNeighboursExact.distanceFunc(testData[i], testData[j]);
				if(d>=this.testStats.eps) continue;
				if(d==0)d = Double.MIN_VALUE;
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		DoubleDBSCANClusters res = dbscan.cluster(mat,true);
		for (int i = 0; i < res.noise.length; i++) {
			assertTrue(res.noise[i] < this.testStats.noutliers);
		}
		assertTrue(res.noise.length == this.testStats.noutliers);
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusterMembers[i])));
		}
	}
	private Set<Integer> toSet(int[] is) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < is.length; i++) {
			set.add(is[i]);
		}
		return set;
	}

}
