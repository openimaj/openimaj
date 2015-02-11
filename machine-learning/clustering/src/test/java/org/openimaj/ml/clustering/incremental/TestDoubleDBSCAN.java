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
package org.openimaj.ml.clustering.incremental;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.experiment.evaluation.cluster.analyser.FScoreClusterAnalyser;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.dbscan.DistanceDBSCAN;
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
	public void loadTest() throws IOException {
		final String[] data = FileUtils.readlines(TestDoubleDBSCAN.class
				.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		final ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
		this.ann = new FScoreClusterAnalyser();
	}

	/**
	 *
	 */
	@Test
	public void testSimDBSCANIncremental() {
		final SparseMatrixDBSCAN dbscan = new DistanceDBSCAN(
				this.testStats.eps,
				this.testStats.minpts
				);
		final SparseMatrix mat = new SparseMatrix(testData.length, testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = DoubleNearestNeighboursExact.distanceFunc(testData[i], testData[j]);
				if (d >= this.testStats.eps)
					continue;
				if (d == 0)
					d = Double.MIN_VALUE;
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		final IncrementalSparseClusterer isc = new IncrementalSparseClusterer(dbscan, 5);
		final IndexClusters c = isc.cluster(mat);

		assertTrue(ann.analyse(testClusters, c.clusters()).fscore(1) == 1.);
	}

	/**
	 *
	 */
	@Test
	public void testSimDBSCANIncrementalLifetime() {
		final SparseMatrixDBSCAN dbscan = new DistanceDBSCAN(
				this.testStats.eps,
				this.testStats.minpts
				);
		final SparseMatrix mat = new SparseMatrix(testData.length, testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = DoubleNearestNeighboursExact.distanceFunc(testData[i], testData[j]);
				if (d >= this.testStats.eps)
					continue;
				if (d == 0)
					d = Double.MIN_VALUE;
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		final IncrementalSparseClusterer isc = new IncrementalLifetimeSparseClusterer(dbscan, 20);
		// IndexClusters c =
		isc.cluster(mat);
		// assertTrue(ann.analyse(testClusters, c.clusters()).fscore(1) == 1.);
	}

}
