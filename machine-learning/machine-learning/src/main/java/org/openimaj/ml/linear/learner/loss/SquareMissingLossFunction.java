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
package org.openimaj.ml.linear.learner.loss;

import gov.sandia.cognition.math.matrix.Matrix;

import org.apache.log4j.Logger;

public class SquareMissingLossFunction extends LossFunction {
	Logger logger = Logger.getLogger(SquareMissingLossFunction.class);

	@Override
	public Matrix gradient(Matrix W) {
		final Matrix resid = X.times(W).minus(Y);
		if (this.bias != null)
			resid.plusEquals(this.bias);
		for (int r = 0; r < Y.getNumRows(); r++) {
			final double yc = Y.getElement(r, 0);
			if (Double.isNaN(yc)) {
				resid.setElement(r, 0, 0);
			}
		}
		return X.transpose().times(resid);
	}

	@Override
	public double eval(Matrix W) {
		Matrix v;
		if (W == null) {
			v = this.X;
		}
		else {
			v = X.times(W);
		}
		final Matrix vWithoutBias = v.clone();
		if (this.bias != null)
			v.plusEquals(this.bias);
		double sum = 0;
		for (int r = 0; r < Y.getNumRows(); r++) {
			for (int c = 0; c < Y.getNumColumns(); c++) {
				final double yr = Y.getElement(r, c);
				if (!Double.isNaN(yr)) {
					final double val = v.getElement(r, c);
					final double valNoBias = vWithoutBias.getElement(r, c);
					final double delta = yr - val;
					logger.debug(
							String.format(
									"yr=%d,y=%3.2f,v=%3.2f,v(no bias)=%2.5f,delta=%2.5f",
									r, yr, val, valNoBias, delta
									)
							);
					sum += delta * delta;
				}
			}
		}
		return sum;
	}

	@Override
	public boolean isMatrixLoss() {
		return false;
	}

}
