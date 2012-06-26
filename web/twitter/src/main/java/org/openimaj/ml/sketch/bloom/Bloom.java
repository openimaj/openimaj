package org.openimaj.ml.sketch.bloom;

import gnu.trove.map.hash.TIntIntHashMap;

import org.openimaj.math.hash.HashFunctionFactory;
import org.openimaj.math.hash.StringMurmurHashFunction;
import org.openimaj.ml.sketch.Sketch;

/**
 * The bloom sketch as described by http://lkozma.net/blog/sketching-data-structures/
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Bloom implements Sketch<String,Boolean>{
	
	private StringMurmurHashFunction[] maps;
	private TIntIntHashMap table;
	private int nwords;
	/**
	 * @param ntables the number of hash functions
	 * @param nwords the range of the hash functions
	 */
	public Bloom(int ntables, int nwords){
		maps = new StringMurmurHashFunction[ntables];
		HashFunctionFactory<String, StringMurmurHashFunction> fact = HashFunctionFactory.get(StringMurmurHashFunction.class);
		for (int i = 0; i < maps.length; i++) {
			maps[i] = fact.create();
		}
		this.nwords = nwords;
		this.table = new TIntIntHashMap(nwords);
	}
	
	@Override
	public void update(String data, Boolean value) {
		for (StringMurmurHashFunction map : maps) {			
			int hash = map.computeHashCode(data);
			int loc = Math.abs(hash) % this.nwords;
			this.table.put(loc, 1);
		}
	}

	@Override
	public Boolean query(String data) {
		boolean found = false;
		for (StringMurmurHashFunction map : maps) {			
			int hash = map.computeHashCode(data);
			int loc = Math.abs(hash) % this.nwords;
			found = this.table.get(loc) == 1;
			if(found)return found;
		}
		return found;
	}

}
