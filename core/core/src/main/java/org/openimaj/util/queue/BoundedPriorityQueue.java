package org.openimaj.util.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A bounded priority queue based on an {@link InvertedPriorityQueue}. Insertions
 * to the queue are O(log(N)). However, {@link #peek()} and {@link #poll()} are
 * very inefficient (O(N)).
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 * @param <T>
 */
public class BoundedPriorityQueue<T> extends InvertedPriorityQueue<T> {
	private static final long serialVersionUID = 1L;
	private int maxSize; 

	/**
     * Creates a {@code BoundedPriorityQueue} with the specified initial capacity
     * that orders its elements according to the inverse of the specified comparator.
     *
     * @param  maxSize the maximum number of elements in this priority queue
     * @param  comparator the comparator that will be used to order this
     *         priority queue.  If {@code null}, the {@linkplain Comparable
     *         natural ordering} of the elements will be used. Internally, the comparator
     *         is inverted to reverse its meaning.
     * @throws IllegalArgumentException if {@code initialCapacity} is
     *         less than 1
     */
	public BoundedPriorityQueue(int maxSize, Comparator<? super T> comparator) {
		super(maxSize, comparator);
		this.maxSize = maxSize;
	}

	/**
     * Creates a {@code BoundedPriorityQueue} with the specified initial
     * capacity that orders its elements according to their inverse
     * {@linkplain Comparable natural ordering}.
     *
     * @param  maxSize the maximum number of elements in this priority queue
     * @throws IllegalArgumentException if {@code initialCapacity} is less
     *         than 1
     */
	public BoundedPriorityQueue(int maxSize) {
		super(maxSize);
		this.maxSize = maxSize;
	}

	/* (non-Javadoc)
	 * @see java.util.PriorityQueue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(T e) {
		if (this.size() < maxSize)
			return super.offer(e);
		else {
			T object = this.peek();

			if (compare(object, e) < 0) {
				super.poll();
				return super.offer(e);
			}
		}
		return false;
	}

	private int compare(T o1, T o2) {
		if (this.comparator() != null) {
			return this.comparator().compare(o1, o2);
		} else {
			@SuppressWarnings("unchecked")
			Comparable<? super T> key = (Comparable<? super T>) o1;
			return key.compareTo(o2);
		}		
	}

	/* (non-Javadoc)
	 * @see java.util.PriorityQueue#peek()
	 */
	@Override
	public T peek() {
		T best = super.peek();

		for (T obj : this) {
			if (compare(best, obj) > 0) {
				best = obj;
			}
		}

		return best;
	}

	/* (non-Javadoc)
	 * @see java.util.PriorityQueue#poll()
	 */
	@Override
	public T poll() {
		T best = peek();
		remove(best);
		return best;
	}
	
	/**
	 * Create a new list with the contents of the queue and sort them into
	 * their natural order, or the order specified by the {@link Comparator}
	 * used in constructing the queue. The list constructed in O(N) time, and
	 * the sorting takes O(log(N)) time.
	 * 
	 * @return a sorted list containing contents of the queue.
	 */
	public List<T> toOrderedList() {
		final int size = size();
		
		List<T> list = new ArrayList<T>(size);
		
		for (T obj : this) {
			list.add(obj);
		}
		
		Collections.sort(list, originalComparator());
		
		return list;
	}
	
	/**
     * Returns an array containing all of the elements in this queue.
     * The elements are in sorted into their natural order, or the 
     * order specified by the {@link Comparator} used in constructing the 
     * queue. The array is constructed in O(N) time, and the sorting 
     * takes O(log(N)) time.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return a sorted array containing all of the elements in this queue
     */
	@SuppressWarnings("unchecked")
	public Object[] toOrderedArray() {
		Object[] array = this.toArray();
		
		Arrays.sort(array, (Comparator<Object>)originalComparator());
		
		return array;
	}
	
	/**
     * Returns a sorted array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * 
     * The elements are in sorted into their natural order, or the 
     * order specified by the {@link Comparator} used in constructing the 
     * queue. The array is constructed in O(N) time, and the sorting 
     * takes O(log(N)) time.
     * 
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If the queue fits in the specified array with room to spare
     * (i.e., the array has more elements than the queue), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
	public Object[] toOrderedArray(T[] a) {
		T[] array = this.toArray(a);
		
		Arrays.sort(array, originalComparator());
		
		return array;
	}
	
	/**
	 * Create a new list with the contents of the queue with the elements inserted
	 * in their natural order, or the order specified by the {@link Comparator}
	 * used in constructing the queue.
	 * 
	 * This method destroys the queue; after the operation completes, the queue will
	 * be empty. The operation completes in O(Nlog(N)) time.
	 * 
	 * @return a sorted list containing contents of the queue.
	 */
	@SuppressWarnings("unchecked")
	public List<T> toOrderedListDestructive() {
		final int size = size();
		
		Object[] list = new Object[size];
		
		for (int i=size-1; i>=0; i--) {
			list[i] = super.poll();
		}
			
		return (List<T>) Arrays.asList(list);
	}
	
	/**
     * Returns an array containing all of the elements in this queue.
     * The elements are in sorted into their natural order, or the 
     * order specified by the {@link Comparator} used in constructing the 
     * queue. 
     * 
     * This method destroys the queue; after the operation completes, the queue will
	 * be empty. The operation completes in O(Nlog(N)) time.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return a sorted array containing all of the elements in this queue
     */
	public Object[] toOrderedArrayDestructive() {
		final int size = size();
		
		Object[] array = new Object[size];
		
		for (int i=size-1; i>=0; i--) {
			array[i] = super.poll();
		}
			
		return array;
	}
	
	/**
     * Returns a sorted array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * 
     * The elements are in sorted into their natural order, or the 
     * order specified by the {@link Comparator} used in constructing the 
     * queue. 
     * 
     * This method destroys the queue; after the operation completes, the queue will
	 * be empty. The operation completes in O(Nlog(N)) time.
     * 
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If the queue fits in the specified array with room to spare
     * (i.e., the array has more elements than the queue), the element in
     * the array immediately following the end of the collection is set to
     * {@code null}.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
	@SuppressWarnings("unchecked")
	public Object[] toOrderedArrayDestructive(T[] a) {
		final int size = size();
		
		if (a.length < size)
            a = (T[]) Arrays.copyOf(a, size, a.getClass());
		
        if (a.length > size)
            a[size] = null;
        
        for (int i=size-1; i>=0; i--) {
			a[i] = super.poll();
		}
			
		return a;
	}
}
