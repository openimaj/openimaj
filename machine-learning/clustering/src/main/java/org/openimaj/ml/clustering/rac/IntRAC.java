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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.MaxIterationsExceededException;
import org.apache.commons.math.analysis.UnivariateRealFunction;
import org.apache.commons.math.analysis.solvers.BisectionSolver;
import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.DataSource;
import org.openimaj.data.RandomData;
import org.openimaj.ml.clustering.CentroidsProvider;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntFloatPair;

/**
 * An implementation of the RAC algorithm proposed by <a
 * href="http://eprints.ecs.soton.ac.uk/21401/">Ramanan and Niranjan</a>.
 * <p>
 * During training, data points are selected at random. The first data point is
 * chosen as a centroid. Every following data point is set as a new centroid if
 * it is outside the threshold of all current centroids. In this way it is
 * difficult to guarantee number of clusters so a minimisation function is
 * provided to allow a close estimate of the required threshold for a given K.
 * <p>
 * This implementation supports int[] cluster centroids.
 * <p>
 * In terms of implementation, this class is a both a clusterer, assigner and
 * the result of the clustering. This is because the RAC algorithm never ends;
 * that is to say that if a new point is being assigned through the
 * {@link HardAssigner} interface, and that point is more than the threshold
 * distance from any other centroid, then a new centroid will be created for the
 * point. If this behaviour is undesirable, the results of clustering can be
 * "frozen" by manually constructing an assigner that takes a
 * {@link CentroidsProvider} (or the centroids provided by calling
 * {@link #getCentroids()}) as an argument.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Inproceedings,
		author = { "Amirthalingam Ramanan", "Mahesan Niranjan" },
		title = "Resource-Allocating Codebook for Patch-based Face Recognition",
		year = "2009",
		booktitle = "IIS",
		url = "http://eprints.ecs.soton.ac.uk/21401/")
public class IntRAC
		implements
		SpatialClusters<int[]>,
		SpatialClusterer<IntRAC, int[]>,
		CentroidsProvider<int[]>,
		HardAssigner<int[], float[], IntFloatPair>
{
	private static class ClusterMinimisationFunction implements UnivariateRealFunction {
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
			final IntRAC r = new IntRAC(radius);
			r.train(samples, distances);
			final int diff = this.nClusters - r.numClusters();
			return diff;
		}
	}

	private static final String HEADER = SpatialClusters.CLUSTER_HEADER + "RAIC";

	protected ArrayList<int[]> codebook;
	protected double threshold;
	protected int nDims;
	protected static int[][] distances;
	protected long totalSamples;

	/**
	 * Sets the threshold to 128
	 */
	public IntRAC() {
		codebook = new ArrayList<int[]>();
		this.threshold = 128;
		this.nDims = -1;
		this.totalSamples = 0;
	}

	/**
	 * Define the threshold at which point a new cluster will be made.
	 * 
	 * @param radiusSquared
	 */
	public IntRAC(double radiusSquared) {
		this();
		this.threshold = radiusSquared;
	}

	/**
	 * Iteratively select subSamples from bKeys and try to choose a threshold
	 * which results in nClusters. This is provided to estimate threshold as
	 * this is a very data dependant value. The threshold is found using a
	 * BisectionSolver with a complete distance matrix (so make sure subSamples
	 * is reasonable)
	 * 
	 * @param bKeys
	 *            All keys to be trained against
	 * @param subSamples
	 *            number of subsamples to select from bKeys each iteration
	 * @param nClusters
	 *            number of clusters to aim for
	 */
	public IntRAC(int[][] bKeys, int subSamples, int nClusters) {
		this();

		distances = new int[subSamples][subSamples];
		int j = 0;
		this.threshold = 0;
		final int thresholdIteration = 5;
		while (j++ < thresholdIteration) {
			final int[][] randomList = new int[subSamples][];
			final int[] randomListIndex = RandomData.getUniqueRandomInts(subSamples, 0, bKeys.length);
			int ri = 0;
			for (int k = 0; k < randomListIndex.length; k++)
				randomList[ri++] = bKeys[randomListIndex[k]];
			try {
				this.threshold += calculateThreshold(randomList, nClusters);
			} catch (final Exception e) {
				this.threshold += 200000;
			}
			System.out.println("Current threshold: " + this.threshold / j);
		}
		this.threshold /= thresholdIteration;
	}

	@SuppressWarnings("deprecation")
	protected static double calculateThreshold(int[][] samples, int nClusters) throws MaxIterationsExceededException,
			FunctionEvaluationException
	{
		int maxDistance = 0;
		for (int i = 0; i < samples.length; i++) {
			for (int j = i + 1; j < samples.length; j++) {
				distances[i][j] = distanceEuclidianSquared(samples[i], samples[j]);
				distances[j][i] = distances[i][j];
				if (distances[i][j] > maxDistance)
					maxDistance = distances[i][j];
			}
		}
		System.out.println("Distance matrix calculated");
		final BisectionSolver b = new BisectionSolver();
		b.setAbsoluteAccuracy(100.0);
		return b.solve(100, new ClusterMinimisationFunction(samples, distances, nClusters), 0, maxDistance);
	}

	int train(int[][] samples, int[][] distances) {
		int foundLength = -1;
		final List<Integer> codebookIndex = new ArrayList<Integer>();
		for (int i = 0; i < samples.length; i++) {
			final int[] entry = samples[i];
			if (foundLength == -1)
				foundLength = entry.length;

			// all the data entries must be the same length otherwise this
			// doesn't make sense
			if (foundLength != entry.length) {
				this.codebook = new ArrayList<int[]>();
				return -1;
			}
			boolean found = false;
			for (final int j : codebookIndex) {
				if (distances[i][j] < threshold) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.codebook.add(entry);
				codebookIndex.add(i);
			}
		}
		this.nDims = foundLength;
		return 0;
	}

	@Override
	public IntRAC cluster(int[][] data) {
		int foundLength = -1;

		for (final int[] entry : data) {
			if (foundLength == -1)
				foundLength = entry.length;

			// all the data entries must be the same length otherwise this
			// doesn't make sense
			if (foundLength != entry.length) {
				this.codebook = new ArrayList<int[]>();
				throw new RuntimeException();
			}
			boolean found = false;
			for (final int[] existing : this.codebook) {
				if (distanceEuclidianSquared(entry, existing) < threshold) {
					found = true;
					break;
				}
			}
			if (!found) {
				this.codebook.add(entry);
				if (this.codebook.size() % 1000 == 0) {
					System.out.println("Codebook increased to size " + this.codebook.size());
				}
			}
		}

		return this;
	}

	@Override
	public IntRAC cluster(DataSource<int[]> data) {
		final int[][] dataArr = new int[data.size()][data.numDimensions()];

		return cluster(dataArr);
	}

	static int distanceEuclidianSquared(int[] a, int[] b) {
		int sum = 0;
		for (int i = 0; i < a.length; i++) {
			final int diff = a[i] - b[i];
			sum += diff * diff;
		}
		return sum;
	}

	static int distanceEuclidianSquared(int[] a, int[] b, int threshold2) {
		int sum = 0;

		for (int i = 0; i < a.length; i++) {
			final int diff = a[i] - b[i];
			sum += diff * diff;
			if (sum > threshold2)
				return threshold2;
		}
		return sum;
	}

	@Override
	public int numClusters() {
		return this.codebook.size();
	}

	@Override
	public int numDimensions() {
		return this.nDims;
	}

	@Override
	public int[] assign(int[][] data) {
		final int[] centroids = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			final int[] entry = data[i];
			centroids[i] = this.assign(entry);
		}
		return centroids;
	}

	@Override
	public int assign(int[] data) {
		int mindiff = -1;
		int centroid = -1;

		for (int i = 0; i < this.numClusters(); i++) {
			final int[] centroids = this.codebook.get(i);
			int sum = 0;
			boolean set = true;

			for (int j = 0; j < centroids.length; j++) {
				final int diff = centroids[j] - data[j];
				sum += diff * diff;
				if (mindiff != -1 && mindiff < sum) {
					set = false;
					break; // Stop checking the distance if you
				}
			}

			if (set) {
				mindiff = sum;
				centroid = i;
				// if(mindiff < this.threshold){
				// return centroid;
				// }
			}
		}
		return centroid;
	}

	@Override
	public String asciiHeader() {
		return "ASCII" + HEADER;
	}

	@Override
	public byte[] binaryHeader() {
		return HEADER.getBytes();
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		throw new UnsupportedOperationException("Not done!");
	}

	@Override
	public void readBinary(DataInput dis) throws IOException {
		threshold = dis.readDouble();
		nDims = dis.readInt();
		final int nClusters = dis.readInt();
		assert (threshold > 0);
		codebook = new ArrayList<int[]>();
		for (int i = 0; i < nClusters; i++) {
			final byte[] wang = new byte[nDims];
			dis.readFully(wang, 0, nDims);
			final int[] cluster = new int[nDims];
			for (int j = 0; j < nDims; j++)
				cluster[j] = wang[j] & 0xFF;
			codebook.add(cluster);
		}
	}

	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		writer.format("%d\n", this.threshold);
		writer.format("%d\n", this.nDims);
		writer.format("%d\n", this.numClusters());
		for (final int[] a : this.codebook) {
			writer.format("%d,", a);
		}
	}

	@Override
	public void writeBinary(DataOutput dos) throws IOException {
		dos.writeDouble(this.threshold);
		dos.writeInt(this.nDims);
		dos.writeInt(this.numClusters());
		for (final int[] arr : this.codebook) {
			for (final int a : arr) {
				dos.write(a);
			}
		}
	}

	@Override
	public int[][] getCentroids() {
		return this.codebook.toArray(new int[0][]);
	}

	@Override
	public void assignDistance(int[][] data, int[] indices, float[] distances) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IntFloatPair assignDistance(int[] data) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public HardAssigner<int[], ?, ?> defaultHardAssigner() {
		return this;
	}

	/**
	 * The number of centroids; this potentially grows as assignments are made.
	 * 
	 * @see org.openimaj.ml.clustering.assignment.HardAssigner#size()
	 */
	@Override
	public int size() {
		return this.nDims;
	}

	@Override
	public int[][] performClustering(int[][] data) {
		return new IndexClusters(this.cluster(data).defaultHardAssigner().assign(data)).clusters();
	}
}
