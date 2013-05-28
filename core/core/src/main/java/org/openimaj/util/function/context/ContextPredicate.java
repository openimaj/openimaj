package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Predicate;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class ContextPredicate<IN> extends ContextWrapper<Predicate<IN>,IN, IN> implements Predicate<Context>{

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextPredicate(ContextExtractionStrategy<IN> extract,ContextInsertionStrategy<IN> insert,Predicate<IN> inner)
	{
		super(inner, extract, insert);
	}

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextPredicate(String extract, String insert, Predicate<IN> inner)
	{
		super(inner, extract, insert);
	}
	/**
	 * @param both
	 * @param inner
	 */
	public ContextPredicate(String both, Predicate<IN> inner)
	{
		super(inner, both, both);
	}


	@Override
	public boolean test(Context in) {
		return inner.test(this.extract.extract(in));
	}

}
