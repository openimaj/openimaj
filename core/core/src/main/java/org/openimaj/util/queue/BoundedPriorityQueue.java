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
package org.openimaj.util.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * A bounded priority queue based on an {@link InvertedPriorityQueue}.
 * Insertions to the queue are worst-case O(log(N)) and O(1) if the insertion is
 * rejected. {@link #peek()} and {@link #poll()} are very inefficient (O(N)) as
 * they have to search for the head of the queue. {@link #peekTail()} and
 * {@link #pollTail()} have complexity O(1) and O(log(N)) respectively.
 * <p>
 * This implementation is ideally suited for storing the top-N items of a
 * process; that is, where items are constantly added, but not removed very
 * often.
 * <p>
 * The class contains a number of utility methods to get a sorted copy of the
 * queue.
 * <p>
 * The Iterator provided in method {@link #iterator()} is <em>not</em>
 * guaranteed to traverse the elements of the priority queue in any particular
 * order.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 */
public class BoundedPriorityQueue<T> extends InvertedPriorityQueue<T> {
	private static final long serialVersionUID = 1L;
	private int maxSize;

	/**
	 * Creates a {@code BoundedPriorityQueue} with the specified initial
	 * capacity that orders its elements according to the inverse of the
	 * specified comparator.
	 *
	 * @param maxSize
	 *            the maximum number of elements in this priority queue
	 * @param comparator
	 *            the comparator that will be used to order this priority queue.
	 *            If {@code null}, the {@linkplain Comparable natural ordering}
	 *            of the elements will be used. Internally, the comparator is
	 *            inverted to reverse its meaning.
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
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
	 * @param maxSize
	 *            the maximum number of elements in this priority queue
	 * @throws IllegalArgumentException
	 *             if {@code initialCapacity} is less than 1
	 */
	public BoundedPriorityQueue(int maxSize) {
		super(maxSize);
		this.maxSize = maxSize;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.PriorityQueue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(T e) {
		if (this.size() < maxSize)
			return super.offer(e);
		else {
			final T object = super.peek();

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
			final Comparable<? super T> key = (Comparable<? super T>) o1;
			return key.compareTo(o2);
		}
	}

	/**
	 * Peak at the head of the queue. To maintain the semantics of a
	 * {@link PriorityQueue}, this is the <it>least</it> element with respect to
	 * the specified ordering.
	 * <p>
	 * Peeking at the head is an expensive (O(n)) operation as the
	 * {@link BoundedPriorityQueue} only maintains the tail for fast insertions.
	 *
	 * @see java.util.PriorityQueue#peek()
	 */
	@Override
	public T peek() {
		T best = super.peek();

		for (final T obj : this) {
			if (compare(best, obj) < 0) {
				best = obj;
			}
		}

		return best;
	}

	/**
	 * Poll the head of the queue. To maintain the semantics of a
	 * {@link PriorityQueue}, this is the <it>least</it> element with respect to
	 * the specified ordering.
	 * <p>
	 * Polling the head is an expensive (O(n)) operation as the
	 * {@link BoundedPriorityQueue} only maintains the tail for fast insertions.
	 *
	 * @see java.util.PriorityQueue#poll()
	 */
	@Override
	public T poll() {
		final T best = peek();
		remove(best);
		return best;
	}

	/**
	 * Create a new list with the contents of the queue and sort them into their
	 * natural order, or the order specified by the {@link Comparator} used in
	 * constructing the queue. The list constructed in O(N) time, and the
	 * sorting takes O(log(N)) time.
	 *
	 * @return a sorted list containing contents of the queue.
	 */
	public List<T> toOrderedList() {
		final int size = size();

		final List<T> list = new ArrayList<T>(size);

		for (final T obj : this) {
			list.add(obj);
		}

		Collections.sort(list, originalComparator());

		return list;
	}

	/**
	 * Returns an array containing all of the elements in this queue. The
	 * elements are in sorted into their natural order, or the order specified
	 * by the {@link Comparator} used in constructing the queue. The array is
	 * constructed in O(N) time, and the sorting takes O(log(N)) time.
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this queue. (In other words, this method must allocate a
	 * new array). The caller is thus free to modify the returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return a sorted array containing all of the elements in this queue
	 */
	@SuppressWarnings("unchecked")
	public Object[] toOrderedArray() {
		final Object[] array = this.toArray();

		Arrays.sort(array, (Comparator<Object>) originalComparator());

		return array;
	}

	/**
	 * Returns a sorted array containing all of the elements in this queue; the
	 * runtime type of the returned array is that of the specified array.
	 * <p>
	 * The elements are in sorted into their natural order, or the order
	 * specified by the {@link Comparator} used in constructing the queue. The
	 * array is constructed in O(N) time, and the sorting takes O(log(N)) time.
	 * <p>
	 * If the queue fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this queue.
	 * <p>
	 * If the queue fits in the specified array with room to spare (i.e., the
	 * array has more elements than the queue), the element in the array
	 * immediately following the end of the collection is set to {@code null}.
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows
	 * precise control over the runtime type of the output array, and may, under
	 * certain circumstances, be used to save allocation costs.
	 * <p>
	 * Suppose <tt>x</tt> is a queue known to contain only strings. The
	 * following code can be used to dump the queue into a newly allocated array
	 * of <tt>String</tt>:
	 *
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 * <p>
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 *
	 * @param a
	 *            the array into which the elements of the queue are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing all of the elements in this queue
	 * @throws ArrayStoreException
	 *             if the runtime type of the specified array is not a supertype
	 *             of the runtime type of every element in this queue
	 * @throws NullPointerException
	 *             if the specified array is null
	 */
	public T[] toOrderedArray(T[] a) {
		final T[] array = this.toArray(a);

		Arrays.sort(array, originalComparator());

		return array;
	}

	/**
	 * Create a new list with the contents of the queue with the elements
	 * inserted in their natural order, or the order specified by the
	 * {@link Comparator} used in constructing the queue.
	 * <p>
	 * This method destroys the queue; after the operation completes, the queue
	 * will be empty. The operation completes in O(Nlog(N)) time.
	 *
	 * @return a sorted list containing contents of the queue.
	 */
	@SuppressWarnings("unchecked")
	public List<T> toOrderedListDestructive() {
		final int size = size();

		final Object[] list = new Object[size];

		for (int i = size - 1; i >= 0; i--) {
			list[i] = super.poll();
		}

		return (List<T>) Arrays.asList(list);
	}

	/**
	 * Returns an array containing all of the elements in this queue. The
	 * elements are in sorted into their natural order, or the order specified
	 * by the {@link Comparator} used in constructing the queue.
	 * <p>
	 * This method destroys the queue; after the operation completes, the queue
	 * will be empty. The operation completes in O(Nlog(N)) time.
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this queue. (In other words, this method must allocate a
	 * new array). The caller is thus free to modify the returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return a sorted array containing all of the elements in this queue
	 */
	public Object[] toOrderedArrayDestructive() {
		final int size = size();

		final Object[] array = new Object[size];

		for (int i = size - 1; i >= 0; i--) {
			array[i] = super.poll();
		}

		return array;
	}

	/**
	 * Returns a sorted array containing all of the elements in this queue; the
	 * runtime type of the returned array is that of the specified array.
	 * <p>
	 * The elements are in sorted into their natural order, or the order
	 * specified by the {@link Comparator} used in constructing the queue.
	 * <p>
	 * This method destroys the queue; after the operation completes, the queue
	 * will be empty. The operation completes in O(Nlog(N)) time.
	 * <p>
	 * If the queue fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this queue.
	 * <p>
	 * If the queue fits in the specified array with room to spare (i.e., the
	 * array has more elements than the queue), the element in the array
	 * immediately following the end of the collection is set to {@code null}.
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows
	 * precise control over the runtime type of the output array, and may, under
	 * certain circumstances, be used to save allocation costs.
	 * <p>
	 * Suppose <tt>x</tt> is a queue known to contain only strings. The
	 * following code can be used to dump the queue into a newly allocated array
	 * of <tt>String</tt>:
	 *
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 * <p>
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 *
	 * @param a
	 *            the array into which the elements of the queue are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing all of the elements in this queue
	 * @throws ArrayStoreException
	 *             if the runtime type of the specified array is not a supertype
	 *             of the runtime type of every element in this queue
	 * @throws NullPointerException
	 *             if the specified array is null
	 */
	@SuppressWarnings("unchecked")
	public T[] toOrderedArrayDestructive(T[] a) {
		final int size = size();

		if (a.length < size)
			a = (T[]) Arrays.copyOf(a, size, a.getClass());

		if (a.length > size)
			a[size] = null;

		for (int i = size - 1; i >= 0; i--) {
			a[i] = super.poll();
		}

		return a;
	}

	/**
	 * Retrieves, but does not remove, the tail of this queue, or returns
	 * <tt>null</tt> if this queue is empty.
	 * <p>
	 * This operation is performed in O(1) time.
	 *
	 * @return the tail of this queue, or <tt>null</tt> if this queue is empty.
	 */
	public T peekTail() {
		return super.peek();
	}

	/**
	 * Retrieves and remove the tail of this queue, or returns <tt>null</tt> if
	 * this queue is empty.
	 * <p>
	 * This operation is performed in O(1) time.
	 *
	 * @return the tail of this queue, or <tt>null</tt> if this queue is empty.
	 */
	public T pollTail() {
		return super.poll();
	}

	/**
	 * Inserts the specified element into this priority queue if possible. This
	 * method is the same as calling {@link #add(Object)} or
	 * {@link #offer(Object)}, but returns a different value depending on the
	 * queue size and whether the item was successfully added.
	 * <p>
	 * Specifically, if the item was not added, then it is returned. If the item
	 * was added to a queue that has not reached its capacity, then
	 * <code>null</code> is returned. If the item was added to a queue at
	 * capacity, then the item that was removed to make space is returned.
	 *
	 * @param item
	 *            The item to attempt to add.
	 *
	 * @return The item if it couldn't be added to the queue; null if the item
	 *         was added without needing to remove anything; or the removed item
	 *         if space had to be made to add the item.
	 * @throws ClassCastException
	 *             if the specified element cannot be compared with elements
	 *             currently in this priority queue according to the priority
	 *             queue's ordering
	 * @throws NullPointerException
	 *             if the specified element is null
	 */
	public T offerItem(T item) {
		T tail = null;

		// tail will have to be removed if item is to be added
		if (this.size() >= maxSize)
			tail = super.peek();

		if (this.offer(item)) {
			return tail;
		}

		// item was not added
		return item;
	}

	/**
	 * Is the {@link BoundedPriorityQueue} full?
	 *
	 * @return true if full; false otherwise
	 */
	public boolean isFull() {
		return this.maxSize == this.size();
	}
}
