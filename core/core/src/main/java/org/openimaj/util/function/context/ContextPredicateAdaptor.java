package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Predicate;

/**
 * An adaptor that allows a {@link Predicate} to be applied to a {@link Context}
 * , based on a single element of the {@link Context}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type processed by the predicate
 */
public class ContextPredicateAdaptor<T> extends ContextAdaptor<Predicate<T>, T, T> implements Predicate<Context> {
	/**
	 * Construct with the given options.
	 * 
	 * @param inner
	 *            the predicate
	 * @param extract
	 *            the extractor
	 * @param insert
	 *            the insertor
	 */
	public ContextPredicateAdaptor(Predicate<T> inner, ContextExtractor<T> extract, ContextInsertor<T> insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the given predicate. The insertor and extractor are
	 * created from the given keys.
	 * 
	 * @param inner
	 *            the predicate
	 * @param extract
	 *            the key to extract from the context to produce the input for
	 *            the object
	 * @param insert
	 *            the key to insert with the the output for the object
	 */
	public ContextPredicateAdaptor(Predicate<T> inner, String extract, String insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the given predicate. The insertor and extractor are
	 * created from the same key, so the output will overwrite the input.
	 * 
	 * @param inner
	 *            the object being adapted
	 * @param both
	 *            the key to extract/insert
	 */
	public ContextPredicateAdaptor(Predicate<T> inner, String both)
	{
		super(inner, both, both);
	}

	@Override
	public boolean test(Context in) {
		return inner.test(this.extract.extract(in));
	}
}
