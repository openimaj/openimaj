package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;

/**
 * {@link ContextInsertor} that inserts an object at a specific key.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type being inserted
 */
public class KeyContextInsertor<T> implements ContextInsertor<T> {
	private String key;

	/**
	 * Construct to insert at the given key
	 * 
	 * @param key
	 *            the key
	 */
	public KeyContextInsertor(String key) {
		this.key = key;
	}

	@Override
	public void insert(T obj, Context c) {
		c.put(key, obj);
	}
}
