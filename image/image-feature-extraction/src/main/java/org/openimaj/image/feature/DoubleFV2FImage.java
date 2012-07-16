package org.openimaj.image.feature;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.image.FImage;
import org.openimaj.util.array.ArrayUtils;

/**
 * Transform a {@link DoubleFV} into an {@link FImage}.
 * Note: this makes a copy of the data 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class DoubleFV2FImage implements FeatureExtractor<FImage, DoubleFV> {
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
	public DoubleFV2FImage(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public FImage extractFeature(DoubleFV object) {
		return new FImage(ArrayUtils.reshape(object.values, width, height));
	}

	/**
	 * Transform a {@link DoubleFV} into an {@link FImage}.
	 * Note: this makes a copy of the data.
	 *  
	 * @param fv the feature vector
	 * @param width the image width
	 * @param height the image height
	 * @return the image
	 */
	public static FImage extractFeature(DoubleFV fv, int width, int height) {
		return new FImage(ArrayUtils.reshape(fv.values, width, height));
	}
}
