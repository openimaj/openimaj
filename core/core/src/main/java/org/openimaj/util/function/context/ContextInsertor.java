package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Given a context, insert an object of type T
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of being inserted
 */
public interface ContextInsertor<T> {
	/**
	 * Insert the object into the context
	 * 
	 * @param obj
	 *            the object
	 * @param c
	 *            the context
	 */
	public void insert(T obj, Context c);
}
