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
package org.openimaj.lsh.sketch;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.lsh.functions.DoubleGaussianFactory;
import org.openimaj.util.hash.HashFunction;
import org.openimaj.util.hash.HashFunctionFactory;
import org.openimaj.util.hash.modifier.LSBModifier;

import cern.jet.random.engine.MersenneTwister;

/**
 * Tests for LSH sketchers.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class LSHSketcherTests {
	private List<HashFunction<double[]>> functions;
	private double[][] data;

	/**
	 * Create hash functions and test data
	 */
	@Before
	public void setup() {
		final MersenneTwister mt = new MersenneTwister(new Date());
		final int ndims = 100;
		final int w = 8;

		final DoubleGaussianFactory innerFactory = new DoubleGaussianFactory(ndims, mt, w);
		final HashFunctionFactory<double[]> factory = new HashFunctionFactory<double[]>()
		{
			@Override
			public HashFunction<double[]> create() {
				return new LSBModifier<double[]>(innerFactory.create());
			}
		};

		final int nFunctions = 128;
		functions = new ArrayList<HashFunction<double[]>>(nFunctions);
		for (int i = 0; i < nFunctions; i++) {
			functions.add(factory.create());
		}

		this.data = RandomData.getRandomDoubleArray(10, ndims, 0.0, 256.0);
	}

	/**
	 * Test all the sketcher implementations an ensure they produce consistent
	 * bit strings.
	 */
	@Test
	public void testAll() {
		final ByteLSHSketcher<double[]> byteSketcher = new ByteLSHSketcher<double[]>(functions);
		final ShortLSHSketcher<double[]> shortSketcher = new ShortLSHSketcher<double[]>(functions);
		final IntLSHSketcher<double[]> intSketcher = new IntLSHSketcher<double[]>(functions);
		final LongLSHSketcher<double[]> longSketcher = new LongLSHSketcher<double[]>(functions);
		final BitSetLSHSketcher<double[]> bsSketcher = new BitSetLSHSketcher<double[]>(functions);

		for (final double[] vector : data) {
			final byte[] byteSketch = byteSketcher.createSketch(vector);
			final short[] shortSketch = shortSketcher.createSketch(vector);
			final int[] intSketch = intSketcher.createSketch(vector);
			final long[] longSketch = longSketcher.createSketch(vector);
			final BitSet bsSketch = bsSketcher.createSketch(vector);

			for (int i = 0; i < functions.size(); i++) {
				final boolean actual = functions.get(i).computeHashCode(vector) == 1;

				final boolean byteSet = isSet(byteSketch, i);
				final boolean shortSet = isSet(shortSketch, i);
				final boolean intSet = isSet(intSketch, i);
				final boolean longSet = isSet(longSketch, i);
				final boolean bsSet = bsSketch.get(i);

				assertEquals(actual, byteSet);
				assertEquals(byteSet, shortSet);
				assertEquals(shortSet, intSet);
				assertEquals(intSet, longSet);
				assertEquals(longSet, bsSet);
			}
		}
	}

	boolean isSet(byte[] arr, int i) {
		final int ele = i / 8;
		final int off = i % 8;

		return ((arr[ele] >> off) & 1) == 1;
	}

	boolean isSet(short[] arr, int i) {
		final int ele = i / 16;
		final int off = i % 16;

		return ((arr[ele] >> off) & 1) == 1;
	}

	boolean isSet(int[] arr, int i) {
		final int ele = i / 32;
		final int off = i % 32;

		return ((arr[ele] >> off) & 1) == 1;
	}

	boolean isSet(long[] arr, int i) {
		final int ele = i / 64;
		final int off = i % 64;

		return ((arr[ele] >> off) & 1) == 1;
	}
}
