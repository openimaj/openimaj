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
package core.openimaj.math.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Test {@link FloatArrayStatsUtils}
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FloatArrayStatsUtilTest {
	/**
	 * Test mean
	 */
	@Test
	public void testMean(){
		float[] arr1D = {1,2,3,4};
		float[][] arr2D = {{1,2},{3,4}};
		
		assertTrue(FloatArrayStatsUtils.mean(arr1D) == 2.5);
		assertTrue(FloatArrayStatsUtils.mean(arr2D) == 2.5);
		assertTrue(FloatArrayStatsUtils.mean(new float[0]) == 0);
		assertTrue(FloatArrayStatsUtils.mean(new float[]{1}) == 1);
	}
	
	/**
	 * Test stddev
	 */
	@Test
	public void testStd(){
		float[] arr1D = {1,2,4,5,6};
		float[][] arr2D = {{1,2,4},{5,6}};
		
		System.out.println(FloatArrayStatsUtils.var(arr2D));
		
		assertTrue(FloatArrayStatsUtils.var(arr1D) == 4.3f);
		assertTrue(FloatArrayStatsUtils.var(arr2D) == 4.3f);
		assertTrue(FloatArrayStatsUtils.var(new float[0]) == 0);
		assertTrue(FloatArrayStatsUtils.var(new float[]{1}) == 0);
		assertTrue(FloatArrayStatsUtils.var(new float[]{1,2}) == 0.5);
	}
}
