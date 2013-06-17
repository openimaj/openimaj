package org.openimaj.image.feature.local.aggregate;

import java.util.List;

import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.Location;

/**
 * Interface describing an object that can convert a list of local features from
 * a single image into an aggregated vector form, using both the featurevector
 * and spatial location of each local feature. An example use is to create
 * aggregate feature vectors that encode go beyond simple
 * {@link VectorAggregator}s in that they additionally encode spatial
 * information.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <FEATURE>
 *            The type of {@link FeatureVector} of the {@link LocalFeature}s
 *            that can be processed.
 * @param <LOCATION>
 *            The type of {@link Location} of the {@link LocalFeature}s that can
 *            be processed.
 * @param <BOUNDS>
 *            The spatial bounds in which the {@link LocalFeature}s were
 *            extracted.
 */
public interface SpatialVectorAggregator<FEATURE extends FeatureVector, LOCATION extends Location, BOUNDS> {
	/**
	 * Aggregate the given features into a vector. The features are assumed to
	 * have spatial locations within the given bounds; typically this might be
	 * the bounds rectangle of the image from which the features were extracted.
	 * 
	 * @param features
	 *            the features to aggregate
	 * @param bounds
	 *            the bounds in which the features were extracted
	 * @return the aggregated vector
	 */
	public FeatureVector aggregate(List<? extends LocalFeature<? extends LOCATION, ? extends FEATURE>> features,
			BOUNDS bounds);
}
