package org.openimaj.ml.linear.learner.perceptron;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.math.matrix.MeanVector;
import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.function.Function;
import org.openimaj.util.pair.DoubleObjectPair;
import org.openimaj.util.pair.IndependentPair;

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
		this(0.1,0.5,k);
	}
	public ThresholdMatrixKernelPerceptron(double weight, double threshold, VectorKernel k) {
		super(k);
		this.weight = weight;
		this.thresh = threshold;
		
	}
	
	@Override
	public PerceptronClass predict(double[] x) {
		double apply = this.mapping.apply(x);
		if(Math.abs(apply) <= this.thresh) apply = -1;
		return PerceptronClass.fromSign(Math.signum(apply));
		
	}
	
	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		bias = 0;
		this.weightedSupport.add(DoubleObjectPair.pair(yt.v()*this.weight, xt.clone()));
	}
	
	
	
}