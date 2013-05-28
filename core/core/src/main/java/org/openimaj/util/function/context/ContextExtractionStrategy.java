package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Given a context, extract an element of type T
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public interface ContextExtractionStrategy<T> {
	/**
	 * @param c
	 * @return the element of type T
	 */
	public T extract(Context c);
}
