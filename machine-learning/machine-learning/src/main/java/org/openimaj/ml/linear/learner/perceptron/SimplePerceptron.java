/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.ml.linear.learner.perceptron;

import java.util.Arrays;
import java.util.List;

import org.openimaj.math.model.EstimatableModel;
import org.openimaj.ml.linear.learner.OnlineLearner;
import org.openimaj.util.pair.IndependentPair;

/**
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class SimplePerceptron implements OnlineLearner<double[], Integer>, EstimatableModel<double[], Integer> {
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
		// System.out.println("Testing: " + Arrays.toString(pt) + " = " +
		// clazz);
		if (w == null) {
			initW(pt.length);
		}
		final int y = predict(pt);
		System.out.println("w: " + Arrays.toString(w));
		w[0] = w[0] + alpha * (clazz - y);
		for (int i = 0; i < pt.length; i++) {
			w[i + 1] = w[i + 1] + alpha * (clazz - y) * pt[i];
		}
		// System.out.println("neww: " + Arrays.toString(w));
	}

	private void initW(int length) {
		w = new double[length + 1];
		w[0] = 1;
	}

	@Override
	public Integer predict(double[] x) {
		if (w == null)
			return 0;
		return (w[0] + project(x)) > 0 ? 1 : 0;
	}

	private double project(double[] x) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			sum += x[i] * w[i + 1];
		}
		return sum;
	}

	@Override
	public boolean estimate(List<? extends IndependentPair<double[], Integer>> data) {
		this.w = new double[] { 1, 0, 0 };

		for (int i = 0; i < iterations; i++) {
			iteration(data);

			final double error = calculateError(data);
			if (error < 0.01)
				break;
		}
		return true;
	}

	private void iteration(List<? extends IndependentPair<double[], Integer>> pts) {
		for (int i = 0; i < pts.size(); i++) {
			final IndependentPair<double[], Integer> pair = pts.get(i);
			process(pair.firstObject(), pair.secondObject());
		}
	}

	@Override
	public int numItemsToEstimate() {
		return 1;
	}

	protected double calculateError(List<? extends IndependentPair<double[], Integer>> pts) {
		double error = 0;

		for (int i = 0; i < pts.size(); i++) {
			final IndependentPair<double[], Integer> pair = pts.get(i);
			error += Math.abs(predict(pts.get(i).firstObject()) - pair.secondObject());
		}

		return error / pts.size();
	}

	/**
	 * Compute NaN-coordinate of a point on the hyperplane given
	 * non-NaN-coordinates. Only one x coordinate may be nan. If more NaN are
	 * seen after the first they are assumed to be 0
	 * 
	 * @param x
	 *            the coordinates, only one may be NaN, all others must be
	 *            provided
	 * @return the y-ordinate
	 */
	public double[] computeHyperplanePoint(double[] x) {
		double total = w[0];
		int nanindex = -1;
		final double[] ret = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double value = x[i];
			if (nanindex != -1 && Double.isNaN(value)) {
				value = 0;
			}
			else if (Double.isNaN(value)) {
				nanindex = i;
				continue;
			}
			ret[i] = value;
			total += w[i + 1] * value;
		}
		if (nanindex != -1)
			ret[nanindex] = total / -w[nanindex + 1];
		return ret;
	}

	@Override
	public SimplePerceptron clone() {
		return new SimplePerceptron(w);
	}

	public double[] getWeights() {
		return this.w;
	}
}
