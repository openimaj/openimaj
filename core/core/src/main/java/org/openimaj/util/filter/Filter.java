package org.openimaj.util.filter;

/**
 * A {@link Filter} is used to determine whether
 * a particular object should be accepted or rejected.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> type of object being filtered
 */
public interface Filter<T> {
	/**
	 * Tests whether a specific object should be accepted or rejected.
	 * @param object the object being tested.
	 * @return true if object is accepted; false if rejected.
	 */
	public abstract boolean accept(T object);
}
