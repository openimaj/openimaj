package org.openimaj.ml.linear.learner.perceptron;

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
	
	
	private Kernel<double[]> kernel;
	
	// This is Caligraphic Beta (calB), there are I of these
	protected List<double[]> supports = new ArrayList<double[]>();
	
	// This is K^{-1}_{calB,calB} in the paper
	private Matrix Kinv;
	
	// This is bold K s.t. K_ij is the kernel.appliy(support_i,support_j)
	private Matrix K;
	
	// the threshold of projection
	private double eta;

	// the weights, italic Beta (itB) in the paper
	private Vector beta;

	/**
	 * 
	 * @param kernel
	 */
	public OISVM(Kernel<double[]> kernel, double eta) {
		this.kernel = kernel;
		this.eta = eta;
	}
	@Override
	public void process(double[] x, PerceptronClass y) {
		double kii = this.kernel.apply(IndependentPair.pair(x,x));
		
		// First calculate optimal weighting vector d
		Vector k = calculatekt(x);
		Vector d_optimal = Kinv.mult(k);
		double delta = kii - d_optimal.dot(k);
		
		if(delta > eta){
			updateSupports(x, k, kii, d_optimal,delta);
		}
		
		Vector outNew = this.K.mult(beta);
		if(MatlibMatrixUtils.any(MatlibMatrixUtils.lessThan(outNew,1))){
			
		}
		
	}
	
	private void updateSupports(double[] x, Vector k, double kii, Vector d_optimal, double delta) {
		this.supports.add(x);
		
		if(this.supports.size() > 1){
			updateKinv(d_optimal, delta);
			updateK(k,kii);
		} else {
			init();
		}
	}
	
	@Override
	public PerceptronClass predict(double[] x) {
		return null;
	}
	
	private void init() {
		double[] only = this.supports.get(0);
		this.K = DenseMatrix.dense(1, 1);
		this.Kinv = DenseMatrix.dense(1, 1);
		double kv = this.kernel.apply(IndependentPair.pair(only,only));
		Kinv.put(0, 0, 1/kv);
		K.put(0, 0, 1/kv);
		this.beta = DenseVector.dense(1);
	}
	private void updateK(Vector kt, double kii) {
		// We're updating K to: [ K kt; kt' kii]
		DenseMatrix newK = new DenseMatrix(K.rowCount()+1, K.columnCount() + 1);
		int lastIndex = newK.rowCount()-1;
		MatlibMatrixUtils.setSubMatrix(newK, 0, 0, K);
		MatlibMatrixUtils.setSubMatrixRow(newK, lastIndex, 0, kt);
		MatlibMatrixUtils.setSubMatrixCol(newK, 0, lastIndex, kt);
		newK.put(lastIndex, lastIndex, kii);
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
