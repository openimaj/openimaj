package org.openimaj.ml.linear.learner.perceptron;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.io.MatrixVectorReader;

import org.openimaj.math.matrix.GramSchmidtProcess;
import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.Matrix;

/**
 * An implementation of a simple {@link KernelPerceptron} which works with
 * {@link Matrix} inputs and is binary.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MatrixKernelPerceptron extends KernelPerceptron<double[], PerceptronClass>{

	
	protected List<double[]> supports = new ArrayList<>();
	protected List<Double> weights = new ArrayList<>();
	
	double bias = 0;
	
	/**
	 * @param k the kernel
	 */
	public MatrixKernelPerceptron(VectorKernel k) {
		super(k);
	}
	
	public double[] correct(double[] in) {
		return in.clone();
	}
	
	protected double mapping(double[] in){
		double ret = bias;
		in = correct(in);
		for (int i = 0; i < supports.size(); i++) {
			double alpha = this.weights.get(i);
			double[] x_i = correct(this.supports.get(i));
			ret += alpha * kernel.apply(IndependentPair.pair(x_i, in));
			
		}
		return ret;
	}
	
	@Override
	public PerceptronClass predict(double[] x) {
		return PerceptronClass.fromSign(Math.signum(mapping(x)));
	}

	@Override
	public void update(double[] xt, PerceptronClass yt, PerceptronClass yt_prime) {
		bias += yt.v();
		this.supports.add(xt);
		this.weights.add((double) yt.v());
	}

	@Override
	public List<double[]> getSupports() {
		return this.supports;
	}

	@Override
	public List<Double> getWeights() {
		return this.weights;
	}

	@Override
	public double getBias() {
		return bias;
	}

	

}
