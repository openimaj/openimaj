package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Given a context, extract an element of type T.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of the data being extracted
 */
public interface ContextExtractor<T> {
	/**
	 * Extract the data from the context
	 * 
	 * @param c
	 *            the context
	 * @return the extracted data of type T from the context
	 */
	public T extract(Context c);
}
