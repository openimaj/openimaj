package org.openimaj.util.list;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * A read-only view on a list with a set of indices from the
 * underlying list that must be skipped. The view is
 * continuous, and presents itself without any gaps.
 * The size  of the view is equal to the length of 
 * the underlying list minus the number of skipped elements. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The type of element in the list
 */
public class SkippingListView<T> extends AbstractList<T> {
	private List<T> list;
	private int[] skips;
	private int size;

	/**
	 * Construct with the underlying list and a set of
	 * indices to skip. 
	 * 
	 * @param list the backing list
	 * @param skips the indices to skip
	 */
	public SkippingListView(List<T> list, int... skips) {
		this.list = list;
		this.size = list.size() - skips.length;
		
		this.skips = skips.clone();
		Arrays.sort(this.skips);
		
		for (int i=0; i<skips.length; i++)
			this.skips[i] -= i;
	}
	
	@Override
	public T get(int index) {
		int shift = Arrays.binarySearch(skips, index);
				
		if (shift < 0) {
			shift = -1 * (shift + 1);
		} else {
			while ((shift < skips.length - 1) && (skips[shift] == skips[shift + 1])) {
				shift++;
			}
			
			shift++;
			
		}
		
		return list.get(index + shift);
	}

	@Override
	public int size() {
		return size;
	}
}
