package org.openimaj.image.feature;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Transform an {@link FImage} into a {@link FloatFV}.
 * Note: this makes a copy of the data 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImage2FloatFV implements FeatureExtractor<FloatFV, FImage> {
	/**
	 * Static instance that can be used instead of creating new objects
	 */
	public static FImage2FloatFV INSTANCE = new FImage2FloatFV();
	
	@Override
	public FloatFV extractFeature(FImage object) {
		return new FloatFV(ArrayUtils.reshape(object.pixels));
	}
}
