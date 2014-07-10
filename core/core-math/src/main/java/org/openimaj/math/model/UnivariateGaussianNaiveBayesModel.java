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
import gov.sandia.cognition.statistics.DataHistogram;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.util.pair.IndependentPair;

/**
 * An implementation of a {@link EstimatableModel} that uses a
 * {@link VectorNaiveBayesCategorizer} to associate a univariate (a
 * {@link Double}) with a category.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            The type of class/category predicted by the model
 */

public class UnivariateGaussianNaiveBayesModel<T> implements EstimatableModel<Double, T> {
	private VectorNaiveBayesCategorizer<T, PDF> model;

	/**
	 * Default constructor.
	 */
	public UnivariateGaussianNaiveBayesModel() {

	}

	/**
	 * Construct with a pre-trained model.
	 * 
	 * @param model
	 *            the pre-trained model.
	 */
	public UnivariateGaussianNaiveBayesModel(VectorNaiveBayesCategorizer<T, PDF> model) {
		this.model = model;
	}

	@Override
	public boolean estimate(List<? extends IndependentPair<Double, T>> data) {
		final VectorNaiveBayesCategorizer.BatchGaussianLearner<T> learner = new VectorNaiveBayesCategorizer.BatchGaussianLearner<T>();
		final List<InputOutputPair<Vector, T>> cfdata = new ArrayList<InputOutputPair<Vector, T>>();

		for (final IndependentPair<Double, T> d : data) {
			final InputOutputPair<Vector, T> iop = new DefaultInputOutputPair<Vector, T>(VectorFactory.getDefault()
					.createVector1D(d.firstObject()), d.secondObject());
			cfdata.add(iop);
		}

		model = learner.learn(cfdata);

		return true;
	}

	@Override
	public T predict(Double data) {
		return model.evaluate(VectorFactory.getDefault().createVector1D(data));
	}

	@Override
	public int numItemsToEstimate() {
		return 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public UnivariateGaussianNaiveBayesModel<T> clone() {
		try {
			return (UnivariateGaussianNaiveBayesModel<T>) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the class distribution for the given class.
	 * 
	 * @param clz
	 *            the class
	 * @return the univariate gaussian distribution.
	 */
	public UnivariateGaussian getClassDistribution(T clz) {
		return model.getConditionals().get(clz).get(0);
	}

	/**
	 * Get the class distribution for all classes.
	 * 
	 * @return a map of classes to distributions
	 */
	public Map<T, UnivariateGaussian> getClassDistribution() {
		final Map<T, UnivariateGaussian> clzs = new HashMap<T, UnivariateGaussian>();

		for (final T c : model.getCategories()) {
			clzs.put(c, model.getConditionals().get(c).get(0));
		}

		return clzs;
	}

	/**
	 * @return The priors for each class
	 */
	public DataHistogram<T> getClassPriors() {
		return model.getPriors();
	}

	/**
	 * Testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final UnivariateGaussianNaiveBayesModel<Boolean> model = new UnivariateGaussianNaiveBayesModel<Boolean>();

		final List<IndependentPair<Double, Boolean>> data = new ArrayList<IndependentPair<Double, Boolean>>();

		data.add(IndependentPair.pair(0.0, true));
		data.add(IndependentPair.pair(0.1, true));
		data.add(IndependentPair.pair(-0.1, true));

		data.add(IndependentPair.pair(9.9, false));
		data.add(IndependentPair.pair(10.0, false));
		data.add(IndependentPair.pair(10.1, false));

		model.estimate(data);

		System.out.println(model.predict(5.1));

		System.out.println(model.model.getConditionals().get(true));
		System.out.println(model.model.getConditionals().get(false));

		System.out.println(model.model.getConditionals().get(true).get(0).getMean());
		System.out.println(model.model.getConditionals().get(true).get(0).getVariance());
		System.out.println(model.model.getConditionals().get(false).get(0).getMean());
		System.out.println(model.model.getConditionals().get(false).get(0).getVariance());

		System.out.println(model.model.getPriors());
	}
}
