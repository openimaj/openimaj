package org.openimaj.ml.linear.learner.perceptron;

import org.openimaj.math.matrix.MeanVector;
import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.ml.linear.learner.OnlineLearner;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.Operation;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <INDEPENDANT> 
 * @param <DEPENDANT> 
 */
public class MeanCenteredKernelPerceptron extends MatrixKernelPerceptron{
	MeanVector mv = new MeanVector();
	
	/**
	 * @param k
	 */
	public MeanCenteredKernelPerceptron(VectorKernel k) {
		super(k);
	}
	
	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		mv.update(xt);
		super.update(xt, yt, yt_prime);
	}
	@Override
	protected double[] correct(double[] in) {
		return center(in);
	}
	
	private double[] center(double[] xt) {
		double[] mvec = mv.vec();
		double[] ret = new double[xt.length];
		if(mvec == null) return ret;
		
		for (int i = 0; i < mvec.length; i++) {
			ret[i] = xt[i] - mvec[i];
		}
		return ret;
	}
}
