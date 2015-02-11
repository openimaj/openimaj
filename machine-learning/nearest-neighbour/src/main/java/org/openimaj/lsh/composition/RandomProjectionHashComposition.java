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

import cern.jet.random.engine.MersenneTwister;

/**
 * Compose a set of hash functions by computing the dot product of the hashes
 * they produce with a random vector.
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 *            Object being hashed
 */
public class RandomProjectionHashComposition<OBJECT> extends HashComposition<OBJECT> {
	int[] projection;

	/**
	 * Construct with the given functions.
	 *
	 * @param rng
	 *            the random number generator
	 * @param functions
	 *            the underlying hash functions.
	 */
	public RandomProjectionHashComposition(MersenneTwister rng, List<HashFunction<OBJECT>> functions) {
		super(functions);
		createProjection(rng);
	}

	/**
	 * Construct with the given functions.
	 *
	 * @param rng
	 *            the random number generator
	 * @param first
	 *            the first function
	 * @param remainder
	 *            the remainder of the functions
	 */
	@SafeVarargs
	public RandomProjectionHashComposition(MersenneTwister rng, HashFunction<OBJECT> first,
			HashFunction<OBJECT>... remainder)
	{
		super(first, remainder);
		createProjection(rng);
	}

	/**
	 * Construct with the factory which is used to produce the required number
	 * of functions.
	 *
	 * @param rng
	 *            the random number generator
	 * @param factory
	 *            the factory to use to produce the underlying hash functions.
	 * @param nFuncs
	 *            the number of functions to create for the composition
	 */
	public RandomProjectionHashComposition(MersenneTwister rng, HashFunctionFactory<OBJECT> factory, int nFuncs) {
		super(factory, nFuncs);
		createProjection(rng);
	}

	private void createProjection(MersenneTwister rng) {
		projection = new int[hashFunctions.size()];
		for (int i = 0; i < hashFunctions.size(); i++)
			projection[i] = rng.nextInt();
	}

	@Override
	public int computeHashCode(OBJECT object) {
		int hash = 0;

		for (int i = 0; i < projection.length; i++) {
			hash += projection[i] * hashFunctions.get(i).computeHashCode(object);
		}

		return hash;
	}
}
