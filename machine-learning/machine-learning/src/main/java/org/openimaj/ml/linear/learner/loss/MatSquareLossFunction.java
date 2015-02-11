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

import org.apache.log4j.Logger;
import org.openimaj.math.matrix.CFMatrixUtils;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class MatSquareLossFunction extends LossFunction{
	Logger logger = Logger.getLogger(MatSquareLossFunction.class);
	private SparseMatrixFactoryMTJ spf;
	public MatSquareLossFunction() {
		spf = SparseMatrixFactoryMTJ.INSTANCE;
	}
	@Override
	public Matrix gradient(Matrix W) {
		Matrix ret = W.clone();
		if(CFMatrixUtils.containsInfinity(X)){
			throw new RuntimeException();
		}
		if(CFMatrixUtils.containsInfinity(W)){
			throw new RuntimeException();
		}
		Matrix resid = CFMatrixUtils.fastdot(X,W);
		if(CFMatrixUtils.containsInfinity(resid)){
			CFMatrixUtils.fastdot(X,W);
			throw new RuntimeException();
		}
		if(this.bias!=null)
		{
			resid.plusEquals(this.bias);
		}
		CFMatrixUtils.fastminusEquals(resid, Y);
		if(CFMatrixUtils.containsInfinity(resid)){
			throw new RuntimeException();
		}
		for (int t = 0; t < resid.getNumColumns(); t++) {
			Vector xcol = this.X.getRow(t).scale(resid.getElement(t, t)).clone();
			CFMatrixUtils.fastsetcol(ret,t, xcol);
		}
		return ret;
	}
	@Override
	public double eval(Matrix W) {
		Matrix resid = null;
		if(W == null){
			resid = X.clone();
		} else {
			resid = CFMatrixUtils.fastdot(X,W);
		}
		Matrix vnobias = resid.clone();
		if(this.bias!=null)
		{
			resid.plusEquals(this.bias);
		}
		Matrix v = resid.clone();
		resid.minusEquals(Y);
		double retval = 0;
		
		for (int t = 0; t < resid.getNumColumns(); t++) {
			double loss = resid.getElement(t, t);
			retval += loss * loss;
			logger.debug(
					String.format(
							"yr=%d,y=%3.2f,v=%3.2f,v(no bias)=%2.5f,error=%2.5f,serror=%2.5f",
							t, Y.getElement(t, t), v.getElement(t, t), vnobias.getElement(t,t), loss, loss*loss
							)
					);
		}
		return retval;
	}
	
	@Override
	public boolean test_backtrack(Matrix W, Matrix grad, Matrix prox, double eta) {
		Matrix tmp = prox.minus(W);
        double evalW = eval(W);
		double evalProx = eval(prox);
		Matrix fastdotGradTmp = CFMatrixUtils.fastdot(grad.transpose(),tmp);
		double normGradProx = CFMatrixUtils.sum(fastdotGradTmp);
		double normTmp = 0.5*eta*tmp.normFrobenius();
		return (evalProx <= evalW + normGradProx + normTmp);
	}
	
	@Override
	public boolean isMatrixLoss() {
		return true;
	}
}
