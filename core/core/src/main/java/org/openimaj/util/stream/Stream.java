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
package org.openimaj.util.stream;

import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.parallel.Parallel;

/**
 * Interface describing a stream of data items. Streams are sequences of items
 * supporting both sequential and parallel bulk operations. Streams support lazy
 * transformative operations (transforming a stream to another stream) such as
 * {@link #filter(Predicate)} and {@link #map(Function)}, and consuming
 * operations, such as {@link #forEach(Operation)} and {@link #next()}.
 * <p>
 * Streams may be either bounded or infinite in length. Once an item has been
 * extracted from a stream, it is said to be consumed and is no longer available
 * for operations on the stream.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            The type of data item in the stream
 */
public interface Stream<T> extends Iterator<T>, Iterable<T> {

	/**
	 * Apply the given {@link Operation} to each item in the stream. Items are
	 * presented to the {@link Operation} in the order they appear in the
	 * stream.
	 * <p>
	 * Note: for an unbounded stream, this method will never return unless some
	 * form of exception is raised.
	 *
	 * @param op
	 *            the {@link Operation} to apply
	 */
	public void forEach(Operation<T> op);

	/**
	 * Apply the given {@link Operation} to each item in the stream. Items are
	 * presented to the {@link Operation} in the order they appear in the
	 * stream. The given {@link Predicate} can be used to stop processing of the
	 * stream once some condition is met.
	 * <p>
	 * Note: for an unbounded stream, this method will never return unless some
	 * form of exception is raised or the condition of the
	 * <tt>stopPredicate</tt> is met.
	 *
	 * @param operation
	 *            the {@link Operation} to apply
	 * @param stopPredicate
	 *            a predicate representing a condition that once met causes
	 *            processing to stop
	 */
	public void forEach(Operation<T> operation, Predicate<T> stopPredicate);

	/**
	 * Apply the given {@link Operation} to each item in the stream. Items are
	 * presented to the {@link Operation} in the order they appear in the
	 * stream. The given {@link Predicate} can be used to stop processing of the
	 * stream once some condition is met.
	 * <p>
	 * Note: for an unbounded stream, this method will never return unless some
	 * form of exception is raised or the condition of the
	 * <tt>stopPredicate</tt> is met.
	 *
	 * @param operation
	 *            the {@link Operation} to apply
	 * @param limit
	 *            the number of items to read from the stream
	 * @return the number of items read
	 */
	public int forEach(Operation<T> operation, int limit);

	/**
	 * Apply the given {@link Operation} to each item in the stream, making use
	 * of multiple threads. The order in which operations are performed on the
	 * stream is not guaranteed.
	 * <p>
	 * This method is intended to be a shortcut to calling
	 * {@link Parallel#forEachUnpartitioned(Iterator, Operation)}.
	 * <p>
	 * Note: for an unbounded stream, this method will never return unless some
	 * form of exception is raised.
	 *
	 * @param op
	 *            the {@link Operation} to apply
	 */
	public void parallelForEach(Operation<T> op);

	/**
	 * Apply the given {@link Operation} to each item in the stream, making use
	 * of multiple threads. The order in which operations are performed on the
	 * stream is not guaranteed.
	 * <p>
	 * This method is intended to be a shortcut to calling
	 * {@link Parallel#forEachUnpartitioned(Iterator, Operation, ThreadPoolExecutor)}.
	 * <p>
	 * Note: for an unbounded stream, this method will never return unless some
	 * form of exception is raised.
	 *
	 * @param op
	 *            the {@link Operation} to apply
	 * @param pool
	 *            the thread pool.
	 */
	public void parallelForEach(Operation<T> op, ThreadPoolExecutor pool);

	/**
	 * Transform the stream by creating a view that consists of only the items
	 * that match the given {@link Predicate}.
	 *
	 * @param filter
	 *            the predicate
	 * @return a new stream consisting of the matched items from this stream
	 */
	public Stream<T> filter(Predicate<T> filter);

	/**
	 * Transform the stream by creating a new stream that transforms the items
	 * in this stream with the given {@link Function}.
	 *
	 * @param mapper
	 *            the function to apply
	 * @return a new stream with transformed items from this stream
	 */
	public <R> Stream<R> map(Function<T, R> mapper);

	/**
	 * Transform the stream by creating a new stream that transforms the items
	 * in this stream with the given {@link Function}.
	 *
	 * @param mapper
	 *            the function to apply
	 * @return a new stream with transformed items from this stream
	 */
	public <R> Stream<R> map(MultiFunction<T, R> mapper);

	/**
	 * Transform the stream using the given function to transform the items in
	 * this stream.
	 *
	 * @param transform
	 *            the transform function
	 * @return a new stream with transformed items from this stream
	 */
	public <R> Stream<R> transform(Function<Stream<T>, Stream<R>> transform);
}
