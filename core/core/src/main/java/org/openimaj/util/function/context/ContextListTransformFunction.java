package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Transform a {@link List} of {@link Context} instances
 * to a {@link List} of IN and immediately hand this list to an internal function.
 * Return this function's application on the klist as stream
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 * @param <OUT>
 */
public class ContextListTransformFunction<IN,OUT> implements Function<List<Context>,OUT> {


	private ContextExtractionStrategy<IN> extractor;
	private Function<List<IN>, OUT> inner;

	/**
	 * @param extract the extraction strategy
	 * @param inner the function applied to the transformed list
	 */
	public ContextListTransformFunction(ContextExtractionStrategy<IN> extract, Function<List<IN>,OUT> inner)
	{
		this.extractor = extract;
		this.inner = inner;
	}
	/**
	 * @param key the key to extract (a {@link KeyContextExtractionStrategy} is used)
	 * @param inner function applied to the transformed list
	 */
	public ContextListTransformFunction(String key, Function<List<IN>,OUT> inner) {
		this.extractor = new KeyContextExtractionStrategy<IN>(key);
		this.inner = inner;
	}

	@Override
	public OUT apply(List<Context> in) {
		List<IN> ret = new ArrayList<IN>();
		for (Context context : in) {
			ret.add(this.extractor.extract(context));
		}
		return inner.apply(ret) ;
	}

}
