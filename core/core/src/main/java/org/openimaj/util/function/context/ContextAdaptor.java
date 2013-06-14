package org.openimaj.util.function.context;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

/**
 * Base class that holds the elements required to adapt something (i.e. a
 * {@link Function}) to work around around a single element of a {@link Context}
 * and output to a potentially different key of that {@link Context}.
 * <p>
 * This base class just holds the parts required for the adaption (the object
 * being adapted, and the {@link ContextExtractor} and
 * {@link ContextInsertor}), but doesn't actually provide any
 * functionality.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <ADAPTED>
 *            The type of object being adapted
 * @param <IN>
 *            The input type of the object being adapted
 * @param <OUT>
 *            The output type of the object being adapted
 */
public abstract class ContextAdaptor<ADAPTED, IN, OUT> {
	protected ADAPTED inner;
	protected ContextExtractor<IN> extract;
	protected ContextInsertor<OUT> insert;

	/**
	 * Construct with the given options.
	 * 
	 * @param inner
	 *            the object being adapted
	 * @param extract
	 *            the extractor
	 * @param insert
	 *            the insertor
	 */
	public ContextAdaptor(ADAPTED inner, ContextExtractor<IN> extract, ContextInsertor<OUT> insert) {
		this.inner = inner;
		this.insert = insert;
		this.extract = extract;
	}

	/**
	 * Construct with the given object to adapt. The insertor and extractor are
	 * created from the given keys.
	 * 
	 * @param inner
	 *            the object being adapted
	 * @param keyin
	 *            the key to extract from the context to produce the input for
	 *            the object
	 * @param keyout
	 *            the key to insert with the the output for the object
	 */
	public ContextAdaptor(ADAPTED inner, String keyin, String keyout) {
		this.inner = inner;
		this.extract = new KeyContextExtractor<IN>(keyin);
		this.insert = new KeyContextInsertor<OUT>(keyout);
	}
}
