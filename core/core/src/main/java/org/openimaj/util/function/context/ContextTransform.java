package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Transform a context into a stream of another type based on items extracted
 * from the context
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being extracted.
 */
public class ContextTransform<T> implements Function<Context, T> {
	private ContextExtractor<T> extractor;

	/**
	 * Construct with the given extractor
	 * 
	 * @param extract
	 *            the extractor
	 */
	public ContextTransform(ContextExtractor<T> extract)
	{
		this.extractor = extract;
	}

	/**
	 * Construct with a {@link KeyContextExtractor} using the given key.
	 * 
	 * @param key
	 *            the key to extract (a {@link KeyContextExtractor} is used)
	 */
	public ContextTransform(String key) {
		this.extractor = new KeyContextExtractor<T>(key);
	}

	@Override
	public T apply(Context in) {
		return this.extractor.extract(in);
	}
}
