package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <IN>
 */
public class ContextOperation<IN> extends ContextWrapper<Operation<IN>,IN, IN> implements Operation<Context>{

	/**
	 * @param inner
	 * @param extract
	 */
	public ContextOperation(ContextExtractionStrategy<IN> extract,Operation<IN> inner)
	{
		super(inner, extract, null);
	}

	/**
	 * @param inner
	 * @param extract
	 */
	public ContextOperation(String extract,Operation<IN> inner)
	{
		super(inner, extract, extract);
	}

	@Override
	public void perform(Context object) {
		inner.perform(extract.extract(object));
	}

	/**
	 * @param extract
	 * @param operation
	 * @return the context operation wrapper
	 */
	public static <IN> Operation<Context> op(String extract, Operation<IN> operation) {
		return new ContextOperation<IN>(extract,operation);
	}




}
