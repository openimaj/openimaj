package org.openimaj.tools.globalfeature;

import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

/**
 * Global feature actors can enact based on an image and a mask
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public interface GlobalFeatureActor {

	/**
	 * 
	 * @param image
	 * @param mask
	 * @return the feature based on an image and mask
	 */
	public FeatureVector enact(MBFImage image, FImage mask);
	
}
