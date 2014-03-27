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
import gov.sandia.cognition.math.matrix.Vector;

/**
 * With a held Y and X, return gradient and evaluations of
 * a loss function of some parameters s.t.
 * 
 * J(W) = F(Y,X,W)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class LossFunction {
	protected Matrix X;
	protected Matrix Y;
	protected Matrix bias;
	public void setX(Matrix X){
		this.X = X;
	}
	public void setY(Matrix Y){
		this.Y = Y;
	}
	public abstract Matrix gradient(Matrix W);
	public abstract double eval(Matrix W);
	public void setBias(Matrix bias) {
		this.bias = bias;
	}
	public abstract boolean isMatrixLoss() ;
	
	public boolean test_backtrack(Matrix W, Matrix grad, Matrix prox, double eta){
		Matrix tmp = prox.clone();
        tmp.minusEquals(W);
        Vector tmpvec = tmp.getColumn(0);
		return (
			eval(prox) <= eval(W) + grad.getColumn(0).dotProduct(tmpvec) + 0.5*eta*tmpvec.norm2());
		
	}
}
