package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

/**
 * An adaptor that allows an {@link Operation} to be applied to a single element
 * of the {@link Context}.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of element that the operation is applied to
 */
public class ContextOperationAdaptor<T> extends ContextAdaptor<Operation<T>, T, T> implements Operation<Context> {
	/**
	 * Construct with the given operation and extractor
	 * 
	 * @param inner
	 *            the operation
	 * @param extract
	 *            the extractor
	 */
	public ContextOperationAdaptor(Operation<T> inner, ContextExtractor<T> extract)
	{
		super(inner, extract, null);
	}

	/**
	 * Construct with the given operation. The extractor is a
	 * {@link KeyContextExtractor} created from the given key.
	 * 
	 * @param inner
	 *            the operation
	 * @param extract
	 *            the key
	 */
	public ContextOperationAdaptor(Operation<T> inner, String extract)
	{
		super(inner, extract, extract);
	}

	@Override
	public void perform(Context object) {
		inner.perform(extract.extract(object));
	}

	/**
	 * Helper to create a new {@link ContextOperationAdaptor}.
	 * 
	 * @param operation
	 *            the operation
	 * @param extract
	 *            the key to extract from
	 * 
	 * @see ContextOperationAdaptor#ContextOperationAdaptor(Operation,
	 *      ContextExtractor)
	 * 
	 * @return the context operation
	 */
	public static <IN> Operation<Context> create(Operation<IN> operation, String extract) {
		return new ContextOperationAdaptor<IN>(operation, extract);
	}
}
