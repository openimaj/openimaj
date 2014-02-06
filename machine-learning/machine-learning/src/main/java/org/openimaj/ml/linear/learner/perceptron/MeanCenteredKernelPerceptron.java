package org.openimaj.ml.linear.learner.perceptron;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.math.matrix.MeanVector;
import org.openimaj.ml.linear.kernel.VectorKernel;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
	public double[] correct(double[] in) {
		return center(in);
	}
	
	@Override
	public List<double[]> getSupports() {
		List<double[]> pre = super.getSupports();
		List<double[]> ret = new ArrayList<double[]>();
		for (double[] ds : pre) {
			ret.add(correct(ds));
		}
		return ret;
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

	public double[] getMean() {
		return this.mv.vec();
	}
	
}
