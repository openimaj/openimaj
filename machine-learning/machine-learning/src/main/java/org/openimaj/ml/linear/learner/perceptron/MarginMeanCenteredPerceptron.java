package org.openimaj.ml.linear.learner.perceptron;

import org.openimaj.ml.linear.kernel.VectorKernel;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MarginMeanCenteredPerceptron extends MeanCenteredKernelPerceptron{

	private static final double DEFAULT_MARGIN = 0.1;
	private double margin = DEFAULT_MARGIN;

	/**
	 * @param kernel
	 * @param margin
	 */
	public MarginMeanCenteredPerceptron(VectorKernel kernel, double margin) {
		super(kernel);
		this.margin = margin;
	}
	/**
	 * @param kernel
	 */
	public MarginMeanCenteredPerceptron(VectorKernel kernel) {
		super(kernel);
	}
	
	@Override
	public void process(double[] xt, PerceptronClass yt) {
		double val = mapping(xt);
		PerceptronClass yt_prime = PerceptronClass.fromSign(Math.signum(val));
		if(!yt_prime.equals(yt) || Math.abs(val) < margin){
			update(xt,yt,yt_prime);
			this.errors ++;
		}
	}
}
