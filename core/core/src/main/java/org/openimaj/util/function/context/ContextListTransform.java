package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Transform a {@link List} of {@link Context} instances to a stream of
 * {@link List} of T. The extraction strategy is used on each item in the list.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of elements in the {@link Context} being extracted
 */
public class ContextListTransform<T> implements Function<List<Context>, List<T>> {
	private ContextExtractor<T> extractor;

	/**
	 * Construct with the given extractor.
	 * 
	 * @param extract
	 *            the extractor
	 */
	public ContextListTransform(ContextExtractor<T> extract)
	{
		this.extractor = extract;
	}

	/**
	 * Construct using a {@link KeyContextExtractor} with the given key.
	 * 
	 * @param key
	 *            the key to extract
	 */
	public ContextListTransform(String key) {
		this.extractor = new KeyContextExtractor<T>(key);
	}

	@Override
	public List<T> apply(List<Context> in) {
		final List<T> ret = new ArrayList<T>();
		for (final Context context : in) {
			ret.add(this.extractor.extract(context));
		}
		return ret;
	}
}
