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
package org.openimaj.ml.linear.learner.matlib.loss;

import org.apache.log4j.Logger;
import org.openimaj.math.matrix.MatlibMatrixUtils;

import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;


public class MatSquareLossFunction extends LossFunction{
	Logger logger = Logger.getLogger(MatSquareLossFunction.class);
	public MatSquareLossFunction() {
	}
	@Override
	public Matrix gradient(Matrix W) {
		Matrix ret = W.newInstance();
		Matrix resid = MatlibMatrixUtils.dotProduct(X, W);
		if(this.bias!=null)
		{
			MatlibMatrixUtils.plusInplace(resid, this.bias);
		}
		MatlibMatrixUtils.minusInplace(resid, Y);
		for (int t = 0; t < resid.columnCount(); t++) {
			Vector row = this.X.row(t);
			row.times(resid.get(t, t));
			MatlibMatrixUtils.setSubMatrixCol(ret, 0, t, row);
		}
		return ret;
	}
	@Override
	public double eval(Matrix W) {
		Matrix resid = null;
		if(W == null){
			resid = X;
		} else {
			resid = MatlibMatrixUtils.dotProduct(X,W);
		}
		Matrix vnobias = MatlibMatrixUtils.copy(X);
		if(this.bias!=null)
		{
			MatlibMatrixUtils.plusInplace(resid, bias);
		}
		Matrix v =  MatlibMatrixUtils.copy(resid);
		MatlibMatrixUtils.minusInplace(resid,Y);
		double retval = 0;
		
		for (int t = 0; t < resid.columnCount(); t++) {
			double loss = resid.get(t, t);
			retval += loss * loss;
			logger.debug(
					String.format(
							"yr=%d,y=%3.2f,v=%3.2f,v(no bias)=%2.5f,error=%2.5f,serror=%2.5f",
							t, Y.get(t, t), v.get(t, t), vnobias.get(t,t), loss, loss*loss
							)
					);
		}
		return retval;
	}
	@Override
	public boolean isMatrixLoss() {
		return true;
	}
}
