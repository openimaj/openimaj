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
 * Test {@link PseudoInverse}
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class PseudoInverseTest {
	private Matrix matrix;
	private Matrix pinv;
	private Matrix pinvRnk2;
	private Matrix pinvRnk1;

	/**
	 * Default constructor
	 */
	public PseudoInverseTest() {
		this.matrix = new Matrix(new double[][] {
				{0.86, 0.16, 1.00},
				{0.33, 0.14, 0.13},
				{0.90, 0.92, 0.99},
				{0.00, 0.81, 0.98},	
			}
		);
		
		this.pinv = new Matrix(new double[][] {
				{0.14, 0.52, 0.78, -1.01},
				{-1.33, 0.36, 1.13, 0.16},
				{1.10, -0.51, -0.86, 0.84}	
		});
		
		this.pinvRnk2 = new Matrix(new double[][] {
				{0.71, 0.24, 0.20, -0.70},
				{-0.33, -0.13, 0.10, 0.70},
				{0.06, 0.01, 0.21, 0.29}	
		});
		
		this.pinvRnk1 = new Matrix(new double[][] {
				{0.11, 0.03, 0.14, 0.10},
				{0.11, 0.03, 0.14, 0.10},
				{0.16, 0.04, 0.22, 0.15}	
		});
	}

	/**
	 * Test pseudo-inverse
	 */
	@Test
	public void testPinv() {
		Matrix p = PseudoInverse.pseudoInverse(matrix);
		
		assertTrue(MatrixUtils.equals(p, pinv, 0.05));
		
		Matrix pp = PseudoInverse.pseudoInverse(p);		
		assertTrue(MatrixUtils.equals(pp, matrix, 0.00001));
	}
	
	/**
	 * Test low rank pseudo-inverse
	 */
	@Test
	public void testPinvRnk3() {
		Matrix p = PseudoInverse.pseudoInverse(matrix, 3);
		
		assertTrue(MatrixUtils.equals(p, pinv, 0.05));
	}
	
	/**
	 * Test low rank pseudo-inverse
	 */
	@Test
	public void testPinvRnk2() {
		Matrix p = PseudoInverse.pseudoInverse(matrix, 2);
		
		assertTrue(MatrixUtils.equals(p, pinvRnk2, 0.05));
	}
	
	/**
	 * Test low rank pseudo-inverse
	 */
	@Test
	public void testPinvRnk1() {
		Matrix p = PseudoInverse.pseudoInverse(matrix, 1);
		
		assertTrue(MatrixUtils.equals(p, pinvRnk1, 0.05));
	}
}
