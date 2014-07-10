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
package org.openimaj.math.model;

import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 * An implementation of a {@link EstimatableModel} that uses a
 * {@link VectorNaiveBayesCategorizer} to associate vectors (actually double[])
 * with a category based on the naive bayes model.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of class/category predicted by the model
 */
public class GaussianVectorNaiveBayesModel<T> implements EstimatableModel<double[], T> {
	VectorNaiveBayesCategorizer.BatchGaussianLearner<T> learner = new VectorNaiveBayesCategorizer.BatchGaussianLearner<T>();
	private VectorNaiveBayesCategorizer<T, PDF> model;

	@Override
	public boolean estimate(List<? extends IndependentPair<double[], T>> data) {
		final List<InputOutputPair<Vector, T>> cfdata = new ArrayList<InputOutputPair<Vector, T>>();

		for (final IndependentPair<double[], T> d : data) {
			final InputOutputPair<Vector, T> iop = new DefaultInputOutputPair<Vector, T>(VectorFactory.getDefault()
					.copyArray(d.firstObject()), d.secondObject());
			cfdata.add(iop);
		}

		model = learner.learn(cfdata);

		return true;
	}

	@Override
	public T predict(double[] data) {
		return model.evaluate(VectorFactory.getDefault().copyArray(data));
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public GaussianVectorNaiveBayesModel<T> clone() {
		try {
			return (GaussianVectorNaiveBayesModel<T>) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final GaussianVectorNaiveBayesModel<Boolean> model = new GaussianVectorNaiveBayesModel<Boolean>();

		final List<IndependentPair<double[], Boolean>> data = new ArrayList<IndependentPair<double[], Boolean>>();

		data.add(IndependentPair.pair(new double[] { 0 }, true));
		data.add(IndependentPair.pair(new double[] { 0.1 }, true));
		data.add(IndependentPair.pair(new double[] { -0.1 }, true));

		data.add(IndependentPair.pair(new double[] { 9.9 }, false));
		data.add(IndependentPair.pair(new double[] { 10 }, false));
		data.add(IndependentPair.pair(new double[] { 10.1 }, false));

		model.estimate(data);

		final double[] q = new double[] { 5.0 };

		System.out.println(model.predict(q));

		System.out.println(" logP(true): "
				+ model.model.computeLogPosterior(VectorFactory.getDefault().copyArray(q), true));
		System.out.println("logP(false): "
				+ model.model.computeLogPosterior(VectorFactory.getDefault().copyArray(q), false));

		System.out.println("    P(true): " + model.model.computePosterior(VectorFactory.getDefault().copyArray(q), true));
		System.out
				.println("   P(false): " + model.model.computePosterior(VectorFactory.getDefault().copyArray(q), false));

		System.out.println(model.model.getPriors());
	}
}
