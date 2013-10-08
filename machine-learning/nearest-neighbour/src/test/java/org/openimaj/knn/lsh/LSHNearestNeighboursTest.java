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
package org.openimaj.knn.lsh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.composition.SimpleComposition;
import org.openimaj.util.hash.modifier.ModuloModifier;
import org.openimaj.util.pair.IntFloatPair;

import cern.jet.random.engine.MersenneTwister;

/**
 * Unit tests for {@link LSHNearestNeighbours}
 * 
 * @author Per Christian Moen
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LSHNearestNeighboursTest {
	private final MersenneTwister mt = new MersenneTwister();

	private DoubleGaussianFactory gauss;

	/**
	 * HashFunctionFactory that returns a set of composed Gaussian hashes which
	 * are then modulo'd
	 */
	private HashFunctionFactory<double[]> factory;

	/**
	 * HashFunctionFactory that returns hash functions that always return 0
	 */
	private HashFunctionFactory<double[]> alwaysZeroHashFunctionFactory;

	/**
	 * HashFunctionFactory that returns hash functions that return the first
	 * element of the input (cast to an int)
	 */
	private HashFunctionFactory<double[]> firstElementHashFunctionFactory;

	/**
	 * Setup hash functions for tests
	 */
	@Before
	public void setup() {
		final int range = 1017881;
		final int nFunctions = 20;
		final int ndims = 128;
		final int w = 8;

		gauss = new DoubleGaussianFactory(ndims, mt, w);
		factory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new ModuloModifier<double[]>(new SimpleComposition<double[]>(gauss, nFunctions), range);
			}
		};

		alwaysZeroHashFunctionFactory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new HashFunction<double[]>() {
					@Override
					public int computeHashCode(final double[] object) {
						// Always returning 0 so that the hash table returns
						// values for our test data
						return 0;
					}
				};
			}
		};

		firstElementHashFunctionFactory = new HashFunctionFactory<double[]>() {
			@Override
			public HashFunction<double[]> create() {
				return new HashFunction<double[]>() {
					@Override
					public int computeHashCode(final double[] object) {
						return (int) object[0];
					}
				};
			}
		};

	}

	/**
	 * Test that when queried, an empty NN always returns negative indexes or
	 * null (searchNN) or an empty list (searchKNN)
	 */
	@Test
	public void emptyLshReturnsNegativeIndexValues() {
		final int numFeatures = 10;
		final int K = 10;

		final double[][] qus = new double[numFeatures][K];
		final int[][] argmins = new int[numFeatures][K];
		final float[][] mins = new float[numFeatures][K];

		final int ntables = 4;
		final LSHNearestNeighbours<double[]> lsh = new LSHNearestNeighbours<double[]>(factory, ntables,
				gauss.distanceFunction());

		lsh.searchKNN(qus, K, argmins, mins);

		for (final int[] neighbours : argmins) {
			for (final int neighbor : neighbours) {
				assertTrue(neighbor < 0);
			}
		}

		for (int i = 0; i < qus.length; i++) {
			assertEquals(null, lsh.searchNN(qus[i]));
			assertEquals(0, lsh.searchKNN(qus[i], K).size());
		}
	}

	/**
	 * Test that if you ask for more neighbours than there is data that you get
	 * negative indexes for the array-based methods and the correct length list
	 * for searchKNN.
	 */
	@Test
	public void searchWithExcessiveNeighboursReturnsNegativeIndexValues() {
		final int numFeatures = 10;
		final int K = 10;

		final LSHNearestNeighbours<double[]> lsh = new LSHNearestNeighbours<double[]>(alwaysZeroHashFunctionFactory, 4,
				gauss.distanceFunction());
		final int featureIndex = lsh.add(RandomData.getRandomDoubleArray(numFeatures, 0, 1, mt));

		final double[][] qus = new double[numFeatures][K];
		final int[][] argmins = new int[numFeatures][K];
		final float[][] mins = new float[numFeatures][K];

		lsh.searchKNN(qus, K, argmins, mins);

		// Closest neighbour should be the the same as the index of the feature
		// in the lsh
		for (final int[] neighbours : argmins) {
			assertEquals(featureIndex, neighbours[0]);

			// The other indices should have a negative values to signal that
			// there are no neighbours
			for (int i = 1; i < neighbours.length; ++i) {
				assertTrue(neighbours[i] < 0);
			}
		}

		for (int i = 0; i < qus.length; i++) {
			assertEquals(featureIndex, lsh.searchNN(qus[i]).first);

			final List<IntFloatPair> knns = lsh.searchKNN(qus[i], K);
			assertEquals(1, knns.size());
			assertEquals(featureIndex, knns.get(0).first);
		}
	}

	/**
	 * Test that searching doesn't fail (i.e. throw an exception) if there are
	 * no hash collisions.
	 */
	@Test
	public void searchUsingQueriesNotFoundInHashTablesShouldNotFail() {
		final int numFeatures = 10;
		final int K = 10;

		final LSHNearestNeighbours<double[]> lsh = new LSHNearestNeighbours<double[]>(firstElementHashFunctionFactory, 4,
				gauss.distanceFunction());

		// First add some features to the index
		for (int i = 0; i < 100; ++i) {
			final double[] randomDoubleArray = RandomData.getRandomDoubleArray(numFeatures, 0, 1, mt);
			// Set the first element to 0 since this is the value returned by
			// the hash function
			randomDoubleArray[0] = 0;
			lsh.add(randomDoubleArray);
		}

		// Create search query
		final double[][] qus = RandomData.getRandomDoubleArray(numFeatures, numFeatures, 0d, 1d);
		for (int i = 0; i < numFeatures; ++i) {
			// Set the first element to another value that the value used when
			// adding train data.
			qus[i][0] = 1;
		}

		final int[][] argmins = new int[numFeatures][K];
		final float[][] mins = new float[numFeatures][K];

		lsh.searchKNN(qus, K, argmins, mins);

		for (int i = 0; i < qus.length; i++) {
			for (final int v : argmins[i])
				assertTrue(v < 0);

			assertEquals(0, lsh.searchKNN(qus[i], K).size());
			assertEquals(null, lsh.searchNN(qus[i]));
		}
	}
}
