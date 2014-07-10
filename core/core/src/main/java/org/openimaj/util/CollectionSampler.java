package org.openimaj.util;

import java.util.Collection;
import java.util.List;

/**
 * Interface defining an object capable of sampling a collection.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            type of items in the collection being sampled
 */
public interface CollectionSampler<T> {
	/**
	 * Set the items to sample from. This should only be called once for any
	 * given set of items. Implementors can assume that calling this resets any
	 * state of the sampler.
	 * 
	 * @param collection
	 *            the items to sample from
	 */
	public void setCollection(Collection<? extends T> collection);

	/**
	 * Samples <code>nItems</code> items from the collection set by
	 * {@link #setCollection(Collection)}, returning a new collection with the
	 * given samples. Implementations can decide what to do if
	 * <code>nItems</code> is bigger than the number of items in the collection.
	 * 
	 * @param nItems
	 *            the number of items to sample
	 * @return the sample; can be <code>null</code> if a sample cannot be made
	 *         for any reason.
	 */
	public List<T> sample(int nItems);
}
