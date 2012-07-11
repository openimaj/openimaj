package org.openimaj.ml.annotation;

/**
 * An identity extractor hands back the object it is given as the extracted feature
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <OBJECT>
 */
public class IdentityFeatureExtractor<OBJECT> implements FeatureExtractor<OBJECT, OBJECT> {
	@Override
	public OBJECT extractFeature(OBJECT object) {
		return object;
	}

}
