package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Extract from the context given a key
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class KeyContextExtractionStrategy<T> implements ContextExtractionStrategy<T> {

	private String key;
	/**
	 * @param key
	 */
	public KeyContextExtractionStrategy(String key) {
		this.key = key;
	}
	@Override
	public T extract(Context c) {
		return c.getTyped(key);
	}

}
