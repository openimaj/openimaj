package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * Insert object at a key
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class KeyContextInsertionStrategy<T> implements ContextInsertionStrategy<T> {

	private String key;

	/**
	 * @param key
	 */
	public KeyContextInsertionStrategy(String key) {
		this.key = key;
	}
	@Override
	public void insert(T obj, Context c) {
		c.put(key, obj);
	}

}
