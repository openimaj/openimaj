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
package org.openimaj.ml.linear.learner.regul;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseRowMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseVector;

import org.openimaj.math.matrix.CFMatrixUtils;

public class L1L2Regulariser implements Regulariser {

	@Override
	public Matrix prox(Matrix W, double lambda) {
		final int nrows = W.getNumRows();
		Matrix ret = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(W.getNumRows(), W.getNumColumns());
		final SparseRowMatrix Wrow = CFMatrixUtils.asSparseRow(W);
		// Matrix Wrow = W;
		ret = CFMatrixUtils.asSparseRow(ret);

		for (int r = 0; r < nrows; r++) {
			// Vector row = W.getRow(r);
			final SparseVector row = Wrow.getRow(r);
			final double rownorm = row.norm2();
			if (rownorm > lambda) {
				final double scal = (rownorm - lambda) / rownorm;
				final Vector scaled = row.scale(scal);
				ret.setRow(r, scaled);
			}
		}
		return CFMatrixUtils.asSparseColumn(ret);
	}

}
