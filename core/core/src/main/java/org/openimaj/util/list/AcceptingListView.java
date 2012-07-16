package org.openimaj.util.list;

import java.util.AbstractList;
import java.util.List;

/**
 * A read-only view on a list with a set of indices from the
 * underlying list that must be accepted; all other
 * indices are discarded. The view is
 * continuous, and presents itself without any gaps.
 * The size of the view is equal to the length of 
 * the number of skipped elements. 
 * <p>
 * The {@link AcceptingListView} is the exact opposite
 * of the {@link SkippingListView}. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The type of element in the list
 */
public class AcceptingListView<T> extends AbstractList<T> {
	private List<T> list;
	private int[] indices;

	/**
	 * Construct with the underlying list and a set of
	 * indices for the view. 
	 * 
	 * @param list the backing list
	 * @param indices the indices
	 */
	public AcceptingListView(List<T> list, int... indices) {
		this.list = list;
				
		this.indices = indices;
	}
	
	@Override
	public T get(int index) {
		return list.get(indices[index]);
	}

	@Override
	public int size() {
		return indices.length;
	}
}
