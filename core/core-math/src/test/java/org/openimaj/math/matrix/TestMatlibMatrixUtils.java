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

import static org.junit.Assert.assertTrue;
import no.uib.cipr.matrix.Matrix.Norm;

import org.junit.Test;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;

/**
 * Tests for {@link MatlibMatrixUtils}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestMatlibMatrixUtils {
	/**
	 * Test dot product
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDotProduct() throws Exception {
		final Matrix X = new DenseMatrix(new double[][] {
				{ 1, 1, },
				{ 2, 2, },
		});
		final Matrix expected = new DenseMatrix(new double[][] {
				{ 3, 3, },
				{ 6, 6, },
		});
		final Matrix m = MatlibMatrixUtils.dotProduct(X, X);
		final double[][] asArray = MatlibMatrixUtils.minusInplace(m, expected).asArray();
		assertTrue(new no.uib.cipr.matrix.DenseMatrix(asArray).norm(Norm.Frobenius) == 0);
	}
}
