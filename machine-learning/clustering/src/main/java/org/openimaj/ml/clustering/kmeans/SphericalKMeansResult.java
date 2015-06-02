package org.openimaj.ml.clustering.kmeans;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.ml.clustering.CentroidsProvider;
import org.openimaj.ml.clustering.Clusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.assignment.hard.ExactDoubleAssigner;
import org.openimaj.util.pair.IntDoublePair;

/**
 * The result of a {@link SpatialClusterer} that just produces a flat set of
 * centroids.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SphericalKMeansResult implements SpatialClusters<double[]>, CentroidsProvider<double[]> {
	final static String HEADER = Clusters.CLUSTER_HEADER + "SpKM";

	/** The centroids of the clusters */
	public double[][] centroids;

	/** The assignments of the training data to clusters */
	public int[] assignments;

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SphericalKMeansResult))
			return false;

		final SphericalKMeansResult other = (SphericalKMeansResult) obj;
		for (int i = 0; i < this.centroids.length; i++) {
			if (!Arrays.equals(this.centroids[i], other.centroids[i]))
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
		// Read Header
		final int K = Integer.parseInt(br.nextLine().trim());
		final int M = Integer.parseInt(br.nextLine().trim());

		centroids = new double[K][M];
		for (int k = 0; k < K; k++) {
			final String[] parts = br.nextLine().split(",");

			for (int d = 0; d < M; d++) {
				centroids[k][d] = Double.parseDouble(parts[d]);
			}
		}

		final int A = Integer.parseInt(br.nextLine().trim());
		assignments = new int[A];
		for (int a = 0; a < A; a++) {
			assignments[a] = Integer.parseInt(br.nextLine().trim());
		}
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final int K = in.readInt();
		final int M = in.readInt();

		centroids = new double[K][M];

		for (int k = 0; k < K; k++) {
			for (int d = 0; d < M; d++) {
				centroids[k][d] = in.readDouble();
			}
		}

		final int A = in.readInt();
		assignments = new int[A];
		for (int a = 0; a < A; a++) {
			assignments[a] = in.readInt();
		}
	}

	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		writer.println(centroids.length);
		writer.println(centroids[0].length);

		for (int k = 0; k < centroids.length; k++) {
			for (int d = 0; d < centroids[0].length; d++) {
				writer.print(centroids[k][d] + ",");
			}
			writer.println();
		}

		writer.println(assignments.length);
		for (int a = 0; a < assignments.length; a++) {
			writer.println(assignments[a]);
		}
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(centroids.length);
		out.writeInt(centroids[0].length);

		for (int k = 0; k < centroids.length; k++) {
			for (int d = 0; d < centroids[0].length; d++) {
				out.writeDouble(centroids[k][d]);
			}
		}

		out.writeInt(assignments.length);
		for (int a = 0; a < assignments.length; a++) {
			out.writeInt(assignments[a]);
		}
	}

	@Override
	public String toString() {
		String str = "";
		str += "DoubleCentroidsResult" + "\n";
		str += "No. of Clusters: " + centroids.length + "\n";
		str += "No. of Dimensions: " + centroids[0].length + "\n";
		return str;
	}

	@Override
	public double[][] getCentroids() {
		return this.centroids;
	}

	@Override
	public HardAssigner<double[], double[], IntDoublePair> defaultHardAssigner() {
		return new ExactDoubleAssigner(this, DoubleFVComparison.INNER_PRODUCT);
	}

	@Override
	public int numDimensions() {
		return centroids[0].length;
	}

	@Override
	public int numClusters() {
		return centroids.length;
	}

	/**
	 * Compute the histogram of number of assignments to each cluster
	 *
	 * @return the histogram
	 */
	public int[] getAssignmentHistogram() {
		final int[] hist = new int[centroids.length];

		for (int i = 0; i < assignments.length; i++) {
			hist[assignments[i]]++;
		}

		return hist;
	}

	/**
	 * Filter the cluster centroids be removing those with less than threshold
	 * items
	 *
	 * @param threshold
	 *            minimum number of items
	 * @return the filtered clusters
	 */
	public double[][] filter(int threshold) {
		final int[] hist = getAssignmentHistogram();
		final TIntArrayList toKeep = new TIntArrayList();
		for (int i = 0; i < hist.length; i++) {
			if (hist[i] > threshold) {
				toKeep.add(i);
			}
		}

		final double[][] fcen = new double[toKeep.size()][];
		toKeep.forEach(new TIntProcedure() {
			int i = 0;

			@Override
			public boolean execute(int value) {
				fcen[i++] = centroids[value];
				return true;
			}
		});

		return fcen;
	}
}
