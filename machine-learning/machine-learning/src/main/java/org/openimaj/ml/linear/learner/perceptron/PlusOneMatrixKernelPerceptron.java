package org.openimaj.ml.linear.learner.perceptron;

import org.openimaj.ml.linear.kernel.VectorKernel;

import ch.akuhn.matrix.Matrix;

/**
 * An implementation of a simple {@link KernelPerceptron} which works with
 * {@link Matrix} inputs and is binary.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class PlusOneMatrixKernelPerceptron extends MatrixKernelPerceptron{

	/**
	 * @param k
	 */
	public PlusOneMatrixKernelPerceptron(VectorKernel k) {
		super(k);
	}
	
	@Override
	protected double[] correct(double[] in) {
		double[] out = new double[in.length + 1];
		out[0] = 1;
		for (int i = 0; i < in.length; i++) {
			out[i+1] = in[i];
		}
		return out;
	}

}
