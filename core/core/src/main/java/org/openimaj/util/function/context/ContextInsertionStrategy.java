package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Given a context insert an object
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public interface ContextInsertionStrategy<T> {
	/**
	 * insert obj into c
	 *
	 * @param obj
	 * @param c
	 */
	public void insert(T obj, Context c);
}
