package org.openimaj.ml.clustering.dbscan;

import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;


/**
 * {@link DBSCANClusters} which also holds the original data
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DoubleDBSCANClusters extends DBSCANClusters implements SpatialClusters<double[]>{

	
	/**
	 * @param noise
	 * @param clusters
	 */
	public DoubleDBSCANClusters(int[] noise, int[][] clusters) {
		super(noise, clusters);
	}
	
	/**
	 * @param noise
	 * @param clusters
	 * @param nEntries
	 */
	public DoubleDBSCANClusters(int[] noise, int[][] clusters, int nEntries) {
		super(noise, clusters, nEntries);
	}

	/**
	 * The data
	 */
	public double[][] data;

	@Override
	public int numDimensions() {
		return data[0].length;
	}

	@Override
	public HardAssigner<double[], ?, ?> defaultHardAssigner() {
		return null;
	}

	
}
