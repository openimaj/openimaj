package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Extract the object from the context for given a key
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type being extracted
 */
public class KeyContextExtractor<T> implements ContextExtractor<T> {
	private String key;

	/**
	 * Construct to extract from the given key
	 * 
	 * @param key
	 *            the key
	 */
	public KeyContextExtractor(String key) {
		this.key = key;
	}

	@Override
	public T extract(Context c) {
		return c.getTyped(key);
	}
}
