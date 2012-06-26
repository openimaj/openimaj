package org.openimaj.ml.sketch.countmin;

import gnu.trove.map.hash.TIntIntHashMap;

import org.openimaj.math.hash.HashFunctionFactory;
import org.openimaj.math.hash.StringMurmurHashFunction;
import org.openimaj.ml.sketch.Sketch;
import org.openimaj.util.pair.IndependentPair;

/**
 * CountMin as described in the reference below
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountMin implements Sketch<String,Integer>{
	
	private static class FunctionHashPair extends IndependentPair<StringMurmurHashFunction,TIntIntHashMap>{

		public FunctionHashPair(StringMurmurHashFunction func, TIntIntHashMap map) {
			super(func, map);
			
		}
	}

	private FunctionHashPair[] maps;
	private int nwords;
	/**
	 * @param ntables the number of hash functions
	 * @param nwords the range of the hash functions
	 */
	public CountMin(int ntables, int nwords){
		maps = new FunctionHashPair[ntables];
		HashFunctionFactory<String, StringMurmurHashFunction> fact = HashFunctionFactory.get(StringMurmurHashFunction.class);
		this.nwords = nwords;
		for (int i = 0; i < maps.length; i++) {
			maps[i] = new FunctionHashPair(fact.create(),new TIntIntHashMap());
		}
	}
	
	@Override
	public void update(String data, Integer value) {
		for (FunctionHashPair map : this.maps) {
			int hash = map.firstObject().computeHashCode(data);
			int loc = Math.abs(hash) % this.nwords;
			map.secondObject().adjustOrPutValue(loc, value, value);
		}
	}

	@Override
	public Integer query(String data) {
		int min = -1;
		for (FunctionHashPair map : this.maps) {
			int hash = map.firstObject().computeHashCode(data);
			int loc = Math.abs(hash) % this.nwords;
			int v = map.secondObject().get(loc);
			if(min == -1 || min > v){
				min = v;
			}
		}
		return min;
	}

}
