package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Transform a context into a stream of another type based on items extracted from the context
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class ContextTransform<T> implements Function<Context,T> {


	private ContextExtractionStrategy<T> extractor;

	/**
	 * @param extract the extraction strategy
	 */
	public ContextTransform(ContextExtractionStrategy<T> extract)
	{
		this.extractor = extract;
	}
	/**
	 * @param key the key to extract (a {@link KeyContextExtractionStrategy} is used)
	 */
	public ContextTransform(String key) {
		this.extractor = new KeyContextExtractionStrategy<T>(key);
	}

	@Override
	public T apply(Context in) {
		return this.extractor.extract(in);
	}

}
