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

import org.junit.Test;
import org.openimaj.data.RandomData;

import Jama.Matrix;
import ch.akuhn.matrix.DenseVector;

/**
 * Tests for updatable cholesky
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TestCholdate {

	/**
	 * Test update
	 */
	@Test
	public void testRandomUpdate() {
		final Matrix X = Matrix.random(100, 10);
		final Matrix V = X.transpose().times(X);
		final UpdateableCholeskyDecomposition c = new UpdateableCholeskyDecomposition(V);

		final Matrix R = c.getL();

		final double[] udata = RandomData.getRandomDoubleArray(R.getRowDimension(), 0, 1);
		final Matrix u = MatlibMatrixUtils.toColJama(DenseVector.wrap(udata));
		final Matrix V1 = V.plus(u.times(u.transpose()));

		final Matrix R1 = new UpdateableCholeskyDecomposition(V1).getL();
		c.cholupdate(udata);
		final Matrix R1_ = c.getL();
		assertTrue(MatrixUtils.equals(R1, R1_, 0.00001));
	}

	/**
	 * Test downdate
	 */
	@Test
	public void testRandomDowndate() {
		final Matrix X = Matrix.random(100, 10);
		final Matrix V = X.transpose().times(X);
		final UpdateableCholeskyDecomposition c = new UpdateableCholeskyDecomposition(V);

		final Matrix R = c.getL().copy();

		final double[] udata = RandomData.getRandomDoubleArray(R.getRowDimension(), 0, 1);
		c.cholupdate(udata);
		c.choldowndate(udata);
		final Matrix R1_ = c.getL();
		assertTrue(MatrixUtils.equals(R, R1_, 0.00001));
	}
}
