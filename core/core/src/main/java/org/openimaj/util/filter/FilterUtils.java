/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.util.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openimaj.util.array.ArrayIterator;
import org.openimaj.util.function.Predicate;

/**
 * Utilities for filtering collections by applying a {@link Predicate}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FilterUtils {
	private FilterUtils() {
	}

	/**
	 * Filter a collection, returning the accepted items in an {@link ArrayList}
	 * .
	 * 
	 * @see Predicate#test(Object)
	 * 
	 * @param <T>
	 *            type of object being filtered
	 * @param <Q>
	 *            type of object accepted by the filter
	 * @param in
	 *            input collection
	 * @param filter
	 *            the filter
	 * @return the list of accepted items
	 */
	public static <T, Q extends T> ArrayList<Q> filter(final Collection<Q> in, final Predicate<T> filter) {
		final ArrayList<Q> out = new ArrayList<Q>();

		for (final Q item : in)
			if (filter.test(item))
				out.add(item);

		return out;
	}

	/**
	 * Filter a collection, storing the accepted items in the given output
	 * collection.
	 * 
	 * @see Predicate#test(Object)
	 * 
	 * @param <T>
	 *            type of object being filtered
	 * @param in
	 *            input collection
	 * @param out
	 *            output collection
	 * @param filter
	 *            the filter
	 * @return the list of accepted items
	 */
	public static <T> Collection<T> filter(final Collection<? extends T> in, final Collection<T> out,
			final Predicate<T> filter)
	{
		for (final T item : in)
			if (filter.test(item))
				out.add(item);

		return out;
	}

	/**
	 * Create an iterator that filters items from the given {@link Iterable}.
	 * 
	 * @see Predicate#test(Object)
	 * 
	 * @param <T>
	 *            type of object being filtered
	 * @param iterable
	 *            {@link Iterable} to filter
	 * @param filter
	 *            the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final Iterable<? extends T> iterable, final Predicate<T> filter) {
		return filteredIterator(iterable.iterator(), filter);
	}

	/**
	 * Create an iterator that filters items from the given {@link Iterator}.
	 * 
	 * @see Predicate#test(Object)
	 * 
	 * @param <T>
	 *            type of object being filtered
	 * @param iterator
	 *            {@link Iterator} to filter
	 * @param filter
	 *            the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final Iterator<? extends T> iterator, final Predicate<T> filter) {
		return new Iterator<T>() {
			T next;

			@Override
			public boolean hasNext() {
				if (next != null)
					return true;

				if (!iterator.hasNext())
					return false;

				while (iterator.hasNext()) {
					next = iterator.next();

					if (filter.test(next))
						return true;
				}

				return false;
			}

			@Override
			public T next() {
				if (next == null)
					hasNext();

				final T ret = next;
				next = null;
				return ret;
			}

			@Override
			public void remove() {
				iterator.remove();
			}
		};
	}

	/**
	 * Create an iterator that filters items from the given array.
	 * 
	 * @see Predicate#test(Object)
	 * 
	 * @param <T>
	 *            type of object being filtered
	 * @param array
	 *            to filter
	 * @param filter
	 *            the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final T[] array, final Predicate<T> filter) {
		return filteredIterator(new ArrayIterator<T>(array), filter);
	}
}
