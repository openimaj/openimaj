package org.openimaj.ml.annotation.linear;

import gov.sandia.cognition.learning.algorithm.svm.PrimalEstimatedSubGradient;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.categorization.LinearBinaryCategorizer;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.ml.annotation.utils.AnnotatedListHelper;
import org.openimaj.ml.feature.FeatureExtractor;

/**
 * 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 * @param <ANNOTATION>
 * @param <EXTRACTOR>
 */
public class LinearSVMAnnotator <
	OBJECT,
	ANNOTATION,
	EXTRACTOR extends FeatureExtractor<? extends FeatureVector, OBJECT>>
extends
	BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR>
{
	private Map<ANNOTATION, LinearBinaryCategorizer> classifiers = new HashMap<ANNOTATION, LinearBinaryCategorizer>();
	private Set<ANNOTATION> annotations;
	
	public LinearSVMAnnotator(EXTRACTOR extractor) {
		super(extractor);
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		AnnotatedListHelper<OBJECT, ANNOTATION> helper = new AnnotatedListHelper<OBJECT, ANNOTATION>(data);
		
		annotations = helper.getAnnotations();
		
		for (ANNOTATION annotation : annotations) {
			PrimalEstimatedSubGradient pegasos = new PrimalEstimatedSubGradient();
			
			List<? extends FeatureVector> features = helper.extractFeatures(annotation, (FeatureExtractor<? extends FeatureVector, OBJECT>) extractor);
			pegasos.learn(convert(features));
			classifiers.put(annotation, pegasos.getResult());
		}
	}

	private Collection<? extends InputOutputPair<? extends Vectorizable, Boolean>> convert(List<? extends FeatureVector> features) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		List<ScoredAnnotation<ANNOTATION>> results = new ArrayList<ScoredAnnotation<ANNOTATION>>();
		
		for (ANNOTATION annotation : annotations) {
			FeatureVector feature = extractor.extractFeature(object);
			Vector vector = convert(feature);
			
			double result = classifiers.get(annotation).evaluateAsDouble(vector);
			
			if (result > 0) {
				results.add(new ScoredAnnotation<ANNOTATION>(annotation, (float) Math.abs(result)));
			}
		}
		
		return results;
	}

	private Vector convert(FeatureVector feature) {
		return VectorFactory.getDenseDefault().copyArray(feature.asDoubleVector());
	}
}
