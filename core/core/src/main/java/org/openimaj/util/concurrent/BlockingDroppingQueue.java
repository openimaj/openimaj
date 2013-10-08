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
package org.openimaj.util.concurrent;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Queue} that additionally supports operations that wait for the queue
 * to become non-empty when retrieving an element, and drop the oldest element
 * from the queue when storing a new element if space is not available for the
 * new element.
 * 
 * <p>
 * <tt>BlockingDroppingQueue</tt> methods come in four forms, with different
 * ways of handling operations that cannot be satisfied immediately, but may be
 * satisfied at some point in the future: one throws an exception, the second
 * returns a special value (either <tt>null</tt>, <tt>false</tt> or the dropped
 * object, depending on the operation), the third blocks the current thread
 * indefinitely until the operation can succeed, and the fourth blocks for only
 * a given maximum time limit before giving up. These methods are summarized in
 * the following table:
 * 
 * <p>
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 * <tr>
 * <td></td>
 * <td ALIGN=CENTER><em>Throws exception</em></td>
 * <td ALIGN=CENTER><em>Special value</em></td>
 * <td ALIGN=CENTER><em>Blocks</em></td>
 * <td ALIGN=CENTER><em>Times out</em></td>
 * </tr>
 * <tr>
 * <td><b>Insert</b></td>
 * <td>{@link #add add(e)}</td>
 * <td>{@link #offer offer(e)}, {@link #put put(e)}</td>
 * <td><em>not applicable</em></td>
 * <td><em>not applicable</em></td>
 * </tr>
 * <tr>
 * <td><b>Remove</b></td>
 * <td>{@link #remove remove()}</td>
 * <td>{@link #poll poll()}</td>
 * <td>{@link #take take()}</td>
 * <td>{@link #poll(long, TimeUnit) poll(time, unit)}</td>
 * </tr>
 * <tr>
 * <td><b>Examine</b></td>
 * <td>{@link #element element()}</td>
 * <td>{@link #peek peek()}</td>
 * <td><em>not applicable</em></td>
 * <td><em>not applicable</em></td>
 * </tr>
 * </table>
 * 
 * <p>
 * <tt>BlockingDroppingQueue</tt>s keep track of statistics on the number of
 * items that have been added to the queue and the number of items that have
 * been dropped from the queue.
 * 
 * <p>
 * A <tt>BlockingDroppingQueue</tt> does not accept <tt>null</tt> elements.
 * Implementations throw <tt>NullPointerException</tt> on attempts to
 * <tt>add</tt>, <tt>put</tt> or <tt>offer</tt> a <tt>null</tt>. A <tt>null</tt>
 * is used as a sentinel value to indicate failure of <tt>poll</tt> operations.
 * 
 * <p>
 * A <tt>BlockingDroppingQueue</tt> may be capacity bounded. At any given time
 * it may have a <tt>remainingCapacity</tt> beyond which no additional elements
 * can be <tt>put</tt> without blocking. A <tt>BlockingDroppingQueue</tt>
 * without any intrinsic capacity constraints always reports a remaining
 * capacity of <tt>Integer.MAX_VALUE</tt>.
 * 
 * <p>
 * <tt>BlockingDroppingQueue</tt> implementations are designed to be used
 * primarily for producer-consumer queues, but additionally support the
 * {@link java.util.Collection} interface. So, for example, it is possible to
 * remove an arbitrary element from a queue using <tt>remove(x)</tt>. However,
 * such operations are in general <em>not</em> performed very efficiently, and
 * are intended for only occasional use, such as when a queued message is
 * cancelled.
 * 
 * <p>
 * <tt>BlockingDroppingQueue</tt> implementations are thread-safe. All queuing
 * methods achieve their effects atomically using internal locks or other forms
 * of concurrency control. However, the <em>bulk</em> Collection operations
 * <tt>addAll</tt>, <tt>containsAll</tt>, <tt>retainAll</tt> and
 * <tt>removeAll</tt> are <em>not</em> necessarily performed atomically unless
 * specified otherwise in an implementation. So it is possible, for example, for
 * <tt>addAll(c)</tt> to fail (throwing an exception) after adding only some of
 * the elements in <tt>c</tt>.
 * 
 * <p>
 * A <tt>BlockingDroppingQueue</tt> does <em>not</em> intrinsically support any
 * kind of &quot;close&quot; or &quot;shutdown&quot; operation to indicate that
 * no more items will be added. The needs and usage of such features tend to be
 * implementation-dependent. For example, a common tactic is for producers to
 * insert special <em>end-of-stream</em> or <em>poison</em> objects, that are
 * interpreted accordingly when taken by consumers.
 * 
 * <p>
 * Memory consistency effects: As with other concurrent collections, actions in
 * a thread prior to placing an object into a {@code BlockingDroppingQueue} <a
 * href="package-summary.html#MemoryVisibility"><i>happen-before</i></a> actions
 * subsequent to the access or removal of that element from the
 * {@code BlockingDroppingQueue} in another thread.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <E>
 *            the type of elements held in this collection
 */
public interface BlockingDroppingQueue<E> extends Queue<E> {
	/**
	 * Inserts the specified element into this queue if it is possible to do so
	 * immediately without violating capacity restrictions, returning
	 * <tt>true</tt> upon success and throwing an <tt>IllegalStateException</tt>
	 * if no space is currently available. When using a capacity-restricted
	 * queue, it is generally preferable to use {@link #offer(Object) offer}.
	 * 
	 * @param e
	 *            the element to add
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 * @throws IllegalStateException
	 *             if the element cannot be added at this time due to capacity
	 *             restrictions
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this queue
	 * @throws NullPointerException
	 *             if the specified element is null
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this queue
	 */
	@Override
	boolean add(E e);

	/**
	 * Inserts the specified element into this queue if it is possible to do so
	 * immediately without violating capacity restrictions, returning
	 * <tt>true</tt> upon success and <tt>false</tt> if no space is currently
	 * available. When using a capacity-restricted queue, this method is
	 * generally preferable to {@link #add}, which can fail to insert an element
	 * only by throwing an exception.
	 * 
	 * @param e
	 *            the element to add
	 * @return <tt>true</tt> if the element was added to this queue, else
	 *         <tt>false</tt>
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this queue
	 * @throws NullPointerException
	 *             if the specified element is null
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this queue
	 */
	@Override
	boolean offer(E e);

	/**
	 * Inserts the specified element into this queue, dropping the oldest
	 * element to make space if necessary.
	 * 
	 * @param e
	 *            the element to add
	 * @return the element that was dropped to make space, or null if no element
	 *         was dropped
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 * @throws ClassCastException
	 *             if the class of the specified element prevents it from being
	 *             added to this queue
	 * @throws NullPointerException
	 *             if the specified element is null
	 * @throws IllegalArgumentException
	 *             if some property of the specified element prevents it from
	 *             being added to this queue
	 */
	E put(E e) throws InterruptedException;

	/**
	 * Retrieves and removes the head of this queue, waiting if necessary until
	 * an element becomes available.
	 * 
	 * @return the head of this queue
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	E take() throws InterruptedException;

	/**
	 * Retrieves and removes the head of this queue, waiting up to the specified
	 * wait time if necessary for an element to become available.
	 * 
	 * @param timeout
	 *            how long to wait before giving up, in units of <tt>unit</tt>
	 * @param unit
	 *            a <tt>TimeUnit</tt> determining how to interpret the
	 *            <tt>timeout</tt> parameter
	 * @return the head of this queue, or <tt>null</tt> if the specified waiting
	 *         time elapses before an element is available
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	E poll(long timeout, TimeUnit unit)
			throws InterruptedException;

	/**
	 * Returns the number of additional elements that this queue can ideally (in
	 * the absence of memory or resource constraints) accept without blocking,
	 * or <tt>Integer.MAX_VALUE</tt> if there is no intrinsic limit.
	 * 
	 * <p>
	 * Note that you <em>cannot</em> always tell if an attempt to insert an
	 * element will succeed by inspecting <tt>remainingCapacity</tt> because it
	 * may be the case that another thread is about to insert or remove an
	 * element.
	 * 
	 * @return the remaining capacity
	 */
	int remainingCapacity();

	/**
	 * Removes a single instance of the specified element from this queue, if it
	 * is present. More formally, removes an element <tt>e</tt> such that
	 * <tt>o.equals(e)</tt>, if this queue contains one or more such elements.
	 * Returns <tt>true</tt> if this queue contained the specified element (or
	 * equivalently, if this queue changed as a result of the call).
	 * 
	 * @param o
	 *            element to be removed from this queue, if present
	 * @return <tt>true</tt> if this queue changed as a result of the call
	 * @throws ClassCastException
	 *             if the class of the specified element is incompatible with
	 *             this queue (optional)
	 * @throws NullPointerException
	 *             if the specified element is null (optional)
	 */
	@Override
	boolean remove(Object o);

	/**
	 * Returns <tt>true</tt> if this queue contains the specified element. More
	 * formally, returns <tt>true</tt> if and only if this queue contains at
	 * least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
	 * 
	 * @param o
	 *            object to be checked for containment in this queue
	 * @return <tt>true</tt> if this queue contains the specified element
	 * @throws ClassCastException
	 *             if the class of the specified element is incompatible with
	 *             this queue (optional)
	 * @throws NullPointerException
	 *             if the specified element is null (optional)
	 */
	@Override
	public boolean contains(Object o);

	/**
	 * Removes all available elements from this queue and adds them to the given
	 * collection. This operation may be more efficient than repeatedly polling
	 * this queue. A failure encountered while attempting to add elements to
	 * collection <tt>c</tt> may result in elements being in neither, either or
	 * both collections when the associated exception is thrown. Attempts to
	 * drain a queue to itself result in <tt>IllegalArgumentException</tt>.
	 * Further, the behavior of this operation is undefined if the specified
	 * collection is modified while the operation is in progress.
	 * 
	 * @param c
	 *            the collection to transfer elements into
	 * @return the number of elements transferred
	 * @throws UnsupportedOperationException
	 *             if addition of elements is not supported by the specified
	 *             collection
	 * @throws ClassCastException
	 *             if the class of an element of this queue prevents it from
	 *             being added to the specified collection
	 * @throws NullPointerException
	 *             if the specified collection is null
	 * @throws IllegalArgumentException
	 *             if the specified collection is this queue, or some property
	 *             of an element of this queue prevents it from being added to
	 *             the specified collection
	 */
	int drainTo(Collection<? super E> c);

	/**
	 * Removes at most the given number of available elements from this queue
	 * and adds them to the given collection. A failure encountered while
	 * attempting to add elements to collection <tt>c</tt> may result in
	 * elements being in neither, either or both collections when the associated
	 * exception is thrown. Attempts to drain a queue to itself result in
	 * <tt>IllegalArgumentException</tt>. Further, the behavior of this
	 * operation is undefined if the specified collection is modified while the
	 * operation is in progress.
	 * 
	 * @param c
	 *            the collection to transfer elements into
	 * @param maxElements
	 *            the maximum number of elements to transfer
	 * @return the number of elements transferred
	 * @throws UnsupportedOperationException
	 *             if addition of elements is not supported by the specified
	 *             collection
	 * @throws ClassCastException
	 *             if the class of an element of this queue prevents it from
	 *             being added to the specified collection
	 * @throws NullPointerException
	 *             if the specified collection is null
	 * @throws IllegalArgumentException
	 *             if the specified collection is this queue, or some property
	 *             of an element of this queue prevents it from being added to
	 *             the specified collection
	 */
	int drainTo(Collection<? super E> c, int maxElements);

	/**
	 * Returns the total number of items that have been inserted into this
	 * {@link BlockingDroppingQueue} within its lifetime.
	 * 
	 * @return the total number of items that have been inserted to the queue
	 */
	long insertCount();

	/**
	 * Returns the total number of items that have been dropped by this
	 * {@link BlockingDroppingQueue} to make space for new elements within its
	 * lifetime.
	 * 
	 * @return the total number of items that have been dropped from the queue
	 */
	long dropCount();
}
