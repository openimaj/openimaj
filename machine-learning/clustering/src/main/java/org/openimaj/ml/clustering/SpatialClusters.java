package org.openimaj.ml.clustering;

import org.openimaj.ml.clustering.assignment.HardAssigner;

/**
 * Interface to describe objects that are the result of the clustering performed
 * by a {@link SpatialClusterer}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <DATATYPE>
 *            the primitive array datatype which represents a centroid of this
 *            cluster.
 */
public interface SpatialClusters<DATATYPE> extends Clusters {
	/**
	 * Get the data dimensionality
	 * 
	 * @return the data dimensionality.
	 */
	public abstract int numDimensions();

	/**
	 * Get the number of clusters.
	 * 
	 * @return number of clusters.
	 */
	public int numClusters();

	/**
	 * Get the default hard assigner for this clusterer. This method is
	 * potentially expensive, so callers should only call it once, and hold on
	 * to the result (and reuse it).
	 * 
	 * @return a hard assigner.
	 */
	public HardAssigner<DATATYPE, ?, ?> defaultHardAssigner();
}
