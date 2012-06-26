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
package org.openimaj.math.matrix;

import static org.junit.Assert.*;

import org.junit.Test;

import Jama.Matrix;

/**
 * Tests for MatrixUtils.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class MatrixUtilsTest {
	/**
	 * Test the sqrt method 
	 */
	@Test
	public void testSqrt() {
		Matrix X = new Matrix( new double[][] {
				{ 5, -4,  1,  0,  0},
				{-4,  6, -4,  1,  0},
				{ 1, -4,  6, -4,  1},
				{ 0,  1, -4,  6, -4},
				{ 0,  0,  1, -4,  5},
		});
		Matrix expected = new Matrix( new double[][] {
				{ 2, -1, -0, -0, -0}, 
				{-1,  2, -1,  0, -0},
				{ 0, -1,  2, -1,  0}, 
				{-0,  0, -1,  2, -1},
				{-0, -0, -0, -1,  2},
		});
		
		Matrix Y = MatrixUtils.sqrt(X);
		
		assertTrue(MatrixUtils.equals(expected, Y, 0.00001));
	}
	
	/**
	 * Test the anyNaNorInf method 
	 */
	@Test
	public void testAnyNaNorInf() {
		Matrix X = new Matrix( new double[][] {
				{ 5, -4},
				{-4,  6},
		});
		assertFalse(MatrixUtils.anyNaNorInf(X));
		
		X = new Matrix( new double[][] {
				{ Double.NEGATIVE_INFINITY, -4},
				{-4,  6},
		});
		assertTrue(MatrixUtils.anyNaNorInf(X));
		
		X = new Matrix( new double[][] {
				{ 1, Double.POSITIVE_INFINITY},
				{-4,  6},
		});
		assertTrue(MatrixUtils.anyNaNorInf(X));
		
		X = new Matrix( new double[][] {
				{ 5, -4},
				{Double.NaN,  6},
		});
		assertTrue(MatrixUtils.anyNaNorInf(X));
	}
	
	/**
	 * Test the maxAbsDiag method 
	 */
	@Test
	public void testMaxAbsDiag() {
		Matrix X = new Matrix( new double[][] {
				{ 5, -4},
				{-4,  6},
		});
		assertEquals(6, MatrixUtils.maxAbsDiag(X), 0.000001);
		
		X = new Matrix( new double[][] {
				{ 5, -4},
				{-4,  -6},
		});
		assertEquals(6, MatrixUtils.maxAbsDiag(X), 0.000001);
	}
	
	/**
	 * Test the minAbsDiag method 
	 */
	@Test
	public void testMinAbsDiag() {
		Matrix X = new Matrix( new double[][] {
				{ 5, -4},
				{-4,  6},
		});
		assertEquals(5, MatrixUtils.minAbsDiag(X), 0.000001);
		
		X = new Matrix( new double[][] {
				{ 5, -1},
				{-4,  -2},
		});
		assertEquals(2, MatrixUtils.minAbsDiag(X), 0.000001);
	}
}
