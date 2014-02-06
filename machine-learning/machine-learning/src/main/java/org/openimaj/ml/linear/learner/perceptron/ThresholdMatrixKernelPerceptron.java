package org.openimaj.ml.linear.learner.perceptron;

import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.pair.DoubleObjectPair;

import ch.akuhn.matrix.Matrix;

/**
 * An implementation of a simple {@link KernelPerceptron} which works with
 * {@link Matrix} inputs and is binary.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ThresholdMatrixKernelPerceptron extends MatrixKernelPerceptron {

	private double weight;
	private double thresh;

	public ThresholdMatrixKernelPerceptron(VectorKernel k) {
		this(0.1,0,k);
	}
	public ThresholdMatrixKernelPerceptron(double weight, double threshold, VectorKernel k) {
		super(k);
		this.weight = weight;
		this.thresh = threshold;
		
	}
	
	@Override
	public PerceptronClass predict(double[] x) {
		double apply = mapping(x);
		if(Math.abs(apply) < this.thresh) apply = -1;
		return PerceptronClass.fromSign(Math.signum(apply));
		
	}
	
	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		System.out.println("UPDATING!!!");
		bias += yt.v() * this.weight;
		this.supports.add(xt);
		this.weights.add(this.weight * yt.v());
	}
	
	
	
}