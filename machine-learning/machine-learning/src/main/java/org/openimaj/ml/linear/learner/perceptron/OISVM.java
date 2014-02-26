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

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.citation.annotation.References;
import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.linear.kernel.Kernel;
import org.openimaj.ml.linear.learner.OnlineLearner;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.DenseVector;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@References(references = {
		@Reference(
				type = ReferenceType.Article,
				author = { "Francesco Orabona", "Claudio Castellini", "Barbara Caputo", "Luo Jie", "Giulio Sandini" },
				title = "On-line independent support vector machines",
				year = "2010",
				journal = "Pattern Recognition",
				pages = { "1402", "1412" },
				number = "4",
				volume = "43"),
})
public class OISVM implements OnlineLearner<double[], PerceptronClass>{
	
	
	private static final int DEFAULT_NEWTON_ITER = 20;

	private Kernel<double[]> kernel;
	
	// This is Caligraphic Beta (calB), there are B of these
	protected List<double[]> supports = new ArrayList<double[]>();
	protected TIntArrayList supportIndex = new TIntArrayList();
	
	// This is y, holding the expected y of the supports
	private List<PerceptronClass> expected = new ArrayList<PerceptronClass>();
	
	// This is K^{-1}_{calB,calB} in the paper
	private Matrix Kinv;
	
	// This is bold K s.t. K_ij is the kernel.appliy(support_i,support_j)
	private Matrix K;
	
	// the threshold of projection
	private double eta;

	// the weights, italic Beta (itB) in the paper
	private Vector beta;
	

	
	int newtonMaxIter = DEFAULT_NEWTON_ITER;
	
	double C = 1;

	private List<double[]> nonSupports = new ArrayList<double[]>();

	

	/**
	 * 
	 * @param kernel
	 * @param eta 
	 */
	public OISVM(Kernel<double[]> kernel, double eta) {
		this.kernel = kernel;
		this.eta = eta;
		this.K = DenseMatrix.dense(0, 0);
		this.Kinv = DenseMatrix.dense(0, 0);
	}
	@Override
	public void process(double[] x, PerceptronClass y) {
		double kii = this.kernel.apply(IndependentPair.pair(x,x));
		
		// First calculate optimal weighting vector d
		Vector kt = calculatekt(x);
		Vector k = kt.times(y.v());
		Vector d_optimal = Kinv.mult(k);
		double delta = kii - d_optimal.dot(k);
		
		if(delta > eta){
			updateSupports(x, y, k, kii, d_optimal,delta);
		}
		else {
			updateNonSupports(x,y,kt,d_optimal);
		}
		
		double ot = k.dot(beta);
		if(ot < 1){
			int newtonIter = 0;
			TIntArrayList I = new TIntArrayList();
			while(newtonIter++ < this.newtonMaxIter){				
				TIntArrayList newI = newtonStep(I);
				if(newI.equals(I)){
					break;
				}
				I = newI;
			}
			
		}
		
	}
	
	private void updateSupports(double[] x, PerceptronClass y,Vector k, double kii, Vector d_optimal, double delta) {
		this.supports.add(x);
		this.expected.add(y);
		supportIndex.add(this.K.columnCount()-1);
		
		if(this.supports.size() > 1){
			updateKinv(d_optimal, delta);
			updateK(k,kii);
			updateBeta();
		} else {
			init();
		}
	}
	
	private void updateNonSupports(double[] x, PerceptronClass y,Vector kk, Vector d_optimal) {
		this.nonSupports.add(d_optimal.unwrap());
		MatlibMatrixUtils.appendColumn(this.K,kk);
		
	}
	private TIntArrayList newtonStep(TIntArrayList currentI) {
		TIntArrayList Icols = new TIntArrayList(this.K.rowCount());
		TDoubleArrayList Yvals = new TDoubleArrayList(this.K.rowCount());
		// K . itB (i.e. kernel weighted by beta)
		Vector v = this.K.mult(beta);
		
		
		// g = K . itB - K_BI . (y_I - K_BI . itB)
		for (int i = 0; i < K.rowCount(); i++) {
			int yi = this.expected.get(i).v();
			double yioi = v.get(i) * yi;
			if(1 - yioi > 0) {
				Icols.add(i);
				Yvals.add(yi);
			}
		}
		if(currentI.equals(Icols))return Icols;
		Matrix Kbi = MatlibMatrixUtils.subMatrix(this.K, 0, this.K.rowCount(),Icols);
		
		// here we calculate g = K . itB - K_BI (y_I - o_I)
		Vector g = DenseVector.wrap(Yvals.toArray()); // g = y_I
		MatlibMatrixUtils.minusInplace(g, Kbi.mult(beta)); // g = y_I - K_BI . itB
		g = Kbi.mult(g); // g = K_BI . (y_I - K_BI . itB)
		g = MatlibMatrixUtils.minus(v,g); // g = K . itB - K_BI (y_I - o_I)
		
		// Here we calculate: P = K + C * Kbi . Kbi^T, then P^-1
		Matrix P = new DenseMatrix(K.rowCount(), K.columnCount()); // P = 0
		MatlibMatrixUtils.dotProductTranspose(Kbi, Kbi, P); // P = Kbi . Kbi^T
		MatlibMatrixUtils.scaleInplace(P, C); // P = C * Kbi . Kbi^T
		MatlibMatrixUtils.plusInplace(P, K); // P = K + C * Kbi . Kbi^T
		Matrix Pinv = MatlibMatrixUtils.fromJama(MatlibMatrixUtils.toJama(P).inverse());
		
		// here we update itB = itB - Pinv . g
		
		MatlibMatrixUtils.minusInplace(beta, Pinv.mult(g));
		return Icols;
	}
	

	@Override
	public PerceptronClass predict(double[] x) {
		return PerceptronClass.fromSign(Math.signum(calculatekt(x).dot(beta)));
	}
	
	private void init() {
		double[] only = this.supports.get(0);
		this.K = DenseMatrix.dense(1, 1);
		this.Kinv = DenseMatrix.dense(1, 1);
		double kv = this.kernel.apply(IndependentPair.pair(only,only));
		Kinv.put(0, 0, 1/kv);
		K.put(0, 0, kv);
		this.beta = DenseVector.dense(1);
	}
	
	private void updateK(Vector kt, double kii) {
		// We're updating K to: [ K kt; kt' kii]
		Vector row = DenseVector.dense(K.columnCount());
		row.put(K.columnCount()-1, kii);
		// TODO: Fill this row... K[Beta] = kt while K[~Beta] has to be calculated
		Matrix newK = MatlibMatrixUtils.appendRow(K, row );
		this.K = newK;
	}
	
	private void updateBeta() {
		// We're updating itB to: [ K kt; kt' kii]
		Vector newB = DenseVector.dense(this.beta.size() + 1);
		MatlibMatrixUtils.setSubVector(newB, 0, beta);
		this.beta = newB;
	}
	
	private void updateKinv(Vector d_optimal, double delta) {
		Matrix newKinv = null;
		
		// We're updating Kinv by calculating: [ Kinv 0; 0... 0] + (1/delta) [d -1]' . [d -1]
		
		// construct the column vector matrix [d -1]'
		Matrix expandDMat = DenseMatrix.dense(d_optimal.size() + 1, 1);
		MatlibMatrixUtils.setSubVector(expandDMat.column(0), 0, d_optimal);
		expandDMat.column(0).put(d_optimal.size(), -1);
		
		// construct a new, expanded Kinv matrix
		newKinv = new DenseMatrix(Kinv.rowCount()+1, Kinv.columnCount() + 1);
		MatlibMatrixUtils.setSubMatrix(newKinv, 0, 0, Kinv);
		
		// construct [d -1]' [d -1]
		Matrix expandDMult = newKinv.newInstance();
		MatlibMatrixUtils.dotProductTranspose(expandDMat, expandDMat, expandDMult);
		
		// scale the new matrix by 1/delta
		MatlibMatrixUtils.scaleInplace(expandDMult, 1/delta);
		// add it to the new Kinv
		MatlibMatrixUtils.plusInplace(newKinv, expandDMult);
		this.Kinv = newKinv;
	}

	private Vector calculatekt(double[] x) {
		Vector ret = Vector.dense(this.supports.size());
		for (int i = 0; i < this.supports.size(); i++) {
			ret.put(i, this.kernel.apply(IndependentPair.pair(x,this.supports.get(i))));
		}
		return ret;
	}

}
