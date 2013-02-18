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

import java.util.Iterator;

/**
 * Wrapper to allow an {@link Iterator} as an {@link Iterable} so it can be used
 * in an a foreach loop. The iterator is consumed by the loop and so must only
 * be used once. Normal usage is as follows:
 * 
 * <pre>
 * for (T t : IterableIterator.in(iterator)) {
 * 	// ...
 * }
 * </pre>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being iterated over.
 */
public class IterableIterator<T> implements Iterable<T> {
	Iterator<T> iterator;

	private IterableIterator(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}

	/**
	 * Wrapper to allow an {@link Iterator} as an {@link Iterable} so it can be
	 * used in a foreach loop. The iterator is consumed by the loop and so must
	 * only be used once. Normal usage is as follows:
	 * 
	 * <pre>
	 * for (T t : IterableIterator.in(iterator)) {
	 * 	// ...
	 * }
	 * </pre>
	 * 
	 * @param <T>
	 *            Type of object being iterated over.
	 * 
	 * @param iterator
	 *            an iterator
	 * @return an iterable wrapper which will traverse the iterator once
	 */
	public static <T> Iterable<T> in(Iterator<T> iterator) {
		return new IterableIterator<T>(iterator);
	}
}
