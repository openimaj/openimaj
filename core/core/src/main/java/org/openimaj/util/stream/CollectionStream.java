package org.openimaj.util.stream;

import java.util.Collection;
import java.util.Iterator;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class CollectionStream<T> extends AbstractStream<T>
{
	private Iterator<T> iter;

	/**
	 * @param coll
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
