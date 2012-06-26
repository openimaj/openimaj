package org.openimaj.ml.clustering.assignment;


/**
 * The {@link HardAssigner} interface describes classes that
 * assign a spatial point to a single cluster.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <DATATYPE> the primitive array datatype which represents a centroid of this cluster.
 * @param <DISTANCES> primitive array datatype for recording distances between points and cluster centroids.
 * @param <DISTANCE_INDEX> datatype for representing an <index, distance> pair.
 */
public interface HardAssigner<DATATYPE, DISTANCES, DISTANCE_INDEX> extends Assigner<DATATYPE> {
	/**
	 * Assign data to a cluster.
	 * 
	 * @param data the data.
	 * @return The cluster indices which the data was assigned to.
	 */
	public abstract int[] assign(final DATATYPE[] data);

	/**
	 * Assign a single point to a cluster.
	 * 
	 * @param data datum to assign.
	 * 
	 * @return the cluster index.
	 */
	public abstract int assign(final DATATYPE data);
	
	/**
	 * Assign data to clusters. The results are returned
	 * in the indices and distances arrays. The return arrays
	 * must have the same length as the data array. 
	 *            
	 * @param data the data.
	 * @param indices the cluster index for each data point.
	 * @param distances the distance to the closest cluster for each data point.
	 */
	public abstract void assignDistance(final DATATYPE[] data, int[] indices, DISTANCES distances);

	/**
	 * Assign a single point to a cluster.
	 * 
	 * @param data point to assign.
	 * 
	 * @return the cluster index and distance.
	 */
	public abstract DISTANCE_INDEX assignDistance(final DATATYPE data);
}
