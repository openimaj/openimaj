package org.openimaj.util.function.context;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <WRAP>
 * @param <IN>
 * @param <OUT>
 */
public abstract class ContextWrapper<WRAP,IN,OUT> {

	protected WRAP inner;
	protected ContextExtractionStrategy<IN> extract;
	protected ContextInsertionStrategy<OUT> insert;

	/**
	 * @param inner
	 * @param extract
	 * @param insert
	 */
	public ContextWrapper(WRAP inner, ContextExtractionStrategy<IN> extract, ContextInsertionStrategy<OUT> insert) {
		this.inner = inner;
		this.insert = insert;
		this.extract = extract;
	}
	/**
	 * @param inner
	 * @param keyin
	 * @param keyout
	 */
	public ContextWrapper(WRAP inner, String keyin, String keyout) {
		this.inner = inner;
		this.extract = new KeyContextExtractionStrategy<IN>(keyin);
		this.insert = new KeyContextInsertionStrategy<OUT>(keyout);

	}
}
