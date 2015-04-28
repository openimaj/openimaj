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

package org.openimaj.ml.clustering;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.ExactFeatureVectorAssigner;
import org.openimaj.util.pair.IntFloatPair;

/**
 * The result of a {@link SpatialClusterer} that just produces a flat set of
 * centroids in the form of {@link FeatureVector}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @param <T>
 *            Type of features
 */
public class FeatureVectorCentroidsResult<T extends FeatureVector> implements SpatialClusters<T>, CentroidsProvider<T> {
	final static String HEADER = Clusters.CLUSTER_HEADER + "Byte".charAt(0) + "Cen";

	/** The centroids of the clusters */
	public T[] centroids;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ByteCentroidsResult))
			return false;

		final FeatureVectorCentroidsResult<?> other = (FeatureVectorCentroidsResult<?>) obj;
		for (int i = 0; i < this.centroids.length; i++) {
			if (!this.centroids[i].equals(other.centroids[i]))
				return false;
		}
		return true;
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
	public void readASCII(Scanner br) throws IOException {
		// // Read Header
		// final int K = Integer.parseInt(br.nextLine().trim());
		// final int M = Integer.parseInt(br.nextLine().trim());
		//
		// centroids = new byte[K][M];
		// for (int k = 0; k < K; k++) {
		// final String[] parts = br.nextLine().split(",");
		//
		// for (int d = 0; d < M; d++) {
		// centroids[k][d] = Byte.parseByte(parts[d]);
		// }
		// }
		throw new UnsupportedOperationException();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		// final int K = in.readInt();
		// final int M = in.readInt();
		//
		// centroids = new byte[K][M];
		//
		// for (int k = 0; k < K; k++) {
		// for (int d = 0; d < M; d++) {
		// centroids[k][d] = in.readByte();
		// }
		// }
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		// writer.println(centroids.length);
		// writer.println(centroids[0].length);
		//
		// for (int k = 0; k < centroids.length; k++) {
		// for (int d = 0; d < centroids[0].length; d++) {
		// writer.print(centroids[k][d] + ",");
		// }
		// writer.println();
		// }
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		// out.writeInt(centroids.length);
		// out.writeInt(centroids[0].length);
		//
		// for (int k = 0; k < centroids.length; k++) {
		// for (int d = 0; d < centroids[0].length; d++) {
		// out.writeByte(centroids[k][d]);
		// }
		// }
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		String str = "";
		str += "ByteCentroidsResult" + "\n";
		str += "No. of Clusters: " + centroids.length + "\n";
		str += "No. of Dimensions: " + centroids[0].length() + "\n";
		return str;
	}

	@Override
	public T[] getCentroids() {
		return this.centroids;
	}

	@Override
	public HardAssigner<T, float[], IntFloatPair> defaultHardAssigner() {
		return new ExactFeatureVectorAssigner<T>(this, null);
	}

	@Override
	public int numDimensions() {
		return centroids[0].length();
	}

	@Override
	public int numClusters() {
		return centroids.length;
	}
}
