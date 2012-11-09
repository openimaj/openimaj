package org.openimaj.feature.normalisation;

import org.openimaj.feature.FeatureVector;

/**
 * Interface describing classes that can normalise a {@link FeatureVector}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <F>
 *            Type of {@link FeatureVector} that can be operated on
 */
public interface Normaliser<F extends FeatureVector> {
	/**
	 * Normalise the feature in some way
	 * 
	 * @param feature
	 *            the feature to normalise
	 */
	void normalise(F feature);
}
