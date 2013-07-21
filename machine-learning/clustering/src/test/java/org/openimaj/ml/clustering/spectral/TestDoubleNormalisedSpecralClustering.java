package org.openimaj.ml.clustering.spectral;

import static org.junit.Assert.assertTrue;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader.TestStats;
import org.openimaj.ml.clustering.dbscan.DBSCANConfiguration;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCAN;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;

/**
 * Test Spectral Clustering implementation using data generated from:
 * http://people.cs.nctu.edu.tw/~rsliang/dbscan/testdatagen.html
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestDoubleNormalisedSpecralClustering {
	private TestStats testStats;
	private double[][] testData;
	private int[][] testClusters;
	/**
	 * @throws IOException
	 */
	@Before
	public void loadTest() throws IOException{
		String[] data = FileUtils.readlines(TestDoubleNormalisedSpecralClustering.class.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
	}
	

	/**
	 * 
	 */
	@Test
	public void testSimSpatialCluster(){
//		KMeansConfiguration<DoubleNearestNeighbours, double[]> kmeansConf = new KMeansConfiguration<DoubleNearestNeighbours, double[]>(
//				2,
//				testStats.ncluster,
//				new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
//		);
//		//		DoubleKMeans inner = new DoubleKMeans(kmeansConf);
		DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf = new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				2, 1, 2, 
				new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleDBSCAN(dbsConf);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
			inner
		);
		DoubleNormalisedSpectralClustering clust = new DoubleNormalisedSpectralClustering(conf);
		SparseMatrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(testData.length,testData.length);
		DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>this.testStats.eps) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.setElement(i, j, d);
				mat.setElement(j, i, d);
			}
		}
		SparseMatrix mat_norm = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = mat.getElement(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.setElement(i, j, 1-d);
				mat_norm.setElement(j, i, 1-d);
			}
		}
		Clusters res = clust.cluster(mat_norm,true);
//		System.out.println(res);
//		System.out.println(new Clusters(this.testClusters));
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
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
