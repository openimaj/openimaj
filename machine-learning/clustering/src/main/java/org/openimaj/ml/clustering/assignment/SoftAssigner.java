package org.openimaj.ml.clustering.assignment;

import org.openimaj.util.pair.IndependentPair;

/**
 * The {@link SoftAssigner} interface describes classes that
 * assign a spatial point to multiple clusters, possibly with 
 * weighting.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATATYPE> the primitive array datatype which represents a centroid of this cluster.
 * @param <DISTANCES> primitive array datatype for recording distances between points and cluster centroids.
 */
public interface SoftAssigner<DATATYPE, DISTANCES> extends Assigner<DATATYPE> {
	/**
	 * Assign data to clusters.
	 * 
	 * @param data the data.
	 * @return The cluster indices which the data was assigned to.
	 */
	public int[][] assign(DATATYPE[] data);

	/**
	 * Assign a single point to some clusters.
	 * 
	 * @param data datum to assign.
	 * 
	 * @return the assigned cluster indices.
	 */
	public int[] assign(DATATYPE data);
	
	/**
	 * Assign data to clusters. The results are returned
	 * in the indices and distances arrays. The return arrays
	 * must have the same length as the data array. 
	 *            
	 * @param data the data.
	 * @param assignments the cluster indices for each data point.
	 * @param weights the weights to the for each cluster for each data point.
	 */
	public void assignWeighted(DATATYPE[] data, int[][] assignments, DISTANCES[] weights);

	/**
	 * Assign a single point to some clusters.
	 * 
	 * @param data point to assign.
	 * 
	 * @return the assigned cluster indices and weights.
	 */
	public IndependentPair<int[], DISTANCES> assignWeighted(DATATYPE data);
}
