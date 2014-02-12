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
package org.openimaj.util.hash;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.util.hash.StringMurmurHashFunction;
import org.openimaj.util.hash.StringMurmurHashFunctionFactory;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StringMurmurHashTest {

	/**
	 * 
	 */
	@Test
	public void testMurmur() {
		final StringMurmurHashFunctionFactory fact = new StringMurmurHashFunctionFactory();
		final StringMurmurHashFunction func1 = fact.create();

		final String st1 = "Cheese";
		final String st2 = "Fheese";
		assertTrue(func1.computeHashCode(st1) == func1.computeHashCode(st1));
		assertTrue(func1.computeHashCode(st1) != func1.computeHashCode(st2));

		final StringMurmurHashFunction func2 = fact.create();
		assertTrue(func1.computeHashCode(st1) != func2.computeHashCode(st1));

		int min, max;
		min = fact.create().computeHashCode(st1);
		max = fact.create().computeHashCode(st1);
		for (int i = 0; i < 10000; i++) {
			int n = fact.create().computeHashCode(st1);
			if (n < min)
				min = n;
			if (n > max)
				max = n;
			n = Math.abs(n);
			assertTrue(n % 1000 < 1000 && n % 1000 >= 0);
		}
		System.out.printf("min = %d, max = %d\n", min, max);
		System.out.printf("minint = %d, maxint = %d", Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
}
