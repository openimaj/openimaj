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
package org.openimaj.ml.annotation.bayes;

import gov.sandia.cognition.learning.algorithm.IncrementalLearner;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer.OnlineLearner;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.DefaultWeightedValueDiscriminant;
import gov.sandia.cognition.math.LogMath;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian;
import gov.sandia.cognition.util.AbstractCloneableSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * Annotator based on a Naive Bayes Classifier. Uses a
 * {@link VectorNaiveBayesCategorizer} as the actual classifier.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <OBJECT>
 *            Type of object being annotated
 * @param <ANNOTATION>
 *            Type of annotation
 */
public class NaiveBayesAnnotator<OBJECT, ANNOTATION>
		extends
		IncrementalAnnotator<OBJECT, ANNOTATION>
{
	private static class PDF extends UnivariateGaussian.PDF {
		private static final long serialVersionUID = 1L;

		private SufficientStatistic target;
	}

	private static class PDFLearner extends AbstractCloneableSerializable
			implements
			IncrementalLearner<Double, PDF>
	{
		private static final long serialVersionUID = 1L;

		final UnivariateGaussian.IncrementalEstimator distrLearner = new UnivariateGaussian.IncrementalEstimator();

		@Override
		public PDF createInitialLearnedObject() {
			final PDF pdf = new PDF();
			pdf.target = distrLearner.createInitialLearnedObject();
			return pdf;
		}

		@Override
		public void update(PDF pdf, Double data) {
			distrLearner.update(pdf.target, data);

			pdf.setMean(pdf.target.getMean());
			pdf.setVariance(pdf.target.getVariance());
		}

		@Override
		public void update(PDF pdf, Iterable<? extends Double> data) {
			distrLearner.update(pdf.target, data);

			pdf.setMean(pdf.target.getMean());
			pdf.setVariance(pdf.target.getVariance());
		}
	}

	/**
	 * Modes of operation for prediction using the {@link NaiveBayesAnnotator}.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 * 
	 */
	public static enum Mode {
		/**
		 * The probability of each class should be calculated, and the
		 * annotations returned should contain every annotation with its
		 * associated probability in decreasing order of probability.
		 */
		ALL {
			@Override
			protected <ANNOTATION> List<ScoredAnnotation<ANNOTATION>>
					getAnnotations(VectorNaiveBayesCategorizer<ANNOTATION, PDF> categorizer, Vector vec)
			{
				final List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();

				double logDenominator = Double.NEGATIVE_INFINITY;
				for (final ANNOTATION category : categorizer.getCategories()) {
					final double logPosterior = categorizer.computeLogPosterior(vec, category);

					logDenominator = LogMath.add(logDenominator, logPosterior);
					results.add(new ScoredAnnotation<ANNOTATION>(category, (float) logPosterior));
				}

				for (final ScoredAnnotation<ANNOTATION> scored : results)
					scored.confidence = (float) Math.exp(scored.confidence - logDenominator);

				Collections.sort(results, Collections.reverseOrder());

				return results;
			}
		},
		/**
		 * Only the single most likely annotation will be returned.
		 */
		MAXIMUM_LIKELIHOOD {
			@Override
			protected <ANNOTATION> List<ScoredAnnotation<ANNOTATION>>
					getAnnotations(VectorNaiveBayesCategorizer<ANNOTATION, PDF> categorizer, Vector vec)
			{
				final List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();

				final DefaultWeightedValueDiscriminant<ANNOTATION> r = categorizer.evaluateWithDiscriminant(vec);

				results.add(new ScoredAnnotation<ANNOTATION>(r.getValue(), (float) Math.exp(r.getWeight())));

				return results;
			}
		};

		protected abstract <ANNOTATION> List<ScoredAnnotation<ANNOTATION>>
				getAnnotations(VectorNaiveBayesCategorizer<ANNOTATION, PDF> categorizer, Vector vec);
	}

	private VectorNaiveBayesCategorizer<ANNOTATION, PDF> categorizer;
	private OnlineLearner<ANNOTATION, PDF> learner;
	private final Mode mode;
	private FeatureExtractor<? extends FeatureVector, OBJECT> extractor;

	/**
	 * Construct a {@link NaiveBayesAnnotator} with the given feature extractor
	 * and mode of operation.
	 * 
	 * @param extractor
	 *            the feature extractor.
	 * @param mode
	 *            the mode of operation during prediction
	 */
	public NaiveBayesAnnotator(FeatureExtractor<? extends FeatureVector, OBJECT> extractor, Mode mode) {
		this.extractor = extractor;
		this.mode = mode;
		reset();
	}

	/**
	 * Convenience method to construct a {@link NaiveBayesAnnotator} in the case
	 * where the raw objects are themselves the feature and thus an
	 * {@link IdentityFeatureExtractor} can be used. This method is equivalent
	 * to calling
	 * <tt>new NaiveBayesAnnotator<OBJECT,ANNOTATION>(new IdentityFeatureExtractor<OBJECT>(), mode)</tt>
	 * .
	 * 
	 * @param mode
	 *            the mode of operation during prediction
	 * @return the new {@link NaiveBayesAnnotator}
	 */
	public static <OBJECT extends FeatureVector, ANNOTATION> NaiveBayesAnnotator<OBJECT, ANNOTATION> create(Mode mode) {
		return new NaiveBayesAnnotator<OBJECT, ANNOTATION>(new IdentityFeatureExtractor<OBJECT>(), mode);
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		final FeatureVector feature = extractor.extractFeature(annotated.getObject());
		final Vector vec = VectorFactory.getDefault().copyArray(feature.asDoubleVector());

		for (final ANNOTATION ann : annotated.getAnnotations()) {
			learner.update(categorizer, new DefaultInputOutputPair<Vector, ANNOTATION>(vec, ann));
		}
	}

	@Override
	public void reset() {
		learner = new VectorNaiveBayesCategorizer.OnlineLearner<ANNOTATION, PDF>();
		learner.setDistributionLearner(new PDFLearner());
		categorizer = learner.createInitialLearnedObject();
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return categorizer.getCategories();
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		final FeatureVector feature = extractor.extractFeature(object);
		final Vector vec = VectorFactory.getDefault().copyArray(feature.asDoubleVector());

		return mode.getAnnotations(categorizer, vec);
	}
}
