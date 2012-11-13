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
package org.openimaj.examples.ml.clustering.kmeans;

import java.util.Arrays;

import org.openimaj.data.RandomData;
import org.openimaj.ml.clustering.ByteCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.ByteKMeans;

/**
 * Example showing how to use the Exact or Approximate KMeans clustering
 * algorithm with in-memory data. The example sets up the clustering algorithm,
 * generates some uniform random data to cluster, and runs the clustering
 * algorithm. The resultant clusters are printed, and the example also shows how
 * to perform cluster assignment (i.e. vector quantisation) in which vectors are
 * assigned to their closest cluster.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class KMeansExample {
	/**
	 * Main method for the example.
	 * 
	 * @param args
	 *            Ignored.
	 */
	public static void main(String[] args) {
		// Set up the variables needed to define the clustering operation
		final int dimensionality = 2;
		final int numItems = 10000;
		final int K = 4;

		// Create the clusterer; there are specific types for all kinds of data
		// (we're using byte data here). The clusters provide a couple of
		// static methods to quickly create an exact or approximate (using a
		// KD-Tree ensemble) K-Means algorithm. Alternatively, the K-Means
		// clusterer can be constructed with a KMeansConfiguration object which
		// offers fine control over the k-means algorithm, including control
		// over the nearest-neighbour implementation (i.e. approximate or not;
		// different distance functions, etc).
		final ByteKMeans kmeans = ByteKMeans.createExact(dimensionality, K);

		// Settings for things like the number of iterations can be set through
		// the KMeansConfiguration either at construction time, or afterwards:
		kmeans.getConfiguration().setMaxIterations(100);

		// Generate some random data to cluster
		final byte[][] data = RandomData.getRandomByteArray(numItems, dimensionality, Byte.MIN_VALUE, Byte.MAX_VALUE);

		// Perform the clustering
		final ByteCentroidsResult result = kmeans.cluster(data);

		// Get an assigner to assign vectors to the closest cluster. You could
		// also construct an assigner implementation manually if you want
		// more control over the assigner.
		final HardAssigner<byte[], ?, ?> assigner = result.defaultHardAssigner();

		// Now investigate which cluster each original data item belonged to:
		for (int i = 0; i < 10; i++) {
			final byte[] vector = data[i];

			// Each leaf-node of the hierarchy is assigned a unique index number
			// between 0 and the total number of leaf-nodes.
			final int globalClusterNumber = assigner.assign(vector);

			System.out.format("%s was assigned to cluster %d\n", Arrays.toString(vector),
					globalClusterNumber);
		}
	}
}
