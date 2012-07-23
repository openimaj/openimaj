package org.openimaj.tools.globalfeature;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * Global feature extractors can enact based on an image and a mask
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public abstract class GlobalFeatureExtractor {

	/**
	 * Create the feature from the image and optional mask.
	 * 
	 * @param image the image
	 * @param mask the mask (be be null)
	 * @return the feature based on an image and mask
	 */
	public abstract FeatureVector extract(MBFImage image, FImage mask);
	
	/**
	 * Create the feature from the image.
	 * 
	 * @param image the image
	 * @return the feature based on an image
	 */
	public FeatureVector extract(MBFImage image) {
		return extract(image, null);
	}
}
