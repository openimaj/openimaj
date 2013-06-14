package org.openimaj.util.function.context;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Apply a {@link Function} to a list held within a single element of a
 * {@link Context}, writing the resultant list back to the {@link Context},
 * potentially with a different key.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <IN>
 *            The input type of the function (and the type of elements in the
 *            list)
 * @param <OUT>
 *            The output type of the function (and type of elements in the
 *            resultant list)
 */
public class ContextListFunction<IN, OUT> extends ContextAdaptor<Function<IN, OUT>, List<IN>, List<OUT>>
		implements
		Function<Context, Context>
{

	/**
	 * Construct with the given options.
	 * 
	 * @param inner
	 *            the function
	 * @param extract
	 *            the extractor
	 * @param insert
	 *            the insertor
	 */
	public ContextListFunction(Function<IN, OUT> inner, ContextExtractor<List<IN>> extract,
			ContextInsertor<List<OUT>> insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the function. The insertor and extractor are created from
	 * the given keys.
	 * 
	 * @param inner
	 *            the function
	 * @param extract
	 *            the key to extract from the context to produce the input for
	 *            the object
	 * @param insert
	 *            the key to insert with the the output for the object
	 */
	public ContextListFunction(Function<IN, OUT> inner, String extract, String insert)
	{
		super(inner, extract, insert);
	}

	/**
	 * Construct with the given function. The insertor and extractor are created
	 * from the same key, so the output will overwrite the input.
	 * 
	 * @param inner
	 *            the function
	 * @param both
	 *            the key to extract/insert
	 */
	public ContextListFunction(Function<IN, OUT> inner, String both)
	{
		super(inner, both, both);
	}

	@Override
	public Context apply(Context in) {
		final List<IN> obj = this.extract.extract(in);
		final List<OUT> out = new ArrayList<OUT>();

		for (final IN inItem : obj) {
			out.add(this.inner.apply(inItem));
		}

		this.insert.insert(out, in);

		return in;
	}
}
