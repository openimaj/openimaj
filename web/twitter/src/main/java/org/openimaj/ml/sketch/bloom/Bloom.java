package org.openimaj.ml.sketch.bloom;

import gnu.trove.map.hash.TIntIntHashMap;

import org.openimaj.math.hash.StringMurmurHashFunction;
import org.openimaj.math.hash.StringMurmurHashFunctionFactory;
import org.openimaj.ml.sketch.SummarySketcher;

/**
 * The bloom sketch as described by
 * http://lkozma.net/blog/sketching-data-structures/
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class Bloom implements SummarySketcher<String, Boolean> {

	private StringMurmurHashFunction[] maps;
	private TIntIntHashMap table;
	private int nwords;

	/**
	 * @param ntables
	 *            the number of hash functions
	 * @param nwords
	 *            the range of the hash functions
	 */
	public Bloom(int ntables, int nwords) {
		maps = new StringMurmurHashFunction[ntables];

		final StringMurmurHashFunctionFactory fact = new StringMurmurHashFunctionFactory();
		for (int i = 0; i < maps.length; i++) {
			maps[i] = fact.create();
		}

		this.nwords = nwords;
		this.table = new TIntIntHashMap(nwords);
	}

	@Override
	public void update(String data, Boolean value) {
		for (final StringMurmurHashFunction map : maps) {
			final int hash = map.computeHashCode(data);
			final int loc = Math.abs(hash) % this.nwords;
			this.table.put(loc, 1);
		}
	}

	@Override
	public Boolean query(String data) {
		boolean found = false;
		for (final StringMurmurHashFunction map : maps) {
			final int hash = map.computeHashCode(data);
			final int loc = Math.abs(hash) % this.nwords;
			found = this.table.get(loc) == 1;
			if (found)
				return found;
		}
		return found;
	}

}
