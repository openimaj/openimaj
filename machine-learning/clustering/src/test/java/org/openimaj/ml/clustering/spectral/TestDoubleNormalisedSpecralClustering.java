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
package org.openimaj.ml.clustering.spectral;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader.TestStats;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;
import org.openimaj.ml.clustering.dbscan.DoubleNNDBSCAN;

import ch.akuhn.matrix.SparseMatrix;

import com.jmatio.types.MLArray;

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
		LoggerUtils.prepareConsoleLogger();
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
	public void testSimSpatialClusterInverse(){
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleNNDBSCAN(
			0.5, 3, new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(inner, new GraphLaplacian.Normalised());
		conf.eigenChooser = new AbsoluteValueEigenChooser(0.2, 0.1);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
 		SparseMatrix mat_norm = normalisedSimilarity();
		IndexClusters res = clust.cluster(mat_norm);
		confirmClusters(res);
	}
	
	/**
	 * @throws IOException 
	 *
	 */
	@Test
	public void testSimSpatialClusterInverseHardcoded() throws IOException{
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleNNDBSCAN(
			0.2, 2, new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(inner, new GraphLaplacian.Normalised());
		conf.eigenChooser = new AbsoluteValueEigenChooser(0.95, 0.1);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		SparseMatrix mat_norm = normalisedSimilarity(Double.MAX_VALUE);
		Collection<MLArray> col = new ArrayList<MLArray>();
		col.add(MatlibMatrixUtils.asMatlab(mat_norm));
//		MatFileWriter writ = new MatFileWriter(new File("/Users/ss/Experiments/python/Whard.mat"), col);
		IndexClusters res = clust.cluster(mat_norm);
		confirmClusters(res);
	}


	private void confirmClusters(IndexClusters res) {
		for (int i = 0; i < this.testClusters.length; i++) {
			System.err.println(toSet(this.testClusters[i]));
			System.err.println(toSet(res.clusters()[i]));
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
		}
	}


	
	private SparseMatrix normalisedSimilarity() {
		return normalisedSimilarity(this.testStats.eps);
	}
	private SparseMatrix normalisedSimilarity(double eps) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i+1; j < testData.length; j++) {
				double d = mat.get(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.put(i, j, 1-d);
				mat_norm.put(j, i, 1-d);
			}
		}
		return mat_norm;
	}
	private Set<Integer> toSet(int[] is) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < is.length; i++) {
			set.add(is[i]);
		}
		return set;
	}

}
