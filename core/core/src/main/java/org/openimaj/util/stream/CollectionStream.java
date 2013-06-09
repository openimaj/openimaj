package org.openimaj.util.stream;

import java.util.Collection;
import java.util.Iterator;

/**
 * {@link Stream} based on any {@link Collection} of items.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of items in the stream/collection
 */
public class CollectionStream<T> extends AbstractStream<T> {
	private Iterator<T> iter;

	/**
	 * Construct with the given collection.
	 * 
	 * @param coll
	 *            the collection
	 */
	public CollectionStream(Collection<T> coll) {
		this.iter = coll.iterator();
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public T next() {
		return iter.next();
	}
}
