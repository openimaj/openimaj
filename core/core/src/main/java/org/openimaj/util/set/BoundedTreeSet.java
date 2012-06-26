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
package org.openimaj.util.set;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Extension of a {@link TreeSet} that has a bounded upper size.
 * Once this size is reached new objects will only be added if they 
 * are less than the last element in the set (according to the @{Comparable} 
 * interface or the given {@link Comparator}). If the new object
 * is eligable to be added, the last item is automatically removed.
 * 
 * Note: do not use this class as a form of priority queue if you 
 * expect items with equal priorities. Because this class is a form
 * of set, items with equal priorities would be considered equal,
 * an only one of them would be admitted to the set.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T> The type of object
 */
public class BoundedTreeSet<T> extends TreeSet<T> {
	private static final long serialVersionUID = 1L;

	protected int maxSize;

	/**
     * Constructs a new, empty tree set, sorted according to the
     * natural ordering of its elements.  All elements inserted into
     * the set must implement the {@link Comparable} interface.
     * Furthermore, all such elements must be <i>mutually
     * comparable</i>: {@code e1.compareTo(e2)} must not throw a
     * {@code ClassCastException} for any elements {@code e1} and
     * {@code e2} in the set.  If the user attempts to add an element
     * to the set that violates this constraint (for example, the user
     * attempts to add a string element to a set whose elements are
     * integers), the {@code add} call will throw a
     * {@code ClassCastException}.
	 * 
	 * The set additionally has a maximum size. Once this size is reached
	 * new objects will only be added if they are less than the last element
	 * in the set (according to the @{Comparable} interface). If the new object
	 * is eligable to be added, the last item is automatically removed. 
	 * 
	 * @param maxSize The maximum allowed number of elements. 
     */
	public BoundedTreeSet(int maxSize) {
		super();
		this.maxSize = maxSize;
	}

	/**
     * Constructs a new, empty tree set, sorted according to the specified
     * comparator.  All elements inserted into the set must be <i>mutually
     * comparable</i> by the specified comparator: {@code comparator.compare(e1,
     * e2)} must not throw a {@code ClassCastException} for any elements
     * {@code e1} and {@code e2} in the set.  If the user attempts to add
     * an element to the set that violates this constraint, the
     * {@code add} call will throw a {@code ClassCastException}.
	 *
	 * The set additionally has a maximum size. Once this size is reached
	 * new objects will only be added if they are less than the last element
	 * in the set (according to the @{Comparable} interface). If the new object
	 * is eligable to be added, the last item is automatically removed. 
	 * 
	 * @param maxSize The maximum allowed number of elements. 
     * @param comparator the comparator that will be used to order this set.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the elements will be used.
     */
	public BoundedTreeSet(int maxSize, Comparator<? super T> comparator) {
		super(comparator);
		this.maxSize = maxSize;
	}

	@Override
	public boolean add(T e) {
		if (contains(e)) return false;
		
		if (this.size() < maxSize) {
			return super.add(e);
		}
		
		T last = this.last();
		
		if (this.comparator() == null) {
			@SuppressWarnings("unchecked")
			Comparable<? super T> key = (Comparable<? super T>) last;
			
			if (key.compareTo(e) > 0) {
				this.remove(last);
				return super.add(e);
			}
		} else {
			if (this.comparator().compare(last, e) > 0) {
				this.remove(last);
				return super.add(e);
			}
		}
		
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		
		for (T e : c) {
			if (add(e))
				changed = true;
		}
		
		return changed;
	}
}
