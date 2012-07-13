package org.openimaj.image.feature;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Transform a {@link FloatFV} into an {@link FImage}.
 * Note: this makes a copy of the data 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class FloatFV2FImage implements FeatureExtractor<FImage, FloatFV> {
	/**
	 * the image width
	 */
	public int width;
	
	/**
	 * the image height 
	 */
	public int height;
	
	/**
	 * Construct the converter with the given image size
	 * @param width
	 * @param height
	 */
	public FloatFV2FImage(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public FImage extractFeature(FloatFV object) {
		return new FImage(ArrayUtils.reshape(object.values, width, height));
	}

}
