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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;
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
		DoubleNNDBSCAN dbscan = new DoubleNNDBSCAN(
			this.testStats.eps,
			this.testStats.minpts,
			new DoubleNearestNeighboursExact.Factory()
		);
		DoubleDBSCANClusters res = dbscan.cluster(testData);
		for (int i = 0; i < res.getNoise().length; i++) {
			assertTrue(res.getNoise()[i] < this.testStats.noutliers);
		}
		assertTrue(res.getNoise().length == this.testStats.noutliers);
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
		}
	}
	/**
	 *
	 */
	@Test
	public void testSimDBSCAN(){
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
		DoubleDBSCANClusters res = dbscan.cluster(mat);
		for (int i = 0; i < res.getNoise().length; i++) {
			assertTrue(res.getNoise()[i] < this.testStats.noutliers);
		}
		assertTrue(res.getNoise().length == this.testStats.noutliers);
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
