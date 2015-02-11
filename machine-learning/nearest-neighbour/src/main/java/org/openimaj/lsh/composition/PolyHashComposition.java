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
package org.openimaj.lsh.composition;

import java.util.List;

import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.composition.HashComposition;

/**
 * {@link HashComposition} that uses a polynomial function to combine the
 * individual hashes. Based on the composition in the <a
 * href="https://code.google.com/p/caltech-image-search/">Caltech Large Scale
 * Image Search Toolbox</a>.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Object being hashed
 */
public class PolyHashComposition<OBJECT> extends HashComposition<OBJECT> {
	private static final int HASH_POLY = 1368547;
	private static final int HASH_POLY_REM = 573440;
	private static final int HASH_POLY_A[] =
	{ 1342, 876454, 656565, 223, 337, 9847, 87676, 34234, 23445, 76543, 8676234, 3497, 9876, 87856, 2342858 };

	/**
	 * Construct with the given functions.
	 *
	 * @param functions
	 *            the underlying hash functions.
	 */
	public PolyHashComposition(List<HashFunction<OBJECT>> functions) {
		super(functions);
	}

	/**
	 * Construct with the given functions.
	 *
	 * @param first
	 *            the first function
	 * @param remainder
	 *            the remainder of the functions
	 */
	@SafeVarargs
	public PolyHashComposition(HashFunction<OBJECT> first, HashFunction<OBJECT>... remainder) {
		super(first, remainder);
	}

	/**
	 * Construct with the factory which is used to produce the required number
	 * of functions.
	 *
	 * @param factory
	 *            the factory to use to produce the underlying hash functions.
	 * @param nFuncs
	 *            the number of functions to create for the composition
	 */
	public PolyHashComposition(HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
	}

	private final int addId(int id, int val, int pos) {
		return (val * HASH_POLY_A[pos % HASH_POLY_A.length] % HASH_POLY) + (id * HASH_POLY_REM % HASH_POLY);
	}

	@Override
	public int computeHashCode(OBJECT object) {
		if (hashFunctions == null || hashFunctions.size() == 0)
			return 0;

		int id = hashFunctions.get(0).computeHashCode(object);
		for (int i = 1, s = hashFunctions.size(); i < s; i++) {
			final int val = hashFunctions.get(i).computeHashCode(object);

			id = addId(id, val, i);
		}
		return id;
	}
}
