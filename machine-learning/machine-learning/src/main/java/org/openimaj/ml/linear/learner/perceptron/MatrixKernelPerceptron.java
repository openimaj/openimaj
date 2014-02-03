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
public class MatrixKernelPerceptron extends KernelPerceptron<double[], PerceptronClass>{

	protected Function<double[], Double> mapping;
	
	List<DoubleObjectPair<double[]>> weightedSupport = new ArrayList<DoubleObjectPair<double[]>>();
	double bias = 0;
	
	/**
	 * @param k the kernel
	 */
	public MatrixKernelPerceptron(VectorKernel k) {
		super(k);
		this.mapping = new Function<double[],Double>(){

			@Override
			public Double apply(double[] in) {
				double ret = bias;
				in = correct(in);
				for (DoubleObjectPair<double[]> we : weightedSupport) {
					double alpha = we.first;
					double[] x_i = correct(we.second);
					ret += alpha * kernel.apply(IndependentPair.pair(x_i, in));
					
				}
				return ret;
			}

			
			
		};
	}
	
	protected double[] correct(double[] in) {
		return in.clone();
	}

	@Override
	public PerceptronClass predict(double[] x) {
		return PerceptronClass.fromSign(Math.signum(this.mapping.apply(x)));
	}

	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		bias += yt.v();
		this.weightedSupport.add(DoubleObjectPair.pair(yt.v(), xt.clone()));
	}

	public List<DoubleObjectPair<double[]>> getSupports() {
		return weightedSupport;
	}

	public Vector getWeights() {
		Vector v = null;
		double suma = 0;
		for (DoubleObjectPair<double[]> vectorEntry : this.weightedSupport) {
			DenseVector scale = new DenseVector(correct(vectorEntry.second)).scale(vectorEntry.first);
			suma += vectorEntry.first;
			if (v == null) {
				v = scale;
			} else {
				v.add(scale);
			}
			
		}
		return v;
	}

}
