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
package org.openimaj.workinprogress.optimisation.params;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.procedure.TObjectDoubleProcedure;

import java.util.Iterator;

import org.openimaj.workinprogress.optimisation.params.KeyedParameters.ObjectDoubleEntry;

public final class KeyedParameters<KEY> implements Parameters<KeyedParameters<KEY>>, Iterable<ObjectDoubleEntry<KEY>> {
	public static class ObjectDoubleEntry<KEY> {
		public KEY key;
		public double value;
	}

	private TObjectDoubleHashMap<KEY> paramsMap = new TObjectDoubleHashMap<KEY>();

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openimaj.workinprogress.params.Parameters#multiplyInplace(org.openimaj
	 * .workinprogress.params.KeyedParameters)
	 */
	@Override
	public void multiplyInplace(final KeyedParameters<KEY> other) {
		paramsMap.forEachEntry(new TObjectDoubleProcedure<KEY>() {
			@Override
			public boolean execute(KEY key, double value) {
				if (other.contains(key)) {
					paramsMap.put(key, other.get(key) * value);
				}
				return true;
			}
		});
	}

	public void multiplyInplace(KEY key, double value) {
		if (paramsMap.contains(key)) {
			paramsMap.put(key, paramsMap.get(key) * value);
		}
	}

	@Override
	public void addInplace(final KeyedParameters<KEY> other) {
		other.paramsMap.forEachEntry(new TObjectDoubleProcedure<KEY>() {
			@Override
			public boolean execute(KEY key, double value) {
				paramsMap.adjustOrPutValue(key, value, value);
				return true;
			}
		});
	}

	public void addInplace(KEY key, double value) {
		paramsMap.adjustOrPutValue(key, value, value);
	}

	public void set(KEY key, double value) {
		paramsMap.put(key, value);
	}

	public double get(KEY key) {
		return paramsMap.get(key);
	}

	public boolean contains(KEY key) {
		return paramsMap.containsKey(key);
	}

	@Override
	public void multiplyInplace(final double value) {
		paramsMap.forEachEntry(new TObjectDoubleProcedure<KEY>() {
			@Override
			public boolean execute(KEY key, double v) {
				paramsMap.put(key, value * v);
				return true;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.openimaj.workinprogress.params.Parameters#addInplace(KEY)
	 */
	@Override
	public void addInplace(final double value) {
		paramsMap.forEachEntry(new TObjectDoubleProcedure<KEY>() {
			@Override
			public boolean execute(KEY key, double v) {
				paramsMap.put(key, value * v);
				return true;
			}
		});
	}

	@Override
	public Iterator<ObjectDoubleEntry<KEY>> iterator() {
		return new Iterator<ObjectDoubleEntry<KEY>>() {
			TObjectDoubleIterator<KEY> iter = paramsMap.iterator();
			ObjectDoubleEntry<KEY> entry = new ObjectDoubleEntry<KEY>();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ObjectDoubleEntry<KEY> next() {
				iter.advance();
				entry.key = iter.key();
				entry.value = iter.value();
				return entry;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}
}
