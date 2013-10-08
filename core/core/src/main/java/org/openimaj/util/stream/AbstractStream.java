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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.parallel.Parallel;

/**
 * Abstract base implementation of a read-only (i.e. {@link #remove()} not
 * supported) {@link Stream}s.
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
			operation.perform(next);
			if (stopPredicate.test(next)) break;
		}
	}
	
	@Override
	public int forEach(Operation<T> operation, int limit) {
		int seen = 0;
		while (hasNext()) {
			final T next = next();
			operation.perform(next);
			seen ++;
			if(seen >= limit) break;
		}
		return seen;
	}

	@Override
	public void parallelForEach(Operation<T> op) {
		Parallel.forEachUnpartitioned(this, op);
	}

	@Override
	public void parallelForEach(Operation<T> op, ThreadPoolExecutor pool) {
		Parallel.forEachUnpartitioned(this, op, pool);
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

			final T toRet = obj;
			obj = null;

			return toRet;
		}
	}

	@Override
	public Stream<T> filter(Predicate<T> filter) {
		return new FilterStream(filter);
	}

	@Override
	public <R> Stream<R> transform(Function<Stream<T>, Stream<R>> transform) {
		return transform.apply(this);
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

	@Override
	public <R> Stream<R> map(final MultiFunction<T, R> mapper) {
		return new AbstractStream<R>() {
			List<R> current;
			int currentIndex;

			@Override
			public boolean hasNext() {
				if (current != null && currentIndex >= current.size()) {
					current = null;
					currentIndex = 0;
				}

				if (current == null) {
					if (AbstractStream.this.hasNext()) {
						for (final T obj : AbstractStream.this) {
							final List<R> list = mapper.apply(obj);

							if (list != null && list.size() > 0) {
								current = list;
								currentIndex = 0;
								return true;
							}
						}
					}
					return false;
				}

				return true;
			}

			@Override
			public R next() {
				if (!hasNext())
					throw new NoSuchElementException();

				final R ret = current.get(currentIndex);
				currentIndex++;

				return ret;
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
