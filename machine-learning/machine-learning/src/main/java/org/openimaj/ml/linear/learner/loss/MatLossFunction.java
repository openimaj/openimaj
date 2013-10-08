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
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

public class MatLossFunction extends LossFunction{
	
	private LossFunction f;
	private SparseMatrixFactoryMTJ spf;
	public MatLossFunction(LossFunction f) {
		this.f = f;
		spf = SparseMatrixFactoryMTJ.INSTANCE;
	}
	
	@Override
	public void setX(Matrix X) {
		super.setX(X);
		f.setX(X);
	}
	
	@Override
	public void setY(Matrix Y) {
		super.setY(Y);
		f.setY(Y);
	}
	
	@Override
	public void setBias(Matrix bias) {
		super.setBias(bias);
		f.setBias(bias);
	}
	@Override
	public Matrix gradient(Matrix W) {
		SparseMatrix ret = spf.createMatrix(W.getNumRows(), W.getNumColumns());
		int allRowsY = Y.getNumRows()-1;
		int allRowsW = W.getNumRows()-1;
		for (int i = 0; i < Y.getNumColumns(); i++) {
			this.f.setY(Y.getSubMatrix(0, allRowsY, i, i));
			if(bias!=null) this.f.setBias(bias.getSubMatrix(0, allRowsY, i, i));
			Matrix submatrix = f.gradient(W.getSubMatrix(0, allRowsW, i, i));
			ret.setSubMatrix(0, i, submatrix);
		}
		return ret;
	}

	@Override
	public double eval(Matrix W) {
		double total = 0;
		f.setBias(this.bias);
		total += f.eval(W);
		return total;
	}

}
