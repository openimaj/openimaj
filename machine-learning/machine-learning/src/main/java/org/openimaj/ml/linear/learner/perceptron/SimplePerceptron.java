package org.openimaj.ml.linear.learner.perceptron;

import java.util.Arrays;
import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.ml.linear.learner.OnlineLearner;
import org.openimaj.util.pair.IndependentPair;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SimplePerceptron implements OnlineLearner<double[], Integer>, Model<double[], Integer>{
	private static final double DEFAULT_LEARNING_RATE = 0.01;
	private static final int DEFAULT_ITERATIONS = 1000;
	double alpha = DEFAULT_LEARNING_RATE;
	private double[] w;
	private int iterations = DEFAULT_ITERATIONS;
	
	private SimplePerceptron(double[] w) {
		this.w = w;
	}
	
	/**
	 * 
	 */
	public SimplePerceptron() {
	}

	@Override
	public void process(double[] pt, Integer clazz) {
//		System.out.println("Testing: " + Arrays.toString(pt) + " = " + clazz);
		if(w == null){
			initW(pt.length);
		}
		final int y = predict(pt);
		System.out.println("w: " + Arrays.toString(w));
		w[0] = w[0] + alpha * (clazz - y);
		for (int i = 0; i < pt.length; i++) {
			w[i+1] = w[i+1] + alpha * (clazz - y) * pt[i];
		}
//		System.out.println("neww: " + Arrays.toString(w));
	}

	private void initW(int length) {
		w = new double[length + 1];
		w[0] = 1;
	}

	@Override
	public Integer predict(double[] x) {
		if(w == null) return 0;
		return (w[0] + project(x)) > 0 ? 1 : 0;
	}

	private double project(double[] x) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			sum += x[i] * w[i+1];
		}
		return sum;
	}

	@Override
	public void estimate(List<? extends IndependentPair<double[], Integer>> data) {
		this.w = new double[] { 1, 0, 0 };
		
		for (int i = 0; i < iterations; i++) {
			iteration(data);

			final double error = calculateError(data);
			if (error < 0.01)
				break;
		}
	}

	private void iteration(List<? extends IndependentPair<double[], Integer>> pts) {
		for (int i = 0; i < pts.size(); i++) {
			IndependentPair<double[], Integer> pair = pts.get(i);
			process(pair.firstObject(), pair.secondObject());
		}
	}

	@Override
	public boolean validate(IndependentPair<double[], Integer> data) {
		return predict(data.firstObject()) == data.secondObject();
	}

	@Override
	public int numItemsToEstimate() {
		return 1;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<double[], Integer>> pts) {
		double error = 0;

		for (int i = 0; i < pts.size(); i++) {
			IndependentPair<double[], Integer> pair = pts.get(i);
			error += Math.abs(predict(pts.get(i).firstObject()) - pair.secondObject());
		}

		return error / pts.size();
	}
	
	/**
	 * Compute NaN-coordinate of a point on the hyperplane given non-NaN-coordinates.
	 * Only one x coordinate may be nan. If more NaN are seen after the first they are assumed to be 0
	 * 
	 * @param x the coordinates, only one may be NaN, all others must be provided
	 * @return the y-ordinate
	 */
	public double[] computeHyperplanePoint(double[] x) {
		double total = w[0];
		int nanindex = -1;
		double[] ret = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double value = x[i];
			if(nanindex != -1 && Double.isNaN(value)) {
				value = 0;
			}
			else if(Double.isNaN(value)){
				nanindex = i;
				continue;
			}
			ret[i] = value;
			total += w[i+1] * value; 
		}
		if(nanindex != -1) ret[nanindex] = total / -w[nanindex+1];
		return ret;
	}

	
	@Override
	public SimplePerceptron clone(){
		return new SimplePerceptron(w);
	}

	public double[] getWeights() {
		return this.w;
	}
}
