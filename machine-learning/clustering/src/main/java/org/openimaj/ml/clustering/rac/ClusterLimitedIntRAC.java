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
package org.openimaj.ml.clustering.rac;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * Similar to {@link IntRAC} but explicitly specify the limit the number of
 * clusters. Attempts to replace clusters which are "closer" with those that are
 * "further".
 * <p>
 * As with the {@link IntRAC} this is a combined clusterer, clusters and
 * assigner. See the {@link IntRAC} documentation for more details.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ClusterLimitedIntRAC extends IntRAC {
	static class ClusterMinimisationFunction implements UnivariateRealFunction {
		private int[][] distances;
		private int[][] samples;
		private int nClusters;

		public ClusterMinimisationFunction(int[][] samples, int[][] distances, int nClusters) {
			this.distances = distances;
			this.samples = samples;
			this.nClusters = nClusters;
		}

		@Override
		public double value(double radius) throws FunctionEvaluationException {
			final ClusterLimitedIntRAC r = new ClusterLimitedIntRAC(radius);
			r.train(samples, distances);
			final int diff = this.nClusters - r.numClusters();
			return diff;
		}

	}

	private int expectedClusters;
	private SortedMap<Float, Integer> thresholdOvershots;

	/**
	 * Sets the expected number of clusters to 100 and radius to 128.
	 */
	public ClusterLimitedIntRAC() {
		super();
		this.expectedClusters = 100;
		thresholdOvershots = new TreeMap<Float, Integer>();
	}

	/**
	 * Set the number of clusters to 100.
	 * 
	 * @param radiusSquared
	 */
	public ClusterLimitedIntRAC(double radiusSquared) {
		super(radiusSquared);
		this.expectedClusters = 100;
		thresholdOvershots = new TreeMap<Float, Integer>();
	}

	/**
	 * Attempt to learn the threshold and uses this as an expected number of
	 * clusters.
	 * 
	 * @param bKeys
	 * @param subSamples
	 * @param nClusters
	 */
	public ClusterLimitedIntRAC(int[][] bKeys, int subSamples, int nClusters) {
		super(bKeys, subSamples, nClusters);
		this.expectedClusters = (int) ((((float) nClusters) / subSamples) * bKeys.length);
	}

	@Override
	public ClusterLimitedIntRAC cluster(int[][] data) {
		int foundLength = this.nDims;

		for (final int[] entry : data) {
			if (foundLength == -1)
				foundLength = entry.length;

			// all the data entries must be the same length otherwise this
			// doesn't make sense
			if (foundLength != entry.length) {
				throw new RuntimeException();
			}

			boolean found = false;
			float minDiff = 0;

			for (final int[] existing : this.codebook) {
				float distance = distanceEuclidianSquared(entry, existing);
				if (distance < threshold) {
					found = true;
					break;
				}
				distance = (float) (distance - threshold);
				if (minDiff == 0 || distance < minDiff)
					minDiff = distance;
			}

			if (!found) {
				if (this.numClusters() >= this.expectedClusters) {
					// Remove the current smallest distance with this centroid
					final Float smallestDistance = this.thresholdOvershots.firstKey();
					if (smallestDistance < minDiff) {

						final Integer index = this.thresholdOvershots.get(smallestDistance);
						this.codebook.remove((int) index);
						this.codebook.add(index, entry);
						this.thresholdOvershots.remove(smallestDistance);
						this.thresholdOvershots.put(minDiff, this.numClusters() - 1);
						// System.out.println("I have replaced a less significant distance, new least significant distance is "
						// + this.thresholdOvershots.firstKey());
					}
				} else {
					this.codebook.add(entry);
					if (this.numClusters() % 1000 == 0) {
						System.out.println("Codebook increased to size " + this.numClusters());
						System.out.println("with nSamples = " + this.totalSamples);
					}
					this.thresholdOvershots.put(minDiff, this.numClusters() - 1);
				}

			}
			this.totalSamples += 1;
		}
		this.nDims = foundLength;

		return this;
	}
}
