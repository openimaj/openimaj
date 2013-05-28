package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Generate a context stream from a stream of other objects
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class ContextGenerator<T> implements Function<T,Context> {


	private ContextInsertionStrategy<T> insert;

	/**
	 * @param insert the insertion strategy
	 */
	public ContextGenerator(ContextInsertionStrategy<T> insert)
	{
		this.insert= insert;
	}
	/**
	 * @param key the key to extract (a {@link KeyContextExtractionStrategy} is used)
	 */
	public ContextGenerator(String key) {
		this.insert = new KeyContextInsertionStrategy<T>(key);
	}

	@Override
	public Context apply(T in) {
		Context c = new Context();
		this.insert.insert(in, c);
		return c;
	}

}
