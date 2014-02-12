package org.openimaj.ml.linear.learner.perceptron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.ml.linear.kernel.VectorKernel;
import org.openimaj.util.pair.IndependentPair;

import ch.akuhn.matrix.Matrix;

/**
 * An implementation of a simple {@link KernelPerceptron} which works with
 * {@link Matrix} inputs and is binary.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MatrixKernelPerceptron extends KernelPerceptron<double[], PerceptronClass>{

	class WrappedDouble{
		private double[] d;

		public WrappedDouble(double[] d) {
			this.d = d;
		}
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof WrappedDouble){
				WrappedDouble that = (WrappedDouble) obj;
				return Arrays.equals(d, that.d);
			}
			return false;
		}
		
		 @Override
		public int hashCode() {
			return Arrays.hashCode(d);
		}
	}
	protected List<double[]> supports = new ArrayList<double[]>();
	protected List<Double> weights = new ArrayList<Double>();
	
	Map<WrappedDouble,Integer> index = new HashMap<WrappedDouble, Integer>();
	
	
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
		double ret = getBias();
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
		WrappedDouble d = new WrappedDouble(xt);
		double updateAmount = this.getUpdateRate() * (double) yt.v();
		if(!this.index.containsKey(d)){
			this.index.put(d, this.supports.size());
			this.supports.add(xt);
			this.weights.add(updateAmount);
		} else {
			int index = this.index.get(d);
			this.weights.set(index, this.weights.get(index) + updateAmount);
		}
	}

	double getUpdateRate() {
		return 1;
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
		double bias = 0;
		for (double d : this.weights) {
			bias += d;
		}
		return bias;
	}
	
	
	

}
