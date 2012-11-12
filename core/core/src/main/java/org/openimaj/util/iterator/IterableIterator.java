package org.openimaj.util.iterator;

import java.util.Iterator;

/**
 * Wrapper to allow an {@link Iterator} as an {@link Iterable} so it can be used
 * in an a foreach loop. The iterator is consumed by the loop and so must only
 * be used once. Normal usage is as follows:
 * 
 * <pre>
 * for (T t : IterableIterator.in(iterator)) {
 * 	// ...
 * }
 * </pre>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being iterated over.
 */
public class IterableIterator<T> implements Iterable<T> {
	Iterator<T> iterator;

	private IterableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}

	/**
	 * Wrapper to allow an {@link Iterator} as an {@link Iterable} so it can be
	 * used in a foreach loop. The iterator is consumed by the loop and so must
	 * only be used once. Normal usage is as follows:
	 * 
	 * <pre>
	 * for (T t : IterableIterator.in(iterator)) {
	 * 	// ...
	 * }
	 * </pre>
	 * 
	 * @param <T>
	 *            Type of object being iterated over.
	 * 
	 * @param iterator
	 *            an iterator
	 * @return an iterable wrapper which will traverse the iterator once
	 */
	public static <T> Iterable<T> in(Iterator<T> iterator) {
		return new IterableIterator<T>(iterator);
	}
}
