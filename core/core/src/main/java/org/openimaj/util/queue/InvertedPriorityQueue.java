package org.openimaj.util.queue;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * This class provides an inverted {@link PriorityQueue} implementation,
 * where objects that are higher (according to the provided {@link Comparator} or
 * the natural order) come first.
 * 
 * The Iterator provided in method {@link #iterator()} is 
 * <em>not</em> guaranteed to traverse the elements of
 * the priority queue in any particular order. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class InvertedPriorityQueue<T> extends PriorityQueue<T> {
	private static final long serialVersionUID = 1L;
	
	private static final int DEFAULT_INITIAL_CAPACITY = 11;
	
	/**
     * Creates a {@code InvertedPriorityQueue} with the default initial
     * capacity (11) that orders its elements according to their
     * inverted {@linkplain Comparable natural ordering}.
     */
	public InvertedPriorityQueue() {
		super(DEFAULT_INITIAL_CAPACITY, InvertedComparableComparator.INSTANCE);
	}

	/**
     * Creates a {@code InvertedPriorityQueue} with the specified initial capacity
     * that orders its elements according to the inverse of the specified comparator.
     *
     * @param  initialCapacity the initial capacity for this priority queue
     * @param  comparator the comparator that will be used to order this
     *         priority queue.  If {@code null}, the {@linkplain Comparable
     *         natural ordering} of the elements will be used. Internally, the comparator
     *         is inverted to reverse its meaning.
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *         less than 1
     */
	@SuppressWarnings("unchecked")
	public InvertedPriorityQueue(int initialCapacity, Comparator<? super T> comparator) {
		super(initialCapacity, comparator == null ? (Comparator<T>)InvertedComparableComparator.INSTANCE : new InvertedComparator<T>(comparator));
	}

	/**
     * Creates a {@code InvertedPriorityQueue} with the specified initial
     * capacity that orders its elements according to their inverse
     * {@linkplain Comparable natural ordering}.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *         than 1
     */
	public InvertedPriorityQueue(int initialCapacity) {
		super(initialCapacity, InvertedComparableComparator.INSTANCE);
	}
	
	protected Comparator<? super T> originalComparator() {
		if (comparator() instanceof InvertedComparator)
			return ((InvertedComparator<? super T>)comparator()).innerComparator;
		else
			return ComparableComparator.INSTANCE;
	}
	
	/**
	 * Inverted natural order comparator.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	private static class InvertedComparableComparator implements Comparator<Object> {
		public static final InvertedComparableComparator INSTANCE = new InvertedComparableComparator();
		
		@Override
		@SuppressWarnings("unchecked")
	    public int compare(Object o1, Object o2) {
	        return ((Comparable<Object>)o2).compareTo(o1);
	    }
	}
	
	/**
	 * Natural order comparator.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	private static class ComparableComparator implements Comparator<Object> {
		public static final ComparableComparator INSTANCE = new ComparableComparator();
		
		@Override
		@SuppressWarnings("unchecked")
	    public int compare(Object o1, Object o2) {
	        return ((Comparable<Object>)o1).compareTo(o2);
	    }
	}
	
	/**
	 * Inverted natural order comparator.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	private static class InvertedComparator<T> implements Comparator<T> {
		protected Comparator<? super T> innerComparator;
		
		public InvertedComparator(Comparator<? super T> innerComparator) {
			this.innerComparator = innerComparator;
		}
		
		@Override
		public int compare(T o1, T o2) {
	        return innerComparator.compare(o2, o1);
	    }
	}
}
