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
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.ml.annotation.IncrementalAnnotator;

/**
 * Annotator based on a Naive Bayes Classifier. Uses a
 * {@link VectorNaiveBayesCategorizer} as the actual classifier.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <O>
 * @param <A>
 * @param <E>
 */
public class NaiveBayesAnnotator<
	O,
	A,
	E extends FeatureExtractor<? extends FeatureVector, O>>
extends
	IncrementalAnnotator<O, A, E> 
{
	private VectorNaiveBayesCategorizer<A, UnivariateGaussian.PDF> categorizer;
	private VectorNaiveBayesCategorizer.OnlineLearner<A, UnivariateGaussian.PDF> learner;
	
	/**
	 * Construct a {@link NaiveBayesAnnotator} with the given feature extractor.
	 * @param extractor the feature extractor.
	 */
	public NaiveBayesAnnotator(E extractor) {
		super(extractor);
		reset();
	}

	@Override
	public void train(Annotated<O, A> annotated) {
		FeatureVector feature = extractor.extractFeature(annotated.getObject());
		Vector vec = VectorFactory.getDefault().copyArray( feature.asDoubleVector() );
		
		for (A ann : annotated.getAnnotations()) {
			learner.update(categorizer, new DefaultInputOutputPair<Vector, A>(vec, ann));
		}
	}

	@Override
	public void reset() {
		learner = new VectorNaiveBayesCategorizer.OnlineLearner<A, UnivariateGaussian.PDF>();
		categorizer = learner.createInitialLearnedObject();
	}

	@Override
	public Set<A> getAnnotations() {
		return categorizer.getCategories();
	}

	@Override
	public List<ScoredAnnotation<A>> annotate(O object) {
		FeatureVector feature = extractor.extractFeature(object);
		Vector vec = VectorFactory.getDefault().copyArray( feature.asDoubleVector() );
		
		List<ScoredAnnotation<A>> results = new ArrayList<ScoredAnnotation<A>>();
		
		for (A category : categorizer.getCategories()) {
			results.add(new ScoredAnnotation<A>(category, (float)(-1.0 * this.categorizer.computeLogPosterior(vec, category))));
		}
		
		Collections.sort(results);
		
		return results;
	}
}
