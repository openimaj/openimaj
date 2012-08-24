package org.openimaj.ml.sketch;


/**
 * As described by: http://lkozma.net/blog/sketching-data-structures/
 * 
 * "Sketching" data structures store a summary of a data set in situations where
 * the whole data would be prohibitively costly to store (at least in a
 * fast-access place like the memory as opposed to the hard disk).
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            the type of data being accumulated into the summary
 * @param <V>
 *            the value type of the summary being sketched
 * 
 */
public interface SummarySketcher<T, V> /* extends Sketcher<Collection<T>, V> */{
	/**
	 * @param data
	 *            update the sketch with this data
	 * @param value
	 *            the value to update with
	 */
	public void update(T data, V value);

	/**
	 * @param data
	 *            the data to query with
	 * @return the value to return
	 */
	public V query(T data);
}
