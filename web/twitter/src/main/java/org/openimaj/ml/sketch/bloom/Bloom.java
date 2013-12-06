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
package org.openimaj.ml.sketch.bloom;

import gnu.trove.map.hash.TIntIntHashMap;

import org.openimaj.ml.sketch.SummarySketcher;
import org.openimaj.util.hash.StringMurmurHashFunction;
import org.openimaj.util.hash.StringMurmurHashFunctionFactory;

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
