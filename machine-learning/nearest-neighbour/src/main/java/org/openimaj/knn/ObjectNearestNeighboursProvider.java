package org.openimaj.knn;

/**
 * Interface for classes able to expose a k-nearest-neighbour object.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 *            Type of object
 */
public interface ObjectNearestNeighboursProvider<T> {
	/**
	 * @return the underlying nearest neighbour object
	 */
	public ObjectNearestNeighbours<T> getNearestNeighbours();
}
