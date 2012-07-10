package org.openimaj.util.comparator;

/**
 * Interface for classes that can compare two objects 
 * and return a distance or similarity
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> type of object being compared
 */
public interface DistanceComparator<T> {
	/**
	 * Compare two objects, returning a score
	 * or distance.
	 * 
	 * @param o1 the first object
	 * @param o2 the second object
	 * @return a score or distance
	 */
	public abstract double compare(T o1, T o2);
	
	/**
	 * @return true if the comparison is a distance; false if similarity.
	 */
	public abstract boolean isDistance();
}
