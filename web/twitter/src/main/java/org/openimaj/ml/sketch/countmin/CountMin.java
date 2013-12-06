/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.ml.sketch.countmin;

import gnu.trove.map.hash.TIntIntHashMap;

import org.openimaj.ml.sketch.SummarySketcher;
import org.openimaj.util.hash.StringMurmurHashFunction;
import org.openimaj.util.hash.StringMurmurHashFunctionFactory;
import org.openimaj.util.pair.IndependentPair;

/**
 * CountMin as described in the reference below
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class CountMin implements SummarySketcher<String, Integer> {

	private static class FunctionHashPair extends IndependentPair<StringMurmurHashFunction, TIntIntHashMap> {

		public FunctionHashPair(StringMurmurHashFunction func, TIntIntHashMap map) {
			super(func, map);

		}
	}

	private FunctionHashPair[] maps;
	private int nwords;

	/**
	 * @param ntables
	 *            the number of hash functions
	 * @param nwords
	 *            the range of the hash functions
	 */
	public CountMin(int ntables, int nwords) {
		maps = new FunctionHashPair[ntables];
		this.nwords = nwords;

		final StringMurmurHashFunctionFactory fact = new StringMurmurHashFunctionFactory();
		for (int i = 0; i < maps.length; i++) {
			maps[i] = new FunctionHashPair(fact.create(), new TIntIntHashMap());
		}
	}

	@Override
	public void update(String data, Integer value) {
		for (final FunctionHashPair map : this.maps) {
			final int hash = map.firstObject().computeHashCode(data);
			final int loc = Math.abs(hash) % this.nwords;

			map.secondObject().adjustOrPutValue(loc, value, value);
		}
	}

	@Override
	public Integer query(String data) {
		int min = -1;
		for (final FunctionHashPair map : this.maps) {
			final int hash = map.firstObject().computeHashCode(data);
			final int loc = Math.abs(hash) % this.nwords;
			final int v = map.secondObject().get(loc);
			if (min == -1 || min > v) {
				min = v;
			}
		}
		return min;
	}

}
