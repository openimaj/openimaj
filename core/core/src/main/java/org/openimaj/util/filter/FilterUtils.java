package org.openimaj.util.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openimaj.util.array.ArrayIterator;

/**
 * Utilities for applying filters to collections.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FilterUtils {
	private FilterUtils() {}
	
	/**
	 * Filter a collection, returning the accepted items
	 * in an {@link ArrayList}.
	 * 
	 * @see Filter#accept(Object)
	 * 
	 * @param <T> type of object being filtered
	 * @param <Q> type of object accepted by the filter
	 * @param in input collection
	 * @param filter the filter
	 * @return the list of accepted items
	 */
	public static <T, Q extends T> ArrayList<Q> filter(final Collection<Q> in, final Filter<T> filter) {
		ArrayList<Q> out = new ArrayList<Q>();
		
		for (Q item : in) 
			if (filter.accept(item))
				out.add(item);
		
		return out;
	}
	
	/**
	 * Filter a collection, storing the accepted items
	 * in the given output collection.
	 * 
	 * @see Filter#accept(Object)
	 * 
	 * @param <T> type of object being filtered
	 * @param in input collection
	 * @param out output collection
	 * @param filter the filter
	 * @return the list of accepted items
	 */
	public static <T> Collection<T> filter(final Collection<? extends T> in, final Collection<T> out, final Filter<T> filter) {
		for (T item : in) 
			if (filter.accept(item))
				out.add(item);
		
		return out;
	}
	
	/**
	 * Create an iterator that filters items from the given {@link Iterable}.
	 * 
	 * @see Filter#accept(Object)
	 * 
	 * @param <T> type of object being filtered
	 * @param iterable {@link Iterable} to filter
	 * @param filter the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final Iterable<? extends T> iterable, final Filter<T> filter) {
		return filteredIterator(iterable.iterator(), filter);
	}
	
	/**
	 * Create an iterator that filters items from the given {@link Iterator}.
	 * 
	 * @see Filter#accept(Object)
	 * 
	 * @param <T> type of object being filtered
	 * @param iterator {@link Iterator} to filter
	 * @param filter the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final Iterator<? extends T> iterator, final Filter<T> filter) {
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
					
					if (filter.accept(next))
						return true;
				}
				
				return false;
			}

			@Override
			public T next() {
				if (next == null)
					hasNext();

				T ret = next;
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
	 * @see Filter#accept(Object)
	 * 
	 * @param <T> type of object being filtered
	 * @param array to filter
	 * @param filter the filter
	 * @return the list of accepted items
	 */
	public static <T> Iterator<T> filteredIterator(final T[] array, final Filter<T> filter) {
		return filteredIterator(new ArrayIterator<T>(array), filter);
	}
}
