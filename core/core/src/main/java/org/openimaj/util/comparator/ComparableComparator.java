package org.openimaj.util.comparator;

import java.util.Comparator;

/**
 * A natural ordering {@link Comparator} for {@link Comparable} objects. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T> Type of {@link Comparable} object
 */
public class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {
	@Override
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
