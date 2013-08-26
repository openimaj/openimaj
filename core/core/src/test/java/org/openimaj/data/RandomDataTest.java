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
package org.openimaj.data;

import static org.junit.Assert.assertEquals;
import gnu.trove.set.hash.TIntHashSet;

import org.junit.Test;

/**
 * Tests for RandomData
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class RandomDataTest {
//	/**
//	 *  Time the unique random int methods and check load factor 
//	 */
//	@Test 
//	public void testGetUniqueRandomInts() {
//		for (int i=10000; i<100000000; i*=10) {
//			double bestLoadFactor = 0;
//			
//			for (double j=0.1; j<=0.9; j+=0.1) {
//				int N = (int) Math.round(j * i);
//				
//				long t1 = System.currentTimeMillis();
//				RandomData.getUniqueRandomIntsA(N, 0, i);
//				long t2 = System.currentTimeMillis();
//				RandomData.getUniqueRandomIntsS(N, 0, i);
//				long t3 = System.currentTimeMillis();
//				
//				long ATime = t2-t1;
//				long STime = t3-t2;
//				
//				if (ATime > STime)
//					bestLoadFactor = j;
//				
//				System.out.printf("%d\t%1.1f\t%d\t%d\n", i, j, t2-t1, t3-t2);
//			}
//			System.out.println(bestLoadFactor);
//		}
//	}
	
	/**
	 * Test the numbers are truly unique
	 */
	@Test
	public void testGetUniqueRandomInts2() {
		int [] rnd = RandomData.getUniqueRandomIntsA(100, 0, 10000);
		TIntHashSet set = new TIntHashSet(rnd);
		assertEquals(rnd.length, set.size());
		rnd = RandomData.getUniqueRandomIntsS(100, 0, 10000);
		set = new TIntHashSet(rnd);
		
		assertEquals(rnd.length, set.size());
		
	}
}
