package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Predicate;

/**
 * Given a list inside a context, filter the items in the list based on the predicate
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class ContextListFilter<IN> extends ContextWrapper<Predicate<IN>,List<IN>, List<IN>> implements Function<Context,Context>{

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextListFilter(ContextExtractionStrategy<List<IN>> extract,ContextInsertionStrategy<List<IN>> insert,Predicate<IN> inner)
	{
		super(inner, extract, insert);
	}


	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextListFilter(String extract, String insert,Predicate<IN> inner)
	{
		super(inner, extract, insert);
	}

	/**
	 * @param inner
	 * @param both the extract and insert key
	 */
	public ContextListFilter(String both,Predicate<IN> inner)
	{
		super(inner, both, both);
	}


	@Override
	public Context apply(Context in) {
		List<IN> obj = this.extract.extract(in);
		List<IN> out = new ArrayList<IN>();
		for (IN inItem : obj) {
			if(this.inner.test(inItem))out.add(inItem);
		}
		this.insert.insert(out, in);
		return in;
	}

}
