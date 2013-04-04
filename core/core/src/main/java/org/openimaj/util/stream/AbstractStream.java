package org.openimaj.util.stream;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;

/**
 * Abstract base implementation of a read-only (i.e. {@link #remove()} not
 * supported) {@link Stream}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of data item in the stream
 */
public abstract class AbstractStream<T> implements Stream<T> {
	@Override
	public void forEach(Operation<T> op) {
		while (hasNext()) {
			op.perform(next());
		}
	}

	@Override
	public void forEach(Operation<T> operation, Predicate<T> stopPredicate) {
		while (hasNext()) {
			final T next = next();

			if (stopPredicate.test(next))
				break;

			operation.perform(next);
		}
	}

	class FilterStream extends AbstractStream<T> {
		Predicate<T> filter;
		T obj = null;

		FilterStream(Predicate<T> predicate) {
			this.filter = predicate;
		}

		@Override
		public boolean hasNext() {
			if (obj != null)
				return true;

			while (AbstractStream.this.hasNext() && !filter.test(obj = AbstractStream.this.next())) {
				obj = null;
			}

			return obj != null;
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException("iteration has no more elements");
			}

			return obj;
		}
	}

	@Override
	public Stream<T> filter(Predicate<T> filter) {
		return new FilterStream(filter);
	}

	@Override
	public <R> Stream<R> map(final Function<T, R> mapper) {
		return new AbstractStream<R>() {
			@Override
			public boolean hasNext() {
				return AbstractStream.this.hasNext();
			}

			@Override
			public R next() {
				return mapper.apply(AbstractStream.this.next());
			}
		};
	}

	/**
	 * Throws an UnsupportedOperationException()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove is not supported");
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
