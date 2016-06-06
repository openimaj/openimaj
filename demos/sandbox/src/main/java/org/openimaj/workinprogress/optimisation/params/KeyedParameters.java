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
