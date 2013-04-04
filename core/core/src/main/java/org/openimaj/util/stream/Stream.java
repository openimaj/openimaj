package org.openimaj.util.stream;

import java.util.Iterator;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;

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
}
