package org.openimaj.ml.annotation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.math.model.Model;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.util.pair.IndependentPair;

/**
 * An {@link BatchAnnotator} backed by a {@link Model}. This only 
 * really makes sense if the dependent variable of the model
 * can take a set of discrete values. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <OBJECT> Type of object being annotated
 * @param <ANNOTATION> Type of annotation
 * @param <EXTRACTOR> Type of feature extractor
 * @param <FEATURE> Type of feature extracted by the extractor
 */
public class ModelAnnotator<OBJECT, ANNOTATION, EXTRACTOR extends FeatureExtractor<FEATURE, OBJECT>, FEATURE> 
extends
	BatchAnnotator<OBJECT, ANNOTATION, EXTRACTOR> 
{
	Model<FEATURE, ANNOTATION> model;
	Set<ANNOTATION> annotations;
	
	/**
	 * Construct with the given parameters.
	 * @param extractor The feature extractor
	 * @param model The model
	 * @param annotations The set of annotations that the model can produce
	 */
	public ModelAnnotator(EXTRACTOR extractor, Model<FEATURE, ANNOTATION> model, Set<ANNOTATION> annotations) {
		super(extractor);
		this.model = model;
		this.annotations = annotations;
	}
	
	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		List<IndependentPair<FEATURE, ANNOTATION>> featureData = new ArrayList<IndependentPair<FEATURE,ANNOTATION>>();
		
		for (Annotated<OBJECT, ANNOTATION> a : data) {
			FEATURE f = extractor.extractFeature(a.getObject());
			
			for (ANNOTATION ann : a.getAnnotations())
				featureData.add(IndependentPair.pair(f, ann));
		}
		
		model.estimate(featureData);
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		return annotations;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		FEATURE f = extractor.extractFeature(object);
		
		List<ScoredAnnotation<ANNOTATION>> result = new ArrayList<ScoredAnnotation<ANNOTATION>>();
		result.add(new ScoredAnnotation<ANNOTATION>(model.predict(f), 1));
		
		return result;
	}
}
