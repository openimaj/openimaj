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
package org.openimaj.feature;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.List;

import org.openimaj.util.array.SparseHashedFloatArray;

/**
 * An extractor which gives {@link SparseFloatFV} instances for a list of words.
 * This is a simple unweighted count.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class BagOfWordsFeatureExtractor implements FeatureExtractor<SparseFloatFV, List<String>> {
	private List<String> dict;
	private TObjectIntHashMap<String> lookup;

	/**
	 * A set of words which are used for their index and therefore feature
	 * vector entry of a given word.
	 * 
	 * @param dictionary
	 */
	public BagOfWordsFeatureExtractor(List<String> dictionary) {
		this.dict = dictionary;
		this.lookup = new TObjectIntHashMap<String>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		int index = 0;
		for (final String string : dictionary) {
			lookup.put(string, index++);
		}
	}

	@Override
	public SparseFloatFV extractFeature(List<String> object) {
		final SparseHashedFloatArray values = new SparseHashedFloatArray(this.dict.size());
		for (final String string : object) {
			final int index = asIndex(string);
			if (index < 0)
				continue;
			values.increment(index, 1f);
		}
		return new SparseFloatFV(values);
	}

	private int asIndex(String string) {
		final int found = this.lookup.get(string);
		return found;
	}
}
