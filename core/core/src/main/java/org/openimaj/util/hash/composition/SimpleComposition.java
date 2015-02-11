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
package org.openimaj.util.hash.composition;

import java.util.List;

import org.openimaj.util.hash.HashCodeUtil;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;

/**
 * Simple composition function. Computes the composite hash using the same
 * method as {@link HashCodeUtil} for int arrays.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Type of object being hashed
 */
public class SimpleComposition<OBJECT> extends HashComposition<OBJECT> {
	/**
	 * Construct with the given functions.
	 *
	 * @param functions
	 *            the underlying hash functions.
	 */
	public SimpleComposition(List<HashFunction<OBJECT>> functions) {
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
	public SimpleComposition(HashFunction<OBJECT> first, HashFunction<OBJECT>... remainder) {
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
	public SimpleComposition(HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
	}

	@Override
	public int computeHashCode(OBJECT object) {
		int result = HashCodeUtil.SEED;

		for (int i = 0; i < hashFunctions.size(); i++)
			result = HashCodeUtil.hash(result, hashFunctions.get(i).computeHashCode(object));

		return result;
	}
}
