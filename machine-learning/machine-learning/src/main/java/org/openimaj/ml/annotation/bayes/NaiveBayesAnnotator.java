package org.openimaj.ml.annotation.bayes;

import gov.sandia.cognition.learning.algorithm.IncrementalLearner;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer.OnlineLearner;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
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
 * @param <EXTRACTOR>
 *            Type of feature extractor
 */
public class NaiveBayesAnnotator<OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<? extends FeatureVector, OBJECT>>
		extends
		IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR>
{
	private class PDF extends UnivariateGaussian.PDF {
		private static final long serialVersionUID = 1L;

		private SufficientStatistic target;
	}

	private class PDFLearner extends AbstractCloneableSerializable
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

	private VectorNaiveBayesCategorizer<ANNOTATION, PDF> categorizer;
	private OnlineLearner<ANNOTATION, PDF> learner;

	/**
	 * Construct a {@link NaiveBayesAnnotator} with the given feature extractor.
	 * 
	 * @param extractor
	 *            the feature extractor.
	 */
	public NaiveBayesAnnotator(EXTRACTOR extractor) {
		super(extractor);
		reset();
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

		final List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();

		for (final ANNOTATION category : categorizer.getCategories()) {
			results.add(new ScoredAnnotation<ANNOTATION>(category, (float)
					(-1.0 * this.categorizer.computeLogPosterior(vec, category))));
		}

		Collections.sort(results);

		System.out.println(this.categorizer.evaluate(vec) + " " + results);

		// results.add(new
		// ScoredAnnotation<ANNOTATION>(categorizer.evaluate(vec), 1));

		return results;
	}
}
