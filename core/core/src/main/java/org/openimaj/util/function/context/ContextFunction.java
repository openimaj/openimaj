package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 * @param <OUT>
 */
public class ContextFunction<IN,OUT> extends ContextWrapper<Function<IN,OUT>,IN, OUT> implements Function<Context,Context>{

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextFunction(ContextExtractionStrategy<IN> extract,ContextInsertionStrategy<OUT> insert,Function<IN, OUT> inner)
	{
		super(inner, extract, insert);
	}


	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextFunction(String extract, String insert,Function<IN, OUT> inner)
	{
		super(inner, extract, insert);
	}

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextFunction(ContextExtractionStrategy<IN> extract, String insert,Function<IN, OUT> inner)
	{
		super(inner, extract, new KeyContextInsertionStrategy<OUT>(insert));
	}

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextFunction(String extract, ContextInsertionStrategy<OUT> insert,Function<IN, OUT> inner)
	{
		super(inner, new KeyContextExtractionStrategy<IN>(extract), insert);
	}

	/**
	 * @param inner
	 * @param both the extract and insert key
	 */
	public ContextFunction(String both,Function<IN, OUT> inner)
	{
		super(inner, both, both);
	}


	@Override
	public Context apply(Context in) {
		OUT obj = inner.apply(this.extract.extract(in));
		this.insert.insert(obj, in);
		return in;
	}


	public static <IN,OUT> Function<Context,Context> func(String extract, String insert, Function<IN,OUT> inner) {
		return new ContextFunction<IN, OUT>(extract, insert, inner);
	}

}
