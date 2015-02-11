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
package org.openimaj.util.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An {@link Iterable} that chains together other {@link Iterable}s or
 * {@link Iterator}s.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <T>
 *            type of objects provided by underlying iterators
 */
public class ConcatenatedIterable<T> implements Iterable<T> {
	class ConcatenatedIterator implements Iterator<T> {
		Iterator<Iterator<T>> it;
		Iterator<T> current;

		public ConcatenatedIterator() {
			if (iterators == null) {
				return;
			}

			it = iterators.iterator();

			if (!it.hasNext()) {
				it = null;
				return;
			}

			current = it.next();
		}

		@Override
		public boolean hasNext() {
			if (it == null)
				return false;

			if (current.hasNext())
				return true;

			if (!it.hasNext())
				return false;

			current = it.next();
			return hasNext();
		}

		@Override
		public T next() {
			if (!current.hasNext()) {
				if (!it.hasNext())
					throw new NoSuchElementException();

				current = it.next();
			}

			return current.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Not supported");
		}
	};

	private List<Iterator<T>> iterators;

	/**
	 * Construct with {@link Iterable}s.
	 * 
	 * @param iterables
	 */
	@SafeVarargs
	public ConcatenatedIterable(Iterable<T>... iterables) {
		iterators = new ArrayList<Iterator<T>>();

		for (final Iterable<T> i : iterables) {
			iterators.add(i.iterator());
		}
	}

	/**
	 * Construct with {@link Iterable}s.
	 * 
	 * @param iterables
	 */
	public ConcatenatedIterable(Collection<? extends Iterable<T>> iterables) {
		iterators = new ArrayList<Iterator<T>>();

		for (final Iterable<T> i : iterables) {
			iterators.add(i.iterator());
		}
	}

	/**
	 * Construct with {@link Iterator}s.
	 * 
	 * @param iterables
	 */
	@SafeVarargs
	public ConcatenatedIterable(Iterator<T>... iterables) {
		iterators = Arrays.asList(iterables);
	}

	@Override
	public Iterator<T> iterator() {
		return new ConcatenatedIterator();
	}
}
