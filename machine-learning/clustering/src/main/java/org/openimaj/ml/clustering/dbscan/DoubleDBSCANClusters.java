package org.openimaj.ml.clustering.dbscan;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;

/**
 * {@link DBSCANClusters} which also holds the original data, therefore allowing an implementation
 * of {@link #defaultHardAssigner()} with a single round of {@link DoubleDBSCAN}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleDBSCANClusters extends DBSCANClusters implements SpatialClusters<double[]>{

	/**
	 * The data
	 */
	public double[][] data;

	@Override
	public void readASCII(Scanner in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String asciiHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] binaryHeader() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numDimensions() {
		return this.conf.M;
	}

	@Override
	public int numClusters() {
		return this.clusterMembers.length;
	}

	@Override
	public HardAssigner<double[], ?, ?> defaultHardAssigner() {
		return null;
	}

}
