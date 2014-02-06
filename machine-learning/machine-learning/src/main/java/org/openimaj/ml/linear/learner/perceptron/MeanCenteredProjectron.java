package org.openimaj.ml.linear.learner.perceptron;


import org.openimaj.math.matrix.MatlibMatrixUtils;
import org.openimaj.math.matrix.MeanVector;
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
public class MeanCenteredProjectron extends Projectron{
	MeanVector mv = new MeanVector();
	/**
	 * 
	 */
	public MeanCenteredProjectron(VectorKernel kernel) {
		super(kernel);
	}
	
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
}
