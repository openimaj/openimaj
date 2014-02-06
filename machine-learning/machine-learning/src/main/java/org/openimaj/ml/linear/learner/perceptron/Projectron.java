package org.openimaj.ml.linear.learner.perceptron;


import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.ml.linear.kernel.LinearVectorKernel;
import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.DenseVector;
import ch.akuhn.matrix.Matrix;
import ch.akuhn.matrix.Vector;
/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class Projectron extends MatrixKernelPerceptron{

	private static final double DEFAULT_ETA = 0.01f;
	private Matrix Kinv;
	private double eta;

	/**
	 * @param kernel
	 */
	public Projectron(VectorKernel kernel, double eta) {
		super(kernel);
		this.eta = eta;
		Kinv = DenseMatrix.dense(0, 0);
	}

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
