package org.openimaj.feature;


import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.List;

import org.openimaj.util.array.SparseHashedFloatArray;

/**
 * An extractor which gives {@link SparseFloatFV} instances for a list of words.
 * This is a simple unweighted count
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BagOfWordsFeatureExtractor implements FeatureExtractor<SparseFloatFV, List<String>>{
	
	private List<String> dict;
	private TObjectIntHashMap<String> lookup;

	/**
	 * A set of words which are used for their index and therefore feature vector
	 * entry of a given word.
	 * @param dictionary
	 */
	public BagOfWordsFeatureExtractor(List<String> dictionary) {
		this.dict = dictionary;
		this.lookup = new TObjectIntHashMap<String>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		int index = 0;
		for (String string : dictionary) {
			lookup.put(string, index++);
		}
	}
	
	@Override
	public SparseFloatFV extractFeature(List<String> object) {
		SparseHashedFloatArray values = new SparseHashedFloatArray(this.dict.size());
		for (String string : object) {
			int index = asIndex(string);
			if(index<0)continue;
			values.increment(index,1f);
		}
		return new SparseFloatFV(values);
	}

	private int asIndex(String string) {
		int found = this.lookup.get(string);
		return found;
	}


}
