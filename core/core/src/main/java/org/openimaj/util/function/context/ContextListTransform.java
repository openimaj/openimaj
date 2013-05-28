package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Transform a {@link List} if {@link Context} instances
 * to a stream of {@link List} of T. The extraction strategy is used
 * on each item in the list
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class ContextListTransform<T> implements Function<List<Context>,List<T>> {


	private ContextExtractionStrategy<T> extractor;

	/**
	 * @param extract the extraction strategy
	 */
	public ContextListTransform(ContextExtractionStrategy<T> extract)
	{
		this.extractor = extract;
	}
	/**
	 * @param key the key to extract (a {@link KeyContextExtractionStrategy} is used)
	 */
	public ContextListTransform(String key) {
		this.extractor = new KeyContextExtractionStrategy<T>(key);
	}

	@Override
	public List<T> apply(List<Context> in) {
		List<T> ret = new ArrayList<T>();
		for (Context context : in) {
			ret.add(this.extractor.extract(context));
		}
		return ret ;
	}

}
