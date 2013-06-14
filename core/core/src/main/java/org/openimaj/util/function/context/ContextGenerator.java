package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Generate a context stream from a stream of other objects.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of input objects
 */
public class ContextGenerator<T> implements Function<T, Context> {
	private ContextInsertor<T> insert;

	/**
	 * Construct with the given insertor.
	 * 
	 * @param insert
	 *            the insertor
	 */
	public ContextGenerator(ContextInsertor<T> insert) {
		this.insert = insert;
	}

	/**
	 * Construct with the given key, which is used to create a
	 * {@link KeyContextInsertor}.
	 * 
	 * @param key
	 *            the key to extract (a {@link KeyContextInsertor} is
	 *            used)
	 */
	public ContextGenerator(String key) {
		this.insert = new KeyContextInsertor<T>(key);
	}

	@Override
	public Context apply(T in) {
		final Context c = new Context();

		this.insert.insert(in, c);

		return c;
	}
}
