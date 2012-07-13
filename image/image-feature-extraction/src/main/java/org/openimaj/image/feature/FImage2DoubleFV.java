package org.openimaj.image.feature;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Transform an {@link FImage} into a {@link DoubleFV}.
 * Note: this makes a copy of the data 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FImage2DoubleFV implements FeatureExtractor<DoubleFV, FImage> {
	/**
	 * Static instance that can be used instead of creating new objects
	 */
	public static FImage2DoubleFV INSTANCE = new FImage2DoubleFV();
	
	@Override
	public DoubleFV extractFeature(FImage object) {
		return new DoubleFV(ArrayUtils.reshapeDouble(object.pixels));
	}
}
