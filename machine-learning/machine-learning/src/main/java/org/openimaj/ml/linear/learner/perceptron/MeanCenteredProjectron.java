package org.openimaj.ml.linear.learner.perceptron;


import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.matrix.MeanVector;
import org.openimaj.ml.linear.kernel.VectorKernel;
/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MeanCenteredProjectron extends Projectron{
	MeanVector mv = new MeanVector();
	/**
	 * @param kernel 
	 * 
	 */
	public MeanCenteredProjectron(VectorKernel kernel) {
		super(kernel);
	}
	
	/**
	 * @param kernel
	 * @param eta
	 */
	public MeanCenteredProjectron(VectorKernel kernel, double eta) {
		super(kernel, eta);
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
	
	private double[] center(double[] xt) {
		double[] mvec = mv.vec();
		double[] ret = new double[xt.length];
		if(mvec == null) return ret;
		
		for (int i = 0; i < mvec.length; i++) {
			ret[i] = xt[i] - mvec[i];
		}
		return ret;
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
	
	public double[] getMean() {
		return mv.vec();
	}
}
