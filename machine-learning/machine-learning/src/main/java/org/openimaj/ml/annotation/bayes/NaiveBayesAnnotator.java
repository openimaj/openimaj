package org.openimaj.ml.annotation.bayes;

import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.IncrementalAnnotator;
import org.openimaj.ml.feature.FeatureExtractor;

/**
 * Annotator based on a Naive Bayes Classifier. Uses a
 * {@link VectorNaiveBayesCategorizer} as the actual classifier.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object being annotated
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of feature extractor
 */
public class NaiveBayesAnnotator<
	OBJECT,
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<? extends FeatureVector, OBJECT>>
extends
	IncrementalAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	private VectorNaiveBayesCategorizer<ANNOTATION, UnivariateGaussian.PDF> categorizer;
	private VectorNaiveBayesCategorizer.OnlineLearner<ANNOTATION, UnivariateGaussian.PDF> learner;
	
	/**
	 * Construct a {@link NaiveBayesAnnotator} with the given feature extractor.
	 * @param extractor the feature extractor.
	 */
	public NaiveBayesAnnotator(EXTRACTOR extractor) {
		super(extractor);
		reset();
	}

	@Override
	public void train(Annotated<OBJECT, ANNOTATION> annotated) {
		FeatureVector feature = extractor.extractFeature(annotated.getObject());
		Vector vec = VectorFactory.getDefault().copyArray( feature.asDoubleVector() );
		
		for (ANNOTATION ann : annotated.getAnnotations()) {
			learner.update(categorizer, new DefaultInputOutputPair<Vector, ANNOTATION>(vec, ann));
		}
	}

	@Override
	public void reset() {
		learner = new VectorNaiveBayesCategorizer.OnlineLearner<ANNOTATION, UnivariateGaussian.PDF>();
		categorizer = learner.createInitialLearnedObject();
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return categorizer.getCategories();
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		FeatureVector feature = extractor.extractFeature(object);
		Vector vec = VectorFactory.getDefault().copyArray( feature.asDoubleVector() );
		
		List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();
		
		for (ANNOTATION category : categorizer.getCategories()) {
			results.add(new ScoredAnnotation<ANNOTATION>(category, (float)(-1.0 * this.categorizer.computeLogPosterior(vec, category))));
		}
		
		Collections.sort(results);
		
		return results;
	}
}
