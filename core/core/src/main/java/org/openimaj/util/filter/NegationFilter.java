package org.openimaj.util.filter;

/**
 * Negates the filtering performed by another
 * {@link Filter}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> type of object being filtered.
 */
public class NegationFilter<T> implements Filter<T> {
	Filter<T> innerFilter;
	
	/**
	 * Construct with the given filter.
	 * @param innerFilter the filter that this filter negates.
	 */
	public NegationFilter(Filter<T> innerFilter) {
		this.innerFilter = innerFilter;
	}
	
	@Override
	public boolean accept(T object) {
		return !innerFilter.accept(object);
	}
}
