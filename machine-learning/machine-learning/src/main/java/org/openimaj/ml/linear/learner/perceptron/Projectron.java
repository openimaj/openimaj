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
package org.openimaj.ml.linear.learner.perceptron;


import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;
/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Projectron extends DoubleArrayKernelPerceptron{

	private static final double DEFAULT_ETA = 0.01f;
	private Matrix Kinv;
	private double eta;
	/**
	 * @param kernel
	 * @param eta 
	 */
	public Projectron(VectorKernel kernel, double eta) {
		super(kernel);
		this.eta = eta;
		Kinv = DenseMatrix.dense(0, 0);
	}

	/**
	 * @param kernel
	 */
	public Projectron(VectorKernel kernel) {
		this(kernel,DEFAULT_ETA);
	}

	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		double kii = this.kernel.apply(IndependentPair.pair(xt,xt));
		
		// First calculate optimal weighting vector d
		Vector kt = calculatekt(xt);
		Vector d_optimal = Kinv.mult(kt);
		double delta = Math.max(kii - d_optimal.dot(kt), 0);
//		this.bias += yt.v();
		if(delta <= eta){
			updateWeights(yt,d_optimal);
		} else{
			super.update(xt, yt, yt_prime);
			updateKinv(d_optimal,delta);
		}
		
	}

	private void updateWeights(PerceptronClass y, Vector d_optimal) {
		for (int i = 0; i < d_optimal.size(); i++) {
			this.weights.set(i, this.weights.get(i) + y.v() * d_optimal.get(i));
		}
		
	}
	
	@Override
	public double getBias() {
		return 0;
	}

	private void updateKinv(Vector d_optimal, double delta) {
		Matrix newKinv = null;
		if(this.supports.size() > 1){
			Vector expandD = Vector.dense(d_optimal.size() + 1);
			Matrix expandDMat = DenseMatrix.dense(expandD.size(), 1);
			MatlibMatrixUtils.setSubVector(expandDMat.column(0), 0, d_optimal);
			expandDMat.column(0).put(d_optimal.size(), -1);
			newKinv = new DenseMatrix(Kinv.rowCount()+1, Kinv.columnCount() + 1);
			MatlibMatrixUtils.setSubMatrix(newKinv, 0, 0, Kinv);
			
			Matrix expandDMult = newKinv.newInstance();
			MatlibMatrixUtils.dotProductTranspose(expandDMat, expandDMat, expandDMult);
			
			MatlibMatrixUtils.scaleInplace(expandDMult, 1/delta);
			MatlibMatrixUtils.plusInplace(newKinv, expandDMult);
		} else {
			double[] only = this.supports.get(0);
			newKinv = DenseMatrix.dense(1, 1);
			newKinv.put(0, 0, 1/this.kernel.apply(IndependentPair.pair(only,only)));
		}
		this.Kinv = newKinv;
	}

	private Vector calculatekt(double[] xt) {
		Vector ret = Vector.dense(this.supports.size());
		for (int i = 0; i < this.supports.size(); i++) {
			ret.put(i, this.kernel.apply(IndependentPair.pair(xt,this.supports.get(i))));
		}
		return ret;
	}
}
