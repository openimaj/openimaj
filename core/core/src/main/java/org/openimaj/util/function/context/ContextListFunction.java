package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 * @param <OUT>
 */
public class ContextListFunction<IN,OUT> extends ContextWrapper<Function<IN,OUT>,List<IN>, List<OUT>> implements Function<Context,Context>{

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextListFunction(ContextExtractionStrategy<List<IN>> extract,ContextInsertionStrategy<List<OUT>> insert,Function<IN, OUT> inner)
	{
		super(inner, extract, insert);
	}


	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextListFunction(String extract, String insert,Function<IN, OUT> inner)
	{
		super(inner, extract, insert);
	}

	/**
	 * @param inner
	 * @param both the extract and insert key
	 */
	public ContextListFunction(String both,Function<IN, OUT> inner)
	{
		super(inner, both, both);
	}


	@Override
	public Context apply(Context in) {
		List<IN> obj = this.extract.extract(in);
		List<OUT> out = new ArrayList<OUT>();
		for (IN inItem : obj) {
			out.add(this.inner.apply(inItem));
		}
		this.insert.insert(out, in);
		return in;
	}

}
