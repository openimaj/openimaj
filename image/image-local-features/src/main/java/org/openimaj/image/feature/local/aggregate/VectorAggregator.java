package org.openimaj.image.feature.local.aggregate;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;

/**
 * Interface describing an object that can convert a list of local features from
 * a single image into an aggregated vector form.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FEATURE>
 *            The type of {@link FeatureVector} of the {@link LocalFeature}s
 *            that can be processed.
 */
public interface VectorAggregator<FEATURE extends FeatureVector> {
	/**
	 * Aggregate the given features into a vector.
	 * 
	 * @param features
	 *            the features to aggregate
	 * @return the aggregated vector
	 */
	public FeatureVector aggregate(List<? extends LocalFeature<?, ? extends FEATURE>> features);
}
