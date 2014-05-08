package org.openimaj.experiment.gmm.retrieval;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.util.function.Function;

/**
 *
 * @param <FEATURE>
 * @param <INPUT>
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class FeatureExtractionFunction<FEATURE,INPUT> implements FeatureExtractor<FEATURE,INPUT>{
	
	private Function<INPUT, FEATURE> func;


	public FeatureExtractionFunction(Function<INPUT,FEATURE> func) {
		this.func = func;
	}
	

	@Override
	public FEATURE extractFeature(INPUT object) {
		return func.apply(object);
	}
	
	public static <FEATURE,INPUT> FeatureExtractionFunction<FEATURE,INPUT> wrap(Function<INPUT,FEATURE> func){
		return new FeatureExtractionFunction(func);
	}

	

}
