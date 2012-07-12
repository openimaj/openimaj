package org.openimaj.ml.annotation.linear;

import java.util.List;
import java.util.Set;

import org.openimaj.feature.FeatureVector;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.ml.annotation.BatchAnnotator;
import org.openimaj.ml.annotation.ScoredAnnotation;
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
	public LinearSVMAnnotator(EXTRACTOR extractor) {
		super(extractor);
	}

	@Override
	public void train(List<? extends Annotated<OBJECT, ANNOTATION>> data) {
		//Map<ANNOTATION, TIntArrayList> groupedData
	}

	@Override
	public Set<ANNOTATION> getAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScoredAnnotation<ANNOTATION>> annotate(OBJECT object) {
		// TODO Auto-generated method stub
		return null;
	}
}
