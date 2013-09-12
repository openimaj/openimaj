package org.openimaj.ml.clustering.incremental;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.evaluation.cluster.analyser.FScoreClusterAnalyser;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.dbscan.DistanceDBSCAN;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;
import org.openimaj.ml.clustering.dbscan.SparseMatrixDBSCAN;
import org.openimaj.ml.clustering.incremental.ClusterTestDataLoader.TestStats;

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
	private FScoreClusterAnalyser ann;
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
		this.ann = new FScoreClusterAnalyser();
	}

	/**
	 *
	 */
	@Test
	public void testSimDBSCANIncremental(){
		SparseMatrixDBSCAN dbscan = new DistanceDBSCAN(
			this.testStats.eps,
			this.testStats.minpts
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
		IncrementalSparseClusterer isc = new IncrementalSparseClusterer(dbscan, 5);
		IndexClusters c = isc.cluster(mat);
		
		assertTrue(ann.analyse(testClusters, c.clusters()).fscore(1) == 1.);
	}
	
	/**
	 *
	 */
	@Test
	public void testSimDBSCANIncrementalLifetime(){
		SparseMatrixDBSCAN dbscan = new DistanceDBSCAN(
			this.testStats.eps,
			this.testStats.minpts
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
		IncrementalSparseClusterer isc = new IncrementalLifetimeSparseClusterer(dbscan, 20);
		IndexClusters c = isc.cluster(mat);
		
//		assertTrue(ann.analyse(testClusters, c.clusters()).fscore(1) == 1.);
	}

}
