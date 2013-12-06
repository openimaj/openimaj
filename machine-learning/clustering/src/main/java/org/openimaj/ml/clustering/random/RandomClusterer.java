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
package org.openimaj.ml.clustering.random;

import gnu.trove.list.array.TIntArrayList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SparseMatrixClusterer;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Given a similarity or distance matrix, this clusterer randomly selects a
 * number of clusters and randomly assigned each row to each cluster.
 * 
 * The number of clusters is a random number from 0 to
 * {@link SparseMatrix#rowCount()}
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class RandomClusterer implements SparseMatrixClusterer<IndexClusters> {

	private Random random;
	private int forceClusters = -1;

	/**
	 * unseeded random
	 */
	public RandomClusterer() {
		this.random = new Random();
	}

	/**
	 * seeded random
	 * 
	 * @param seed
	 */
	public RandomClusterer(long seed) {

		this.random = new Random(seed);
	}

	/**
	 * seeded random
	 * 
	 * @param nclusters
	 */
	public RandomClusterer(int nclusters) {
		this();
		this.forceClusters = nclusters;
	}

	/**
	 * seeded random
	 * 
	 * @param nclusters
	 * @param seed
	 *            random seed
	 */
	public RandomClusterer(int nclusters, long seed) {
		this(seed);
		this.forceClusters = nclusters;
	}

	@Override
	public IndexClusters cluster(SparseMatrix data) {
		int nClusters = 0;

		if (this.forceClusters > 0)
			nClusters = this.forceClusters;
		else
			nClusters = this.random.nextInt(data.rowCount());

		final Map<Integer, TIntArrayList> clusters = new HashMap<Integer, TIntArrayList>();

		for (int i = 0; i < data.rowCount(); i++) {
			final int cluster = this.random.nextInt(nClusters);
			TIntArrayList l = clusters.get(cluster);

			if (l == null) {
				clusters.put(cluster, l = new TIntArrayList());
			}

			l.add(i);
		}

		final int[][] outClusters = new int[clusters.size()][];
		int i = 0;
		for (final Entry<Integer, TIntArrayList> is : clusters.entrySet()) {
			outClusters[i++] = is.getValue().toArray();
		}

		return new IndexClusters(outClusters, data.rowCount());
	}

	@Override
	public int[][] performClustering(SparseMatrix data) {
		return this.cluster(data).clusters();
	}

}
